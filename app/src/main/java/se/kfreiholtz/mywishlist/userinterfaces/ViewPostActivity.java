package se.kfreiholtz.mywishlist.userinterfaces;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import se.kfreiholtz.mywishlist.utilities.ParseConstants;
import se.kfreiholtz.mywishlist.R;

/**
 * Activity for watching lists from the inbox
 *
 * @author Kristoffer Freiholtz
 * @version 1.0 2014-11-18
 */
public class ViewPostActivity extends Activity {

    private CheckBox mPostCheckBox;
    private MenuItem mSaveMenuItem;
    private Boolean mPostBought = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        getActionBar().setDisplayShowTitleEnabled(false); // remove the title

        TextView postNameText = (TextView) findViewById(R.id.postNameText);
        TextView postLocationText = (TextView) findViewById(R.id.postLocationText);
        TextView postExtraText = (TextView) findViewById(R.id.postExtraText);
        ImageView postImageView = (ImageView) findViewById(R.id.postImageView);
        mPostCheckBox = (CheckBox)findViewById(R.id.postCheckBox);

        postNameText.setText(getIntent().getStringExtra(ParseConstants.KEY_POST_NAME));
        postLocationText.setText(getIntent().getStringExtra(ParseConstants.KEY_POST_LOCATION));
        postExtraText.setText(getIntent().getStringExtra(ParseConstants.KEY_POST_EXTRA));

        if(postLocationText.getText().toString().equals("")){
            postLocationText.setVisibility(View.GONE);
        }
        if(postExtraText.getText().toString().equals("")){
            postExtraText.setVisibility(View.GONE);
        }

        if(getIntent().getData() != null){
            Uri imageUri = getIntent().getData();
            Picasso.with(this).load(imageUri.toString()).into(postImageView);
        }

        if(getIntent().getBooleanExtra(ParseConstants.KEY_POST_BOUGHT, false)){
            mPostCheckBox.setChecked(true);
            mPostBought = true;
            if(!getIntent().getStringExtra(ParseConstants.KEY_POST_BOUGHT_BY).equals(ParseUser.getCurrentUser().getUsername())){
                mPostCheckBox.setEnabled(false);
            }
        }

        mPostCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckMenuItems(view);
            }
        });
    }

    /**
     * Sets the visible state of the save icon
     */
    private void CheckMenuItems(View view){
        if(mPostCheckBox.isChecked()) {
            mSaveMenuItem.setVisible(true);
        }
        else if(mPostBought){
            mSaveMenuItem.setVisible(true);
        }
        else{
            mSaveMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_post, menu);
        mSaveMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save_bought_post) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(ParseConstants.KEY_POST_ID, getIntent().getStringExtra(ParseConstants.KEY_POST_ID));
            if(mPostCheckBox.isChecked()){
                setResult(Activity.RESULT_OK, resultIntent);
            }
            else{
                setResult(ParseConstants.UNBOUGHT, resultIntent);
            }

            finish();
        }
        else if(id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
