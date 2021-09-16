package com.lucasrivaldo.cloneifood.model;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;

import java.io.Serializable;
import java.util.HashMap;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.COMPANY;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.QUERY_NAME;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;


public class Company implements Serializable {

    public Company() {}

    private String email, id, name;
    private Restaurant restaurant;


    public boolean save(boolean isSaving){

        DatabaseReference companiesRef = ConfigurateFirebase.getFireDBRef()
                .child(COMPANY)
                .child(this.getId());
        try {

            if (isSaving) {
                companiesRef.setValue(this);
            }else {
                companiesRef.removeValue();
            }

            return true;

        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "Company.save(): "+e.getMessage());
            return false;
        }
    }

    public boolean update(){

        DatabaseReference companiesRef = ConfigurateFirebase.getFireDBRef()
                .child(COMPANY)
                .child(this.getId());

        HashMap companyMap = new HashMap();

        companyMap.put("name", this.getName());
        companyMap.put(QUERY_NAME, this.getName().toLowerCase());


        try {

            if (this.getRestaurant() != null) {
                if (this.getRestaurant().update()) // UPDATING COMPANY RESTAURANT INFO
                    if (this.getRestaurant().save()) // UPDATING REST_REPO RESTAURANT INFO
                        companiesRef.updateChildren(companyMap);
            }else {
                companiesRef.updateChildren(companyMap);
            }
            return true;

        }catch (Exception e ){
            e.printStackTrace();
            Log.d(TAG, "Company.update(): "+e.getMessage());

            return false;
        }

    }

    public Restaurant getRestaurant() { return restaurant; }
    public String getEmail() { return email; }
    public String getId() { return id; }
    public String getName() { return name; }

    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    public void setEmail(String email) { this.email = email; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}
