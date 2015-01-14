package se.kfreiholtz.mywishlist.userinterfaces;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import se.kfreiholtz.mywishlist.utilities.ParseConstants;
import se.kfreiholtz.mywishlist.R;

/**
 * Activity for creating new posts
 *
 * @author Kristoffer Freiholtz
 * @version 1.0 2014-11-18
 */
public class NewPostActivity extends Activity {

    private EditText mPostName;
    private EditText mPostLocation;
    private EditText mPostExtra;
    private int mPostPosition;
    private boolean mPostEdit = false;
    private ImageView mImageView;
    private byte[] byteImageArray;
    private Uri mPhotoUri;
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int PICK_PHOTO_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mImageView = (ImageView)findViewById(R.id.newPostImageView);

        getActionBar().setDisplayShowTitleEnabled(false); // remove the title

        Button pictureButton = (Button) findViewById(R.id.pictureButton);
        mPostName = (EditText)findViewById(R.id.newPostNameField);
        mPostLocation = (EditText)findViewById(R.id.newPostLocationField);
        mPostExtra = (EditText)findViewById(R.id.newPostExtraField);

        // If list is being edited, set the values of the post
        if(getIntent().getExtras() != null) {
            mPostName.setText(getIntent().getExtras().getString(ParseConstants.KEY_POST_NAME));
            mPostLocation.setText(getIntent().getExtras().getString(ParseConstants.KEY_POST_LOCATION));
            mPostExtra.setText(getIntent().getExtras().getString(ParseConstants.KEY_POST_EXTRA));
            mPostPosition = getIntent().getExtras().getInt(ParseConstants.KEY_POST_POSITION);
            mPostEdit = true;
        }

        // if image is available load it
        if(getIntent().getData() != null){
            Uri imageUri = getIntent().getData();
            Picasso.with(this).load(imageUri.toString()).into(mImageView);
        }

        // starts the camera intent through a dialog
        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_post) {
            if(mPostName.getText().toString().equals("")){
                Toast.makeText(NewPostActivity.this, getString(R.string.enter_post_name), Toast.LENGTH_LONG).show();
            }
            else{
                Intent resultIntent = new Intent();
                resultIntent.putExtra(ParseConstants.KEY_POST_NAME, mPostName.getText().toString());
                resultIntent.putExtra(ParseConstants.KEY_POST_LOCATION, mPostLocation.getText().toString());
                resultIntent.putExtra(ParseConstants.KEY_POST_EXTRA, mPostExtra.getText().toString());
                if(byteImageArray != null && !mPostEdit){
                    resultIntent.putExtra(ParseConstants.KEY_POST_IMAGE, byteImageArray);
                }
                if(mPostEdit) {
                    resultIntent.putExtra(ParseConstants.KEY_POST_POSITION, mPostPosition);
                }
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }

        }
        else if (id == R.id.action_remove_post) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(ParseConstants.KEY_POST_POSITION, mPostPosition);
            setResult(ParseConstants.RESULT_REMOVED, resultIntent);
            finish();
        }

        else if(id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
     public void onBackPressed() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which){
                case 0: // Take picture
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    else{
                        Toast.makeText(NewPostActivity.this, getString(R.string.no_camera_image), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                    break;
                case 1: // Choose Picture
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(data == null){
                Toast.makeText(this, getString(R.string.error_title), Toast.LENGTH_LONG).show();
            }
            else{
                mPhotoUri = data.getData();
                if(requestCode == TAKE_PHOTO_REQUEST){
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(mPhotoUri);
                    sendBroadcast(mediaScanIntent);
                }

                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mPhotoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(imageBitmap != null){
                    if(imageBitmap.getWidth() > 800){
                        ResizeImage(imageBitmap);
                    }
                }
            }

        }
        else if (resultCode != RESULT_CANCELED){
            Toast.makeText(this, getString(R.string.error_title), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Resize the bitmap to 800 in width and a scaled proportional height
     * @param  imageBitmap the bitmap to be resized
     */
    private void ResizeImage(Bitmap imageBitmap) {
        double targetWidth = 800.0;
        double scaleFactor = targetWidth/imageBitmap.getWidth();
        int scaledWidth = (int)(imageBitmap.getWidth()*scaleFactor);
        int scaledHeight = (int)(imageBitmap.getHeight()*scaleFactor);
        imageBitmap = RotateImage(imageBitmap, scaledWidth, scaledHeight);
        //imageBitmap = imageBitmap.createScaledBitmap(imageBitmap, scaledWidth, scaledHeight, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        // get byte array here
        byteImageArray = stream.toByteArray();
        mImageView.setImageBitmap(imageBitmap);
    }

    /**
     * Rotates the bitmap depending on the rotation of the image taken or selected
     * @param  imageBitmap the bitmap to be rotated
     * @param  width the scaled width of the image
     * @param  height the scaled height of the image
     * @return the rotated bitmap
     */
    private Bitmap RotateImage(Bitmap imageBitmap, int width, int height) {
        try {
            Boolean rotated = false;
            String imagePath = getRealPathFromURI(this, mPhotoUri);
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                rotated = true;
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
                rotated = true;
            }
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true); // rotating bitmap
            if(rotated){
                // height and width switches places when bitmap is rotated 90 or 270 degrees
                // to prevent unwanted stretching of the bitmap
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, height, width, true);
            }
            else{
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, true);
            }
            return imageBitmap;
        }
        catch (Exception ignored) {
            
        }
        return null;
    }

    /**
     * Gets the absolute path of an URI
     * @param  context the current context
     * @param  contentUri the URI to get the path from
     * @return the absolute path of the URI
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] path = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  path, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }










}
