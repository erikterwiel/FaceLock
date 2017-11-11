package erikterwiel.phoneprotection;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
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
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DetectionService extends Service {

    private static final String TAG = "DetectionService.java";
    private static final String POOL_ID_UNAUTH = "us-east-1:d2040261-6a0f-4cba-af96-8ead1b66ec38";
    private static final String POOL_REGION = "us-east-1";
    private static final String PATH_STREAM = "sdcard/Pictures/PhoneProtection/Stream";
    private static final float CONFIDENCE_THRESHOLD = 70F;

    private ScheduledExecutorService mScheduledExecutor;
    private ArrayList<String> mUserList = new ArrayList<>();
    private AWSCredentialsProvider mCredentialsProvider;
    private AmazonRekognitionClient mRekognition;
    private SurfaceTexture mSurfaceTexture;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() called");

        int size = intent.getIntExtra("size", 0);
        for (int i = 0; i < size; i++) {
            mUserList.add(intent.getStringExtra("user" + i));
        }

        mSurfaceTexture = new SurfaceTexture(0);

        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.fromName(POOL_REGION));
        mRekognition = new AmazonRekognitionClient(mCredentialsProvider);

        mScheduledExecutor = Executors.newScheduledThreadPool(1);
        mScheduledExecutor.scheduleAtFixedRate(new Runnable() {
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
                            InputStream inputStream2 = new FileInputStream(mUserList.get(i));
                            ByteBuffer imageBytes2 = ByteBuffer.wrap(IOUtils.toByteArray(inputStream2));
                            Image sourceImage = new Image().withBytes(imageBytes2);

                            Log.i(TAG, "Attempting to compare faces");
                            CompareFacesRequest compareFacesRequest = new CompareFacesRequest()
                                    .withSourceImage(sourceImage)
                                    .withTargetImage(targetImage)
                                    .withSimilarityThreshold(CONFIDENCE_THRESHOLD);
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
        }, 0, 30, TimeUnit.SECONDS);
        return super.onStartCommand(intent, flags, startId);
    }

    private void lockDown() {
        Log.i(TAG, "lockDown() activated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
        mScheduledExecutor = Executors.newScheduledThreadPool(0);
        mScheduledExecutor.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
