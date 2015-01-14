package se.kfreiholtz.mywishlist;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;

import se.kfreiholtz.mywishlist.userinterfaces.MainActivity;
import se.kfreiholtz.mywishlist.utilities.ParseConstants;

/**
 * Created by Stationary on 2014-10-14.
 */
public class MyWishlistApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "O89hEqEFnuC9XJmPGGmtMNEGIoPhCotHRAXt5ZFj", "xhZlfwl2lNv3x4Vx410Rlp1Z2Hdku8PBYS66GL7b");

        PushService.setDefaultPushCallback(this, MainActivity.class, R.drawable.ic_stat_notification);
    }

    public static void updateParseInstallation(ParseUser user){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
        installation.saveInBackground();
    }
}
