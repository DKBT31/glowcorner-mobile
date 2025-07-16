package com.example.mobile.Adapter;

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
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.CartResponse;
import com.example.mobile.Models.ProductResponse;
import com.example.mobile.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private static final String TAG = "CartAdapter";
    private List<CartResponse.Item> items;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onQuantityChanged(String productId, int newQuantity);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CartResponse.Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        CartResponse.Item item = items.get(position);

        // Set basic item data
        holder.productNameTextView.setText(item.getProductName());
        holder.quantityTextView.setText(String.valueOf(item.getQuantity()));
        holder.totalAmountTextView.setText("$" + item.getTotalAmount());

        // Load product image from API
        loadProductImage(holder, item.getProductID());

        // Handle button clicks
        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                int newQuantity = item.getQuantity() - 1;
                listener.onQuantityChanged(item.getProductID(), newQuantity);
            }
        });

        holder.addButton.setOnClickListener(v -> {
            if (listener != null) {
                int newQuantity = item.getQuantity() + 1;
                listener.onQuantityChanged(item.getProductID(), newQuantity);
            }
        });

        // Handle price display with discounts
        if (item.getDiscountedTotalAmount() != null && item.getDiscountedTotalAmount() < item.getTotalAmount()) {
            // Show original price with strikethrough
            holder.priceTextView.setText("$" + item.getProductPrice());
            holder.priceTextView.setPaintFlags(holder.priceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.priceTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));

            // Show discounted price
            holder.discountedPriceTextView.setText("$" + (item.getDiscountedTotalAmount() / item.getQuantity()));
            holder.discountedPriceTextView.setVisibility(View.VISIBLE);
        } else {
            // No discount, show normal price
            holder.priceTextView.setText("$" + item.getProductPrice());
            holder.priceTextView.setPaintFlags(holder.priceTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.priceTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            holder.discountedPriceTextView.setVisibility(View.GONE);
        }
    }

    private void loadProductImage(CartViewHolder holder, String productId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = apiService.getProductById(productId);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Access the product data directly from ProductResponse
                    ProductResponse productResponse = response.body();
                    if (productResponse.getData() != null && !productResponse.getData().isEmpty()) {
                        String imageUrl = productResponse.getData().get(0).getImageUrl();

                        Glide.with(holder.itemView.getContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.rounded_background)
                                .error(R.drawable.rounded_background)
                                .into(holder.productImageView);
                    } else {
                        // Use placeholder if no product data
                        holder.productImageView.setImageResource(R.drawable.rounded_background);
                    }
                } else {
                    // Use placeholder on unsuccessful response
                    holder.productImageView.setImageResource(R.drawable.rounded_background);
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                // Keep placeholder on failure
                holder.productImageView.setImageResource(R.drawable.rounded_background);
            }
        });
    }
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public TextView productNameTextView;
        public TextView priceTextView;
        public TextView quantityTextView;
        public TextView totalAmountTextView;
        public TextView discountedPriceTextView;
        public ImageView productImageView;
        public Button removeButton;
        public Button addButton;

        public CartViewHolder(View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.cart_product_name);
            priceTextView = itemView.findViewById(R.id.cart_price);
            quantityTextView = itemView.findViewById(R.id.cart_quantity);
            totalAmountTextView = itemView.findViewById(R.id.cart_total_amount);
            discountedPriceTextView = itemView.findViewById(R.id.cart_discounted_price);
            productImageView = itemView.findViewById(R.id.cart_product_image);
            removeButton = itemView.findViewById(R.id.btn_remove_item);
            addButton = itemView.findViewById(R.id.btn_add_item);
        }
    }
}