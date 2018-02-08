package erikterwiel.phoneprotection;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class SirenService extends Service {

    private static final String TAG = "SirenService.java";

    private SharedPreferences mDatabase;
    private MediaPlayer mSiren;
    private UnlockReceiver mReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() called");
        mDatabase = getSharedPreferences("settings", MODE_PRIVATE);

        mReceiver = new UnlockReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mReceiver, filter);

        if (mDatabase.getBoolean("max", false)) {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0);
        }
        mSiren = MediaPlayer.create(this, R.raw.siren);
        mSiren.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
        unregisterReceiver(mReceiver);
        if (mSiren != null) {
            mSiren.stop();
            mSiren.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
