package com.lucasrivaldo.cloneifood.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.helper.AlertDialogUtil;
import com.lucasrivaldo.cloneifood.model.User;

public class SettingsClientActivity extends AppCompatActivity
        implements AlertDialogUtil.ReturnAlertData {

    private static final String NAME = "name";
    private static final String ADDRESS = "address";

    private User mLoggedUser;
    private TextView mTextViewUserName, mTextViewUserAddress;

    /** ####################################  INITIALIZE  #################################### **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_client);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            mLoggedUser = (User) bundle.getSerializable("bundle");
            if (mLoggedUser!=null) {
                preLoad();
                loadInterFace();
            }
        }
    }

    private void preLoad() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar(toolbar);
    }

    private void loadInterFace() {
        mTextViewUserName = findViewById(R.id.textViewUserName);
        mTextViewUserAddress = findViewById(R.id.textViewUserAddress);

        mTextViewUserName.setText(mLoggedUser.getName());
        mTextViewUserAddress.setText(mLoggedUser.getAddress());
    }

    /** ##################################  CLICK LISTENERS  ################################## **/

    public void editUsername(View view) {
        AlertDialogUtil.editTextDialog(this, NAME, this::returnFilterData);

    }

    public void editUserAddress(View view) {
        AlertDialogUtil.editTextDialog(this, ADDRESS, this::returnFilterData);
    }


    /** ####################################  LISTENERS  ##################################### **/

    @Override
    public void returnFilterData(String text, String requestCode) {

        switch (requestCode){
            case NAME:
                mLoggedUser.setName(text);
                mTextViewUserName.setText(text);
                break;

            case ADDRESS:
                mLoggedUser.setAddress(text);
                mTextViewUserAddress.setText(text);
                break;
        }
        if (mLoggedUser.update())
            throwToast(requestCode+" updated!", false);

    }

    /** #################################  ACTIVITY PROCESS  ################################## **/

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

        /** ####################################  HELPERS  #################################### **/

    private void throwToast(String message, boolean isLong) {
        Toast.makeText(this, message,
                isLong ? Toast.LENGTH_LONG :Toast.LENGTH_SHORT).show();
    }
}
