package erikterwiel.phoneprotection.Activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;

import erikterwiel.phoneprotection.R;

import static erikterwiel.phoneprotection.Keys.CognitoKeys.CLIENT_ID;
import static erikterwiel.phoneprotection.Keys.CognitoKeys.CLIENT_SECRET;
import static erikterwiel.phoneprotection.Keys.CognitoKeys.POOL_ID;

public class ForgotActivity extends AppCompatActivity {

    private EditText mEmail;
    private Button mSubmit;
    private String mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        getSupportActionBar().hide();

        mEmail = findViewById(R.id.forgot_email);
        mSubmit = findViewById(R.id.forgot_submit);
        mSubmit.setOnClickListener(view -> new ForgotPassword().execute());
    }

    private class ForgotPassword extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... inputs) {
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            CognitoUserPool userPool = new CognitoUserPool(
                    ForgotActivity.this, POOL_ID, CLIENT_ID, CLIENT_SECRET, clientConfiguration);
            CognitoUser cognitoUser = userPool.getUser(mEmail.getText().toString());
            cognitoUser.forgotPassword(new ForgotPasswordHandler() {
                @Override
                public void onSuccess() {}
                @Override
                public void getResetCode(ForgotPasswordContinuation continuation) {
                    mResult = "A link has been sent to your email to reset your password.";
                }
                @Override
                public void onFailure(Exception exception) {
                    mResult = exception.getMessage().split("\\(")[0];
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(ForgotActivity.this, mResult, Toast.LENGTH_LONG).show();
        }
    }
}
