package se.kfreiholtz.mywishlist.userinterfaces;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import se.kfreiholtz.mywishlist.R;
import se.kfreiholtz.mywishlist.utilities.ParseConstants;

/**
 * Activity for creating new lists
 *
 * @author Kristoffer Freiholtz
 * @version 1.0 2014-11-18
 */
public class NewListActivity extends ListActivity {

    private Spinner mListTypeSpinner;
    private EditText mListName;
    private List<ParseObject> mPosts = new ArrayList<ParseObject>();
    private ParseObject mWishList;
    private ParseRelation<ParseObject> mListPostRelation;
    private boolean mListEdit = false; // true if its an existing list being edited
    private MenuItem mSendMenuItem;
    private MenuItem mDeleteMenuItem;
    public static final int NEW_POST_REQUEST = 0;
    public static final int CHANGE_POST_REQUEST = 1;
    public static final int SAVE_LIST_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_new_list);

        getActionBar().setDisplayShowTitleEnabled(false); // remove the title

        mListTypeSpinner = (Spinner) findViewById(R.id.listTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.list_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mListTypeSpinner.setAdapter(adapter);

        mListName = (EditText)findViewById(R.id.eventField);
        Button newItemButton = (Button) findViewById(R.id.newItemButton);

        CheckEditList();

        // When button is pressed. Open new post activity
        newItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewListActivity.this, NewPostActivity.class);
                startActivityForResult(intent, NEW_POST_REQUEST);
            }
        });

    }

    /**
     * Sets the visible state of the delete and save icon
     */
    private void CheckMenuItems(){
        if(mPosts.size() > 0 || mListEdit) {
            mSendMenuItem.setVisible(true);
            mDeleteMenuItem.setVisible(true);
        }
        else{
            mSendMenuItem.setVisible(false);
            mDeleteMenuItem.setVisible(false);
        }
    }

    /**
     * Check if its a new list or and existing being edited
     * If edited it will set the values of the list
     */
    private void CheckEditList() {
        if(getIntent().getExtras() != null) {
            mListEdit = true;

            setProgressBarIndeterminateVisibility(true);

            ParseQuery<ParseObject> listQuery = ParseQuery.getQuery(ParseConstants.CLASS_LISTS);
            String objectId = getIntent().getExtras().getString(ParseConstants.KEY_LIST_ID);
            listQuery.getInBackground(objectId, (new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null){
                        mWishList = parseObject;
                        mListName.setText(parseObject.getString(ParseConstants.KEY_LIST_NAME));
                        //Fill the spinner with the selected value
                        String listType = mWishList.getString(ParseConstants.KEY_LIST_TYPE);
                        String[] listTypes = getResources().getStringArray(R.array.list_types);
                        if(listType.equals(listTypes[0])){
                            mListTypeSpinner.setSelection(0, false);
                        }
                        else if (listType.equals(listTypes[1])) {
                            mListTypeSpinner.setSelection(1, false);
                        }
                        else if (listType.equals(listTypes[2])) {
                            mListTypeSpinner.setSelection(2, false);
                        }
                        else if (listType.equals(listTypes[3])) {
                            mListTypeSpinner.setSelection(3, false);
                        }

                        mListPostRelation = mWishList.getRelation(ParseConstants.KEY_LIST_POST_RELATION);
                        ParseQuery<ParseObject> postQuery = mListPostRelation.getQuery();
                        postQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> posts, ParseException e) {
                                setProgressBarIndeterminateVisibility(false);

                                if(e == null){
                                    mPosts = posts;
                                    String[] postNames = new String[mPosts.size()];
                                    int i = 0;
                                    for (ParseObject post : mPosts) {
                                        postNames[i] = post.get(ParseConstants.KEY_POST_NAME).toString();
                                        i++;
                                    }
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                            getListView().getContext(),
                                            R.layout.new_list_item,
                                            postNames);
                                    setListAdapter(adapter);
                                    CheckMenuItems();
                                }

                                else{
                                    Toast.makeText(NewListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else{
                        // turns of the progressbar in the event of an error
                        setProgressBarIndeterminateVisibility(false);
                    }
                }
            }));
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {

            if(requestCode == NEW_POST_REQUEST || requestCode == CHANGE_POST_REQUEST) {
                setProgressBarIndeterminateVisibility(true);
                String postName = data.getExtras().getString(ParseConstants.KEY_POST_NAME);
                String postLocation = data.getExtras().getString(ParseConstants.KEY_POST_LOCATION);
                String postExtra = data.getExtras().getString(ParseConstants.KEY_POST_EXTRA);
                final ParseObject post = new ParseObject(ParseConstants.KEY_POST);
                final int postPosition = data.getExtras().getInt(ParseConstants.KEY_POST_POSITION);
                post.put(ParseConstants.KEY_POST_NAME, postName);
                post.put(ParseConstants.KEY_POST_LOCATION, postLocation);
                post.put(ParseConstants.KEY_POST_EXTRA, postExtra);
                if(requestCode == NEW_POST_REQUEST){
                    post.put(ParseConstants.KEY_POST_BOUGHT, false);
                    post.put(ParseConstants.KEY_POST_BOUGHT_BY, "");
                }
                if(data.getByteArrayExtra(ParseConstants.KEY_POST_IMAGE) !=null ) {
                    byte[] byteImageArray = data.getByteArrayExtra(ParseConstants.KEY_POST_IMAGE);
                    ParseFile imageFile = new ParseFile(ParseConstants.KEY_POST_IMAGE + ".jpg", byteImageArray);
                    post.put(ParseConstants.KEY_POST_IMAGE, imageFile);
                }
                post.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        setProgressBarIndeterminateVisibility(false);
                        if (e == null) {
                            // success!
                            if(requestCode == NEW_POST_REQUEST){
                                mPosts.add(post);
                                Toast.makeText(NewListActivity.this, getString(R.string.new_wish_created), Toast.LENGTH_LONG).show();
                            }
                            else{
                                mPosts.add(postPosition, post);
                                mPosts.remove(postPosition + 1);
                                Toast.makeText(NewListActivity.this, getString(R.string.wish_edited), Toast.LENGTH_LONG).show();
                            }
                            RefreshList();
                        }
                        else {
                            Toast.makeText(NewListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        CheckMenuItems();
                    }
                });
            }

            // Save the List
            else if (requestCode == SAVE_LIST_REQUEST){
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }

        // Remove deleted post
        else if (resultCode == ParseConstants.RESULT_REMOVED){
            if (requestCode == CHANGE_POST_REQUEST) {
                setProgressBarIndeterminateVisibility(true);
                final int postPosition = data.getExtras().getInt(ParseConstants.KEY_POST_POSITION);
                final ParseObject post = mPosts.get(postPosition);
                post.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        setProgressBarIndeterminateVisibility(false);
                        if (e == null) {
                            // success!
                            mPosts.remove(postPosition);
                            RefreshList();
                            Toast.makeText(NewListActivity.this, getString(R.string.wish_deleted) + mPosts.size(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(NewListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        CheckMenuItems();
                    }
                });
            }
        }
    }

    /**
     * Refresh the listview after adding or deleting posts.
     */
    private void RefreshList() {
        String[] posts = new String[mPosts.size()];
        int i = 0;
        for (ParseObject post : mPosts) {
            posts[i] = post.getString(ParseConstants.KEY_POST_NAME);
            i++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getListView().getContext(),
                R.layout.new_list_item,
                posts);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // When a list item is clicked it will open that post for editing
        ParseObject post = mPosts.get(position);
        Intent intent = new Intent(NewListActivity.this, NewPostActivity.class);
        intent.putExtra(ParseConstants.KEY_POST_NAME, post.getString(ParseConstants.KEY_POST_NAME));
        intent.putExtra(ParseConstants.KEY_POST_LOCATION, post.getString(ParseConstants.KEY_POST_LOCATION));
        intent.putExtra(ParseConstants.KEY_POST_EXTRA, post.getString(ParseConstants.KEY_POST_EXTRA));
        intent.putExtra(ParseConstants.KEY_POST_POSITION, position);
        if(post.getParseFile(ParseConstants.KEY_POST_IMAGE) != null){
            ParseFile file = post.getParseFile(ParseConstants.KEY_POST_IMAGE);
            Uri fileUri = Uri.parse(file.getUrl());
            intent.setData(fileUri);
        }
        startActivityForResult(intent, CHANGE_POST_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_list, menu);
        mDeleteMenuItem = menu.getItem(0);
        mSendMenuItem = menu.getItem(1);
        CheckMenuItems();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_list) {
            if(mListName.getText().toString().equals("")){
                Toast.makeText(NewListActivity.this, getString(R.string.enter_name_for_list), Toast.LENGTH_LONG).show();
            }
            else{
                if(!mListEdit){ // Create a new Parseobject WishList
                    mWishList = new ParseObject(ParseConstants.CLASS_LISTS);
                }

                String listName = mListName.getText().toString();
                String listType = mListTypeSpinner.getSelectedItem().toString();
                listName = listName.trim();
                mListPostRelation = mWishList.getRelation(ParseConstants.KEY_LIST_POST_RELATION);
                mWishList.put(ParseConstants.KEY_LIST_NAME, listName);
                mWishList.put(ParseConstants.KEY_LIST_TYPE, listType);
                mWishList.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
                mWishList.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
                mWishList.put(ParseConstants.KEY_LIST_DELETED_BY_SENDER, false);

                // adds all the relations
                for (ParseObject post : mPosts) {
                    mListPostRelation.add(post);
                }

                mWishList.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(NewListActivity.this, getString(R.string.list_saved), Toast.LENGTH_SHORT).show();
                            ParseUser currentUser = ParseUser.getCurrentUser();
                            ParseRelation<ParseObject> userListRelation = currentUser.getRelation(ParseConstants.KEY_LISTS);
                            userListRelation.add(mWishList);
                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Intent recipientsIntent = new Intent(NewListActivity.this, RecipientsActivity.class);
                                    recipientsIntent.putExtra(ParseConstants.KEY_LIST_ID, mWishList.getObjectId());
                                    startActivityForResult(recipientsIntent, SAVE_LIST_REQUEST);
                                }
                            });

                        } else {
                            Toast.makeText(NewListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

        // Remove List
        else if (id == R.id.action_remove_list) {
            if(mListEdit){
                mWishList.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Intent resultIntent = new Intent(NewListActivity.this, NewPostActivity.class);
                            resultIntent.putExtra(ParseConstants.KEY_LIST_ID, mWishList.getObjectId());
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                    }
                });
            }
            else{
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
