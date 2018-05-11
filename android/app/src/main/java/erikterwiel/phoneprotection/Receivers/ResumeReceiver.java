package erikterwiel.phoneprotection.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import erikterwiel.phoneprotection.Singletons.Protection;

public class ResumeReceiver extends BroadcastReceiver {

    private static final String TAG = "ResumeReceiver.java";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive() called");
        Protection.getInstance().enableProtection();
    }
}
