package erikterwiel.phoneprotection.Singletons;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import erikterwiel.phoneprotection.R;
import erikterwiel.phoneprotection.Receivers.DetectionReceiver;
import erikterwiel.phoneprotection.User;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class Protection {

    private static final String TAG = "Protection.java";
    private static final int START_PROTECTION = 105;
    private static final int NOTIFICATION_SCAN = 106;
    private static final int NOTIFICATION_PROTECTED = 107;
    private static final String NOTIFICATION_CHANNEL = "108";

    private static Protection instance;
    private Context mContext;
    private Intent mIntent;
    private ArrayList<User> mUserList;
    private SharedPreferences mDatabase;
    private Timer mPauseTimer;
    private PendingIntent mPendingReceiverIntent;
    private AlarmManager mAlarmManager;
    private NotificationManager mNotificationManager;
    private Notification mScanNotification;
    private Notification mProtectedNotification;

    private Protection(Context context, Intent intent, ArrayList<User> userList) {
        mContext = context;
        mIntent = intent;
        mUserList = userList;
        mDatabase = mContext.getSharedPreferences("settings", MODE_PRIVATE);
        mPauseTimer = new Timer();

        Intent receiverIntent = new Intent(mContext, DetectionReceiver.class);
        receiverIntent.putExtra("size", mUserList.size());
        for (int i = 0; i < mUserList.size(); i++) {
            receiverIntent.putExtra("user" + i, mUserList.get(i).getFileName());
            receiverIntent.putExtra("bucketfiles" + i, mUserList.get(i).getName() + ".jpg");
        }
        receiverIntent.putExtra("username", mIntent.getStringExtra("username"));
        Log.i(TAG, "Passing " + mIntent.getStringExtra("username") + " to DetectionService");
        mPendingReceiverIntent = PendingIntent.getBroadcast(
                mContext,
                START_PROTECTION,
                receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);

        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "Swiper No Swiping Noficiation",
                    NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
            Notification.Builder builder1 = new Notification.Builder(mContext, NOTIFICATION_CHANNEL);
            builder1.setSmallIcon(R.drawable.ic_security_black_48dp);
            builder1.setContentTitle("Protection Active");
            builder1.setContentText("Currently monitoring phone users");
            builder1.setCategory(Notification.CATEGORY_STATUS);
            builder1.setAutoCancel(false);
            builder1.setOngoing(true);
            mScanNotification = builder1.build();
            Notification.Builder builder2 = new Notification.Builder(mContext, NOTIFICATION_CHANNEL);
            builder2.setSmallIcon(R.drawable.ic_check_black_48dp);
            builder2.setContentTitle("Protection Active");
            builder2.setContentText("Phone user identified as authorised");
            builder2.setCategory(Notification.CATEGORY_STATUS);
            builder2.setAutoCancel(false);
            builder2.setOngoing(true);
            mProtectedNotification = builder2.build();
        } else {
            mScanNotification = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_security_black_48dp)
                    .setContentTitle("Protection Active")
                    .setContentText("Currently monitoring phone users")
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .build();
            mProtectedNotification = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_check_black_48dp)
                    .setContentTitle("Protection Active")
                    .setContentText("Phone user identified as authorised")
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .build();
        }
    }

    public static void init(Context context, Intent intent, ArrayList<User> userList) {
        if (instance == null) {
            context = context.getApplicationContext();
            instance = new Protection(context, intent, userList);
        }
    }

    public static Protection getInstance() {
        return instance;
    }

    public void enableProtection() {
        Log.i(TAG, "enableProtection() called");
        if (mPauseTimer != null) {
            mPauseTimer.cancel();
            mPauseTimer.purge();
        }
        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1,
                mDatabase.getInt("scan_frequency", 60000),
                mPendingReceiverIntent);
        mNotificationManager.cancel(NOTIFICATION_PROTECTED);
        mNotificationManager.notify(NOTIFICATION_SCAN, mScanNotification);
    }

    public void pauseProtection() {
        Log.i(TAG, "pauseProtection() called");
        mAlarmManager.cancel(mPendingReceiverIntent);
        mNotificationManager.cancel(NOTIFICATION_SCAN);
        mNotificationManager.notify(NOTIFICATION_PROTECTED, mProtectedNotification);
        mPauseTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                enableProtection();
            }
        }, mDatabase.getInt("safe_duration", 900000), 7200000);
    }

    public void disableProtection() {
        Log.i(TAG, "disableProtection() called");
        mAlarmManager.cancel(mPendingReceiverIntent);
        mNotificationManager.cancel(NOTIFICATION_SCAN);
        mNotificationManager.cancel(NOTIFICATION_PROTECTED);
        if (mPauseTimer != null) {
            mPauseTimer.cancel();
            mPauseTimer.purge();
        }
    }


}
