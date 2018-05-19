package erikterwiel.phoneprotection.Activities;

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

import erikterwiel.phoneprotection.R;

import static erikterwiel.phoneprotection.Keys.CognitoKeys.CLIENT_ID;
import static erikterwiel.phoneprotection.Keys.CognitoKeys.CLIENT_SECRET;
import static erikterwiel.phoneprotection.Keys.CognitoKeys.POOL_ID;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity.java";

    private CognitoUserPool mUserPool;
    private MenuItem mDone;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mConfirm;
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        mUserPool = new CognitoUserPool(
                this, POOL_ID, CLIENT_ID, CLIENT_SECRET, clientConfiguration);

        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);
        mRegister = findViewById(R.id.register_register);
        mConfirm = findViewById(R.id.register_confirm);

        mRegister.setOnClickListener(view -> {
            if (mPassword.getText().toString().equals(mConfirm.getText().toString())) {
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
                                exception.getClass().toString().split(" ")[1].split("\\.")[5],
                                Toast.LENGTH_LONG).show();
                    }
                };
                CognitoUserAttributes userAttributes = new CognitoUserAttributes();
                mUserPool.signUpInBackground(mEmail.getText().toString(),
                        mPassword.getText().toString(),
                        userAttributes, null, signupCallback);
            } else {
                Toast.makeText(
                        RegisterActivity.this,
                        "Entered password does not match confirmation.",
                        Toast.LENGTH_LONG).show();
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
