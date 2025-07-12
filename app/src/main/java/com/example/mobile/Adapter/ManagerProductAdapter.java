package com.example.mobile.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobile.Models.Product;
import com.example.mobile.R;

import java.util.Collections;
import java.util.List;

public class ManagerProductAdapter extends RecyclerView.Adapter<ManagerProductAdapter.ManagerProductViewHolder> {
    private static final String TAG = "ManagerProductAdapter";
    private List<Product> productList;
    private Context context;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onUpdateClick(Product product);
        void onDeleteClick(Product product);
    }

    public ManagerProductAdapter(List<Product> productList, OnItemActionListener listener) {
        this.productList = productList != null ? productList : Collections.emptyList();
        this.listener = listener;
    }

    @Override
    public ManagerProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manager_product, parent, false);
        context = parent.getContext();
        return new ManagerProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ManagerProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productIdTextView.setText(product.getProductID());
        holder.productNameTextView.setText(product.getProductName());
        holder.categoryTextView.setText(product.getCategory());
        holder.ratingTextView.setText(String.format("%.1f", product.getRating()));

        // Handle skinTypes safely
        String skinTypes = product.getSkinTypes().isEmpty() ? "N/A" : String.join(", ", product.getSkinTypes());
        holder.skinTypesTextView.setText(skinTypes);

        // Handle price & discounted price
        Double price = product.getPrice();
        Double discountedPrice = product.getDiscountedPrice();

        if (discountedPrice != null && price > discountedPrice) {
            holder.priceTextView.setVisibility(View.VISIBLE);
            holder.discountedPriceTextView.setVisibility(View.VISIBLE);

            holder.priceTextView.setText(String.format("$%.2f", price));
            holder.priceTextView.setPaintFlags(holder.priceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.priceTextView.setTextColor(Color.RED);

            holder.discountedPriceTextView.setText(String.format("$%.2f", discountedPrice));
            holder.discountedPriceTextView.setTextColor(Color.RED);
        } else {
            holder.priceTextView.setVisibility(View.VISIBLE);
            holder.discountedPriceTextView.setVisibility(View.GONE);

            holder.priceTextView.setText(String.format("$%.2f", price));
            holder.priceTextView.setPaintFlags(holder.priceTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.priceTextView.setTextColor(Color.BLACK);
        }

        // Load image
        if (!product.getImageUrl().isEmpty()) {
            Glide.with(context).load(product.getImageUrl()).into(holder.productImageView);
        } else {
            holder.productImageView.setImageResource(R.drawable.bo);
        }

        // Set click listeners
        holder.updateButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateClick(product);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateData(List<Product> newProductList) {
        this.productList = newProductList != null ? newProductList : Collections.emptyList();
        notifyDataSetChanged();
    }

    public static class ManagerProductViewHolder extends RecyclerView.ViewHolder {
        public TextView productIdTextView, productNameTextView, categoryTextView, priceTextView, discountedPriceTextView, ratingTextView, skinTypesTextView;
        public ImageView productImageView;
        public Button updateButton, deleteButton;

        public ManagerProductViewHolder(View itemView) {
            super(itemView);
            productIdTextView = itemView.findViewById(R.id.product_id);
            productNameTextView = itemView.findViewById(R.id.product_name);
            categoryTextView = itemView.findViewById(R.id.category);
            priceTextView = itemView.findViewById(R.id.price);
            discountedPriceTextView = itemView.findViewById(R.id.discounted_price);
            ratingTextView = itemView.findViewById(R.id.rating);
            skinTypesTextView = itemView.findViewById(R.id.skin_types);
            productImageView = itemView.findViewById(R.id.product_image);
            updateButton = itemView.findViewById(R.id.update_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
