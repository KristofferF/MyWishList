package se.kfreiholtz.mywishlist.userinterfaces;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

import se.kfreiholtz.mywishlist.R;
import se.kfreiholtz.mywishlist.adapters.ListAdapterPost;
import se.kfreiholtz.mywishlist.utilities.ParseConstants;

/**
 * Activity for watching lists from the inbox
 *
 * @author Kristoffer Freiholtz
 * @version 1.0 2014-11-18
 */
public class ViewListActivity extends ListActivity {

    private TextView mListNameText;
    private ParseObject mWishList;
    private List<ParseObject> mPosts;
    private ParseRelation<ParseObject> mListPostRelation;
    private List<ParseUser> mRecipients;
    public static final int VIEW_POST_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        getActionBar().setDisplayShowTitleEnabled(false); // remove the title

        mListNameText = (TextView)findViewById(R.id.listNameText);

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_LISTS);
        String objectId = getIntent().getExtras().getString(ParseConstants.KEY_LIST_ID);
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject list, ParseException e) {
                if(e == null){
                    mWishList = list;
                    mRecipients = mWishList.getList(ParseConstants.KEY_RECIPIENT_IDS);
                    mListNameText.setText(mWishList.getString(ParseConstants.KEY_LIST_NAME));
                    mListPostRelation = mWishList.getRelation(ParseConstants.KEY_LIST_POST_RELATION);
                    ParseQuery<ParseObject> postQuery = mListPostRelation.getQuery();
                    postQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> posts, ParseException e) {
                            if(e == null){
                                mPosts = posts;
                                if(getListView().getAdapter() == null){
                                    ListAdapterPost adapter = new ListAdapterPost(
                                            getListView().getContext(),
                                            mPosts);
                                    setListAdapter(adapter);
                                }
                                else{
                                    ((ListAdapterPost)getListView().getAdapter()).refill(mPosts);
                                }
                            }
                            else{
                                Toast.makeText(ViewListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });


    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ParseObject post = mPosts.get(position);
        Intent intent = new Intent(ViewListActivity.this, ViewPostActivity.class);
        intent.putExtra(ParseConstants.KEY_POST_ID, post.getObjectId());
        intent.putExtra(ParseConstants.KEY_POST_NAME, post.getString(ParseConstants.KEY_POST_NAME));
        intent.putExtra(ParseConstants.KEY_POST_LOCATION, post.getString(ParseConstants.KEY_POST_LOCATION));
        intent.putExtra(ParseConstants.KEY_POST_EXTRA, post.getString(ParseConstants.KEY_POST_EXTRA));
        intent.putExtra(ParseConstants.KEY_POST_BOUGHT, post.getBoolean(ParseConstants.KEY_POST_BOUGHT));
        intent.putExtra(ParseConstants.KEY_POST_BOUGHT_BY, post.getString(ParseConstants.KEY_POST_BOUGHT_BY));
        if(post.getParseFile(ParseConstants.KEY_POST_IMAGE) != null){
            ParseFile file = post.getParseFile(ParseConstants.KEY_POST_IMAGE);
            Uri fileUri = Uri.parse(file.getUrl());
            intent.setData(fileUri);
        }
        startActivityForResult(intent, VIEW_POST_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            for (ParseObject post : mPosts) {
                if(post.getObjectId().equals(data.getStringExtra(ParseConstants.KEY_POST_ID))){
                    sendPushNotifications();
                    post.put(ParseConstants.KEY_POST_BOUGHT, true);
                    post.put(ParseConstants.KEY_POST_BOUGHT_BY, ParseUser.getCurrentUser().getUsername());
                    post.saveInBackground();
                }
            }
        }
        else if(resultCode == ParseConstants.UNBOUGHT){
            for (ParseObject post : mPosts) {
                if(post.getObjectId().equals(data.getStringExtra(ParseConstants.KEY_POST_ID))){
                    sendPushNotifications();
                    post.put(ParseConstants.KEY_POST_BOUGHT, false);
                    post.put(ParseConstants.KEY_POST_BOUGHT_BY, "");
                    post.saveInBackground();
                }

            }
        }
    }

    /**
     * Send a push notification to the recipients of the list when a post
     * is marked/unmarked as bought
     */
    protected void sendPushNotifications(){
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        mRecipients.remove(ParseUser.getCurrentUser().getObjectId());
        query.whereContainedIn(ParseConstants.KEY_USER_ID, mRecipients);

        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(getString(R.string.push_list_edit, mWishList.getString(ParseConstants.KEY_SENDER_NAME)));
        push.sendInBackground();
    }
}
