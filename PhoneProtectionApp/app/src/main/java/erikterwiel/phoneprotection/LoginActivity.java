package erikterwiel.phoneprotection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity.java";

    private static final String POOL_ID_UNAUTH = "us-east-1:d2040261-6a0f-4cba-af96-8ead1b66ec38";
    
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.US_EAST_1);

        mRegister = (Button) findViewById(R.id.login_register);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }
}
