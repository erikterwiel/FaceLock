package erikterwiel.phoneprotection;

import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DetectionService extends Service {
    private static final String TAG = "DetectionService.java";

    private ScheduledExecutorService mScheduledExecutor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() called");
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
                            camera.setPreviewTexture(new SurfaceTexture(0));
                            camera.startPreview();
                        } catch (Exception e) {
                            Log.i(TAG, "Could not set the surface preview texture");
                        }
                        camera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] bytes, Camera camera) {
                                File folder = new File("sdcard/Pictures/PhoneProtection/Stream");
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
                }


            }
        }, 0, 30, TimeUnit.SECONDS);
        return super.onStartCommand(intent, flags, startId);
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
