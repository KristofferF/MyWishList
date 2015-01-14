package se.kfreiholtz.mywishlist.utilities;

import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;

import se.kfreiholtz.mywishlist.userinterfaces.MainActivity;

/**
 * Created by Stationary on 2014-11-18.
 */
public class Receiver extends ParsePushBroadcastReceiver {

    @Override
    public void onPushOpen(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}