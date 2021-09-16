package com.lucasrivaldo.cloneifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.adapter.AdapterProducts;
import com.lucasrivaldo.cloneifood.helper.AlertDialogUtil;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Product;
import com.lucasrivaldo.cloneifood.model.User;
import com.lucasrivaldo.cloneifood.model.cart.Cart;
import com.lucasrivaldo.cloneifood.model.cart.CartHelper;
import com.lucasrivaldo.cloneifood.model.cart.CartRestaurant;
import com.lucasrivaldo.cloneifood.model.cart.CartUtil;

import java.util.HashMap;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.CART;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.USERS;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.invalidText;

public class MyCartActivity extends AppCompatActivity
        implements CartUtil.OnItemChangeListener, IfoodHelper.DataListener{

    private int mLastListCounter, mMapListSize;

    private Handler mHandler;
    private User mLoggedUser;

    private RecyclerView mRecyclerProducts;
    private AdapterProducts mAdapterProducts;

    private TextView mTextCartQtt, mTextCartPrice;
    private AlertDialog mDialog;

    /** ####################################  INITIALIZE  #################################### **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);


        UserFirebase.getLoggedUserData(USERS, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mLoggedUser = snapshot.getValue(User.class);

                if (mLoggedUser != null){
                    preLoad();
                    loadInterface();

                }else {

                    UserFirebase.signOut();
                    throwToast("User data failure:\nAuthenticate again.", true);
                    startActivity(new Intent(MyCartActivity.this, AuthenticationActivity.class));
                    finishAffinity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void preLoad() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar(toolbar);

        mHandler = new Handler();
        CartUtil.cancelListener();
    }

    private void loadInterface() {
        mTextCartQtt = findViewById(R.id.textCartQtt);
        mTextCartPrice = findViewById(R.id.textCartPrice);

        mRecyclerProducts = findViewById(R.id.recyclerProducts);
        setRecyclerProducts();
        updateCartTab();
    }

    private void setRecyclerProducts() {
        mAdapterProducts = new AdapterProducts(CartHelper.resizeList(CartUtil.getMyCartList(null)),
                Cart.getInstance(), this);

        mRecyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerProducts.setHasFixedSize(true);
        mRecyclerProducts.setAdapter(mAdapterProducts);

        mAdapterProducts.notifyDataSetChanged();

    }

    /** ##################################  CLICK LISTENERS  ################################## **/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_confirm_order) sendCartResquests();

        return super.onOptionsItemSelected(item);
    }

    /** ####################################  LISTENERS  ##################################### **/

    @Override
    public void onItemChanged(boolean isAdding, Product product) {

        updateCartTab();
        mAdapterProducts.notifyDataSetChanged();
    }

    @Override
    public void onReturnData(Object... values) {
        mHandler.post(() -> {
            for (Object value : values) {

                Class classe = value.getClass();
                if (classe.equals(DatabaseError.class)) {
                    DatabaseError error = (DatabaseError) value;

                    if (error != null) {
                        try { throw error.toException(); }
                        catch (Exception e) { throwExceptionResults(e); }
                    }

                } else if (classe.equals(DatabaseReference.class)) {

                    DatabaseReference ref = (DatabaseReference) value;

                    if (ref.getKey().equals(CART)) {
                        throwToast("Cart saved", false);

                        mDialog.dismiss();
                        sendForMaps();

                    } else
                        Log.d(TAG, "onReturnData: " + ref.getRef());
                }
            }
        });
    }

    /** ####################################  MY METHODS  #################################### **/

    private void sendCartResquests() {

        mLastListCounter = 0;
        mMapListSize = Cart.getInstance().getCartList().size();
        mDialog = AlertDialogUtil.progressDialogAlert(this, "Sending requests...");

        //SAVE CART LOG AT USER'S FIREBASE

        User user = new User();
        user.setName(mLoggedUser.getName());
        user.setId(UserFirebase.getCurrentUserID());
        user.setAddress(mLoggedUser.getAddress());

        if(CartHelper.addRestsDelivTax()) {
            updateCartTab();

            for (String restId : Cart.getInstance().getCartList().keySet()) {

                confirmOrderList(Cart.getInstance().getCartList().get(restId));
                Cart.getInstance().getCartList().get(restId).setClient(user);
            }
        }
    }

    private void confirmOrderList(CartRestaurant cartRest) {

        DialogInterface.OnClickListener clickListener = (dialogInterface, i) -> {
            if (mLastListCounter == mMapListSize - 1){
                CartHelper.updateCart(Cart.getInstance(), this);
                mDialog.show();
            }else
                mLastListCounter++;
        };

        String message = CartHelper.returnOrderStringList(cartRest);

        AlertDialogUtil.confirmOrderDataAlert
                (this, clickListener, cartRest.getRestaurant().getName(), message);
    }

    private void sendForMaps() {
        mLastListCounter =  0 ;

        for (String restId : Cart.getInstance().getCartList().keySet())
            sendMaps(restId, Cart.getInstance().getCartList().get(restId));
    }

    private void sendMaps(String restId, CartRestaurant cartRest) {

        OnCompleteListener<Void> completeListener = task -> {

            if (mLastListCounter == mMapListSize -1) {
                mDialog.dismiss();
                resetCartData();

                CartUtil.getOrdersList().add(cartRest);

                throwToast("Requests finished", false);
                startActivity(new Intent(MyCartActivity.this, ClientHomeActivity.class));
                finishAffinity();

            }

            mLastListCounter++;
        };

        AlertDialogUtil.selectPaymentTypeAlert(this, cartRest.getRestaurant().getName(),
                (text, requestCode) -> {

                    HashMap<String, CartRestaurant> map = new HashMap<>();
                    map.put(restId, cartRest);

                    map.get(restId).setPaymentMethod(IfoodHelper.getPayType(requestCode));
                    if (!invalidText(text)) map.get(restId).setOrderObs(text);
                    CartHelper.sendReqsForRests(restId, map, completeListener);

                    if (mLastListCounter == mMapListSize - 1)
                        mHandler.post(() -> mDialog.show());
                });
    }

    private void updateCartTab() {

        mTextCartQtt.setText(String.valueOf(Cart.getInstance().getItems_quantity()));
        mTextCartPrice.setText(Cart.getInstance().getTotalPrice());
    }

    /** ###############################  ACTIVITY LIFE-CYCLE  ################################ **/

    @Override
    protected void onPause() {
        super.onPause();
        CartUtil.cancelListener();
    }


    @Override
    protected void onResume() {
        super.onResume();
        CartUtil.cancelListener();
        CartUtil.getMyCartList(this);
    }

    /** #################################  ACTIVITY PROCESS  ################################## **/

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);


        return super.onCreateOptionsMenu(menu);
    }

    /** ####################################  HELPERS  #################################### **/

    private void resetCartData() {

        Cart.resetCart();
        CartUtil.resetUtils();
        CartUtil.cancelListener();
    }

    private void throwToast(String message, boolean isLong) {
        Toast.makeText(this, message,
                isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    private void  throwExceptionResults(Exception e){
        e.printStackTrace();
        throwToast(e.getMessage(), true);
        Log.d(TAG, "MyCartActivity.throwExceptionResults: "+e.getMessage());
    }
}
