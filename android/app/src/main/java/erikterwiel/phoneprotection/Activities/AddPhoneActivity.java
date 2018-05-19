package erikterwiel.phoneprotection.Activities;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import erikterwiel.phoneprotection.R;
import erikterwiel.phoneprotection.Singletons.DynamoDB;
import erikterwiel.phoneprotection.Account;

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
            Account account = mMapper.load(Account.class, getIntent().getStringExtra("username"));
            if (account == null) {
                account = new Account();
                account.setUsername(getIntent().getStringExtra("username"));
            }
            account.addUnique(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            account.addName(mName.getText().toString());
            account.addLatitude(0.0);
            account.addLongitude(0.0);
            mMapper.save(account);
            finish();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mDialog.dismiss();
        }
    }
}
