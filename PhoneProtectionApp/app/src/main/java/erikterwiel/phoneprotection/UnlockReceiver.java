package erikterwiel.phoneprotection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UnlockReceiver extends BroadcastReceiver {

    private static final String TAG = "UnlockerReceiver.java";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive() called");
        Intent stopServiceIntent = new Intent(context, SirenService.class);
        context.stopService(stopServiceIntent);
    }
}
