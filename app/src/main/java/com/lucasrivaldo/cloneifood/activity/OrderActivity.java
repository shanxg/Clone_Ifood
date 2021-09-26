package com.lucasrivaldo.cloneifood.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.adapter.AdapterOrder;
import com.lucasrivaldo.cloneifood.helper.AlertDialogUtil;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.Product;
import com.lucasrivaldo.cloneifood.model.cart.CartHelper;
import com.lucasrivaldo.cloneifood.model.cart.CartRestaurant;
import com.lucasrivaldo.cloneifood.model.cart.CartUtil;

import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.CANCELLED;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.CONFIRMED;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.FINALIZED;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.PENDING;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.READY;

public class OrderActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isForClient;

    private RecyclerView mRecyclerOrderList;
    private AdapterOrder mAdapterOrderList;

    private CartRestaurant mCurrentCart;
    private TextView mTextOrderAddress, mTextOrderPaymentType, mTextOrderTotalPrice, mTextOrderStatus;
    private Button mButtonUpdateStatus;

    /** ####################################  INITIALIZE  #################################### **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            mCurrentCart = (CartRestaurant) bundle.getSerializable("bundle");
            if (mCurrentCart != null) {
                isForClient = bundle.getBoolean("isForClient");


                Product product = new Product();
                product.setName(getResources().getString(R.string.text_stat_deliv_tax));
                product.setPrice(mCurrentCart.getRestaurant().getDeliveryTax());


                if (mCurrentCart.getRestaurant().getProducts().add(product)) {
                    preLoad();
                    loadInterface();
                }
            }
        }else {
            finish();
        }
    }

    private void preLoad() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle( isForClient ?
                mCurrentCart.getRestaurant().getName() : mCurrentCart.getClient().getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadInterface(){
        
        mRecyclerOrderList =findViewById(R.id.recyclerOrderList);

        // ORDERS LIST VIEWS
        mTextOrderAddress = findViewById(R.id.textOrderAddress);
        mTextOrderStatus = findViewById(R.id.textOrderStatus);
        mTextOrderPaymentType = findViewById(R.id.textOrderPaymentType);
        mTextOrderTotalPrice = findViewById(R.id.textOrderTotalPrice);

        mButtonUpdateStatus = findViewById(R.id.buttonUpateOrderStatus);
        if (isForClient) {
            if (mCurrentCart.getOrderStatus().equals(READY)) {
                mButtonUpdateStatus.setVisibility(View.VISIBLE);
            }else {
                mButtonUpdateStatus.setVisibility(View.GONE);
            }

            mButtonUpdateStatus.setOnClickListener(view -> {
                mCurrentCart.setOrderStatus(FINALIZED);
                if (CartUtil.removeRef(mCurrentCart.getOrderId()))
                        if (mCurrentCart.update(mCurrentCart.getRestaurant().getCompanyId()))
                            finish();
            });

            mButtonUpdateStatus.setText(getResources().getString(R.string.txt_stat_finalize));
            setOrderStatusText(FINALIZED);

        }else {
            mButtonUpdateStatus.setOnClickListener(this);
            setBtnUpdateStatus();
        }


        setRecyclerOrderList();
        loadCartData();
    }

    private void setBtnUpdateStatus() {
        String status = mCurrentCart.getOrderStatus();

        if (status.equals(PENDING))
            mButtonUpdateStatus.setText(getResources().getString(R.string.txt_stat_confirm_order));
        else if (status.equals(CONFIRMED))
            mButtonUpdateStatus.setText(getResources().getString(R.string.txt_stat_order_ready));
        else if (status.equals(READY))
            mButtonUpdateStatus.setText(getResources().getString(R.string.txt_stat_waiting_client));
        else
            mButtonUpdateStatus.setText(FINALIZED);
    }

    private void setRecyclerOrderList() {
        mAdapterOrderList = new AdapterOrder(isForClient, true, mCurrentCart);
        mRecyclerOrderList.setHasFixedSize(false);
        mRecyclerOrderList.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerOrderList.setAdapter(mAdapterOrderList);

        mAdapterOrderList.notifyDataSetChanged();
    }

    /** ##################################  CLICK LISTENERS  ################################## **/

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonUpateOrderStatus){

            String status = mCurrentCart.getOrderStatus();

            if (status.equals(PENDING)){


                DialogInterface.OnClickListener clickListener = (dialogInterface, i) -> {
                    mButtonUpdateStatus.setEnabled(true);
                    mButtonUpdateStatus.setClickable(true);
                    mCurrentCart.setOrderStatus(CONFIRMED);

                    setOrderStatusText(CONFIRMED);

                    mCurrentCart.update(UserFirebase.getCurrentUserID());
                    setBtnUpdateStatus();
                };

                DialogInterface.OnCancelListener cancelListener = dialogInterface -> {
                    mCurrentCart.setOrderStatus(CANCELLED);
                    if (mCurrentCart.update(UserFirebase.getCurrentUserID()))
                        if(mCurrentCart.cancel())
                            finish();
                };


                AlertDialogUtil.confirmOrderAlert(this, clickListener, cancelListener);

                status = CONFIRMED;
            }
            else if (status.equals(CONFIRMED)) {
                mButtonUpdateStatus.setEnabled(true);
                mButtonUpdateStatus.setClickable(true);
                mCurrentCart.setOrderStatus(READY);
                status = READY;
            }else if (status.equals(READY)){
                mButtonUpdateStatus.setEnabled(false);
                mButtonUpdateStatus.setClickable(false);
            }else {
                mButtonUpdateStatus.setEnabled(false);
                mButtonUpdateStatus.setClickable(false);
            }


            if (!status.equals(CONFIRMED)) {
                setOrderStatusText(status);

                if (mCurrentCart.update(UserFirebase.getCurrentUserID()))
                    setBtnUpdateStatus();
            }
        }
    }

    /** ####################################  LISTENERS  ##################################### **/

    /** ####################################  MY METHODS  #################################### **/


    private void loadCartData() {

        mTextOrderAddress.setText(mCurrentCart.getClient().getAddress());
        mTextOrderPaymentType.setText(mCurrentCart.getPaymentMethod());
        mTextOrderTotalPrice.setText(CartHelper.formatPrice(mCurrentCart.getPartial_price()));

        String status = mCurrentCart.getOrderStatus();
        setOrderStatusText(status);
    }

    private void setOrderStatusText(String status){
        mTextOrderStatus.setText(status);
        mTextOrderStatus.setTextColor(IfoodHelper.getStatusColor(this, status));

        if (status.equals(CONFIRMED) || status.equals(FINALIZED))
            mTextOrderStatus.setTypeface(null, Typeface.BOLD);
        else if (status.equals(PENDING) || status.equals(READY))
            mTextOrderStatus.setTypeface(null, Typeface.ITALIC);
    }

    /** ###############################  ACTIVITY LIFE-CYCLE  ################################ **/



    /** #################################  ACTIVITY PROCESS  ################################## **/

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    /** ####################################  HELPERS  #################################### **/
}

