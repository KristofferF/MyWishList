package se.kfreiholtz.mywishlist.userinterfaces;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.kfreiholtz.mywishlist.R;
import se.kfreiholtz.mywishlist.adapters.ListAdapter;
import se.kfreiholtz.mywishlist.utilities.ParseConstants;

/**
 * Fragment that shows the lists created by the logged in user
 *
 * @author Kristoffer Freiholtz
 * @version 1.0 2014-11-18
 */
public class MyListsFragment extends ListFragment {

    private List<ParseObject> mLists;
    public static final int CHANGE_LIST_REQUEST = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_lists, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setProgressBarIndeterminateVisibility(true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> userListRelation = currentUser.getRelation(ParseConstants.KEY_LISTS);
        ParseQuery<ParseObject> query = userListRelation.getQuery();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> lists, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    // Found lists
                    mLists = lists;
                    ArrayList<String> listNames = new ArrayList<String>();
                    ParseObject list;
                    Iterator<ParseObject> i = mLists.iterator();
                    while(i.hasNext()){
                        list = i.next();
                        if(!list.getBoolean(ParseConstants.KEY_LIST_DELETED_BY_SENDER)){
                            listNames.add(list.get(ParseConstants.KEY_LIST_NAME).toString());
                        }
                        else{
                            i.remove();
                        }
                    }
                    // If listview is empty fill upp the list.
                    // Else refresh it
                    if(getListView().getAdapter() == null){
                        ListAdapter adapter = new ListAdapter(
                                getListView().getContext(),
                                mLists);
                        setListAdapter(adapter);
                    }
                    else{
                        ((ListAdapter)getListView().getAdapter()).refill(mLists);
                    }
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getListView().getContext());
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // When a list item is clicked it will open that list for editing
        ParseObject list = mLists.get(position);
        Intent intent = new Intent(getActivity(), NewListActivity.class);
        intent.putExtra(ParseConstants.KEY_LIST_ID, list.getObjectId());
        startActivityForResult(intent, CHANGE_LIST_REQUEST);
    }
}
