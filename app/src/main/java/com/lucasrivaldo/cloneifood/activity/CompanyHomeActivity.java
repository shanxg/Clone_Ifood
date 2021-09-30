package com.lucasrivaldo.cloneifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.adapter.AdapterOrder;
import com.lucasrivaldo.cloneifood.adapter.AdapterProducts;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;
import com.lucasrivaldo.cloneifood.helper.AlertDialogUtil;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.RecyclerItemClickListener;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Company;
import com.lucasrivaldo.cloneifood.model.Product;
import com.lucasrivaldo.cloneifood.model.Restaurant;
import com.lucasrivaldo.cloneifood.model.cart.CartRestaurant;
import com.lucasrivaldo.cloneifood.model.cart.CartUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.COMPANY;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.PRODUCTS;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.REQUESTS;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.CANCELLED;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.FINALIZED;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.PENDING;

public class CompanyHomeActivity extends AppCompatActivity
        implements IfoodHelper.DataListener {

    private boolean wasStopped = false;

    private HashMap<String, Boolean> myOrdersMap;

    private AdapterProducts mAdapterProducts;
    private RecyclerView mRecyclerProducts;

    private RecyclerView mRecyclerOrders;
    private AdapterOrder mAdapterOrders;

    private ValueEventListener myOrdersListener, mRefsListener;

    private Company myCompany;
    private Restaurant myRestaurant;
    private List<Product> mProducts;


    /** ####################################  INITIALIZE  #################################### **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_home);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                myCompany = (Company) bundle.getSerializable("bundle");
                if (myCompany != null) {
                    preLoad();
                    loadInterface();
                    getMyData();
                    getMyOrders();
                }
            }
    }

    private void preLoad() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name)+" - "+myCompany.getName());
        setSupportActionBar(toolbar);

        mProducts = new ArrayList<>();


        myOrdersMap = new HashMap<>();
    }

    private void loadInterface() {

        mRecyclerOrders = findViewById(R.id.recyclerOrders);
        setRecyclerOrders();

        mRecyclerProducts = findViewById(R.id.recyclerProducts);
        startRecyclerProd();
    }

    private void startRecyclerProd() {

        mAdapterProducts = new AdapterProducts(mProducts, false, null);
        mRecyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerProducts.setHasFixedSize(true);
        mRecyclerProducts.setAdapter(mAdapterProducts);

        mAdapterProducts.notifyDataSetChanged();
        addSwipeMove();
    }

    private void setRecyclerOrders(){

        new Handler().post(() ->{

            mAdapterOrders = new AdapterOrder(false, CartUtil.getOrdersList());
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
                            i.putExtra("isForClient", false);
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

    public void addSwipeMove(){

        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags
                    (@NonNull RecyclerView recyclerView,
                     @NonNull RecyclerView.ViewHolder viewHolder) {

                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.START;
                //int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove
                    (@NonNull RecyclerView recyclerView,
                     @NonNull RecyclerView.ViewHolder viewHolder,
                     @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                deleteProduct(viewHolder);
            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(mRecyclerProducts);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_orders:
                toggleRecyclerRest(false);
                toggleRecyclerOrders(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                notifyOrdersAdapter();


                break;

            case R.id.menu_new_product:
                if (mRecyclerProducts.getVisibility() == View.GONE){
                    toggleRecyclerOrders(false);
                    toggleRecyclerRest(true);
                }else {
                    startActivity(new Intent(this, AddProductActivity.class));
                }
                break;

            case R.id.menu_settings:
                startSettingsActivity();
                break;

            case R.id.menu_sign_out:
                setOrdersListener(false);
                if(UserFirebase.signOut()){
                    startActivity(new Intent(this, AuthenticationActivity.class));
                    finish();

                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /** ####################################  LISTENERS  ##################################### **/

    private void setOrdersListener(boolean isStarting){
       DatabaseReference ordersRef =
                ConfigurateFirebase.getFireDBRef()
                        .child(REQUESTS)
                        .child(UserFirebase.getCurrentUserID());

       Query queryPending = ordersRef.orderByChild("orderStatus").equalTo(PENDING);
       if (isStarting){
           queryPending.addValueEventListener(mRefsListener);
       }else {
           queryPending.removeEventListener(mRefsListener);
       }
    }

    private void getMyData() {
        UserFirebase.getLoggedUserData(COMPANY, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myCompany = snapshot.getValue(Company.class);
                if (myCompany != null) {
                    if (myCompany.getRestaurant() != null){
                        myRestaurant =   myCompany.getRestaurant();
                        getProducts();
                    }else {
                        throwToast("First set restaurant data", false);
                        myRestaurant = new Restaurant();
                        startSettingsActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void getProducts() {
        UserFirebase.getProductsData(this);
    }

    @Override
    public void onReturnData(Object... values) {

        for (Object value : values) {
            Class valueClass = value.getClass();

            if (valueClass.equals(HashMap.class)){
                HashMap<String, List<Product>> prodsMap = (HashMap<String, List<Product>>) value;

                if (prodsMap.containsKey(PRODUCTS)) {
                    mProducts.clear();
                    List<Product> products = new ArrayList<>();
                    products = prodsMap.get(PRODUCTS) != null ? prodsMap.get(PRODUCTS) : products;

                    mProducts.addAll(products);
                    mAdapterProducts.notifyDataSetChanged();

                    myRestaurant.setProducts(mProducts);
                    if (mProducts.size()>0) updateMarket();
                }
            }
        }
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
                                    }else if (CartUtil.getOrdersList().indexOf(cart)
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
                    public void onCancelled(@NonNull DatabaseError error) {}
                };

                mRefsListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            for (DataSnapshot restCartData : snapshot.getChildren()) {

                                DatabaseReference orderRef = restCartData.getRef();
                                if (!CartUtil.getMyOrdersRefList().contains(orderRef)){

                                    CartUtil.getMyOrdersRefList().add(orderRef);
                                    int i = CartUtil.getMyOrdersRefList().indexOf(orderRef);

                                    CartUtil.getMyOrdersRefList().get(i).addValueEventListener(myOrdersListener);
                                }
                            }
                            notifyOrdersAdapter();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                };
            }


            if (CartUtil.getMyOrdersRefList().size() > 0) {

                for (DatabaseReference orderRef : CartUtil.getMyOrdersRefList()) {

                    int i = CartUtil.getMyOrdersRefList().indexOf(orderRef);
                    CartUtil.getMyOrdersRefList().get(i).addValueEventListener(myOrdersListener);
                }

            } else {
                UserFirebase.getMyOrdersData(false, mRefsListener);
            }

            setOrdersListener(true);
        });
    }

    /** ####################################  MY METHODS  #################################### **/

    private void notifyOrdersAdapter() {
        if (CartUtil.getOrdersList().size()>0) {
            toggleRecyclerRest(false);
            toggleRecyclerOrders(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAdapterOrders.notifyDataSetChanged();
    }

    public void deleteProduct(RecyclerView.ViewHolder viewHolder){

        // DIALOG INTERFACE BUTTONS
        DialogInterface.OnClickListener positiveButton = (dialog, which) -> {

            int itemPosition = viewHolder.getAdapterPosition();

            Product product = mProducts.get(itemPosition);
            product.save(false, task -> {

                if (task.isSuccessful()) {

                    if(product.rmvAtMarket())
                        throwToast("Product deleted!", false);

                    mProducts.remove(product);
                    myRestaurant.setProducts(mProducts);
                    mAdapterProducts.notifyItemRemoved(itemPosition);

                }else {

                    String exception ;
                    try{ throw task.getException(); }
                    catch (Exception e){
                        exception = e.getMessage();
                        e.printStackTrace();
                    }

                    throwToast("Delete failure: \n"+ exception, true);
                }
            });
        };

        DialogInterface.OnClickListener negativeButton =(dialog, which) -> {

            throwToast("CANCELLED", false);
            mAdapterProducts.notifyDataSetChanged();
        };
        AlertDialogUtil.deleteProductAlert(this, positiveButton, negativeButton);
    }

    /** ###############################  ACTIVITY LIFE-CYCLE  ################################ **/

    @Override
    protected void onStop() {
        super.onStop();

        wasStopped = true;

        if (UserFirebase.getCurrentUser()!=null) setOrdersListener(false);

        for (DatabaseReference orderRef : CartUtil.getMyOrdersRefList()) {
            int i = CartUtil.getMyOrdersRefList().indexOf(orderRef);
            if (myOrdersListener != null)
                CartUtil.getMyOrdersRefList().get(i).removeEventListener(myOrdersListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (wasStopped) {
            getMyOrders();
            getMyData();
            wasStopped = false;
        }
    }

    /** #################################  ACTIVITY PROCESS  ################################## **/

    @Override
    public boolean onSupportNavigateUp() {
        toggleRecyclerOrders(false);
        toggleRecyclerRest(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_company, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /** ####################################  HELPERS  #################################### **/

    private void startSettingsActivity() {
        Intent configIntent = new Intent(this, SettingsCompanyActivity.class);
        configIntent.putExtra("bundle", myCompany);
        startActivity(configIntent);
    }

    private void updateMarket() {
        if (myRestaurant.updateMarket()) {
            throwToast("Market updated!", false);
        }
    }

    private void toggleRecyclerRest(boolean isOpening){
        mRecyclerProducts.setVisibility(isOpening ? View.VISIBLE : View.GONE);
    }

    private void toggleRecyclerOrders(boolean isOpening){
        mRecyclerOrders.setVisibility(isOpening ? View.VISIBLE : View.GONE);
    }

    private void throwToast(String message, boolean isLong) {
        Toast.makeText(this, message,
                isLong ? Toast.LENGTH_LONG :Toast.LENGTH_SHORT).show();
    }
}
