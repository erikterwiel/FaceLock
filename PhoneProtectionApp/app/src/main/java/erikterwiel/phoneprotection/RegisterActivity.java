package erikterwiel.phoneprotection;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Regions;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity.java";
    private static final String POOL_ID_AUTH = "us-east-1_quEHfVOLz";
    private static final String CLIENT_ID = "3f9c5tmbc37qkos75d69nfmbsm";
    private static final String CLIENT_SECRET = "ikcnfkqik9k6srh3ms6bt7vpbsgj55s0h0bfrh435bkh0topkl4";

    private CognitoUserPool mUserPool;
    private MenuItem mDone;
    private EditText mEmail;
    private EditText mPassword;
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        mUserPool = new CognitoUserPool(
                this, POOL_ID_AUTH, CLIENT_ID, CLIENT_SECRET, clientConfiguration);

        mEmail = (EditText) findViewById(R.id.register_email);
        mPassword = (EditText) findViewById(R.id.register_password);
        mRegister = (Button) findViewById(R.id.register_register);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 SignUpHandler signupCallback = new SignUpHandler() {
                    @Override
                    public void onSuccess(CognitoUser user, boolean signUpConfirmationState, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful, please check email for verification link.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Toast.makeText(RegisterActivity.this,
                                "Registration unsuccessful, please try again.",
                                Toast.LENGTH_LONG).show();
                        exception.printStackTrace();
                    }
                };
                CognitoUserAttributes userAttributes = new CognitoUserAttributes();
                mUserPool.signUpInBackground(mEmail.getText().toString(),
                        mPassword.getText().toString(),
                        userAttributes, null, signupCallback);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.register_done, menu);
        mDone = menu.findItem(R.id.register_done);
        mDone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                finish();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
