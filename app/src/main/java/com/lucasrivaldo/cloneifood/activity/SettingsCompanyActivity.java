package com.lucasrivaldo.cloneifood.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.helper.AlertDialogUtil;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.SystemPermissions;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Company;
import com.lucasrivaldo.cloneifood.model.Restaurant;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.IMAGES;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.RESTAURANT;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.invalidText;

public class SettingsCompanyActivity extends AppCompatActivity
        implements View.OnClickListener {

    private String mPhotoStringHolder;
    private Company myCompany;
    private Restaurant myRestaurant;

    private CurrencyEditText mEditDeliveryTax;
    private EditText mEditCategory, mEditDeliveryTime, mEditName;
    private CircleImageView mProfileImage;

    private AlertDialog mDialog;

    /** ####################################  INITIALIZE  #################################### **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_settings);

        SystemPermissions.validatePermissions(this, 1);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            myCompany = (Company) bundle.getSerializable("bundle");
            if (myCompany != null){

                preLoad();
                loadInterface();
            }
        }else {

            throwToast("Company data is null", true);
            if (UserFirebase.signOut()) {
                startActivity(new Intent(this, AuthenticationActivity.class));
                finishAffinity();
            }
        }
    }

    private void preLoad() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar(toolbar);

        if (myCompany.getRestaurant() != null)
            myRestaurant = myCompany.getRestaurant();
    }

    private void loadInterface() {
        mEditCategory = findViewById(R.id.editCategory);
        mEditDeliveryTime = findViewById(R.id.editDeliveryTime);
        mEditDeliveryTax = findViewById(R.id.editDeliveryTax);
        mEditName = findViewById(R.id.editProductName);

        if (!IfoodHelper.invalidText(myCompany.getName()))
            mEditName.setText(myCompany.getName());

        mProfileImage = findViewById(R.id.profile_image);

        Locale currentLocale = new Locale("pt", "BR");
        mEditDeliveryTax.setLocale(currentLocale);

        setClickListeners();

        if (myRestaurant != null)
            loadRestaurantInfo();
    }

    private void loadRestaurantInfo() {
        String category, deliveryTime, deliveryTax, name, photo;

        name = myCompany.getName();
        if (!invalidText(name)) mEditName.setText(name);

        category = myRestaurant.getCategory();
        if (!invalidText(category)) mEditCategory.setText(category);

        deliveryTax = myRestaurant.getDeliveryTax();
        if (!invalidText(deliveryTax)) mEditDeliveryTax.setText(deliveryTax);

        deliveryTime = myRestaurant.getDeliveryTime();
        if (!invalidText(deliveryTime)) mEditDeliveryTime.setText(deliveryTime);

        photo = myRestaurant.getPhoto();
        if (!invalidText(photo)){
            mPhotoStringHolder = photo;
            Picasso.get().load(Uri.parse(photo)).into(mProfileImage);
        }
    }

    /** ##################################  CLICK LISTENERS  ################################## **/

    private void setClickListeners() {
        mProfileImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int itemId = view.getId();
        switch (itemId) {
            case R.id.profile_image:

                Intent galleryIntent = new Intent
                        (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(galleryIntent, 0);
                break;
        }
    }

    // CLICK LISTENER FOR BUTTON REGISTER ANNOUNCEMENT
    public void registerAnnouncement(View view) {
        if (validateAnnouncementData()) {
            throwToast(getResources().getString(R.string.text_updating_prof), false);
        }
    }

    /**  ####################################  MY METHODS  #################################### **/

    private boolean validateAnnouncementData() {

        if (!validateText())
            return false;

        else if (invalidText(mPhotoStringHolder)) {

            throwToast(getResources().getString(R.string.text_img_required), true);
            return false;

        } else {

            mDialog = AlertDialogUtil.progressDialogAlert // UPDATING PROFILE DIALOG
                    (this, getResources().getString(R.string.text_updating_prof));

            // ON SAVE COMPLETION DIALOG DISMISS AND CLOSES SETTINGS
            mDialog.setOnDismissListener
                    (dialogInterface -> throwToast("Update complete!", false));

            mDialog.show();

            return uploadImages();
        }
    }

    private boolean validateText() {

        String companyName = mEditName.getText().toString();
        String category = mEditCategory.getText().toString();
        long deliv_tax_value = mEditDeliveryTax.getRawValue();
        String deliv_time = mEditDeliveryTime.getText().toString();


        if (invalidText(companyName)){
            throwToast("Company name is empty!", true);
            return false;
        } else if (invalidText(category)) {

            throwToast("Restaurant category is empty!", true);
            return false;

        } else if (deliv_tax_value == 0) {

            throwToast("Restaurant delivery tax is empty!", true);
            return false;

        } else if (invalidText(deliv_time)) {

            throwToast("Restaurant delivery time is empty!", true);
            return false;

        }else {

            String deliv_tax_string = mEditDeliveryTax.getText().toString();

            myRestaurant = new Restaurant(myCompany.getId());

            myRestaurant.setName(companyName);
            myRestaurant.setCategory(category);
            myRestaurant.setDeliveryTax(deliv_tax_string);
            myRestaurant.setDeliveryTime(deliv_time);

            if (!companyName.equals(myCompany.getName()))
                myCompany.setName(companyName);

            return true;
        }
    }

    private boolean uploadImages() {

        StorageReference announcesImgRef =
                ConfigurateFirebase.getStorageRef()
                        .child(IMAGES)
                        .child(RESTAURANT)
                        .child(UserFirebase.getCurrentUserID());

        try {
            if (mPhotoStringHolder != null) {

                if (mPhotoStringHolder.equals(myRestaurant.getPhoto()))
                    updateCompany();

                else{
                    StorageReference imgRef = announcesImgRef.child("profile_image");

                    UploadTask uploadTask = imgRef.putFile(Uri.parse(mPhotoStringHolder));
                    uploadTask.addOnCompleteListener(task ->
                            imgRef.getDownloadUrl().addOnCompleteListener(task1 -> {

                                String photoUrl = task1.getResult().toString();

                                myRestaurant.setPhoto(photoUrl);
                                updateCompany();
                            })
                    );
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            throwToast(e.getMessage(), true);
            Log.d(TAG, "uploadImages: " + e.getMessage());

            return false;
        }
    }

    private void updateCompany() {
        myCompany.setRestaurant(myRestaurant);
        if (myCompany.update()) mDialog.dismiss();
        else {
            throwToast("error updating", false);
            finish();
        }
    }


    /** #################################  ACTIVITY PROCESS  ################################## **/

    @Override
    public boolean onSupportNavigateUp() {
        if (!validateAnnouncementData())
            return false;
        else {
            mDialog.dismiss();
            finish();
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult
    (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissionResult : grantResults) {
            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                AlertDialogUtil.permissionValidationAlert(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            Uri selectedImage = data.getData();
            mProfileImage.setImageURI(selectedImage);

            mPhotoStringHolder = selectedImage.toString(); // IMAGE ADDRESS
        }
    }

    /** ####################################  HELPERS  #################################### **/

    private void throwToast(String message, boolean isLong) {
        Toast.makeText(this, message,
                isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }
}
