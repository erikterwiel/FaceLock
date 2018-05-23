package erikterwiel.phoneprotection.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import erikterwiel.phoneprotection.MyAdminReceiver;
import erikterwiel.phoneprotection.R;
import erikterwiel.phoneprotection.Singletons.Rekognition;
import erikterwiel.phoneprotection.Singletons.S3;

import static erikterwiel.phoneprotection.Keys.DynamoDBKeys.POOL_ID_UNAUTH;
import static erikterwiel.phoneprotection.Keys.DynamoDBKeys.POOL_REGION;

public class DetectionService extends Service {

    private static final String TAG = "DetectionService.java";
    private static final String PATH_STREAM = "sdcard/Pictures/PhoneProtection/Stream";
    private static final String BUCKET_NAME = "phoneprotectionpictures";
    private static final float CONFIDENCE_THRESHOLD = 70F;  
    private static final int NOTIFICATION_SCAN = 104;
    private static final int NOTIFICATION_PROTECTED = 106;
    private static final String NOTIFICATION_CHANNEL = "105";

    private NotificationManager mNotificationManager;
    private Notification mScanNotification;
    private Notification mProtectedNotification;
    private Timer mScanTimer;
    private Timer mPauseTimer;
    private ArrayList<String> mUserList = new ArrayList<>();
    private ArrayList<String> mBucketFiles = new ArrayList<>();
    private AWSCredentialsProvider mCredentialsProvider;
    private AmazonRekognitionClient mRekognition;
    private TransferUtility mTransferUtility;
    private SurfaceTexture mSurfaceTexture;
    private String mUsername;
    private Intent mIntent;
    private SharedPreferences mDatabase;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() called");

        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                this,
                POOL_ID_UNAUTH,
                Regions.fromName(POOL_REGION));

        mDatabase = getSharedPreferences("settings", MODE_PRIVATE);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "Swiper No Swiping Noficiation",
                    NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
            Notification.Builder builder1 = new Notification.Builder(this, NOTIFICATION_CHANNEL);
            builder1.setSmallIcon(R.drawable.ic_security_black_48dp);
            builder1.setContentTitle("Protection Active");
            builder1.setContentText("Currently monitoring phone users");
            builder1.setCategory(Notification.CATEGORY_STATUS);
            builder1.setAutoCancel(false);
            builder1.setOngoing(true);
            mScanNotification = builder1.build();
            Notification.Builder builder2 = new Notification.Builder(this, NOTIFICATION_CHANNEL);
            builder2.setSmallIcon(R.drawable.ic_check_black_48dp);
            builder2.setContentTitle("Protection Active");
            builder2.setContentText("Phone user identified as authorised");
            builder2.setCategory(Notification.CATEGORY_STATUS);
            builder2.setAutoCancel(false);
            builder2.setOngoing(true);
            mProtectedNotification = builder2.build();
        } else {
             mScanNotification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_security_black_48dp)
                     .setContentTitle("Protection Active")
                     .setContentText("Currently monitoring phone users")
                     .setCategory(Notification.CATEGORY_STATUS)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .build();
            mProtectedNotification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_check_black_48dp)
                    .setContentTitle("Protection Active")
                    .setContentText("Phone user identified as authorised")
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .build();
        }

        mIntent = intent;
        mUsername = intent.getStringExtra("username");
        int size = intent.getIntExtra("size", 0);
        for (int i = 0; i < size; i++) {
            mUserList.add(intent.getStringExtra("user" + i));
            mBucketFiles.add(mUsername + "/" + intent.getStringExtra("bucketfiles" + i));
        }

        mSurfaceTexture = new SurfaceTexture(0);


        mTransferUtility = S3.getInstance().getTransferUtility();
        mRekognition = Rekognition.getInstance().getRekognitionClient();
        enableProtection();
        return START_STICKY;
    }

    private void enableProtection() {
        mNotificationManager.cancel(NOTIFICATION_PROTECTED);
        mNotificationManager.notify(NOTIFICATION_SCAN, mScanNotification);
        Log.i(TAG, "enableProtection() called");
        if (mPauseTimer != null) {
            mPauseTimer.cancel();
            mPauseTimer.purge();
        }
        mScanTimer = new Timer();
        mScanTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "run() called");

                // Captures picture and saves it
                Camera camera = null;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(1, cameraInfo);
                try {
                    camera = Camera.open(1);
                } catch (RuntimeException e) {
                    Log.i(TAG, "Camera 1 not available");
                }
                try {
                    if (camera == null) {
                        Log.i(TAG, "Could not get camera instance");
                    } else {
                        try {
                            camera.setPreviewTexture(mSurfaceTexture);
                            camera.startPreview();
                        } catch (Exception e) {
                            Log.i(TAG, "Could not set the surface preview texture");
                        }
                        camera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] bytes, Camera camera) {
                                File folder = new File(PATH_STREAM);
                                if (!folder.exists()) folder.mkdir();
                                File file = new File(folder, "Stream.jpg");
                                try {
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(bytes);
                                    fos.close();
                                    Log.i(TAG, "Image saved");
                                } catch (Exception e) {
                                    Log.i(TAG, "Image could not be saved");
                                }
                                camera.release();
                            }
                        });
                    }
                } catch (Exception e) {
                    camera.release();
                    e.printStackTrace();
                }

                // Compares captured and saved pictures to S3 database
                boolean isFace = false;
                boolean isUser = false;
                try {
                    // Turns .jpg file to Amazon Image file
                    Thread.sleep(3000);
                    InputStream inputStream = new FileInputStream(PATH_STREAM + "/Stream.jpg");
                    ByteBuffer imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
                    Image targetImage = new Image().withBytes(imageBytes);

                    // Checks if file contains a face
                    DetectLabelsRequest detectLabelsRequest = new DetectLabelsRequest()
                            .withImage(targetImage)
                            .withMinConfidence(CONFIDENCE_THRESHOLD);
                    DetectLabelsResult detectLabelsResult = mRekognition.detectLabels(detectLabelsRequest);
                    List<Label> labels = detectLabelsResult.getLabels();
                    for (int i = 0; i < labels.size(); i++) {
                        String label = labels.get(i).getName();
                        if (label.equals("People") || label.equals("Person") || label.equals("Human"))
                            isFace = true;
                        Log.i(TAG, labels.get(i).getName() + ":" + labels.get(i).getConfidence().toString());
                    }

                    // Compares faces if above fail contains a face
                    if (isFace) {
                        for (int i = 0; i < mUserList.size(); i++) {
                            String[] inputNameSplit = mBucketFiles.get(i).split("/");
                            if (!inputNameSplit[1].equals("Faustin.jpg") &&
                                    !inputNameSplit[1].equals("Mansour.jpg")) {
                                Log.i(TAG, "Attempting to compare faces");
                                CompareFacesRequest compareFacesRequest = new CompareFacesRequest()
                                        .withSourceImage(new Image()
                                                .withS3Object(new S3Object()
                                                        .withName(mBucketFiles.get(i))
                                                        .withBucket(BUCKET_NAME)))
                                        .withTargetImage(targetImage)
                                        .withSimilarityThreshold(CONFIDENCE_THRESHOLD);
                                Log.i(TAG, "Comparing face to " + mBucketFiles.get(i) + " in " + BUCKET_NAME);
                                CompareFacesResult compareFacesResult =
                                        mRekognition.compareFaces(compareFacesRequest);
                                List<CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
                                for (int j = 0; j < faceDetails.size(); j++) {
                                    ComparedFace face = faceDetails.get(j).getFace();
                                    BoundingBox position = face.getBoundingBox();
                                    Log.i(TAG, "Face at " + position.getLeft().toString()
                                            + " " + position.getTop()
                                            + " matches with " + face.getConfidence().toString()
                                            + "% confidence.");
                                    isUser = true;
                                }
                                if (isUser) {
                                    disableProtection();
                                    break;
                                }
                            }
                        }
                        if (!isUser) lockDown();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 0, mDatabase.getInt("scan_frequency", 60000));

    }

    private void disableProtection() {
        Log.i(TAG, "disableProtection() called");
        mNotificationManager.cancel(NOTIFICATION_SCAN);
        mNotificationManager.notify(NOTIFICATION_PROTECTED, mProtectedNotification);
        if (mScanTimer != null) {
            mScanTimer.cancel();
            mScanTimer.purge();
        }

        mPauseTimer = new Timer();
        Log.i(TAG, "enableProtection() in " + (mDatabase.getInt("safe_duration", 900000)/1000 + "seconds"));
        mPauseTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                enableProtection();
            }
        }, mDatabase.getInt("safe_duration", 900000), 7200000);
    }

    private void lockDown() {
        Log.i(TAG, "lockDown() activated");

        // Upload picture of intruder to S3
        String randomID = UUID.randomUUID().toString();
        File file = new File(PATH_STREAM + "/Stream.jpg");
        TransferObserver observer = mTransferUtility.upload(
                BUCKET_NAME,
                mIntent.getStringExtra("username") + "/Intruder/" + randomID + ".jpg",
                file);
        Log.i(TAG, "Uploading");
        observer.setTransferListener(new UploadListener());

        // Plays siren if selected
        if (mDatabase.getBoolean("siren", false)) {
            Log.i(TAG, "Starting SirenService");
            Intent sirenIntent = new Intent(this, SirenService.class);
            startService(sirenIntent);
        }

        // Vibrates phone
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(3000,255));

        // Emails and texts owner of phone
        AmazonSNSClient snsClient = new AmazonSNSClient(mCredentialsProvider);
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        String msg = "Swiper No Swiping has identified this individual using your phone.\n" +
                "https://s3.amazonaws.com/phoneprotectionpictures/" +
                mUsername + "/Intruder/" + randomID + ".jpg\n\n" +
                "Go to http://swipernoswiping.com/ to locate your phone";
        String subject = "URGENT: Someone Has Your Phone";
        PublishRequest publishRequest = new PublishRequest(
                "arn:aws:sns:us-east-1:132885165810:email-list", msg, subject);
        snsClient.publish(publishRequest);

        // Starts rapid location services
        Intent trackerIntent = new Intent(this, TrackerService.class);
        trackerIntent.putExtra("username", mUsername);
        Log.i(TAG, "Passing " + mUsername + " to TrackerService");
//        startService(trackerIntent);

        // Lock down phone
        DevicePolicyManager deviceManager =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(this, MyAdminReceiver.class);
        if (deviceManager.isAdminActive(compName)) deviceManager.lockNow();

        // Shutdown service
        onDestroy();
    }

    private class UploadListener implements TransferListener {

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.i(TAG, state + "");
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            int percentage = (int) (bytesCurrent / bytesTotal * 100);
            Log.i(TAG, Integer.toString(percentage) + "% uploaded");
        }

        @Override
        public void onError(int id, Exception ex) {
            ex.printStackTrace();
            Log.i(TAG, "Error detected");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
        mNotificationManager.cancel(NOTIFICATION_SCAN);
        mNotificationManager.cancel(NOTIFICATION_PROTECTED);
        if (mScanTimer != null) {
            mScanTimer.cancel();
            mScanTimer.purge();
        }
        if (mPauseTimer != null) {
            mPauseTimer.cancel();
            mPauseTimer.purge();
        }
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
