package erikterwiel.phoneprotection;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

public class AddUserActivity extends AppCompatActivity {

    private static final String TAG = "AddUserActivity.java";
    private static final String POOL_ID_UNAUTH = "us-east-1:d2040261-6a0f-4cba-af96-8ead1b66ec38";
    private static final String POOL_REGION = "us-east-1";
    private static final String PROVIDER_AUTHORITY = "erikterwiel.phoneprotectionapp.fileprovider";
    private static final String BUCKET_NAME = "phoneprotectionpictures";
    private static final int REQUEST_CAMERA = 101;

    private AmazonS3Client mS3Client;
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

        mTransferUtility = getTransferUtility(this);

        mName = (EditText) findViewById(R.id.add_name);
        mImage = (ImageView) findViewById(R.id.add_image);
        mAdd = (Button) findViewById(R.id.add_add);

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
        mDone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                finish();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
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
}
