package com.example.mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobile.Adapter.HomeProductAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Product;
import com.example.mobile.Models.ProductResponse;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    private String productID;
    private ProgressBar progressBar;
    private ImageView productImage;
    private TextView productName, category, price, discountedPrice, skinTypes, description, quantityText;
    private RatingBar ratingBar;
    private TextView ratingText;
    private Button decrementButton, incrementButton, addToCartButton;
    private int quantity = 1;
    private RecyclerView relatedProductsRecyclerView;
    private HomeProductAdapter relatedProductsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize views
        progressBar = findViewById(R.id.progress_bar);
        productImage = findViewById(R.id.product_image);
        productName = findViewById(R.id.product_name);
        category = findViewById(R.id.category);
        price = findViewById(R.id.price);
        discountedPrice = findViewById(R.id.discounted_price);
        skinTypes = findViewById(R.id.skin_types);
        description = findViewById(R.id.description);
        ratingBar = findViewById(R.id.rating_bar);
        ratingText = findViewById(R.id.rating_text);
        quantityText = findViewById(R.id.quantity);
        decrementButton = findViewById(R.id.decrement_button);
        incrementButton = findViewById(R.id.increment_button);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        relatedProductsRecyclerView = findViewById(R.id.related_products_recycler_view);

        // Initialize RecyclerView for related products
        relatedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        relatedProductsAdapter = new HomeProductAdapter(null);
        relatedProductsRecyclerView.setAdapter(relatedProductsAdapter);

        // Get productID from intent
        productID = getIntent().getStringExtra("productID");
        if (productID == null) {
            Toast.makeText(this, "Product ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up back button
        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());

        // Set up quantity controls
        decrementButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
            }
        });
        incrementButton.setOnClickListener(v -> {
            quantity++;
            quantityText.setText(String.valueOf(quantity));
        });

        // Set up add to cart
        addToCartButton.setOnClickListener(v -> addToCart());

        // Load product details and related products
        loadProductDetails();
    }

    private void loadProductDetails() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = apiService.getFilterProductById(productID);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Product product = response.body().getData().get(0); // Assuming single product
                    updateUI(product);
                    loadRelatedProducts(product.getCategory()); // Load related products based on category
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRelatedProducts(String category) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = apiService.getProductsByCategory(category);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Product> relatedProducts = response.body().getData();
                    // Filter out the current product
                    relatedProducts.removeIf(p -> p.getProductID().equals(productID));
                    relatedProductsAdapter.updateData(relatedProducts);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "No related products found", Toast.LENGTH_SHORT).show();
                    relatedProductsAdapter.updateData(null);
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Error loading related products: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                relatedProductsAdapter.updateData(null);
            }
        });
    }

    private void updateUI(Product product) {
        Glide.with(this).load(product.getImageUrl()).into(productImage);
        productName.setText(product.getProductName());
        category.setText("Category: " + product.getCategory());
        ratingBar.setRating((float) product.getRating());
        ratingText.setText(String.format(" (%.1f reviews)", product.getRating()));
        skinTypes.setText("Skin Types: " + String.join(", ", product.getSkinTypes()));

        Double discountedPriceValue = product.getDiscountedPrice();
        if (discountedPriceValue != null && product.getPrice() > discountedPriceValue) {
            price.setVisibility(View.VISIBLE);
            price.setText(String.format("$%.2f", product.getPrice()));
            price.setPaintFlags(price.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG); // Apply strikethrough
            discountedPrice.setText(String.format("$%.2f", discountedPriceValue));
        } else {
            price.setVisibility(View.GONE);
            price.setPaintFlags(price.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG)); // Remove strikethrough
            discountedPrice.setText(String.format("$%.2f", product.getPrice()));
        }

        description.setText(product.getDescription());
    }

    private void addToCart() {
        String userID = SignInActivity.getStoredValue(this, "userID");
        if (userID == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        addToCartButton.setEnabled(false);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.addToCart(userID, productID, quantity);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                addToCartButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Response String: " + responseString);
                        if ("success".equalsIgnoreCase(responseString) || responseString.contains("\"success\":true")) {
                            Toast.makeText(ProductDetailActivity.this, "Added to cart successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProductDetailActivity.this, "Failed to add to cart: " + responseString, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                        Toast.makeText(ProductDetailActivity.this, "Error adding to cart", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                addToCartButton.setEnabled(true);
                Log.e(TAG, "API Call Failed: " + t.getMessage(), t);
                Toast.makeText(ProductDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}