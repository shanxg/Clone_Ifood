package com.lucasrivaldo.cloneifood.model.cart;

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;

import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.CART;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.REPO;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.REQUESTS;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.PENDING;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;

public class CartHelper {

    public static double parsePrice(String priceText){
        double price;

        String format = priceText.replaceAll("\\W","");
        String noWords = format.replace("R", "");
        String doubleFormat = noWords.replaceAll("(\\d+)(\\d{2})", "$1.$2");

        price = Double.parseDouble(doubleFormat);

        return price;
    }

    public static String formatPrice(double price){

        Locale locale = new Locale("pt", "BR");
        NumberFormat numFormat = NumberFormat.getCurrencyInstance(locale);

        return numFormat.format(price);
    }

    private static IfoodHelper.DataListener mListener;

    public static void updateCart(Cart cart, IfoodHelper.DataListener listener) {
        mListener = listener;

        new Handler().post(() -> {

            for (String restId : cart.getCartList().keySet()) {
                DatabaseReference restReqsRef =
                        ConfigurateFirebase.getFireDBRef()
                                .child(REQUESTS)
                                .child(restId)
                                .push();

                String orderId = restReqsRef.getKey();
                restReqsRef.setValue("uploading data...");
                Cart.getInstance().getCartList().get(restId).setOrderId(orderId);
            }

            HashMap cartMap = new HashMap();

            if (cart.getCartList() != null)
                cartMap.put("cartList", cart.getCartList());

            if (cart.getTotalPrice() != null) {
                cartMap.put("totalPrice", cart.getTotalPrice());
                cartMap.put("items_quantity", cart.getItems_quantity());
            }

            DatabaseReference myCartRef =
                    ConfigurateFirebase.getFireDBRef()
                            .child(CART)
                            .child(UserFirebase.getCurrentUserID());

            try {
                myCartRef.child(CART).updateChildren(cartMap, (error, ref) -> {

                    if (error != null)
                        mListener.onReturnData(error);
                    else
                        mListener.onReturnData(ref);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "CartUtil.updateCart: " + e.getMessage());
            }
        });
    }

    public static boolean addRestsDelivTax(){

        // SEPARATE ORDER BY RESTAURANT
        for (String restId : Cart.getInstance().getCartList().keySet()){

            double tax = CartUtil.getRestsTaxMap().get(restId);
            CartUtil.calcDelivTax(true, restId, tax);
        }
        return true;
    }

    public static void sendReqsForRests(String restId, HashMap<String,CartRestaurant> orderMap,
                                        OnCompleteListener<Void> listener){

        DatabaseReference repoCartRef =
                ConfigurateFirebase.getFireDBRef()
                        .child(CART)
                        .child(UserFirebase.getCurrentUserID())
                        .child(REPO)
                        .child(orderMap.get(restId).getOrderId());

        Cart.getInstance().getCartList().get(restId).setOrderStatus(PENDING);
        repoCartRef.setValue(Cart.getInstance().getCartList().get(restId));


        // SEND REQUEST FOR RESTAURANTS
        DatabaseReference restReqsRef =
                ConfigurateFirebase.getFireDBRef()
                        .child(REQUESTS)
                        .child(restId)
                        .child(orderMap.get(restId).getOrderId());

        orderMap.get(restId).setOrderStatus(PENDING);
        restReqsRef.setValue(orderMap.get(restId)).addOnCompleteListener(listener);

        CartUtil.getMyOrdersRefList().add(restReqsRef);
    }

    public static String returnOrderStringList(CartRestaurant cart){

        StringBuilder stringBuilder = new StringBuilder();
        List<Product> products = new ArrayList<>();

        for(String prodId : cart.getProducts().keySet()){
            for (Product product : CartUtil.getMyCartList(null)){
                if (prodId.equals(product.getProd_id()) && !products.contains(product)){
                    products.add(product);
                }
            }
        }
        String restId = null;
        for (Product product : products){
            restId = product.getRest_id();

            int qt = cart.getProducts().get(product.getProd_id());
            String orderListLine = product.getName() + " x "+ qt +" - "+ product.getPrice()+"\n";
            stringBuilder.append(orderListLine);
        }

        String taxLine = "\n\nDelivery tax - " +formatPrice(CartUtil.getRestsTaxMap().get(restId))+"\n\n";
        String lastLine = "\n                             " +
                "Total price - "+formatPrice(cart.getPartial_price());
        stringBuilder.append(taxLine+lastLine);

        return stringBuilder.toString();
    }

    public static List<Product> resizeList(List<Product> prodList) {
        List<Product> products = new ArrayList<>();

        for (Product product : prodList)
            if (!products.contains(product)) products.add(product);

        return products;
    }
}
