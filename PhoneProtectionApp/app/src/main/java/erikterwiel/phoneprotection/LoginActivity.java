package erikterwiel.phoneprotection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.regions.Regions;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity.java";
    private static final String POOL_ID_AUTH = "us-east-1_quEHfVOLz";
    private static final String CLIENT_ID = "3f9c5tmbc37qkos75d69nfmbsm";
    private static final String CLIENT_SECRET = "ikcnfkqik9k6srh3ms6bt7vpbsgj55s0h0bfrh435bkh0topkl4";

    private CognitoUserPool mUserPool;
    private CognitoUser mCognitoUser;
    private EditText mEmail;
    private EditText mPassword;
    private Button mLogin;
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        mUserPool = new CognitoUserPool(
                this, POOL_ID_AUTH, CLIENT_ID, CLIENT_SECRET, clientConfiguration);
        mCognitoUser = mUserPool.getUser();

        mEmail = (EditText) findViewById(R.id.login_email);
        mPassword = (EditText) findViewById(R.id.login_password);
        mLogin = (Button) findViewById(R.id.login_login);
        mRegister = (Button) findViewById(R.id.login_register);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
                    @Override
                    public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                        Toast.makeText(LoginActivity.this,
                                "Login successful.",
                                Toast.LENGTH_LONG).show();
                        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                        homeIntent.putExtra("username", userSession.getUsername());
                        startActivity(homeIntent);
                    }

                    @Override
                    public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                        AuthenticationDetails authenticationDetails = new AuthenticationDetails(
                                mEmail.getText().toString(),
                                mPassword.getText().toString(),
                                null);
                        authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                        authenticationContinuation.continueTask();
                    }

                    @Override
                    public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                        continuation.setMfaCode(null);
                        continuation.continueTask();
                    }

                    @Override
                    public void authenticationChallenge(ChallengeContinuation continuation) {}

                    @Override
                    public void onFailure(Exception exception) {
                        Toast.makeText(LoginActivity.this,
                                "Login unsuccessful, please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                };
                mCognitoUser.getSessionInBackground(authenticationHandler);
            }
        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }
}
