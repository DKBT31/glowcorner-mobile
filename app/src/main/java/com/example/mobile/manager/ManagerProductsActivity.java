package com.example.mobile.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Adapter.ManagerProductAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Product;
import com.example.mobile.Models.ProductResponse;
import com.example.mobile.R;
import com.example.mobile.SignInActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerProductsActivity extends AppCompatActivity {
    private static final String TAG = "ManagerProductsActivity";

    private RecyclerView recyclerView;
    private ManagerProductAdapter adapter;
    private TextView userIdTextView;
    private Spinner skinTypeSpinner;
    private Spinner categorySpinner;
    private EditText searchEditText;
    private Button searchButton;
    private FloatingActionButton addProductButton;

    private final List<String> skinTypes = Arrays.asList("All", "Dry", "Oily", "Combination", "Sensitive");
    private final List<String> categories = Arrays.asList("All", "Cleanser", "Serum", "Moisturizer");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_products);

        initViews();
        setupSpinners();
        setupSearch();
        setupAddButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        userIdTextView = findViewById(R.id.userIdTextView);
        skinTypeSpinner = findViewById(R.id.skin_type_spinner);
        categorySpinner = findViewById(R.id.category_spinner);
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        addProductButton = findViewById(R.id.add_product_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManagerProductAdapter(null, new ManagerProductAdapter.OnItemActionListener() {
            @Override
            public void onUpdateClick(Product product) {
                Intent intent = new Intent(ManagerProductsActivity.this, EditProductActivity.class);
                intent.putExtra("productID", product.getProductID());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Product product) {
                deleteProduct(product.getProductID());
            }
        });
        recyclerView.setAdapter(adapter);

        String userID = SignInActivity.getStoredValue(this, "userID");
        userIdTextView.setText(userID != null ? "User ID: " + userID : "User ID: Not logged in");
    }

    private void setupSpinners() {
        ArrayAdapter<String> skinTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, skinTypes);
        skinTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinTypeSpinner.setAdapter(skinTypeAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        skinTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadFilteredProducts(skinTypes.get(position), categorySpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadFilteredProducts(skinTypeSpinner.getSelectedItem().toString(), categories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                loadProducts();
                return;
            }
            searchProduct(query);
        });
    }

    private void setupAddButton() {
        addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerProductsActivity.this, CreateProductActivity.class);
            startActivity(intent);
        });
    }

    private void searchProduct(String query) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = query.matches("\\d+") ?
                apiService.getProductById(query) :
                apiService.getProductsByName(query);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Product> products = response.body().getData();
                    adapter.updateData(products);
                } else {
                    Toast.makeText(ManagerProductsActivity.this, "No products found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(ManagerProductsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFilteredProducts(String skinType, String category) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ProductResponse> call;

        if (skinType.equals("All") && category.equals("All")) {
            call = apiService.getProducts();
        } else if (!skinType.equals("All")) {
            call = apiService.getProductsBySkinType(skinType);
        } else {
            call = apiService.getProductsByCategory(category);
        }

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    adapter.updateData(response.body().getData());
                } else {
                    Toast.makeText(ManagerProductsActivity.this, "No products available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(ManagerProductsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        loadFilteredProducts("All", "All");
    }

    private void deleteProduct(String productID) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.deleteProduct(productID);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManagerProductsActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    loadProducts();
                } else {
                    Toast.makeText(ManagerProductsActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ManagerProductsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}