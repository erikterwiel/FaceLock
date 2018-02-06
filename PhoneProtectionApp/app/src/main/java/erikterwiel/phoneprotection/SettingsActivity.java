package erikterwiel.phoneprotection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity.java";

    private MenuItem mDone;
    private SeekBar mScanBar;
    private TextView mScanDisplay;
    private SeekBar mSafeBar;
    private TextView mSafeDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mScanBar = (SeekBar) findViewById(R.id.settings_scan_frequency_bar);
        mScanDisplay = (TextView) findViewById(R.id.settings_scan_frequency_display);
        mSafeBar = (SeekBar) findViewById(R.id.settings_safe_mode_duration_bar);
        mSafeDisplay = (TextView) findViewById(R.id.settings_safe_mode_duration_display);

        mScanDisplay.setText(mScanBar.getProgress() + "s");
        mScanBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mScanDisplay.setText(i + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSafeDisplay.setText(mSafeBar.getProgress() + "s");
        mSafeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mSafeDisplay.setText(i + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_done, menu);
        mDone = menu.findItem(R.id.settings_done);
        mDone.setOnMenuItemClickListener((menuItem) -> {
            finish();
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }


}
