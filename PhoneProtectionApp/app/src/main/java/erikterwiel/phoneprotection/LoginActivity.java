package erikterwiel.phoneprotection;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity.java";
    private static final String POOL_ID_AUTH = "us-east-1_quEHfVOLz";
    private static final String CLIENT_ID = "3f9c5tmbc37qkos75d69nfmbsm";
    private static final String CLIENT_SECRET = "ikcnfkqik9k6srh3ms6bt7vpbsgj55s0h0bfrh435bkh0topkl4";
    private static final int REQUEST_PERMISSION = 100;
    private static final int REQUEST_ADMIN = 105;

    private CognitoUserPool mUserPool;
    private CognitoUser mCognitoUser;
    private EditText mEmail;
    private EditText mPassword;
    private Button mLogin;
    private Button mRegister;
    private SharedPreferences mMemory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate() called");
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        ActivityCompat.requestPermissions(
                this,
                new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA},
                REQUEST_PERMISSION);

        DevicePolicyManager devicePolicyManager =
                (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        List<ComponentName> admins = devicePolicyManager.getActiveAdmins();
        boolean isAdminApp = false;
        for (ComponentName name : admins) {
            if (name.getClassName().equals("erikterwiel.phoneprotection.MyAdminReceiver")) {
                isAdminApp = true;
                break;
            }
        }
        if (!isAdminApp) {
            ComponentName compName = new ComponentName(this, MyAdminReceiver.class);
            Intent adminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            adminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            adminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Admin app privilege needed to lock the screen on lock down.");
            startActivityForResult(adminIntent, REQUEST_ADMIN);
        }

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        mUserPool = new CognitoUserPool(
                this, POOL_ID_AUTH, CLIENT_ID, CLIENT_SECRET, clientConfiguration);
        mCognitoUser = mUserPool.getUser();

        mEmail = (EditText) findViewById(R.id.login_email);
        mPassword = (EditText) findViewById(R.id.login_password);
        mLogin = (Button) findViewById(R.id.login_login);
        mRegister = (Button) findViewById(R.id.login_register);

        mMemory = getSharedPreferences("memory", Context.MODE_PRIVATE);
        if (mMemory.contains("email")) {
            mEmail.setText(mMemory.getString("email", null));
            mPassword.requestFocus();
        }

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
                if (actionID == EditorInfo.IME_ACTION_SEND) {
                    Log.i(TAG, "Attempting to perform a click");
                    mLogin.performClick();
                    return true;
                }
                return false;
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick() called");
                AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
                    @Override
                    public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                        Toast.makeText(LoginActivity.this,
                                "Login successful.",
                                Toast.LENGTH_LONG).show();
                        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                        homeIntent.putExtra("username", userSession.getUsername());
                        Log.i(TAG, "Passing " + userSession.getUsername() + " to HomeActivity");
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

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop() called");
        SharedPreferences.Editor memoryEditor = mMemory.edit();
        memoryEditor.putString("email", mEmail.getText().toString());
        memoryEditor.apply();
    }
}
