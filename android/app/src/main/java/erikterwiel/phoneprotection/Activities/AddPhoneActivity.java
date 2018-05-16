package erikterwiel.phoneprotection.Activities;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import erikterwiel.phoneprotection.R;
import erikterwiel.phoneprotection.Singletons.DynamoDB;
import erikterwiel.phoneprotection.Username;

public class AddPhoneActivity extends AppCompatActivity {

    private static final String TAG = "AddPhoneActivity.java";

    private DynamoDBMapper mMapper;
    private EditText mName;
    private Button mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone);

        mMapper = DynamoDB.getInstance().getMapper();
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
            Username username = mMapper.load(Username.class, getIntent().getStringExtra("username"));
            if (username == null) {
                username = new Username();
                username.setUsername(getIntent().getStringExtra("username"));
                String[] uniques = new String[1];
                String[] phoneNames = new String[1];
                uniques[0] = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                phoneNames[0] = mName.getText().toString();
                username.setNames(phoneNames);
            } else {
                String[] uniques = new String[username.getUniques().length];
                String[] phoneNames = new String[username.getNames().length];
                uniques[username.getUniques().length] = Settings.Secure.getString(
                        getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                phoneNames[username.getNames().length] = mName.getText().toString();
                username.setUniques(uniques);
                username.setNames(phoneNames);
            }
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
