package com.lucasrivaldo.cloneifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Company;
import com.lucasrivaldo.cloneifood.model.User;
import com.lucasrivaldo.cloneifood.view_model.UserTypeViewModel;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.USERS;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;

public class SplashActivity extends AppCompatActivity {

    private UserTypeViewModel mTypeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        preLoad();


        if (UserFirebase.getCurrentUser() == null) {
            new Handler().postDelayed(this::openAuthActivity, 3000);
        } else {
            mTypeViewModel.setUType(UserFirebase.getCurrentUser().getDisplayName());
        }

    }

    private void preLoad() {


        mTypeViewModel = ViewModelProviders.of(this).get(UserTypeViewModel.class);

        mTypeViewModel.getUType().observe(this, type -> {


            if (type != null){
                new Handler().postDelayed(() ->  getLoggedUser(type), 2000);
            }
        });
    }

    private void getLoggedUser(String uType) {

        UserFirebase.getLoggedUserData(uType, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (uType.equals(USERS)) {

                    User user = snapshot.getValue(User.class);
                    if (user != null) {

                        Intent intent = new Intent(getApplicationContext(), ClientHomeActivity.class);
                        intent.putExtra("bundle", user);

                        startActivity(intent);
                        finish();
                    }else
                        invalidLogin();


                }else {

                    Company company = snapshot.getValue(Company.class);

                    if (company != null) {

                        Intent intent = new Intent(getApplicationContext(), CompanyHomeActivity.class);
                        intent.putExtra("bundle", company);

                        startActivity(intent);
                        finish();
                    }else
                        invalidLogin();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void invalidLogin(){
        openAuthActivity();
        UserFirebase.signOut();
        throwToast("Authentication Failed\nUser not found.", true);
    }

    private void openAuthActivity(){
        startActivity(new Intent(this, AuthenticationActivity.class));
        finish();
    }

    private void throwToast(String message, boolean isLong) {
        Toast.makeText(this, message,
                isLong ? Toast.LENGTH_LONG :Toast.LENGTH_SHORT).show();
    }
}
