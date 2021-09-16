package com.lucasrivaldo.cloneifood.helper;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.CART;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.COMPANY;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.MARKET;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.PRODUCTS;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.REPO;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.REQUESTS;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.RESTAURANT;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.CONFIRMED;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.PENDING;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.READY;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;


public class UserFirebase {

    public static boolean signOut(){

        ConfigurateFirebase.getFirebaseAuth().signOut();

        return getCurrentUser() == null;
    }

    public static String getCurrentUserID() {

        return getCurrentUser().getUid();
    }

    public static FirebaseUser getCurrentUser() {

        return ConfigurateFirebase.getFirebaseAuth().getCurrentUser();
    }

    public static boolean updateUserProfType(String profileType) {

        // USING FIREBASE USER PROFILE NAME TO HOLD TYPE;
        try {
            FirebaseUser user = getCurrentUser();

            UserProfileChangeRequest userChangeRequest =
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName(profileType)
                            .build();


            user.updateProfile(userChangeRequest).addOnCompleteListener(task -> {

                if (!task.isSuccessful()) {
                    Log.i(TAG,"Error updating profile name at firebase user. \n"
                                    + task.getException().getMessage());
                }
            });
            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }

    public static void getLoggedUserData(String userType, ValueEventListener valueEventListener){
        DatabaseReference userRef = ConfigurateFirebase.getFireDBRef()
                                                .child(userType)
                                                    .child(getCurrentUserID());

        userRef.addListenerForSingleValueEvent(valueEventListener);
    }

    public static void getProductsData(IfoodHelper.DataListener dataListener){

        DatabaseReference productsRef = ConfigurateFirebase.getFireDBRef()
                .child(RESTAURANT)
                .child(UserFirebase.getCurrentUserID())
                .child(PRODUCTS);

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot productsData) {

                List<Product> products = new ArrayList<>();
                HashMap<String, List<Product>> prodMap = new HashMap<>();

                for (DataSnapshot productData : productsData.getChildren()){

                    Product product = productData.getValue(Product.class);
                    if (product!=null) products.add(product);
                }

                prodMap.put(PRODUCTS, products);
                dataListener.onReturnData(prodMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public static void getRestaurantProductsData(String restID, IfoodHelper.DataListener dataListener){

        new Handler().post(() -> {

            DatabaseReference productsRef = ConfigurateFirebase.getFireDBRef()
                    .child(MARKET)
                    .child(restID);

            productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot productsData) {


                    List<Product> products = new ArrayList<>();
                    HashMap<String, List<Product>> prodMap = new HashMap<>();

                    for (DataSnapshot productData : productsData.getChildren()) {

                        Product product = productData.getValue(Product.class);
                        if (product != null) products.add(product);
                    }

                    prodMap.put(PRODUCTS, products);
                    dataListener.onReturnData(prodMap);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });
    }

    public static void getRestaurantSingleData(String restId, String dataName,
                                               IfoodHelper.DataListener dataListener,
                                               ValueEventListener valueEventListener){

        new Handler().post(() -> {

            DatabaseReference restaurantRef = ConfigurateFirebase.getFireDBRef()
                    .child(COMPANY)
                    .child(restId)
                    .child(RESTAURANT)
                    .child(dataName);

            if (dataListener != null) {
                restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Class classe = snapshot.getValue().getClass();

                        dataListener.onReturnData(snapshot.getValue(classe));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }else {
                restaurantRef.addListenerForSingleValueEvent(valueEventListener);
            }
        });
    }

    public static void getMyOrdersData(boolean isForClient, ValueEventListener valueEventListener){
        DatabaseReference ordersRef;
        Query queryPending, queryConfirmed, queryReady;
        if (isForClient){

            ordersRef = ConfigurateFirebase.getFireDBRef()
                            .child(CART)
                            .child(UserFirebase.getCurrentUserID())
                            .child(REPO);

        }else {

            ordersRef =
                    ConfigurateFirebase.getFireDBRef()
                            .child(REQUESTS)
                            .child(UserFirebase.getCurrentUserID());
        }

        queryPending = ordersRef.orderByChild("orderStatus").equalTo(PENDING);
        queryConfirmed = ordersRef.orderByChild("orderStatus").equalTo(CONFIRMED);
        queryReady = ordersRef.orderByChild("orderStatus").equalTo(READY);

        queryPending.addListenerForSingleValueEvent(valueEventListener);
        queryConfirmed.addListenerForSingleValueEvent(valueEventListener);
        queryReady.addListenerForSingleValueEvent(valueEventListener);
    }
}
