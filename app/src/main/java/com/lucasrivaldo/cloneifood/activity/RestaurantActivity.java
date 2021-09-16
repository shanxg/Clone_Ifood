package com.lucasrivaldo.cloneifood.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.adapter.AdapterProducts;
import com.lucasrivaldo.cloneifood.model.Company;
import com.lucasrivaldo.cloneifood.model.User;
import com.lucasrivaldo.cloneifood.model.cart.Cart;
import com.lucasrivaldo.cloneifood.model.cart.CartHelper;
import com.lucasrivaldo.cloneifood.model.cart.CartUtil;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Product;
import com.lucasrivaldo.cloneifood.model.Restaurant;
import com.lucasrivaldo.cloneifood.model.cart.CartRestaurant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyResourceBundle;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.PRODUCTS;

public class RestaurantActivity extends AppCompatActivity
        implements IfoodHelper.DataListener, CartUtil.OnItemChangeListener {

    private Handler mHandler;

    private boolean mCreated = false;
    private String mRestaurantId;

    private Restaurant mCurrentRestaurant;
    private List<Product> mProducts;


    private RecyclerView mRecyclerProducts;
    private AdapterProducts mAdapterProducts;

    private CircleImageView mCivRestaurantImage;
    private TextView mTextRestName, mTextCartQtt, mTextCartPrice;
    private FloatingActionButton mFab;

    /** ####################################  INITIALIZE  #################################### **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        Bundle bundle = getIntent().getExtras();

        assert bundle != null;
        if (bundle.containsKey("bundle")) {
            mCurrentRestaurant = (Restaurant) bundle.getSerializable("bundle");
            preLoad();
            loadInterface();

        } else { // FAILURE ON SAVING RESTAURANT DATA

            throwToast("Data failure:\nError loading restaurant data.", true);
            finish();
        }
    }

    private void preLoad() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle(mCurrentRestaurant.getName());
        setSupportActionBar(toolbar);

        mRestaurantId = mCurrentRestaurant.getCompanyId();

        mProducts = new ArrayList<>();
        mHandler = new Handler();
    }

    private void loadInterface() {
        mTextCartQtt = findViewById(R.id.textCartQtt);
        mTextCartPrice = findViewById(R.id.textCartPrice);

        mCivRestaurantImage = findViewById(R.id.civRestaurantImage);
        mTextRestName = findViewById(R.id.textRestName);

        mTextRestName.setText(mCurrentRestaurant.getName());
        Picasso.get().load(Uri.parse(mCurrentRestaurant.getPhoto())).into(mCivRestaurantImage);

        mRecyclerProducts = findViewById(R.id.recyclerProducts);
        mFab = findViewById(R.id.cartFab);

        setRecyclerProducts();
        setClickListeners();
    }

    private void setRecyclerProducts() {
        mAdapterProducts = new AdapterProducts(mProducts, true, this);
        mRecyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerProducts.setHasFixedSize(true);
        mRecyclerProducts.setAdapter(mAdapterProducts);

        mAdapterProducts.notifyDataSetChanged();
        getRestaurantProducts();
        mCreated = true;
    }

    /** ##################################  CLICK LISTENERS  ################################## **/

     private void setClickListeners(){
         mFab.setOnClickListener(view ->{
                 startActivity(new Intent(this, MyCartActivity.class));
         });
     }

    /** ####################################  LISTENERS  ##################################### **/

    private void getRestaurantProducts(){
        UserFirebase.getRestaurantProductsData(mCurrentRestaurant.getCompanyId(), this);
    }

    @Override
    public void onReturnData(Object... values) {
        for (Object value : values) {
            Class valueClass = value.getClass();

            if (valueClass.equals(HashMap.class)){
                HashMap<String, List<Product>> prodsMap = (HashMap<String, List<Product>>) value;

                if (prodsMap.containsKey(PRODUCTS)) {
                    mProducts.clear();
                    List<Product> products = new ArrayList<>(prodsMap.get(PRODUCTS));

                    mProducts.addAll(products);
                    mAdapterProducts.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onItemChanged(boolean isAdding, Product product) {


        if (isAdding){
            if (!CartUtil.getRestsTaxMap().containsKey(mRestaurantId)){

                double tax = CartHelper.parsePrice(mCurrentRestaurant.getDeliveryTax());
                CartUtil.getRestsTaxMap().put(mRestaurantId, tax);
            }

            if (Cart.getInstance().getCartList().get(mRestaurantId) != null
                    && Cart.getInstance().getCartList().get(mRestaurantId).getRestaurant() != null){

                Cart.getInstance()
                        .getCartList()
                        .get(mRestaurantId)
                        .getRestaurant()
                        .setName(mCurrentRestaurant.getName());

                Cart.getInstance()
                        .getCartList()
                        .get(mRestaurantId)
                        .getRestaurant()
                        .setCompanyId(mRestaurantId);
            }

        }else {
            if (!Cart.getInstance().getCartList().containsKey(mRestaurantId)){
                CartUtil.getRestsTaxMap().remove(mRestaurantId);
            }


        }

        updateCartTab();
    }

    /** ####################################  MY METHODS  #################################### **/


    private void updateCartTab() {
        toggleCartLayout(CartUtil.getMyCartList(null).size() > 0);

        mTextCartQtt.setText(String.valueOf(Cart.getInstance().getItems_quantity()));
        mTextCartPrice.setText(Cart.getInstance().getTotalPrice());
    }

    /** ###############################  ACTIVITY LIFE-CYCLE  ################################ **/

    @Override
    protected void onResume() {
        super.onResume();

        CartUtil.cancelListener();

        if (mCreated){
            getRestaurantProducts();
            updateCartTab();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        CartUtil.cancelListener();
    }

    /** #################################  ACTIVITY PROCESS  ################################## **/

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** ####################################  HELPERS  #################################### **/

    private void toggleCartLayout(boolean isOpening){
        findViewById(R.id.layoutCart).setVisibility(isOpening ? View.VISIBLE :  View.GONE);
    }

    private void throwToast(String message, boolean isLong) {
        Toast.makeText(this, message,
                isLong ? Toast.LENGTH_LONG :Toast.LENGTH_SHORT).show();
    }
}
