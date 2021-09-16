package com.lucasrivaldo.cloneifood.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.helper.UserFirebase;
import com.lucasrivaldo.cloneifood.model.cart.Cart;
import com.lucasrivaldo.cloneifood.model.cart.CartHelper;
import com.lucasrivaldo.cloneifood.model.cart.CartUtil;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.model.Product;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class AdapterProducts extends RecyclerView.Adapter<AdapterProducts.ProductsViewHolder> {

    private Context mContext;
    private Handler mHandler;

    private boolean mIsForMarket;
    private List<Product> mProducts;

    private Cart mCart;
    private CartUtil.OnItemChangeListener mCartListener;

    // CONSTRUCTOR FOR CHART ACTIVITY
    public AdapterProducts(List<Product> products,
                           Cart cart,
                           CartUtil.OnItemChangeListener cartListener) {


        this.mProducts = products;
        this.mCart = cart;
        this.mCartListener = cartListener;
        mIsForMarket = cart!=null;
        mHandler = new Handler();
    }

    public AdapterProducts(List<Product> products,
                           boolean isForMarket,
                           CartUtil.OnItemChangeListener cartListener) {

        this.mProducts = products;
        this.mIsForMarket = isForMarket;
        this.mCartListener = cartListener;
        mHandler = new Handler();
    }

    @NonNull
    @Override
    public ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemList = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_products, parent, false);

        mContext = parent.getContext();

        return new ProductsViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsViewHolder holder, int position) {
        mHandler.post(() -> {
            int posit = position;
            Product product = mProducts.get(posit);

            String prodId = product.getProd_id();
            String restId = product.getRest_id();
            int quantity = !hasProdAtMap(restId, prodId) ? 0 :
                    Cart.getInstance().getCartList().get(restId).getProducts().get(prodId);

            holder.textDisplayItemCounter.setText(String.valueOf(quantity));
            holder.textDisplayItemCounter.setVisibility(quantity == 0 ? View.GONE : View.VISIBLE);


            String photoUrl = product.getPhoto();
            if (!IfoodHelper.invalidText(photoUrl))
                Picasso.get().load(Uri.parse(photoUrl)).into(holder.civProductImage);

            String name = product.getName();
            if (!IfoodHelper.invalidText(name))
                holder.textDisplayProdName.setText(name);

            String description = product.getDescription();
            if (!IfoodHelper.invalidText(description))
                holder.textDisplayProdDescription.setText(description);

            String price = product.getPrice();
            if (!IfoodHelper.invalidText(price))
                holder.textDisplayProdPrice.setText(price);

            if (!mIsForMarket) {
                holder.layoutForCart.setVisibility(View.GONE);

            } else {


                if (mCart != null) {
                    mHandler.post(() -> {

                        String restaurantName =
                                Cart.getInstance().getCartList().get(restId).getRestaurant().getName();

                        holder.textRestaurantName.setVisibility(View.VISIBLE);
                        holder.textRestaurantName.setText(restaurantName);


                        int item_qt = hasProdAtMap(restId, prodId) ?
                                Cart.getInstance().getCartList().get(restId).getProducts().get(prodId)
                                : 0;
                        holder.textDisplayItemCounter.setText(String.valueOf(item_qt));

                        holder.textDisplayItemCounter.setVisibility
                                (item_qt == 0 ? View.GONE : View.VISIBLE);


                    });
                }

                if (!IfoodHelper.invalidText(prodId)) {

                    // DECREASE BUTTON
                    holder.btnDecreaseItem.setOnClickListener(view -> {

                        mHandler.post(() -> {
                            if (hasProdAtMap(restId, prodId)) {

                                String counter = holder.textDisplayItemCounter.getText().toString();
                                String textZero = mContext.getResources().getString(R.string.text_zero);
                                int itemCounter = !counter.equals(textZero) ?
                                        Integer.parseInt(counter) : 0;

                                int value = itemCounter <= 0 ? 0 : (itemCounter - 1);
                                holder.textDisplayItemCounter.setText(String.valueOf(value));

                                holder.textDisplayItemCounter.setVisibility
                                        (value == 0 ? View.GONE : View.VISIBLE);

                                if (mCart != null && value == 0) {
                                    mProducts.remove(product);
                                    notifyDataSetChanged();
                                }
                                CartUtil.getMyCartList(mCartListener).remove(product);
                            }
                        });
                    });

                    // INCREASE BUTTON
                    holder.btnIncreaseItem.setOnClickListener(view -> {

                        mHandler.post(() -> {
                            String counter = holder.textDisplayItemCounter.getText().toString();
                            String textZero = mContext.getResources().getString(R.string.text_zero);
                            int itemCounter = !counter.equals(textZero) ?
                                    Integer.parseInt(counter) : 0;

                            int value = itemCounter + 1;
                            holder.textDisplayItemCounter.setText(String.valueOf(value));

                            holder.textDisplayItemCounter.setVisibility
                                    (value == 0 ? View.GONE : View.VISIBLE);


                            CartUtil.getMyCartList(mCartListener).add(product);
                        });
                    });
                }
            }
        });
    }

    private boolean hasProdAtMap(String restId, String prodId) {

        if (Cart.getInstance().getCartList()!= null
                && Cart.getInstance().getCartList().containsKey(restId)
                && Cart.getInstance().getCartList().get(restId).getProducts()!= null
                && Cart.getInstance().getCartList().get(restId).getProducts().containsKey(prodId)){
            return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        int size = mProducts.size();

        return size;
    }

    class ProductsViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout layoutForCart;

        private CircleImageView civProductImage;
        private TextView textDisplayProdName, textDisplayProdDescription,
                textDisplayProdPrice, textRestaurantName;
        private TextView textDisplayItemCounter;
        private ImageView btnDecreaseItem, btnIncreaseItem;

        ProductsViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutForCart = itemView.findViewById(R.id.layoutForCart);

            textRestaurantName = itemView.findViewById(R.id.textRestaurantName);
            civProductImage = itemView.findViewById(R.id.civProductImage);
            textDisplayProdName = itemView.findViewById(R.id.textDisplayProdName);
            textDisplayProdDescription = itemView.findViewById(R.id.textDisplayProdDescription);
            textDisplayProdPrice = itemView.findViewById(R.id.textDisplayProdPrice);
            textDisplayItemCounter = itemView.findViewById(R.id.textDisplayItemCounter);
            btnDecreaseItem  = itemView.findViewById(R.id.btnDecreaseItem);
            btnIncreaseItem = itemView.findViewById(R.id.btnIncreaseItem);
        }
    }
}
