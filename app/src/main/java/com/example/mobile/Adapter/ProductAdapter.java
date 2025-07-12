package com.example.mobile.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.widget.Filter;
import android.widget.Filterable;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {
    private static final String TAG = "ProductAdapter";
    private List<Product> productList;
    private List<Product> productListFull;
    private Context context;
    private Set<String> selectedProductIDs = new HashSet<>();
    private OnSelectionChangedListener selectionChangedListener;

    // Interface for selection change callback
    public interface OnSelectionChangedListener {
        void onSelectionChanged(Set<String> selectedProductIDs);
    }

    public ProductAdapter(List<Product> productList) {
        this.productList = productList != null ? new ArrayList<>(productList) : new ArrayList<>();
        this.productListFull = new ArrayList<>(this.productList);
    }

    // Set initial selected product IDs
    public void setInitialSelectedProductIDs(Set<String> initialSelected) {
        this.selectedProductIDs.clear();
        if (initialSelected != null) {
            this.selectedProductIDs.addAll(initialSelected);
        }
        notifyDataSetChanged();
    }

    // Clear all selections
    public void clearSelections() {
        selectedProductIDs.clear();
        notifyDataSetChanged();
    }

    // Set selection change listener
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manager_promotion_product, parent, false);
        context = parent.getContext();
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productIdTextView.setText(product.getProductID());
        holder.productNameTextView.setText(product.getProductName());
        holder.categoryTextView.setText(product.getCategory());

        // Price and discounted price
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

        // Checkbox for selection
        holder.cbSelect.setChecked(selectedProductIDs.contains(product.getProductID()));
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedProductIDs.add(product.getProductID());
            } else {
                selectedProductIDs.remove(product.getProductID());
            }
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged(new HashSet<>(selectedProductIDs));
            }
        });

        // Disable add to cart button since it's not needed in promotion context
        holder.addButton.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateData(List<Product> newProductList) {
        this.productList = newProductList != null ? new ArrayList<>(newProductList) : new ArrayList<>();
        this.productListFull = new ArrayList<>(this.productList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchTerm = constraint != null ? constraint.toString().toLowerCase().trim() : "";
                List<Product> filteredList = new ArrayList<>();

                if (searchTerm.isEmpty()) {
                    filteredList.addAll(productListFull);
                } else {
                    for (Product product : productListFull) {
                        if (product.getProductName().toLowerCase().contains(searchTerm) ||
                                product.getProductID().toLowerCase().contains(searchTerm) ||
                                (product.getCategory() != null && product.getCategory().toLowerCase().contains(searchTerm))) {
                            filteredList.add(product);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                productList.clear();
                productList.addAll((List<Product>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public Set<String> getSelectedProductIDs() {
        return new HashSet<>(selectedProductIDs);
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView productIdTextView, productNameTextView, categoryTextView, priceTextView, discountedPriceTextView;
        public ImageView productImageView;
        public Button addButton;
        public CheckBox cbSelect;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productIdTextView = itemView.findViewById(R.id.product_id);
            productNameTextView = itemView.findViewById(R.id.product_name);
            categoryTextView = itemView.findViewById(R.id.category);
            priceTextView = itemView.findViewById(R.id.price);
            discountedPriceTextView = itemView.findViewById(R.id.discounted_price);
            productImageView = itemView.findViewById(R.id.product_image);
            addButton = itemView.findViewById(R.id.add_button);
            cbSelect = itemView.findViewById(R.id.cb_select);
        }
    }
}