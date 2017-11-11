package erikterwiel.phoneprotection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
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
    private ArrayList<User> mUserList = new ArrayList<>();
    private int mCompletedDownloads;
    private RecyclerView mUsers;
    private UserAdapter mUserAdapter;
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
        AmazonDynamoDBClient
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
                String key = summary.getKey();
                if (key.contains(getIntent().getStringExtra("username"))) {
                    map.put("key", key);
                    mTransferRecordMaps.add(map);
                }
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

        User user = new User();
        String filePath = file.getAbsolutePath();
        String[] filePathSplit = filePath.split("/");
        String nameJpg = filePathSplit[filePathSplit.length - 1];
        String name = nameJpg.substring(0, nameJpg.length() - 4);
        user.setFileName(filePath);
        user.setName(name);
        mUserList.add(user);
        Log.i(TAG, user.getFileName());
        Log.i(TAG, user.getName());
    }

    private class DownloadListener implements TransferListener {
        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.i(TAG, state + "");
            if (state == TransferState.COMPLETED) {
                mCompletedDownloads += 1;
                if (mCompletedDownloads == mUserList.size()) displayList();
            }
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

    public void displayList() {
        for (int i = 0; i < mUserList.size(); i++) {
            Bitmap bitmap = BitmapFactory.decodeFile(mUserList.get(i).getFileName());
            mUserList.get(i).setImage(bitmap);
        }
        mUsers = (RecyclerView) findViewById(R.id.home_users);
        mUsers.setLayoutManager(new LinearLayoutManager(this));
        mUserAdapter = new UserAdapter(mUserList);
        mUsers.setAdapter(mUserAdapter);
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

    private class UserAdapter extends RecyclerView.Adapter<UserHolder> {
        private ArrayList<User> userList;

        public UserAdapter(ArrayList<User> incomingList) {
            userList = incomingList;
        }

        @Override
        public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(HomeActivity.this);
            View view = layoutInflater.inflate(R.layout.item_user, parent, false);
            return new HomeActivity.UserHolder(view);
        }

        @Override
        public void onBindViewHolder(UserHolder holder, int position) {
            User user = userList.get(position);
            holder.bindUser(user);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        private User mUser;
        private ImageView mImage;
        private TextView mName;

        public UserHolder(View itemView) {
            super(itemView);
            mImage = (ImageView) itemView.findViewById(R.id.user_image);
            mName = (TextView) itemView.findViewById(R.id.user_name);
        }

        public void bindUser(User user) {
            mUser = user;
            mImage.setImageBitmap(Bitmap.createScaledBitmap(
                    mUser.getImage(),200, 200, false));
            mName.setText(mUser.getName());
        }
    }

}