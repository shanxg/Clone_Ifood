package com.lucasrivaldo.cloneifood.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.blackcat.currencyedittext.CurrencyEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.helper.AlertDialogUtil;
import com.lucasrivaldo.cloneifood.helper.SystemPermissions;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Product;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.IMAGES;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.PRODUCTS;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.RESTAURANT;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.invalidText;

public class AddProductActivity extends AppCompatActivity
implements View.OnClickListener {

    private String mPhotoStringHolder;


    private Product newProduct;

    private CurrencyEditText mEditProductPrice;
    private EditText mEditProductDescription, mEditProductName;
    private CircleImageView mCivProductImage;

    private AlertDialog mDialog;

    /** ####################################  INITIALIZE  #################################### **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        SystemPermissions.validatePermissions(this, 1);

        preLoad();
        loadInterface();
    }

    private void preLoad() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar(toolbar);
    }

    private void loadInterface() {
        mEditProductDescription = findViewById(R.id.editProductDescription);
        mEditProductPrice = findViewById(R.id.editProductPrice);
        mEditProductName = findViewById(R.id.editProductName);

        mCivProductImage = findViewById(R.id.civProductImage);

        Locale currentLocale = new Locale("pt", "BR");
        mEditProductPrice.setLocale(currentLocale);

        setClickListeners();
    }

    /** ##################################  CLICK LISTENERS  ################################## **/

    private void setClickListeners() {
        mCivProductImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.civProductImage:

                Intent galleryIntent = new Intent
                        (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(galleryIntent, 0);
                break;
        }
    }

    // CLICK LISTENER FOR BUTTON REGISTER ANNOUNCEMENT
    public void registerProduct(View view) {
        if (validateAnnouncementData())
            throwToast(getResources().getString(R.string.text_updating_prof), false);

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
                    (this, getResources().getString(R.string.text_adding_product));

            // ON SAVE COMPLETION DIALOG DISMISS AND CLOSES SETTINGS
            mDialog.setOnDismissListener
                    (dialogInterface -> {
                        throwToast("Update complete!", false);
                        finish();
                    });

            mDialog.show();

            return uploadImages();
        }
    }

    private boolean validateText() {

        String productName = mEditProductName.getText().toString();
        String productDescription = mEditProductDescription.getText().toString();
        long productPriceRawValue = mEditProductPrice.getRawValue();



        if (invalidText(productName)){
            throwToast("Company name is empty!", true);
            return false;
        } else if (invalidText(productDescription)) {

            throwToast("Restaurant category is empty!", true);
            return false;

        } else if (productPriceRawValue == 0) {

            throwToast("Restaurant delivery tax is empty!", true);
            return false;

        } else {

            String productPrice = mEditProductPrice.getText().toString();

            newProduct = new Product();

            newProduct.setName(productName);
            newProduct.setDescription(productDescription);
            newProduct.setPrice(productPrice);
            newProduct.setRest_id(UserFirebase.getCurrentUserID());

            return true;
        }
    }

    private boolean uploadImages() {

        StorageReference announcesImgRef =
                ConfigurateFirebase.getStorageRef()
                        .child(IMAGES)
                        .child(RESTAURANT)
                        .child(UserFirebase.getCurrentUserID())
                        .child(PRODUCTS)
                        .child(newProduct.getProd_id());

        try {
            if (mPhotoStringHolder != null) {

                StorageReference imgRef = announcesImgRef.child("image");

                UploadTask uploadTask = imgRef.putFile(Uri.parse(mPhotoStringHolder));
                uploadTask.addOnCompleteListener(task ->
                        imgRef.getDownloadUrl().addOnCompleteListener(task1 -> {

                            String photoUrl = task1.getResult().toString();

                            newProduct.setPhoto(photoUrl);

                            if (newProduct.save(true, null))
                                mDialog.dismiss();

                        })
                );
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            throwToast(e.getMessage(), true);
            Log.d(TAG, "uploadImages: " + e.getMessage());

            return false;
        }
    }



    /** #################################  ACTIVITY PROCESS  ################################## **/

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
            mCivProductImage.setImageURI(selectedImage);

            mPhotoStringHolder = selectedImage.toString(); // IMAGE ADDRESS
        }
    }

    /** ####################################  HELPERS  #################################### **/

    private void throwToast(String message, boolean isLong) {
        Toast.makeText(this, message,
                isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }
}