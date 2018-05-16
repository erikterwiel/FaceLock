package erikterwiel.phoneprotection.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import erikterwiel.phoneprotection.R;

public class ForgotActivity extends AppCompatActivity {

    private EditText mEmail;
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        mEmail = findViewById(R.id.forgot_email);
        mSubmit.setOnClickListener(view -> {

        });
    }
}
