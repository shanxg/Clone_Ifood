package com.lucasrivaldo.cloneifood.model.cart;

import java.io.Serializable;
import java.util.HashMap;

public class Cart implements Serializable {

    public Cart() {}

    private static Cart instance;
    public static Cart getInstance(){
        if (instance==null) {
            instance = new Cart();
            instance.cartList = new HashMap<>();
        }
        return instance;
    }

    public static void resetCart(){
        instance = null;
    }

    private HashMap<String, CartRestaurant> cartList;
    private String totalPrice;
    private int items_quantity;

    public HashMap<String, CartRestaurant> getCartList() { return cartList; }
    public String getTotalPrice() { return totalPrice; }
    public int getItems_quantity() { return items_quantity; }

    public void setCartList(HashMap<String, CartRestaurant> cartList) { this.cartList = cartList; }
    public void setTotalPrice(String totalPrice) { this.totalPrice = totalPrice; }
    public void setItems_quantity(int items_quantity) { this.items_quantity = items_quantity; }
}
