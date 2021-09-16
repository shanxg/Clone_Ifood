package com.lucasrivaldo.cloneifood.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.helper.AlertDialogUtil;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Company;
import com.lucasrivaldo.cloneifood.model.User;
import com.lucasrivaldo.cloneifood.view_model.UserTypeViewModel;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.COMPANY;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.USERS;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;

public class AuthenticationActivity extends AppCompatActivity implements AlertDialogUtil.ReturnAlertData {

    private static final String TYPE_SIGN = "sign";
    private static final String TYPE_REG = "reg";

    private UserTypeViewModel uTypeVM;
    private String uType;

    private User mUser;
    private Company mCompany;

    private EditText mEditTextUserEmail, mEditTextUserPW;
    private TextView mTextButtonSwitchReg, mTextButtonSwitchSign;
    private RadioButton mBtnCustomer, mBtnCompany;
    private RadioGroup mTypeGroup;
    private Switch mSwitchType;
    private Button mButtonSignIn;
    private android.app.AlertDialog mProgressDialog;



    /** ####################################  INITIALIZE  #################################### **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        preLoad();
        loadInterface();
    }

    private void preLoad() {
        uTypeVM = ViewModelProviders.of(this).get(UserTypeViewModel.class);
        setViewModelObservers();

        mProgressDialog = AlertDialogUtil.progressDialogAlert(this, "");
    }

    private void loadInterface() {
        mEditTextUserEmail = findViewById(R.id.editEmail);
        mEditTextUserPW = findViewById(R.id.editPW);
        mButtonSignIn = findViewById(R.id.buttonLogin);

        mTextButtonSwitchReg = findViewById(R.id.textButtonSwitchReg);
        mTextButtonSwitchSign = findViewById(R.id.textButtonSwitchSign);

        mTypeGroup = findViewById(R.id.typeRadioGroup);
        mBtnCompany = findViewById(R.id.radioBtnCompany);
        mBtnCustomer = findViewById(R.id.radioBtnCustomer);
        toggleRadioGroup(false);

        mSwitchType = findViewById(R.id.switchType);

        setListeners();
        setClickListeners();
    }

    /** ####################################  LISTENERS  ##################################### **/

    private void setListeners(){
        mTypeGroup.setOnCheckedChangeListener((radioGroup, i) -> {

            if (i == R.id.radioBtnCompany){

                mBtnCompany.setTextColor(getColor(android.R.color.white));
                mBtnCustomer.setTextColor(getColor(R.color.colorAccent));

            }else {

                mBtnCustomer.setTextColor(getColor(android.R.color.white));
                mBtnCompany.setTextColor(getColor(R.color.colorAccent));

            }
            uType = getUserType();

        });
    }

    @Override
    public void returnFilterData(String text, String requestCode) {


        boolean isComplete;
        if (uType.equals(USERS)){

            mUser.setName(text);
            isComplete = mUser.update();

        }else {

            mCompany.setName(text);
            isComplete = mCompany.update();
        }

        if (isComplete){
            finalizeRegistration();
            throwToast("update complete", false);
        }

    }

    private void setViewModelObservers() {
        uTypeVM.getUType().observe(this, type -> {
            uType = type;
            if (uType != null) {
                finishLogin();
                throwToast(getResources().getString(R.string.text_sign_complete), false);
            }
        });
    }

    /** ##################################  CLICK LISTENERS  ################################## **/


    private void setClickListeners(){

        mButtonSignIn.setOnClickListener(view -> {

            String emailText = mEditTextUserEmail.getText().toString();
            String pwText = mEditTextUserPW.getText().toString();

            if (validateText(emailText, pwText)) {
                mProgressDialog.show();

                if (getUserAuth().equals(TYPE_SIGN))
                    authUser(emailText, pwText);
                else
                    registerUser(emailText, pwText);
            }
        });

        mTextButtonSwitchReg.setOnClickListener
                (view ->{
                    mSwitchType.setChecked(true);
                    setLogTypeTextColor();
                });

        mTextButtonSwitchSign.setOnClickListener
                (view ->{
                    mSwitchType.setChecked(false);
                    setLogTypeTextColor();
                });
        mSwitchType.setOnCheckedChangeListener
                ((compoundButton, isChecked) -> setLogTypeTextColor());
    }


    /** ####################################  MY METHODS  #################################### **/

    private void finishLogin() {

        startActivity(new Intent(this, SplashActivity.class));

        mProgressDialog.dismiss();

        finish();
    }

    private void authUser(String emailText, String pwText){

        ConfigurateFirebase.getFirebaseAuth()
                .signInWithEmailAndPassword(emailText, pwText)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        uTypeVM.setUType(UserFirebase.getCurrentUser().getDisplayName());

                    }else {

                        mProgressDialog.dismiss();

                        try { throw task.getException(); }
                        catch (Exception e) {
                            e.printStackTrace();
                            throwToast(e.getMessage(), true);
                            Log.d(TAG, "LoginActivity - authUser: "+ e.getMessage());
                        }
                    }
                });
    }

    private void getUserName() {
        AlertDialogUtil.editTextDialog(this, "name", this::returnFilterData);
    }



    private void registerUser(String emailText, String pwText) {

        ConfigurateFirebase.getFirebaseAuth()
                .createUserWithEmailAndPassword(emailText, pwText)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){

                        uType = getUserType();

                        if (UserFirebase.updateUserProfType(uType)) {

                            getUserName();

                            if (uType.equals(USERS))
                                saveUser(emailText);
                            else
                                saveCompany(emailText);



                            throwToast(getResources().getString(R.string.text_reg_complete),
                                    false);

                        }
                    }else {

                        mProgressDialog.dismiss();

                        try { throw task.getException(); }
                        catch (Exception e) {
                            e.printStackTrace();
                            throwToast(e.getMessage(), true);
                            Log.d(TAG, "LoginActivity - registerUser: "+ e.getMessage());
                        }
                    }
                });
    }

    private void saveUser(String emailText) {
        mUser = new User();
        mUser.setEmail(emailText);
        mUser.setId(UserFirebase.getCurrentUserID());

    }

    private void saveCompany(String emailText) {
        mCompany = new Company();
        mCompany.setEmail(emailText);
        mCompany.setId(UserFirebase.getCurrentUserID());

    }

    private void finalizeRegistration() {

        if (uType.equals(USERS)){
            if (mUser.save(true)) finishLogin();
        }else
        if (mCompany.save(true)) finishLogin();
    }

    /** #################################  ACTIVITY PROCESS  ################################## **/

    /** ####################################  HELPERS  #################################### **/

    private boolean validateText(String emailText, String pwText){

        if (IfoodHelper.invalidText(emailText)) {

            throwToast("User email text is empty", true);
            return false;

        }else if (IfoodHelper.invalidText(pwText)){

            throwToast("User password text is empty", true);
            return false;

        }else
            return true;
    }

    private void setLogTypeTextColor(){
        boolean isChecked = mSwitchType.isChecked();
        toggleRadioGroup(isChecked);

        if (isChecked) {

            mTextButtonSwitchSign.setTextColor(getResources().getColor(R.color.colorAccent));
            mTextButtonSwitchReg.setTextColor(getResources().getColor(android.R.color.white));

        } else {

            mTextButtonSwitchReg.setTextColor(getResources().getColor(R.color.colorAccent));
            mTextButtonSwitchSign.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private String getUserAuth(){
        return  mSwitchType.isChecked() ? TYPE_REG :  TYPE_SIGN;
    }

    private String getUserType(){
        return uType = mBtnCustomer.isChecked() ? USERS : COMPANY;
    }


    private void toggleRadioGroup(boolean isOpening) {
        mTypeGroup.setVisibility(isOpening ? View.VISIBLE : View.GONE);
    }

    private void throwToast(String message, boolean isLong) {
        Toast.makeText(this, message,
                isLong ? Toast.LENGTH_LONG :Toast.LENGTH_SHORT).show();
    }

}