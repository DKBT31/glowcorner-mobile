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
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Product;
import com.example.mobile.R;
import com.example.mobile.SignInActivity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeProductAdapter extends RecyclerView.Adapter<HomeProductAdapter.ProductViewHolder> {
    private static final String TAG = "ProductAdapter";
    private List<Product> productList;
    private Context context;

    public HomeProductAdapter(List<Product> productList) {
        this.productList = productList != null ? productList : Collections.emptyList();
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        context = parent.getContext();
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productIdTextView.setText(product.getProductID());
        holder.productNameTextView.setText(product.getProductName());
        holder.categoryTextView.setText(product.getCategory());
        holder.ratingTextView.setText(String.format("%.1f", product.getRating()));

        // Skin types
        String skinTypes = product.getSkinTypes().isEmpty() ? "N/A" : String.join(", ", product.getSkinTypes());
        holder.skinTypesTextView.setText(skinTypes);

        // New logic: show both price and discountedPrice if price > discountedPrice
        Double price = product.getPrice();
        Double discountedPrice = product.getDiscountedPrice();

        if (discountedPrice != null && price > discountedPrice) {
            // Show both prices
            holder.priceTextView.setVisibility(View.VISIBLE);
            holder.discountedPriceTextView.setVisibility(View.VISIBLE);

            holder.priceTextView.setText(String.format("$%.2f", price));
            holder.priceTextView.setPaintFlags(holder.priceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.priceTextView.setTextColor(Color.RED);

            holder.discountedPriceTextView.setText(String.format("$%.2f", discountedPrice));
            holder.discountedPriceTextView.setTextColor(Color.RED);
        } else {
            // Show only price
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
            holder.productImageView.setImageResource(R.drawable.bo); // fallback image
        }

        // Add to cart
        holder.addButton.setOnClickListener(v -> {
            String userID = SignInActivity.getStoredValue(context, "userID");
            String productID = product.getProductID();
            int quantity = 1;

            if (userID == null) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<ResponseBody> call = apiService.addToCart(userID, productID, quantity);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseString = response.body().string();
                            Log.d(TAG, "Response String: " + responseString);
                            if ("success".equalsIgnoreCase(responseString) || responseString.contains("\"success\":true")) {
                                Toast.makeText(context, "Added to cart successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to add to cart: " + responseString, Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading response: " + e.getMessage());
                            Toast.makeText(context, "Error adding to cart", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ResponseBody errorBody = response.errorBody();
                        String errorMsg = "Error adding to cart";
                        if (errorBody != null) {
                            try {
                                errorMsg = errorBody.string();
                                Log.e(TAG, "Raw Response: " + errorMsg);
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading response body: " + e.getMessage());
                            }
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "API Call Failed: " + t.getMessage(), t);
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView productIdTextView, productNameTextView, categoryTextView, priceTextView, discountedPriceTextView, ratingTextView, skinTypesTextView;
        public ImageView productImageView;
        public Button addButton;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productIdTextView = itemView.findViewById(R.id.product_id);
            productNameTextView = itemView.findViewById(R.id.product_name);
            categoryTextView = itemView.findViewById(R.id.category);
            priceTextView = itemView.findViewById(R.id.price);
            discountedPriceTextView = itemView.findViewById(R.id.discounted_price);
            ratingTextView = itemView.findViewById(R.id.rating);
            skinTypesTextView = itemView.findViewById(R.id.skin_types);
            productImageView = itemView.findViewById(R.id.product_image);
            addButton = itemView.findViewById(R.id.add_button);
        }
    }
}