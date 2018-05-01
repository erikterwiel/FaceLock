package erikterwiel.phoneprotection;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import static erikterwiel.phoneprotection.DynamoDBKeys.POOL_ID_UNAUTH;
import static erikterwiel.phoneprotection.DynamoDBKeys.POOL_REGION;

public class AddPhoneActivity extends AppCompatActivity {

    private static final String TAG = "AddPhoneActivity.java";

    private TransferUtility mTransferUtility;
    private AmazonDynamoDBClient mDDBClient;
    private DynamoDBMapper mMapper;
    private EditText mName;
    private Button mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone);

        mTransferUtility = S3.getInstance().getTransferUtility();
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.fromName(POOL_REGION));
        mDDBClient = new AmazonDynamoDBClient(credentialsProvider);
        mMapper = new DynamoDBMapper(mDDBClient);

        mName = findViewById(R.id.phone_name);
        mAdd = findViewById(R.id.phone_add_phone);

        mAdd.setOnClickListener(view ->
            new UploadPhone().execute()
        );
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
}
