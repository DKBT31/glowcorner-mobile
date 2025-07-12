package com.example.mobile.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Order;
import com.example.mobile.OrderDetailActivity;
import com.example.mobile.R;
import com.example.mobile.SignInActivity;
import com.example.mobile.manager.ManagerOrdersActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderAdapter extends ArrayAdapter<Order> {
    private Context context;
    private OnOrderDeletedListener deleteListener;

    public interface OnOrderDeletedListener {
        void onOrderDeleted(String orderId);
    }

    public OrderAdapter(Context context, List<Order> orders) {
        super(context, 0, orders);
        this.context = context;
        if (context instanceof OnOrderDeletedListener) {
            this.deleteListener = (OnOrderDeletedListener) context;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            int layoutId = context instanceof ManagerOrdersActivity
                    ? R.layout.manager_item_order
                    : R.layout.list_item_order;
            listItem = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        }

        Order order = getItem(position);

        TextView orderIdTextView = listItem.findViewById(R.id.order_id);
        TextView dateTextView = listItem.findViewById(R.id.order_date);
        TextView nameTextView = listItem.findViewById(R.id.order_customer_name);
        TextView amountTextView = listItem.findViewById(R.id.order_amount);
        TextView statusTextView = listItem.findViewById(R.id.order_status);
        Button detailButton = listItem.findViewById(R.id.btn_order_detail);

        orderIdTextView.setText("Order ID: " + order.getOrderID());
        dateTextView.setText("Date: " + order.getOrderDate());
        nameTextView.setText("Customer: " + order.getCustomerName());

        // Use discounted amount if available, otherwise use total amount
        double displayAmount = order.getDiscountedTotalAmount() > 0 ? order.getDiscountedTotalAmount()
                : order.getTotalAmount();
        amountTextView.setText("Total Paid: $" + String.format("%.2f", displayAmount));

        // Set status text and background color (only if status TextView exists)
        if (statusTextView != null) {
            statusTextView.setText(order.getStatus());
            setStatusBackground(statusTextView, order.getStatus());
        }

        detailButton.setOnClickListener(v -> {
            if (context instanceof androidx.appcompat.app.AppCompatActivity
                    && !((androidx.appcompat.app.AppCompatActivity) context).isFinishing()) {
                detailButton.setEnabled(false);
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("orderId", order.getOrderID());
                context.startActivity(intent);
                ((androidx.appcompat.app.AppCompatActivity) context).overridePendingTransition(0, 0);
                detailButton.postDelayed(() -> detailButton.setEnabled(true), 1000);
            }
        });

        if (context instanceof ManagerOrdersActivity) {
            ImageButton deleteButton = listItem.findViewById(R.id.btn_order_delete);
            if (deleteButton != null) {
                deleteButton.setOnClickListener(v -> {
                    if (context instanceof androidx.appcompat.app.AppCompatActivity
                            && !((androidx.appcompat.app.AppCompatActivity) context).isFinishing()) {
                        deleteButton.setEnabled(false);
                        deleteOrder(order.getOrderID(), position);
                        deleteButton.postDelayed(() -> deleteButton.setEnabled(true), 1000);
                    }
                });
            }
        }

        return listItem;
    }

    private void deleteOrder(String orderId, int position) {
        String jwtToken = SignInActivity.getStoredValue(context, "jwtToken");
        if (jwtToken == null) {
            Toast.makeText(context, "Please log in to delete orders.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userRole = SignInActivity.getStoredValue(context, "userRole");
        if (!"STAFF".equals(userRole)) {
            Toast.makeText(context, "Only staff can delete orders.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.deleteOrder("Bearer " + jwtToken, orderId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getBoolean("success") && jsonObject.getInt("status") == 200) {
                            Toast.makeText(context, "Order deleted successfully", Toast.LENGTH_SHORT).show();
                            if (deleteListener != null) {
                                deleteListener.onOrderDeleted(orderId);
                            }
                        } else {
                            Toast.makeText(context, "Failed to delete order: " + jsonObject.getString("description"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        Toast.makeText(context, "Error parsing delete response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Failed to delete order. Status: " + response.code(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStatusBackground(TextView statusTextView, String status) {
        int colorResId;
        switch (status.toUpperCase()) {
            case "PENDING":
                colorResId = R.color.pending_color;
                break;
            case "COMPLETED":
            case "DELIVERED":
                colorResId = R.color.completed_color;
                break;
            case "PROCESSING":
                colorResId = R.color.processing_color;
                break;
            case "CANCELLED":
                colorResId = R.color.cancelled_color;
                break;
            default:
                colorResId = R.color.default_color;
                break;
        }
        statusTextView.setBackgroundColor(ContextCompat.getColor(context, colorResId));
    }
}