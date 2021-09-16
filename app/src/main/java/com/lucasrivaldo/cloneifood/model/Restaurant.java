package com.lucasrivaldo.cloneifood.model;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.COMPANY;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.MARKET;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.PRODUCTS;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.QUERY_NAME;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.RESTAURANT;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;

public class Restaurant implements Serializable {

    private String name, category, deliveryTime, deliveryTax, companyId, photo;

    private List<Product> products;

    public Restaurant() {}

    public Restaurant(String companyId) {
        this.companyId = companyId;
    }

    boolean save(){
        DatabaseReference restaurantRef = ConfigurateFirebase.getFireDBRef()
                .child(RESTAURANT)
                .child(this.getCompanyId());

        HashMap companyMap = new HashMap();

        companyMap.put("name", this.getName());
        companyMap.put("category", this.getCategory());
        companyMap.put("deliveryTime", this.getDeliveryTime());
        companyMap.put("deliveryTax", this.getDeliveryTax());
        companyMap.put("photo", this.getPhoto());
        companyMap.put("companyId", this.getCompanyId());

        try {

            restaurantRef.updateChildren(companyMap);
            return true;

        }catch (Exception e ){

            e.printStackTrace();
            Log.d(TAG, "Restaurant.update(): "+e.getMessage());

            return false;
        }
    }

    boolean update(){
        DatabaseReference companiesRef = ConfigurateFirebase.getFireDBRef()
                .child(COMPANY)
                .child(this.getCompanyId())
                .child(RESTAURANT);

        HashMap companyMap = new HashMap();

        companyMap.put("name", this.getName());
        companyMap.put("category", this.getCategory());
        companyMap.put("deliveryTime", this.getDeliveryTime());
        companyMap.put("deliveryTax", this.getDeliveryTax());
        companyMap.put("photo", this.getPhoto());
        companyMap.put("companyId", this.getCompanyId());

        try {

            companiesRef.updateChildren(companyMap);
            return true;

        }catch (Exception e ){

            e.printStackTrace();
            Log.d(TAG, "Restaurant.update(): "+e.getMessage());

            return false;
        }
    }

    public boolean updateMarket(){
        DatabaseReference marketRef = ConfigurateFirebase.getFireDBRef()
                .child(MARKET)
                .child(UserFirebase.getCurrentUserID());

        HashMap productsMap = new HashMap();

        try {
            for (Product product : this.getProducts())
                productsMap.put(product.getProd_id(), product);

            marketRef.updateChildren(productsMap);

            return true;

        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "Restaurant.save(): "+e.getMessage());
            return false;
        }
    }

    public boolean remove(){

        DatabaseReference restaurantRef = ConfigurateFirebase.getFireDBRef()
                .child(RESTAURANT)
                .child(this.getCompanyId());
        try {
            restaurantRef.removeValue();
            return true;

        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "Restaurant.save(): "+e.getMessage());
            return false;
        }
    }

    public String getPhoto() { return photo; }
    public String getCompanyId() { return companyId; }
    public String getCategory() { return category; }
    public String getDeliveryTime() { return deliveryTime; }
    public String getDeliveryTax() { return deliveryTax; }
    public List<Product> getProducts() { return products; }
    public String getName() { return name; }

    public void setPhoto(String photo) { this.photo = photo; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public void setCategory(String category) { this.category = category; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
    public void setDeliveryTax(String deliveryTax) { this.deliveryTax = deliveryTax; }
    public void setProducts(List<Product> products) { this.products = products; }
    public void setName(String name) { this.name = name; }
}
