package erikterwiel.phoneprotection.Services;


import android.app.Notification;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import erikterwiel.phoneprotection.R;
import erikterwiel.phoneprotection.Username;

public class TrackerService extends Service {

    private static final String TAG = "TrackerService.java";
    private static final String POOL_ID_UNAUTH = "us-east-1:d2040261-6a0f-4cba-af96-8ead1b66ec38";
    private static final int NOTIFICATION_ID = 104;

    private AmazonDynamoDBClient mDDBClient;
    private DynamoDBMapper mMapper;
    private Notification mNotification;
    private FusedLocationProviderClient mFusedLocationClient;
    private Intent mIntent;
    private Username mPhone;
    private Timer mTimer;
    private int mRunCounter;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() called");

        mNotification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_security_black_48dp)
                .setContentTitle("Phone Protection")
                .setContentText("Monitoring phone users")
                .setCategory(Notification.CATEGORY_STATUS)
                .setPriority(Notification.PRIORITY_MIN)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, mNotification);

        LocationServices.getFusedLocationProviderClient(this);
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.US_EAST_1);
        mDDBClient = new AmazonDynamoDBClient(credentialsProvider);
        mMapper = new DynamoDBMapper(mDDBClient);
        mIntent = intent;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        new DownloadPhone().execute();
        try {
            Thread.sleep(1500);
        } catch (Exception ex) {}
        new UpdatePhone().execute();

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Log.i(TAG, "Latitude: " + location.getLatitude());
                                Log.i(TAG, "Longitude: " + location.getLongitude());
                                mPhone.setLatitude(location.getLatitude());
                                mPhone.setLongitude(location.getLongitude());
                            }
                        }
                    });
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                }
                new UpdatePhone().execute();
                mRunCounter += 1;
                if (mRunCounter == 20) onDestroy();
            }
        }, 0, 15000);
        return START_STICKY;
    }

    private class DownloadPhone extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... inputs) {
            mPhone = mMapper.load(Username.class, mIntent.getStringExtra("username"));
            return null;
        }
    }

    private class UpdatePhone extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(TAG, "doInBackground() called");
            mMapper.save(mPhone);
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
        mTimer.cancel();
        mTimer.purge();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
