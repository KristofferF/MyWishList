package se.kfreiholtz.mywishlist.userinterfaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import se.kfreiholtz.mywishlist.R;
import se.kfreiholtz.mywishlist.adapters.ListAdapterInbox;
import se.kfreiholtz.mywishlist.utilities.ParseConstants;

/**
 * Fragment to watch the inbox of lists from friends
 *
 * @author Kristoffer Freiholtz
 * @version 1.0 2014-11-18
 */
public class InboxFragment extends ListFragment {

    private List<ParseObject> mLists;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static final int VIEW_LIST_REQUEST = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setProgressBarIndeterminateVisibility(true);
        retrieveLists();
    }

    private void retrieveLists() {
        // Retrieve the current user and show the shared lists from other users
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_LISTS);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> lists, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);

                if(mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                if (e == null){
                    // Lists found
                    mLists = lists;
                    // If listview is empty fill upp the list.
                    // Else refresh it
                    if(getListView().getAdapter() == null){
                            ListAdapterInbox adapter = new ListAdapterInbox(
                                    getListView().getContext(),
                                    mLists);
                            setListAdapter(adapter);
                    }
                    else{
                        ((ListAdapterInbox)getListView().getAdapter()).refill(mLists);
                    }
                }
            }
        });
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // When a list item is clicked it will open that list in ViewListActivity
        ParseObject list = mLists.get(position);
        Intent intent = new Intent(getActivity(), ViewListActivity.class);
        intent.putExtra(ParseConstants.KEY_LIST_ID, list.getObjectId());
        startActivityForResult(intent, VIEW_LIST_REQUEST);
    }


    /**
    * Refreshes the inbox with a swipe on the screen
    */
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveLists();
        }
    };
}
