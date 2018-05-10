package erikterwiel.phoneprotection.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import erikterwiel.phoneprotection.Services.DetectionService;

public class StartProtectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, DetectionService.class);
        context.startService(serviceIntent);
    }
}
