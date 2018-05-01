package erikterwiel.phoneprotection.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;

import erikterwiel.phoneprotection.R;
import erikterwiel.phoneprotection.Singletons.S3;

public class AddUserActivity extends AppCompatActivity {

    private static final String TAG = "AddUserActivity.java";
    private static final String PROVIDER_AUTHORITY = "erikterwiel.phoneprotectionapp.fileprovider";
    private static final String BUCKET_NAME = "phoneprotectionpictures";
    private static final int REQUEST_CAMERA = 101;

    private TransferUtility mTransferUtility;
    private MenuItem mDone;
    private EditText mName;
    private ImageView mImage;
    private Button mAdd;
    private String mOutputPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        mTransferUtility = S3.getInstance().getTransferUtility();

        mName = findViewById(R.id.add_name);
        mImage = findViewById(R.id.add_image);
        mAdd = findViewById(R.id.add_add);

        mAdd.setOnClickListener(view -> {
            if (!mName.getText().toString().equals("")) {
                beginCamera();
            } else {
                Toast.makeText(AddUserActivity.this,
                        "Please enter a user name", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void beginCamera() {
        File file = getFile();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                AddUserActivity.this, PROVIDER_AUTHORITY, file));
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    private File getFile() {
        File folder = new File("sdcard/Pictures/PhoneProtection/Output");
        if (!folder.exists()) folder.mkdir();
        File image = new File(
                folder, mName.getText() + ".jpg");
        mOutputPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File file = new File(mOutputPath);
        TransferObserver observer = mTransferUtility.upload(
                BUCKET_NAME,
                getIntent().getStringExtra("username") + "/" + file.getName(),
                file);
        Log.i(TAG, "Uploading");
        observer.setTransferListener(new UploadListener());
        mImage.setImageDrawable(Drawable.createFromPath(mOutputPath));
        mAdd.setText(R.string.add_replace_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_done, menu);
        mDone = menu.findItem(R.id.add_done);
        mDone.setOnMenuItemClickListener(menuItem -> {
            finish();
            return false;
        });
        return super.onCreateOptionsMenu(menu);
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
}
