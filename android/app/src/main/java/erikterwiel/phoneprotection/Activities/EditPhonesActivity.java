package erikterwiel.phoneprotection.Activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import java.util.ArrayList;

import erikterwiel.phoneprotection.Account;
import erikterwiel.phoneprotection.Phone;
import erikterwiel.phoneprotection.R;
import erikterwiel.phoneprotection.Singletons.DynamoDB;

public class EditPhonesActivity extends AppCompatActivity {

    private Account mAccount;
    private ArrayList<Phone> mPhoneList = new ArrayList<>();
    private RecyclerView mPhones;
    private PhoneAdapter mPhoneAdapter;
    private MenuItem mDone;
    private DynamoDBMapper mMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phones);
        mMapper = DynamoDB.getInstance().getMapper();
        mPhones = findViewById(R.id.edit_phones);
        mPhones.setLayoutManager(new LinearLayoutManager(this));
        new DownloadPhone().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_done, menu);
        mDone = menu.findItem(R.id.add_done);
        mDone.setOnMenuItemClickListener(menuItem -> {
            for (int i = 0; i < mPhoneList.size(); i++) {
                EditText phoneText = mPhones.getChildAt(i).findViewById(R.id.phone_name);
                if (phoneText.getText().toString().equals("")) {
                    Toast.makeText(this, "All phones must have a name.", Toast.LENGTH_LONG).show();
                    return false;
                }
                mAccount.replaceName(i, phoneText.getText().toString());
            }
            new UploadPhone().execute();
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

    private class DownloadPhone extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... inputs) {
            mAccount = mMapper.load(Account.class, getIntent().getStringExtra("username"));
            for (int i = 0; i < mAccount.getUniques().size(); i++) {
                mPhoneList.add(new Phone(
                        mAccount.getUniques().get(i),
                        mAccount.getNames().get(i),
                        mAccount.getLatitudes().get(i),
                        mAccount.getLongitudes().get(i)));
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            mPhoneAdapter = new PhoneAdapter(mPhoneList);
            mPhones.setAdapter(mPhoneAdapter);
        }
    }

    private class UploadPhone extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... inputs) {
            mMapper.save(mAccount);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            finish();
        }
    }

    private class PhoneAdapter extends RecyclerView.Adapter<PhoneHolder> {
        private ArrayList<Phone> phoneList;

        public PhoneAdapter(ArrayList<Phone> incomingList) {
            phoneList = incomingList;
        }

        @Override
        public PhoneHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(EditPhonesActivity.this);
            View view = layoutInflater.inflate(R.layout.item_phone_edit, parent, false);
            return new PhoneHolder(view);
        }

        @Override
        public void onBindViewHolder(PhoneHolder holder, int position) {
            Phone phone = phoneList.get(position);
            holder.bindPhone(phone);
        }

        @Override
        public int getItemCount() {
            return phoneList.size();
        }
    }

    private class PhoneHolder extends RecyclerView.ViewHolder {
        private Phone mPhone;
        private EditText mName;
        private TextView mLatitude;
        private TextView mLongitude;

        public PhoneHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.phone_name);
            mLatitude = itemView.findViewById(R.id.phone_latitude);
            mLongitude = itemView.findViewById(R.id.phone_longitude);
        }

        public void bindPhone(Phone phone) {
            mPhone = phone;
            mName.setText(mPhone.getName());
            mLatitude.setText("Latitude: " + mPhone.getLatitude());
            mLongitude.setText("Longitude: " + mPhone.getLongitude());
        }
    }
}
