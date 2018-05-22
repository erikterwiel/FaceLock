package erikterwiel.phoneprotection.Activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    private static final String TAG = "ForgotActivity.java";

    private EditText mEmail;
    private Button mSubmit;
    private LinearLayout mCode;
    private TextView[] mCodeDigits = new TextView[6];
    private EditText mCodeInput;
    private EditText mPassword;
    private EditText mConfirm;
    private Button mSet;
    private String mResult;
    private boolean mSuccess = false;
    private CognitoUser mCognitoUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        getSupportActionBar().hide();

        mEmail = findViewById(R.id.forgot_email);
        mSubmit = findViewById(R.id.forgot_reset);
        mCode = findViewById(R.id.forgot_code);
        mCodeDigits[0] = findViewById(R.id.forgot_code0);
        mCodeDigits[1] = findViewById(R.id.forgot_code1);
        mCodeDigits[2] = findViewById(R.id.forgot_code2);
        mCodeDigits[3] = findViewById(R.id.forgot_code3);
        mCodeDigits[4] = findViewById(R.id.forgot_code4);
        mCodeDigits[5] = findViewById(R.id.forgot_code5);
        mCodeInput = findViewById(R.id.forgot_code_input);
        mPassword = findViewById(R.id.forgot_password);
        mConfirm = findViewById(R.id.forgot_confirm);
        mSet = findViewById(R.id.forgot_set);

        mSubmit.setOnClickListener(view -> new RequestNewPassword().execute());
        mCode.setOnClickListener(view -> {
            mCodeInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(mCodeInput, InputMethodManager.SHOW_IMPLICIT);
        });
        mCodeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence input, int i, int i1, int i2) {
                if (i2 == 1) {
                    mCodeDigits[i].setText(Character.toString(mCodeInput.getText().toString().charAt(i)));
                } else {
                    mCodeDigits[i].setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        mConfirm.setOnEditorActionListener((textView, i, keyEvent) -> {
            mSet.performClick();
            return true;
        });
        mSet.setOnClickListener(view -> {
            if (mPassword.getText().toString().equals(mConfirm.getText().toString())) {
                new SetPassword().execute();
            } else {
                Toast.makeText(
                        ForgotActivity.this,
                        "Entered password does not match confirmation.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private class RequestNewPassword extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... inputs) {
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            CognitoUserPool userPool = new CognitoUserPool(
                    ForgotActivity.this, POOL_ID, CLIENT_ID, CLIENT_SECRET, clientConfiguration);
            mCognitoUser = userPool.getUser(mEmail.getText().toString());
            mCognitoUser.forgotPassword(new ForgotPasswordHandler() {
                @Override
                public void onSuccess() {}
                @Override
                public void getResetCode(ForgotPasswordContinuation continuation) {
                    mResult = "A verification code has been sent to your email to reset your password.";
                    mSuccess = true;

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
            if (mSuccess) {
                mSuccess = false;
                mEmail.setVisibility(View.GONE);
                mSubmit.setVisibility(View.GONE);
                mCode.setVisibility(View.VISIBLE);
                mPassword.setVisibility(View.VISIBLE);
                mConfirm.setVisibility(View.VISIBLE);
                mSet.setVisibility(View.VISIBLE);
            }
            Toast.makeText(ForgotActivity.this, mResult, Toast.LENGTH_LONG).show();
        }
    }

    private class SetPassword extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            mCognitoUser.confirmPassword(
                    mCodeInput.getText().toString(),
                    mPassword.getText().toString(),
                    new ForgotPasswordHandler() {
                        @Override
                        public void onSuccess() {
                            mResult = "Your password has been reset successfully.";
                            mSuccess = true;
                        }

                        @Override
                        public void getResetCode(ForgotPasswordContinuation continuation) { }

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
            if (mSuccess) finish();
        }
    }
}
