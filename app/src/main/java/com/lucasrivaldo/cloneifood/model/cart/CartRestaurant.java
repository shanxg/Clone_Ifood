package com.lucasrivaldo.cloneifood.model.cart;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Company;
import com.lucasrivaldo.cloneifood.model.Restaurant;
import com.lucasrivaldo.cloneifood.model.User;

import java.io.Serializable;
import java.util.HashMap;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.CART;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.REPO;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.REQUESTS;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;

public class CartRestaurant implements Serializable {

    public CartRestaurant() {
    }

    private HashMap<String, Integer> products;

    private String orderId, orderStatus, orderObs, paymentMethod;
    private User client;
    private Restaurant restaurant;
    private double partial_price;
    private int items_quantity;

    public boolean cancel(){

        DatabaseReference restReqsRef =
                ConfigurateFirebase.getFireDBRef()
                        .child(REQUESTS)
                        .child(UserFirebase.getCurrentUserID())
                        .child(this.getOrderId());
        try {
            restReqsRef.removeValue();
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean update(String restId) {

        DatabaseReference repoCartRef =
                ConfigurateFirebase.getFireDBRef()
                        .child(CART)
                        .child(this.getClient().getId())
                        .child(REPO)
                        .child(this.getOrderId());

        DatabaseReference restReqsRef =
                ConfigurateFirebase.getFireDBRef()
                        .child(REQUESTS)
                        .child(restId)
                        .child(this.getOrderId());

        HashMap restMap = new HashMap();

        restMap.put("orderStatus", this.getOrderStatus());

        try {
            repoCartRef.updateChildren(restMap);
            restReqsRef.updateChildren(restMap);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "CartRestaurant.update(): " + e.getMessage());

            return false;
        }
    }

    public boolean updateRepo(String clientId) {

        DatabaseReference repoCartRef =
                ConfigurateFirebase.getFireDBRef()
                        .child(CART)
                        .child(clientId)
                        .child(REPO)
                        .child(this.getOrderId());

        HashMap restMap = new HashMap();

        restMap.put("orderStatus", this.getOrderStatus());


        try {
            repoCartRef.updateChildren(restMap);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "CartRestaurant.update(): " + e.getMessage());

            return false;
        }
    }


    public Restaurant getRestaurant() {
        return restaurant;
    }
    public double getPartial_price() {
        return partial_price;
    }
    public int getItems_quantity() {
        return items_quantity;
    }
    public HashMap<String, Integer> getProducts() {
        return products;
    }
    public String getOrderId() {
        return orderId;
    }
    public String getOrderObs() {
        return orderObs;
    }
    public User getClient() {
        return client;
    }
    public String getOrderStatus() {
        return orderStatus;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }


    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    public void setPartial_price(double partial_price) {
        this.partial_price = partial_price;
    }
    public void setItems_quantity(int items_quantity) {
        this.items_quantity = items_quantity;
    }
    public void setProducts(HashMap<String, Integer> products) {
        this.products = products;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public void setOrderObs(String orderObs) {
        this.orderObs = orderObs;
    }
    public void setClient(User client) {
        this.client = client;
    }
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
