package se.kfreiholtz.mywishlist.userinterfaces;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import se.kfreiholtz.mywishlist.utilities.ParseConstants;
import se.kfreiholtz.mywishlist.R;

/**
 * Activity for choosing recipients for the list to be sent
 *
 * @author Kristoffer Freiholtz
 * @version 1.0 2014-11-18
 */
public class RecipientsActivity extends ListActivity {

    public static  final String TAG = RecipientsActivity.class.getSimpleName();

    private List<ParseUser> mFriends;
    private ParseObject mWishList;
    private MenuItem mSendMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_recipients);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


        getActionBar().setDisplayShowTitleEnabled(false); // remove the title
    }

    @Override
    protected void onResume() {
        super.onResume();

        setProgressBarIndeterminateVisibility(true);
        // Retrieve the current user and show the friends of that user in a listview
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseRelation<ParseUser> friendsRelation = currentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        ParseQuery<ParseUser> query = friendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    mFriends = friends;
                    ParseQuery<ParseObject> listQuery = ParseQuery.getQuery(ParseConstants.CLASS_LISTS);
                    String objectId = getIntent().getExtras().getString(ParseConstants.KEY_LIST_ID);
                    listQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject recipientsIdList, ParseException e) {
                            if (e == null) {
                                String[] usernames = new String[mFriends.size()];
                                int i = 0;
                                for (ParseUser user : mFriends) {
                                    usernames[i] = user.getUsername();
                                    i++;
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                        getListView().getContext(),
                                        android.R.layout.simple_list_item_checked,
                                        usernames);
                                setListAdapter(adapter);

                                List<String> recipientIds = recipientsIdList.getList(ParseConstants.KEY_RECIPIENT_IDS);
                                if(recipientIds != null){
                                    i = 0;
                                    for (ParseUser user : mFriends) {
                                        for (String recipientsId : recipientIds){
                                            if (recipientsId.equals(user.getObjectId())){
                                                getListView().setItemChecked(i, true);
                                            }
                                        }
                                        i++;
                                    }
                                    SetSendButtonVisibleState();
                                }
                            }
                        }
                    });
                }
                else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        SetSendButtonVisibleState();
    }

    /**
     * Check if any friends is checked
     * If so then show the sendButton
     */
    private void SetSendButtonVisibleState() {
        ListView listView = getListView();
        if(listView.getCheckedItemCount() > 0) {
            mSendMenuItem.setVisible(true);
        }
        else{
            mSendMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipients, menu);
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            ParseQuery<ParseObject> listQuery = ParseQuery.getQuery(ParseConstants.CLASS_LISTS);
            String objectId = getIntent().getExtras().getString(ParseConstants.KEY_LIST_ID);
            listQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    mWishList = parseObject;
                    mWishList.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
                    mWishList.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                sendPushNotifications();
                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }

                        }
                    });
                }
            });
        }
        else if(id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Returns the selected recipients
     * @return   arraylist of strings with the selected recipients
     */
    protected ArrayList<String> getRecipientIds(){
        ArrayList<String> recipientsIds = new ArrayList<String>();
        for (int i = 0; i < getListView().getCount(); i++){
            if(getListView().isItemChecked(i)){
                recipientsIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientsIds;
    }

    /**
     * Send a push notification to the recipients
     */
    protected void sendPushNotifications(){
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());

        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(getString(R.string.push_list, ParseUser.getCurrentUser().getUsername()));
        push.sendInBackground();
    }
}
