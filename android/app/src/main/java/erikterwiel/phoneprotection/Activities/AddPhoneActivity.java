package erikterwiel.phoneprotection.Activities;

import android.location.Location;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import erikterwiel.phoneprotection.R;
import erikterwiel.phoneprotection.Singletons.DynamoDB;
import erikterwiel.phoneprotection.Account;

public class AddPhoneActivity extends AppCompatActivity {

    private static final String TAG = "AddPhoneActivity.java";

    private DynamoDBMapper mMapper;
    private EditText mName;
    private Button mAdd;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone);

        mMapper = DynamoDB.getInstance().getMapper();
        mName = findViewById(R.id.phone_name);
        mAdd = findViewById(R.id.phone_add_phone);

        mAdd.setOnClickListener(view -> {
            if (mName.getText().toString().equals("")) {
                Toast.makeText(this, "Your phone must have a name!", Toast.LENGTH_LONG).show();
            } else {
                new UploadPhone().execute();
            }
        });
    }

    private class UploadPhone extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... inputs) {
            try {
                FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(AddPhoneActivity.this);
                locationClient.getLastLocation().addOnSuccessListener(AddPhoneActivity.this, location -> {
                    if (location != null) {
                        mLocation = location;
                    }
                });
            } catch (SecurityException ex) {
                ex.printStackTrace();
            }

            Account account = mMapper.load(Account.class, getIntent().getStringExtra("username"));
            if (account == null) {
                account = new Account();
                account.setUsername(getIntent().getStringExtra("username"));
            }
            account.addUnique(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            account.addName(mName.getText().toString());
            account.addLatitude(mLocation.getLatitude());
            account.addLongitude(mLocation.getLongitude());
            mMapper.save(account);
            finish();
            return null;
        }
    }
}