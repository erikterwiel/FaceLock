package erikterwiel.phoneprotection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity.java";
    private static final String POOL_ID_UNAUTH = "us-east-1:d2040261-6a0f-4cba-af96-8ead1b66ec38";
    private static final String POOL_REGION = "us-east-1";
    private static final String BUCKET_NAME = "phoneprotectionpictures";

    private TransferUtility mTransferUtility;
    private AmazonS3Client mS3Client;
    private AWSCredentialsProvider mCredentialsProvider;
    private ArrayList<HashMap<String, Object>> mTransferRecordMaps = new ArrayList<>();
    private MenuItem mSettings;
    private FloatingActionButton mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mTransferUtility = getTransferUtility(this);
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.US_EAST_1);
        new DownloadUsers().execute();

        mAdd = (FloatingActionButton) findViewById(R.id.home_add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addUserIntent = new Intent(HomeActivity.this, AddUserActivity.class);
                addUserIntent.putExtra("username", getIntent().getStringExtra("username"));
                startActivity(addUserIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_settings, menu);
        mSettings = menu.findItem(R.id.home_settings);
        return super.onCreateOptionsMenu(menu);
    }

    private class DownloadUsers extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(HomeActivity.this,
                    getString(R.string.home_downloading),
                    getString(R.string.home_wait));
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            ObjectListing objectListing = mS3Client.listObjects(BUCKET_NAME);
            List<S3ObjectSummary> s3ObjList = objectListing.getObjectSummaries();
            for (S3ObjectSummary summary : s3ObjList) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", summary.getKey());
                mTransferRecordMaps.add(map);
            }
            for (int i = 0; i < mTransferRecordMaps.size(); i++) {
                beginDownload((String) mTransferRecordMaps.get(i).get("key"));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mDialog.dismiss();
        }
    }

    public void beginDownload(String key) {
        File folder = new File("sdcard/Pictures/PhoneProtection/Input");
        if (!folder.exists()) folder.mkdir();
        File file = new File(folder, key);
        TransferObserver observer = mTransferUtility.download(BUCKET_NAME, key, file);
        observer.setTransferListener(new DownloadListener());
    }

    public TransferUtility getTransferUtility(Context context) {
        TransferUtility sTransferUtility = new TransferUtility(
                getS3Client(context.getApplicationContext()), context.getApplicationContext());
        return sTransferUtility;
    }

    public AmazonS3Client getS3Client(Context context) {
        mS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
        return mS3Client;
    }

    private AWSCredentialsProvider getCredProvider(Context context) {
        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.fromName(POOL_REGION));
        return mCredentialsProvider;
    }

    private class DownloadListener implements TransferListener {
        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.i(TAG, state + "");
        }
        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            if (bytesTotal != 0) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                Log.i(TAG, Integer.toString(percentage) + "% downloaded");
            }
        }
        @Override
        public void onError(int id, Exception ex) {
            ex.printStackTrace();
            Log.i(TAG, "Error detected");
        }
    }

}