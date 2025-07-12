package com.example.mobile.manager;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.Adapter.OrderAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Order;
import com.example.mobile.Models.OrderDetail;
import com.example.mobile.R;
import com.example.mobile.SignInActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerOrdersActivity extends AppCompatActivity implements OrderAdapter.OnOrderDeletedListener {
    private static final String TAG = "ManagerOrdersActivity";
    private ListView orderListView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.manager_orders);

        // Initialize UI components
        orderListView = findViewById(R.id.order_list_view);

        // Initialize order list and adapter
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        orderListView.setAdapter(orderAdapter);

        // Load orders
        loadOrders();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }

    private void loadOrders() {
        String jwtToken = SignInActivity.getStoredValue(this, "jwtToken");

        if (jwtToken == null) {
            Toast.makeText(this, "Please log in to view orders.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getOrders("Bearer " + jwtToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Order Response: " + responseString);

                        // Parse response as a JSON array
                        JsonParser parser = new JsonParser();
                        JsonArray dataArray = parser.parse(responseString).getAsJsonArray();

                        orderList.clear();
                        for (JsonElement element : dataArray) {
                            JsonObject orderObj = element.getAsJsonObject();
                            String orderId = orderObj.get("orderID").getAsString();
                            String customerId = orderObj.get("customerID").getAsString();
                            String customerName = orderObj.get("customerName").isJsonNull()
                                    ? "Unknown Customer"
                                    : orderObj.get("customerName").getAsString();
                            String orderDate = orderObj.get("orderDate").getAsString();
                            String status = orderObj.get("status").getAsString();
                            double totalAmount = orderObj.get("totalAmount").getAsDouble();
                            double discountedTotalAmount = orderObj.get("discountedTotalAmount").getAsDouble();

                            JsonArray detailsArray = orderObj.getAsJsonArray("orderDetails");
                            List<OrderDetail> orderDetails = new ArrayList<>();
                            for (JsonElement detailElement : detailsArray) {
                                JsonObject detailObj = detailElement.getAsJsonObject();
                                String detailOrderId = detailObj.get("orderID").getAsString();
                                String productId = detailObj.get("productID").getAsString();
                                String productName = detailObj.get("productName").getAsString();
                                int quantity = detailObj.get("quantity").getAsInt();
                                double productPrice = detailObj.get("productPrice").getAsDouble();
                                String discountName = detailObj.get("discountName").isJsonNull()
                                        ? null
                                        : detailObj.get("discountName").getAsString();
                                Double discountPercentage = detailObj.get("discountPercentage").isJsonNull()
                                        ? null
                                        : detailObj.get("discountPercentage").getAsDouble();
                                double detailTotalAmount = detailObj.get("totalAmount").getAsDouble();
                                Double detailDiscountedTotalAmount = detailObj.get("discountedTotalAmount").isJsonNull()
                                        ? null
                                        : detailObj.get("discountedTotalAmount").getAsDouble();

                                orderDetails.add(new OrderDetail(
                                        detailOrderId,
                                        productId,
                                        productName,
                                        quantity,
                                        productPrice,
                                        discountName,
                                        discountPercentage,
                                        detailTotalAmount,
                                        detailDiscountedTotalAmount));
                            }

                            orderList.add(new Order(
                                    orderId,
                                    customerId,
                                    customerName,
                                    orderDate,
                                    status,
                                    totalAmount,
                                    discountedTotalAmount,
                                    orderDetails,
                                    null, null, null, null, null));
                        }

                        orderAdapter.notifyDataSetChanged();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                        Toast.makeText(ManagerOrdersActivity.this, "Error reading order data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManagerOrdersActivity.this, "Failed to load orders. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for orders: " + t.getMessage(), t);
                Toast.makeText(ManagerOrdersActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onOrderDeleted(String orderId) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderID().equals(orderId)) {
                orderList.remove(i);
                orderAdapter.notifyDataSetChanged();
                break;
            }
        }
    }
}