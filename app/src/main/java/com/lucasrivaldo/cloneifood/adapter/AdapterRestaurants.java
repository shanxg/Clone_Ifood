package com.lucasrivaldo.cloneifood.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lucasrivaldo.cloneifood.R;
import com.lucasrivaldo.cloneifood.helper.IfoodHelper;
import com.lucasrivaldo.cloneifood.model.Product;
import com.lucasrivaldo.cloneifood.model.Restaurant;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterRestaurants extends RecyclerView.Adapter<AdapterRestaurants.RestaurantViewHolder>{

    private List<Restaurant> mRestaurantList;

    public AdapterRestaurants(List<Restaurant> mRestaurantList) {
        this.mRestaurantList = mRestaurantList;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_restaurants, parent, false);

        return new RestaurantViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {

        Restaurant restaurant = mRestaurantList.get(position);

        String photoUrl = restaurant.getPhoto();
        if (!IfoodHelper.invalidText(photoUrl))
            Picasso.get().load(Uri.parse(photoUrl)).into(holder.civRestaurantImage);

        String name = restaurant.getName();
        if (!IfoodHelper.invalidText(name))
            holder.textRestName.setText(name);

        String category = restaurant.getCategory();
        if (!IfoodHelper.invalidText(category))
            holder.textRestCategory.setText(category);

        String deliveryTime = restaurant.getDeliveryTime();
        if (!IfoodHelper.invalidText(deliveryTime))
            holder.textRestDelivTime.setText(deliveryTime);

        String deliveryTax = restaurant.getDeliveryTax();
        if (!IfoodHelper.invalidText(deliveryTax))
            holder.textRestDelivTax.setText(deliveryTax);
    }

    @Override
    public int getItemCount() {
        return mRestaurantList.size();
    }

    class RestaurantViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView civRestaurantImage;
        private TextView textRestName, textRestCategory,
                textRestDelivTime, textRestDelivTax;

        RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);

            civRestaurantImage = itemView.findViewById(R.id.civRestaurantImage);
            textRestName = itemView.findViewById(R.id.textRestName);
            textRestCategory = itemView.findViewById(R.id.textRestCategory);
            textRestDelivTime = itemView.findViewById(R.id.textRestDelivTime);
            textRestDelivTax = itemView.findViewById(R.id.textRestDelivTax);
        }
    }
}