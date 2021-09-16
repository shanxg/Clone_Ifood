package com.lucasrivaldo.cloneifood.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



public class ConfigurateFirebase {

    //public static final String xxxxxxxxxx  = "x";

    // FIRE DB REF STATICS
    public static final String USERS = "users";
    public static final String COMPANY = "companies";
    public static final String RESTAURANT = "restaurant";
    public static final String QUERY_NAME  = "name_query";
    public static final String MARKET  = "market";
    public static final String CART  = "cart";
    public static final String REPO = "repo";
    public static final String PRODUCTS = "products";
    public static final String REQUESTS  = "requests";

    // STORAGE REF STATICS
    public static final String IMAGES = "images";

    private static FirebaseAuth mAuth;
    private static DatabaseReference mFireDBRef;
    private static StorageReference mStorage;

    public static DatabaseReference getFireDBRef(){

        if(mFireDBRef ==null){
            mFireDBRef = FirebaseDatabase.getInstance().getReference();
        }

        return mFireDBRef;
    }


    public static FirebaseAuth getFirebaseAuth() {

        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }

        return mAuth;
    }

    public static StorageReference getStorageRef(){
        if (mStorage == null)
            mStorage = FirebaseStorage.getInstance().getReference();

        return mStorage;
    }
}
