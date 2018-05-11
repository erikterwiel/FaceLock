package erikterwiel.phoneprotection.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import erikterwiel.phoneprotection.Services.DetectionService;

public class DetectionReceiver extends BroadcastReceiver {

    private static final String TAG = "DetectionReceiver.java";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive() called");
        Intent serviceIntent = new Intent(context, DetectionService.class);
        serviceIntent.putExtra("size", intent.getIntExtra("size", 0));
        for (int i = 0; i < intent.getIntExtra("size", 0); i++) {
            serviceIntent.putExtra("user" + i, intent.getStringExtra("user" + i));
            serviceIntent.putExtra("bucketfiles" + i, intent.getStringExtra("bucketfiles" + i));
        }
        serviceIntent.putExtra("username", intent.getStringExtra("username"));
        context.startService(serviceIntent);
    }
}
