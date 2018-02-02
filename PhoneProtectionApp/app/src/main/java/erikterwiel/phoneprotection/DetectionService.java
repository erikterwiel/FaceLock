package erikterwiel.phoneprotection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
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
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DetectionService extends Service {

    private static final String TAG = "DetectionService.java";
    private static final String POOL_ID_UNAUTH = "us-east-1:d2040261-6a0f-4cba-af96-8ead1b66ec38";
    private static final String POOL_REGION = "us-east-1";
    private static final String PATH_STREAM = "sdcard/Pictures/PhoneProtection/Stream";
    private static final String BUCKET_NAME = "phoneprotectionpictures";
    private static final float CONFIDENCE_THRESHOLD = 70F;
    private static final int NOTIFICATION_ID = 104;

    private Notification mNotification;
    private Timer mTimer;
    private ArrayList<String> mUserList = new ArrayList<>();
    private ArrayList<String> mBucketFiles = new ArrayList<>();
    private AWSCredentialsProvider mCredentialsProvider;
    private AmazonRekognitionClient mRekognition;
    private TransferUtility mTransferUtility;
    private AmazonS3Client mS3Client;
    private SurfaceTexture mSurfaceTexture;
    private String mUsername;
    private Intent mIntent;

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

        mIntent = intent;
        mUsername = intent.getStringExtra("username");
        int size = intent.getIntExtra("size", 0);
        for (int i = 0; i < size; i++) {
            mUserList.add(intent.getStringExtra("user" + i));
            mBucketFiles.add(mUsername + "/" + intent.getStringExtra("bucketfiles" + i));
        }

        mSurfaceTexture = new SurfaceTexture(0);

        mTransferUtility = getTransferUtility(this);
        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.fromName(POOL_REGION));
        mRekognition = new AmazonRekognitionClient(mCredentialsProvider);

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
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
                            if (isUser) break;
                        }
                        if (!isUser) lockDown();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 0, 10000);
        return START_STICKY;
    }

    private void lockDown() {
        Log.i(TAG, "lockDown() activated");

        // Upload picture of intruder to S3
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
                BUCKET_NAME,
                mIntent.getStringExtra("username") + "/Intruder/99.jpg",
                BUCKET_NAME,
                mIntent.getStringExtra("username") + "/Intruder/98.jpg");
        mS3Client.copyObject(copyObjectRequest);
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {}

        copyObjectRequest = new CopyObjectRequest(
                BUCKET_NAME,
                mIntent.getStringExtra("username") + "/Intruder/100.jpg",
                BUCKET_NAME,
                mIntent.getStringExtra("username") + "/Intruder/99.jpg");
        mS3Client.copyObject(copyObjectRequest);
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {}
        String randomID = UUID.randomUUID().toString();
        File file = new File(PATH_STREAM + "/Stream.jpg");
        TransferObserver observer = mTransferUtility.upload(
                BUCKET_NAME,
                mIntent.getStringExtra("username") + "/Intruder/" + 100 + ".jpg",
                file);
        Log.i(TAG, "Uploading");
        observer.setTransferListener(new UploadListener());

        // Vibrates phone
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(3000,255));

        // Email owner of phone
        AmazonSNSClient snsClient = new AmazonSNSClient(mCredentialsProvider);
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        String msg = "Phone Protection has identified this individual using your phone.\n" +
                "https://s3.amazonaws.com/phoneprotectionpictures/" +
                mUsername + "/Intruder/" + 100 + ".jpg\n\n" +
                "Go to http://phoneprotection.com/ to locate your phone";
        String subject = "IMPORTANT: Someone Has Your Phone";
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

    public TransferUtility getTransferUtility(Context context) {
        mS3Client = getS3Client(context.getApplicationContext());
        TransferUtility sTransferUtility = new TransferUtility(
                mS3Client, context.getApplicationContext());
        return sTransferUtility;
    }

    public static AmazonS3Client getS3Client(Context context) {
        AmazonS3Client sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
        return sS3Client;
    }

    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        CognitoCachingCredentialsProvider sCredProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.fromName(POOL_REGION));
        return sCredProvider;
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
