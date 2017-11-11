package erikterwiel.phoneprotection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity.java";
    private static final String POOL_ID_UNAUTH = "us-east-1:d2040261-6a0f-4cba-af96-8ead1b66ec38";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView test = (TextView) findViewById(R.id.test);
        test.setText(getIntent().getStringExtra("username"));

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.US_EAST_1);

    }
}
