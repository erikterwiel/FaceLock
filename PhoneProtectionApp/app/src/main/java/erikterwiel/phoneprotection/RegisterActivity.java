package erikterwiel.phoneprotection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;

import static erikterwiel.phoneprotection.CognitoKeys.CLIENT_ID;
import static erikterwiel.phoneprotection.CognitoKeys.CLIENT_SECRET;
import static erikterwiel.phoneprotection.CognitoKeys.POOL_ID;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity.java";

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
                this, POOL_ID, CLIENT_ID, CLIENT_SECRET, clientConfiguration);

        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);
        mRegister = findViewById(R.id.register_register);

        mRegister.setOnClickListener(view -> {
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
