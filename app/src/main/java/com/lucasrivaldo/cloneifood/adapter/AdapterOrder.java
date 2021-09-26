package com.lucasrivaldo.cloneifood.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.model.Product;
import com.lucasrivaldo.cloneifood.model.cart.CartHelper;
import com.lucasrivaldo.cloneifood.model.cart.CartRestaurant;
import com.lucasrivaldo.cloneifood.model.cart.CartUtil;

import java.util.List;

import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.CONFIRMED;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.FINALIZED;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.PENDING;
import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.READY;

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.OrdersAdapter> {

    private boolean isForClient, isForOrderList;
    private List<CartRestaurant> mOrders;
    private CartRestaurant mCart;

    private Context mContext;

    public AdapterOrder(boolean isForClient,
                        boolean isForOrderList,
                        CartRestaurant cart) {

        this.isForClient = isForClient;
        this.isForOrderList = isForOrderList;
        this.mCart = cart;
    }

    public AdapterOrder(boolean isForClient, List<CartRestaurant> orders) {
        this.isForClient =  isForClient;
        this.mOrders = orders;
    }

    @NonNull
    @Override
    public OrdersAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList;
        if (isForOrderList){
            itemList = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_order_list, parent, false);
        }else {
            itemList = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_orders, parent, false);
        }

        if (mContext == null) mContext = parent.getContext();

        return new OrdersAdapter(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersAdapter holder, int position) {

        if (isForOrderList){
            Product product =  mCart.getRestaurant().getProducts().get(position);

            if (product.getName()
                    .equals(mContext.getResources().getString(R.string.text_stat_deliv_tax))){

                holder.textOrderProdName.setText(product.getName());
                holder.textOrderProdQt.setText(String.valueOf(1));
                holder.textOrderPartialPrice.setText(product.getPrice());

            }else {


            String prodId = product.getProd_id();
            int prod_qt = mCart.getProducts().get(prodId);
            double prodPrice = CartHelper.parsePrice(product.getPrice());
            String partial_price = CartHelper.formatPrice(prodPrice*prod_qt);


                holder.textOrderProdName.setText(product.getName());
                holder.textOrderProdQt.setText(String.valueOf(prod_qt));
                holder.textOrderPartialPrice.setText(partial_price);
            }

        }else {
            CartRestaurant restCart = mOrders.get(position);

            if (isForClient)
                holder.textOrderAddress.setText(restCart.getRestaurant().getName());
            else
                holder.textOrderAddress.setText(restCart.getClient().getAddress());

            holder.textOrderPaymentType.setText(restCart.getPaymentMethod());
            holder.textOrderTotalPrice.setText(CartHelper.formatPrice(restCart.getPartial_price()));

            String status = restCart.getOrderStatus();
            holder.textOrderStatus.setText(status);
            holder.textOrderStatus.setTextColor(IfoodHelper.getStatusColor(mContext, status));

            if (status.equals(CONFIRMED) || status.equals(FINALIZED))
                holder.textOrderStatus.setTypeface(null, Typeface.BOLD);
            else if (status.equals(PENDING) || status.equals(READY))
                holder.textOrderStatus.setTypeface(null, Typeface.ITALIC);
        }
    }

    @Override
    public int getItemCount() {
        int size;
        if (isForOrderList){
            size = mCart.getRestaurant().getProducts().size();
        }else {
            size = mOrders.size();
        }

        return size;
    }

    class OrdersAdapter extends RecyclerView.ViewHolder{

        // ORDERS LIST VIEWS
        private TextView textOrderAddress, textOrderPaymentType, textOrderTotalPrice, textOrderStatus;

        // ORDER VIEWS
        private TextView textOrderProdName, textOrderProdQt, textOrderPartialPrice;

        OrdersAdapter(@NonNull View itemView) {
            super(itemView);

            textOrderProdName = itemView.findViewById(R.id.textOrderProdName);
            textOrderProdQt = itemView.findViewById(R.id.textOrderProdQt);
            textOrderPartialPrice = itemView.findViewById(R.id.textOrderPartialPrice);


            // ORDERS LIST VIEWS
            textOrderAddress = itemView.findViewById(R.id.textOrderAddress);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            textOrderPaymentType = itemView.findViewById(R.id.textOrderPaymentType);
            textOrderTotalPrice = itemView.findViewById(R.id.textOrderTotalPrice);

            // ORDER VIEWS
            textOrderProdName = itemView.findViewById(R.id.textOrderProdName);
            textOrderProdQt = itemView.findViewById(R.id.textOrderProdQt);
            textOrderPartialPrice = itemView.findViewById(R.id.textOrderPartialPrice);
        }
    }
}
