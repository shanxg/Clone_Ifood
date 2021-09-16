package com.lucasrivaldo.cloneifood.model;

import android.util.Log;

import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;
import com.lucasrivaldo.cloneifood.activity.SplashActivity;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.view_model.UserTypeViewModel;

import java.io.Serializable;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.MARKET;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.PRODUCTS;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.RESTAURANT;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;

public class Product implements Serializable {

    private String name, price, description, photo, prod_id, rest_id;

    public Product() {

        if (IfoodHelper.invalidText(this.getProd_id()))
            this.setProd_id(createId());
    }

    public boolean save(boolean isSaving, OnCompleteListener<Void> listener){

        DatabaseReference productsRef = ConfigurateFirebase.getFireDBRef()
                .child(RESTAURANT)
                .child(UserFirebase.getCurrentUserID())
                .child(PRODUCTS)
                .child(this.getProd_id());
        try {


            if (listener != null){
                if (isSaving) {
                    productsRef.setValue(this).addOnCompleteListener(listener);
                }else {
                    productsRef.removeValue().addOnCompleteListener(listener);
                }

            }else {
                if (isSaving) {
                    productsRef.setValue(this);
                }else {
                    productsRef.removeValue();
                }
            }

            return true;

        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "Restaurant.save(): "+e.getMessage());
            return false;
        }
    }

    public boolean rmvAtMarket(){
        DatabaseReference marketRef = ConfigurateFirebase.getFireDBRef()
                .child(MARKET)
                .child(UserFirebase.getCurrentUserID())
                .child(this.getProd_id());

        try {
            marketRef.removeValue();
            return true;

        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "Restaurant.rmvAtMarket(): "+e.getMessage());
            return false;
        }
    }

    public void rmvAtMarket(OnCompleteListener<Void> listener){
        DatabaseReference marketRef = ConfigurateFirebase.getFireDBRef()
                .child(MARKET)
                .child(UserFirebase.getCurrentUserID())
                .child(this.getProd_id());

        marketRef.removeValue().addOnCompleteListener(listener);
    }

    public String getPhoto() { return photo; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getDescription() { return description; }
    public String getProd_id() { return prod_id; }
    public String getRest_id() { return rest_id; }

    public void setPhoto(String photo) { this.photo = photo; }
    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setProd_id(String prod_id) { this.prod_id = prod_id; }
    public void setRest_id(String rest_id) { this.rest_id = rest_id; }

    private String createId(){
        return ConfigurateFirebase.getFireDBRef()
                .child(RESTAURANT)
                .child(UserFirebase.getCurrentUserID())
                .child(PRODUCTS)
                .push()
                .getKey();
    }
}
