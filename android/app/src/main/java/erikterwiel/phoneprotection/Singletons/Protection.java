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

import erikterwiel.phoneprotection.R;
import erikterwiel.phoneprotection.Receivers.DetectionReceiver;
import erikterwiel.phoneprotection.Receivers.ResumeReceiver;
import erikterwiel.phoneprotection.User;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class Protection {

    private static final String TAG = "Protection.java";
    private static final int START_PROTECTION = 105;
    private static final int RESUME_PROTECTION = 106;
    private static final int NOTIFICATION_MONITORING = 107;
    private static final int NOTIFICATION_SCANNING = 108;
    private static final int NOTIFICATION_PROTECTED = 109;
    private static final String NOTIFICATION_CHANNEL = "110";

    private static Protection instance;
    private Context mContext;
    private Intent mIntent;
    private ArrayList<User> mUserList;
    private SharedPreferences mDatabase;
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingDetectionIntent;
    private PendingIntent mPendingResumeIntent;
    private NotificationManager mNotificationManager;
    private Notification mMonitorNotification;
    private Notification mScanNotification;
    private Notification mProtectedNotification;

    private Protection(Context context, Intent intent, ArrayList<User> userList) {
        mContext = context;
        mIntent = intent;
        mUserList = userList;
        mDatabase = mContext.getSharedPreferences("settings", MODE_PRIVATE);

        mAlarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        Intent detectionIntent = new Intent(mContext, DetectionReceiver.class);
        detectionIntent.putExtra("size", mUserList.size());
        for (int i = 0; i < mUserList.size(); i++) {
            detectionIntent.putExtra("user" + i, mUserList.get(i).getFileName());
            detectionIntent.putExtra("bucketfiles" + i, mUserList.get(i).getName() + ".jpg");
        }
        detectionIntent.putExtra("username", mIntent.getStringExtra("username"));
        detectionIntent.putExtra("email", mIntent.getStringExtra("email"));
        Log.i(TAG, "Passing " + mIntent.getStringExtra("username") + " to DetectionService");
        mPendingDetectionIntent = PendingIntent.getBroadcast(
                mContext,
                START_PROTECTION,
                detectionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent resumeIntent = new Intent(mContext, ResumeReceiver.class);
        mPendingResumeIntent = PendingIntent.getBroadcast(
                mContext,
                RESUME_PROTECTION,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "Face Lock Noficiation",
                    NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
            Notification.Builder builder1 = new Notification.Builder(mContext, NOTIFICATION_CHANNEL);
            builder1.setSmallIcon(R.drawable.ic_security_black_48dp);
            builder1.setContentTitle("Protection Active");
            builder1.setContentText("Monitoring phone users");
            builder1.setCategory(Notification.CATEGORY_STATUS);
            builder1.setAutoCancel(false);
            builder1.setOngoing(true);
            mMonitorNotification = builder1.build();
            Notification.Builder builder2 = new Notification.Builder(mContext, NOTIFICATION_CHANNEL);
            builder2.setSmallIcon(R.drawable.baseline_sync_black_48dp);
            builder2.setContentTitle("Protection Active");
            builder2.setContentText("Processing current phone user's face");
            builder2.setCategory(Notification.CATEGORY_STATUS);
            builder2.setAutoCancel(false);
            builder2.setOngoing(true);
            mScanNotification = builder2.build();
            Notification.Builder builder3 = new Notification.Builder(mContext, NOTIFICATION_CHANNEL);
            builder3.setSmallIcon(R.drawable.baseline_verified_user_black_48dp);
            builder3.setContentTitle("Protection Active");
            builder3.setContentText("Phone user identified as authorised");
            builder3.setCategory(Notification.CATEGORY_STATUS);
            builder3.setAutoCancel(false);
            builder3.setOngoing(true);
            mProtectedNotification = builder3.build();
        } else {
            mMonitorNotification = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_security_black_48dp)
                    .setContentTitle("Protection Active")
                    .setContentText("Monitoring phone users")
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .build();
            mScanNotification = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.baseline_sync_black_48dp)
                    .setContentTitle("Protection Active")
                    .setContentText("Processing current phone user's face")
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .build();
            mProtectedNotification = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.baseline_verified_user_black_48dp)
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
        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1,
                mDatabase.getInt("scan_frequency", 60000),
                mPendingDetectionIntent);
        mNotificationManager.cancel(NOTIFICATION_PROTECTED);
        mNotificationManager.cancel(NOTIFICATION_SCANNING);
        mNotificationManager.notify(NOTIFICATION_MONITORING, mMonitorNotification);
    }

    public void enableScanning() {
        mNotificationManager.cancel(NOTIFICATION_MONITORING);
        mNotificationManager.notify(NOTIFICATION_SCANNING, mScanNotification);
    }

    public void pauseProtection() {
        Log.i(TAG, "pauseProtection() called");
        mAlarmManager.cancel(mPendingDetectionIntent);
        mAlarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + mDatabase.getInt("safe_duration", 900000),
                mPendingResumeIntent);
        mNotificationManager.cancel(NOTIFICATION_SCANNING);
        mNotificationManager.notify(NOTIFICATION_PROTECTED, mProtectedNotification);
    }

    public void disableProtection() {
        Log.i(TAG, "disableProtection() called");
        mAlarmManager.cancel(mPendingDetectionIntent);
        mAlarmManager.cancel(mPendingResumeIntent);
        mNotificationManager.cancel(NOTIFICATION_MONITORING);
        mNotificationManager.cancel(NOTIFICATION_SCANNING);
        mNotificationManager.cancel(NOTIFICATION_PROTECTED);
    }
}
