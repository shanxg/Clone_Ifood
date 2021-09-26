package com.lucasrivaldo.cloneifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.adapter.AdapterOrder;
import com.lucasrivaldo.cloneifood.adapter.AdapterRestaurants;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.RecyclerItemClickListener;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Company;
import com.lucasrivaldo.cloneifood.model.Restaurant;
import com.lucasrivaldo.cloneifood.model.User;
import com.lucasrivaldo.cloneifood.model.cart.Cart;
import com.lucasrivaldo.cloneifood.model.cart.CartRestaurant;
import com.lucasrivaldo.cloneifood.model.cart.CartUtil;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.COMPANY;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.QUERY_NAME;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.USERS;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.CANCELLED;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.FINALIZED;

public class ClientHomeActivity extends AppCompatActivity {

    private boolean wasStopped = false;

    private Handler mHandler;

    private HashMap<String, Boolean> myOrdersMap;
    private List<Restaurant> mRestaurantList;
    private User mLoggedUser;

    private ValueEventListener myOrdersListener;

    private RecyclerView mRecyclerRestaurants, mRecyclerOrders;
    private AdapterOrder mAdapterOrders;
    private AdapterRestaurants mAdapterRestaurants;

    private TextView mTextUserName, mTextCartQtt, mTextCartPrice;
    private MaterialSearchView mMaterialSearchView;
    private FloatingActionButton mFab;



    /** ####################################  INITIALIZE  #################################### **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);
        mHandler = new Handler();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            mLoggedUser = (User) bundle.getSerializable("bundle");
            if (mLoggedUser != null) {
                preLoad();
                loadInterface();
                getMyData(true);


            }
        }else {
            getMyData(false);

        }
    }

    private void preLoad() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);



        mRestaurantList = new ArrayList<>();
        myOrdersMap = new HashMap<>();
    }

    private void loadInterface() {
        mMaterialSearchView = findViewById(R.id.searchView);

        mRecyclerRestaurants = findViewById(R.id.recyclerRestaurants);
        mRecyclerOrders = findViewById(R.id.recyclerOrders);
        setRecyclerRestaurants();
        setRecyclerOrders();

        mTextUserName = findViewById(R.id.textUserName);
        mTextUserName.setText(mLoggedUser.getName());

        mTextCartQtt = findViewById(R.id.textCartQtt);
        mTextCartPrice = findViewById(R.id.textCartPrice);
        mFab = findViewById(R.id.cartFab);

        setClickListeners();

        setSearchViewListeners();
    }

    private void setRecyclerRestaurants() {
        mHandler.post(() -> {

            mAdapterRestaurants = new AdapterRestaurants(mRestaurantList);

            mRecyclerRestaurants.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerRestaurants.setHasFixedSize(true);
            mRecyclerRestaurants.setAdapter(mAdapterRestaurants);

            mRecyclerRestaurants.addOnItemTouchListener(new RecyclerItemClickListener(
                    this,
                    mRecyclerRestaurants,
                    new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            Intent i = new Intent(getApplicationContext(), RestaurantActivity.class);
                            i.putExtra("bundle", mRestaurantList.get(position));
                            startActivity(i);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {
                        }

                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        }
                    }));

            notifyRestAdapter();
        });
    }

    private void setRecyclerOrders(){

        mHandler.post(() -> {

            mAdapterOrders = new AdapterOrder(true, CartUtil.getOrdersList());
            mRecyclerOrders.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerOrders.setHasFixedSize(true);

            mRecyclerOrders.setAdapter(mAdapterOrders);

            mRecyclerOrders.addOnItemTouchListener(new RecyclerItemClickListener(
                    this,
                    mRecyclerOrders,
                    new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            Intent i = new Intent(getApplicationContext(), OrderActivity.class);
                            i.putExtra("bundle", CartUtil.getOrdersList().get(position));
                            i.putExtra("isForClient", true);
                            startActivity(i);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {}

                        @Override
                        public void onItemClick
                                (AdapterView<?> adapterView, View view, int i, long l) {}
                    }));

            notifyOrdersAdapter();
        });
    }

    /** ##################################  CLICK LISTENERS  ################################## **/

    private void setClickListeners(){
        mFab.setOnClickListener(view ->{
            startActivity(new Intent(this, MyCartActivity.class));
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_orders:
                toggleRecyclerRest(false);
                toggleRecyclerOrders(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getMyOrders();

                break;

            case R.id.menu_settings:

                Intent settingsIntent = new Intent(this, SettingsClientActivity.class);
                settingsIntent.putExtra("bundle", mLoggedUser);
                startActivity(settingsIntent);
                break;

            case R.id.menu_sign_out:

                if(UserFirebase.signOut()){
                    startActivity(new Intent(this, AuthenticationActivity.class));
                    finish();
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /** ####################################  LISTENERS  ##################################### **/

    private void getMyData(boolean hasBundle) {

        mHandler.post(() -> {
            UserFirebase.getLoggedUserData(USERS, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        mLoggedUser = snapshot.getValue(User.class);
                        if (mLoggedUser != null) {

                            if (!hasBundle) {
                                preLoad();
                                loadInterface();
                            }

                            mTextUserName.setText(mLoggedUser.getName());
                            getMyOrders();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });
    }

    public void getMyOrders() {

        CartUtil.getOrdersList().clear();

        new Handler().post(() -> {


            if (myOrdersListener == null) {

                myOrdersListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            CartRestaurant restCart = snapshot.getValue(CartRestaurant.class);

                            myOrdersMap.put(restCart.getOrderId(), true);

                            if (CartUtil.getOrdersList().size() == 0){

                                CartUtil.getOrdersList().add(restCart);
                            }else {

                                for (CartRestaurant cart : CartUtil.getOrdersList()) {
                                    if (cart.getOrderId().equals(snapshot.getKey())){
                                        int i = CartUtil.getOrdersList().indexOf(cart);

                                        CartUtil.getOrdersList().remove(i);
                                        CartUtil.getOrdersList().add(restCart);
                                        break;
                                    }else if(CartUtil.getOrdersList().indexOf(cart)
                                            == CartUtil.getOrdersList().size()-1
                                            && !cart.getOrderId().equals(snapshot.getKey())){

                                        CartUtil.getOrdersList().add(restCart);
                                    }
                                }
                            }

                            if (restCart.getOrderStatus().equals(FINALIZED)
                                    || restCart.getOrderStatus().equals(CANCELLED)){
                                if (CartUtil.removeRef(restCart.getOrderId()))
                                    CartUtil.getOrdersList().remove(restCart);
                            }

                            notifyOrdersAdapter();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                };
            }

            if (CartUtil.getMyOrdersRefList().size() > 0) {

                for (DatabaseReference orderRef : CartUtil.getMyOrdersRefList()) {

                    int i = CartUtil.getMyOrdersRefList().indexOf(orderRef);
                    CartUtil.getMyOrdersRefList().get(i).addValueEventListener(myOrdersListener);
                }

            } else {

                UserFirebase.getMyOrdersData(true, new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            for (DataSnapshot restCartData : snapshot.getChildren()) {

                                DatabaseReference orderRef = restCartData.getRef();
                                if (!CartUtil.getMyOrdersRefList().contains(orderRef)){

                                    CartUtil.getMyOrdersRefList().add(orderRef);
                                    int i = CartUtil.getMyOrdersRefList().indexOf(orderRef);

                                    CartUtil.getMyOrdersRefList().get(i)
                                            .addValueEventListener(myOrdersListener);
                                }
                            }
                            notifyOrdersAdapter();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    private void setSearchViewListeners() {

        mMaterialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                validateQueryText(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                validateQueryText(newText);
                return true;
            }
        });

        mMaterialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {

            @Override
            public void onSearchViewShown() {
                toggleRecyclerOrders(false);
                toggleRecyclerRest(true);
            }

            @Override
            public void onSearchViewClosed() {
                toggleRecyclerRest(false);
            }
        });
    }

    /** ####################################  MY METHODS  #################################### **/

    private void updateCartTab() {

        int items_qtt = Cart.getInstance().getItems_quantity();
        mTextCartQtt.setText(String.valueOf(items_qtt));

        if (Cart.getInstance().getTotalPrice() != null) {
            String priceText = Cart.getInstance().getTotalPrice();
            mTextCartPrice.setText(priceText);
        }
    }

    private void validateQueryText(String text) {

        String queryText = text.toLowerCase();

        if (!IfoodHelper.invalidText(queryText) && queryText.length() >= 2)
            queryRestaurants(queryText);

    }

    private void queryRestaurants(String queryText) {

        mHandler.post(() -> {
            DatabaseReference restaurantsRef = ConfigurateFirebase.getFireDBRef().child(COMPANY);

            Query queryRestaurants =
                    restaurantsRef
                            .orderByChild(QUERY_NAME)
                            .startAt(queryText)
                            .endAt(queryText + "\uf8ff");

            queryRestaurants.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mRestaurantList.clear();

                    for (DataSnapshot restsData : snapshot.getChildren()) {

                        Company company = restsData.getValue(Company.class);

                        if (company != null) {

                            Restaurant restaurant = company.getRestaurant();
                            if (restaurant != null) mRestaurantList.add(restaurant);
                        }
                    }
                    notifyRestAdapter();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        });
    }

    private void notifyRestAdapter() {
        if (mRestaurantList.size() > 0){
            toggleRecyclerOrders(false);
            toggleRecyclerRest(true);
        }
        mAdapterRestaurants.notifyDataSetChanged();
    }

    private void notifyOrdersAdapter() {
        if (CartUtil.getOrdersList().size()>0) {
            toggleRecyclerRest(false);
            toggleRecyclerOrders(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAdapterOrders.notifyDataSetChanged();
    }

    /** ###############################  ACTIVITY LIFE-CYCLE  ################################ **/




    @Override
    protected void onStop() {
        super.onStop();
        wasStopped = true;
        for (DatabaseReference orderRef : CartUtil.getMyOrdersRefList()) {
            int i = CartUtil.getMyOrdersRefList().indexOf(orderRef);
            if (myOrdersListener != null)
                CartUtil.getMyOrdersRefList().get(i).removeEventListener(myOrdersListener);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (wasStopped){
            getMyData(true);

            updateCartTab();
        }
    }

    /** #################################  ACTIVITY PROCESS  ################################## **/

    @Override
    public boolean onSupportNavigateUp() {

        toggleRecyclerOrders(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_client, menu);

        MenuItem item = menu.findItem(R.id.menu_search);
        mMaterialSearchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    /** ####################################  HELPERS  #################################### **/

    private void toggleRecyclerRest(boolean isOpening){
        mRecyclerRestaurants.setVisibility(isOpening ? View.VISIBLE : View.GONE);
        toggleWelcomeLayout(!isOpening);
    }

    private void toggleRecyclerOrders(boolean isOpening){
        mRecyclerOrders.setVisibility(isOpening ? View.VISIBLE : View.GONE);
    }

    private void toggleWelcomeLayout(boolean isOpening) {
        findViewById(R.id.welcomeLayout).setVisibility(isOpening ? View.VISIBLE : View.GONE);

    }

    private void throwToast(String message, boolean isLong) {
        Toast.makeText(this, message,
                isLong ? Toast.LENGTH_LONG :Toast.LENGTH_SHORT).show();
    }
}
