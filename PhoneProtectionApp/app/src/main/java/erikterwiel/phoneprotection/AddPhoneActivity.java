package erikterwiel.phoneprotection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3Client;

public class AddPhoneActivity extends AppCompatActivity {

    private static final String TAG = "AddPhoneActivity.java";
    private static final String POOL_ID_UNAUTH = "us-east-1:d2040261-6a0f-4cba-af96-8ead1b66ec38";
    private static final String POOL_REGION = "us-east-1";

    private TransferUtility mTransferUtility;
    private AmazonS3Client mS3Client;
    private AWSCredentialsProvider mCredentialsProvider;
    private AmazonDynamoDBClient mDDBClient;
    private DynamoDBMapper mMapper;
    private EditText mName;
    private Button mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone);

        mTransferUtility = getTransferUtility(this);
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.US_EAST_1);
        mDDBClient = new AmazonDynamoDBClient(credentialsProvider);
        mMapper = new DynamoDBMapper(mDDBClient);

        mName = (EditText) findViewById(R.id.phone_name);
        mAdd = (Button) findViewById(R.id.phone_add_phone);

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UploadPhone().execute();
            }
        });
    }

    private class UploadPhone extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(AddPhoneActivity.this,
                    getString(R.string.phone_uploading),
                    getString(R.string.phone_wait));
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            Username username = new Username();
            username.setUsername(getIntent().getStringExtra("username"));
            username.setName(mName.getText().toString());
            mMapper.save(username);
            finish();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mDialog.dismiss();
        }
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
}
