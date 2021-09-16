package com.lucasrivaldo.cloneifood.model.cart;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Product;
import com.lucasrivaldo.cloneifood.model.Restaurant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CartUtil {

    private static OnItemChangeListener mListener;

    public interface OnItemChangeListener{
        void onItemChanged(boolean isAdding, Product product);
    }

    public static void cancelListener(){
        mListener = null;
    }

    public static void resetUtils(){
        cartList.clear();
        mListener = null;
    }

    private static HashMap<String, Double> restsTaxList = new HashMap<>();
    public static HashMap<String, Double> getRestsTaxMap(){ return  restsTaxList;}

    private static List<DatabaseReference> myOrdersRefList = new ArrayList<>();
    public static List<DatabaseReference> getMyOrdersRefList(){ return  myOrdersRefList;}

    private static List<CartRestaurant> ordersList = new ArrayList<>();
    public static List<CartRestaurant> getOrdersList(){ return ordersList; }

    public static boolean removeRef(String orderId){

        for (DatabaseReference ref : myOrdersRefList){
            if (orderId.equals(ref.getKey())){
                myOrdersRefList.remove(ref);
                return true;
            }
        }
        return false;
    }


    public static List<Product> getMyCartList(OnItemChangeListener listener) {

        if (mListener == null) mListener = listener;

        return cartList;
    }

    private static List<Product> cartList = new ArrayList<Product>(){

        @Override
        public boolean remove(@Nullable Object product) {

            postItem(false, (Product) product);

            return super.remove(product);
        }

        @Override
        public boolean add(Product product) {

            postItem(true, product);

            return super.add(product);
        }
    };

    private static void postItem(boolean isAdding, Product product){

        new Handler().post(() -> setCartData(isAdding, product));
    }

    private static void setCartData(boolean isAdding, Product product) {
        String restId = product.getRest_id();

        UserFirebase.getRestaurantSingleData(restId, "deliveryTax", values -> {

            int quantity;
            String prodID = product.getProd_id();
            boolean hasRestCart = (Cart.getInstance().getCartList().containsKey(restId));

            boolean hasProd = false;
            if (hasRestCart)
                hasProd = Cart.getInstance().getCartList().get(restId).getProducts().containsKey(prodID);

            quantity = ! hasProd ?
                    0 : Cart.getInstance().getCartList().get(restId).getProducts().get(prodID);


            if (isAdding) {

                quantity ++;

            } else { // IS DECREASING

                quantity --;
                quantity = quantity < 0 ? 0 : quantity;
            }

            if (quantity <= 0) {
                Cart.getInstance().getCartList().get(restId).getProducts().remove(prodID);

            } else {
                if (hasRestCart)
                    Cart.getInstance().getCartList().get(restId).getProducts().put(prodID, quantity);
                else{
                    Cart.getInstance();
                    Cart.getInstance().getCartList().put(restId, new CartRestaurant());
                    Cart.getInstance().getCartList().get(restId).setProducts(new HashMap<>());
                    Cart.getInstance().getCartList().get(restId).getProducts().put(prodID, quantity);

                    if (Cart.getInstance().getCartList().get(restId).getRestaurant() == null){
                        Cart.getInstance().getCartList().get(restId).setRestaurant(new Restaurant());
                        Cart.getInstance().getCartList()
                                .get(restId)
                                .getRestaurant()
                                .setDeliveryTax((String) values[0]);
                    }

                }
            }



            boolean restHasProd;

            if (isAdding){

                if (Cart.getInstance().getCartList()
                        .get(restId).getRestaurant().getProducts() == null){


                    Cart.getInstance().getCartList()
                            .get(restId)
                            .getRestaurant()
                            .setProducts(new ArrayList<>());

                    Cart.getInstance().getCartList()
                            .get(restId)
                            .getRestaurant()
                            .getProducts()
                            .add(product);

                }else {

                    restHasProd = (quantity == 1);

                    if (restHasProd) {
                        Cart.getInstance().getCartList()
                                .get(restId)
                                .getRestaurant()
                                .getProducts()
                                .add(product);
                    }
                }

            }else {

                restHasProd = (quantity == 0);

                if (restHasProd) {
                    Cart.getInstance().getCartList()
                            .get(restId)
                            .getRestaurant()
                            .getProducts()
                            .remove(product);
                }
            }

            calculateCartPrice(isAdding , product);
        }, null);
    }

    private static void updateItemsQuantity(boolean isAdding, Product product){
        String restId = product.getRest_id();

        int partial_items_quantity = Cart.getInstance().getCartList().get(restId).getItems_quantity();
        int total_items_qtt = Cart.getInstance().getItems_quantity();

        if (isAdding) {

            partial_items_quantity++;
            total_items_qtt++;

        } else {

            partial_items_quantity--;
            total_items_qtt--;

            partial_items_quantity = partial_items_quantity < 0 ? 0 : partial_items_quantity;
            total_items_qtt = total_items_qtt < 0 ? 0 : total_items_qtt;
        }

        Cart.getInstance().getCartList().get(restId).setItems_quantity(partial_items_quantity);
        Cart.getInstance().setItems_quantity(total_items_qtt);

        mListener.onItemChanged(isAdding, product);
    }

    private static void calculateCartPrice(boolean isAdding, Product product) {

        new Handler().post(() -> {

            String restId = product.getRest_id();

            double partial_price = Cart.getInstance().getCartList().get(restId).getPartial_price();

            String totalPrice = Cart.getInstance().getTotalPrice();
            double total_price = totalPrice != null ? CartHelper.parsePrice(totalPrice) : 0;


            if (isAdding) {

                partial_price += CartHelper.parsePrice(product.getPrice()) ;
                total_price += CartHelper.parsePrice(product.getPrice());

            } else {

                partial_price -= CartHelper.parsePrice(product.getPrice());
                total_price -= CartHelper.parsePrice(product.getPrice());

            }


            Cart.getInstance().getCartList().get(restId).setPartial_price(partial_price);
            Cart.getInstance().setTotalPrice(CartHelper.formatPrice(total_price));

            updateItemsQuantity(isAdding , product);
        });
    }

    public static void calcDelivTax(boolean isAdding, String restId, double restDelivTax) {

        new Handler().post(() -> {

            double partial_price, total_price;

            partial_price = Cart.getInstance().getCartList().get(restId) != null ?
                    Cart.getInstance().getCartList().get(restId).getPartial_price() : 0;

            total_price = Cart.getInstance().getTotalPrice() != null ?
                    CartHelper.parsePrice(Cart.getInstance().getTotalPrice()) : 0;

            if (isAdding) {
                partial_price += restDelivTax;
                total_price += restDelivTax;
            } else {
                partial_price -= restDelivTax;
                total_price -= restDelivTax;
            }

            Cart.getInstance().setTotalPrice(CartHelper.formatPrice(total_price));
            Cart.getInstance().getCartList().get(restId).setPartial_price(partial_price);
        });
    }
}