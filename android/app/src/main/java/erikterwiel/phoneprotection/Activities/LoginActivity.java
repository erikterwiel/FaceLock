package erikterwiel.phoneprotection.Activities;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;

import java.util.List;

import erikterwiel.phoneprotection.MyAdminReceiver;
import erikterwiel.phoneprotection.R;

import static erikterwiel.phoneprotection.Keys.CognitoKeys.POOL_ID;
import static erikterwiel.phoneprotection.Keys.CognitoKeys.CLIENT_ID;
import static erikterwiel.phoneprotection.Keys.CognitoKeys.CLIENT_SECRET;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity.java";
    private static final int REQUEST_PERMISSION = 100;
    private static final int REQUEST_ADMIN = 105;

    private CognitoUserPool mUserPool;
    private CognitoUser mCognitoUser;
    private EditText mEmail;
    private EditText mPassword;
    private Button mLogin;
    private Button mRegister;
    private Button mForgot;
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
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSION);


        Log.i(TAG, "Unique ID: " + Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));

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

        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mLogin = findViewById(R.id.login_login);
        mRegister = findViewById(R.id.login_register);
        mForgot = findViewById(R.id.login_forgot);

        mMemory = getSharedPreferences("memory", Context.MODE_PRIVATE);
        if (mMemory.contains("email")) {
            mEmail.setText(mMemory.getString("email", null));
            mPassword.requestFocus();
        }

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        mUserPool = new CognitoUserPool(
                this, POOL_ID, CLIENT_ID, CLIENT_SECRET, clientConfiguration);
        mLogin.setOnClickListener(view -> {
            mCognitoUser = mUserPool.getUser();
            AuthenticationHandler handler = new AuthenticationHandler() {
                @Override
                public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                    Toast.makeText(
                            LoginActivity.this,
                            "Authentication successful, loading...",
                            Toast.LENGTH_SHORT).show();
                    Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                    homeIntent.putExtra("username", userSession.getUsername());
                    startActivity(homeIntent);
                }

                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(
                            LoginActivity.this,
                            exception.getMessage().split("\\(")[0],
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void getAuthenticationDetails(AuthenticationContinuation continuation, String userId) {
                    AuthenticationDetails authDetails = new AuthenticationDetails(
                            mEmail.getText().toString() ,
                            mPassword.getText().toString(),
                            null
                    );
                    continuation.setAuthenticationDetails(authDetails);
                    continuation.continueTask();
                }

                @Override
                public void getMFACode(MultiFactorAuthenticationContinuation continuation) {}

                @Override
                public void authenticationChallenge(ChallengeContinuation continuation) {}
            };
            mCognitoUser.getSessionInBackground(handler);
        });

        mPassword.setOnEditorActionListener((textView, actionID, keyEvent) -> {
            if (actionID == EditorInfo.IME_ACTION_SEND) {
                Log.i(TAG, "Attempting to perform a click");
                mLogin.performClick();
                return true;
            }
            return false;
        });

        mRegister.setOnClickListener(view -> {
            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        });

        mForgot.setOnClickListener(view -> {
            Intent forgotIntent = new Intent(LoginActivity.this, ForgotActivity.class);
            startActivity(forgotIntent);
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
