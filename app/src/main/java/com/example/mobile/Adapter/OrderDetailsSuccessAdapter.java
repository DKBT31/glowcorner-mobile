package com.example.mobile.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailsSuccessAdapter extends RecyclerView.Adapter<OrderDetailsSuccessAdapter.OrderDetailViewHolder> {
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderDetail detail = orderDetails.get(position);
        holder.productNameTextView.setText(detail.getProductName());
        holder.quantityTextView.setText("Qty: " + detail.getQuantity());
        holder.priceTextView.setText("Price: $" + detail.getProductPrice());
        holder.totalTextView.setText("Total: $" + detail.getTotalAmount());
    }

    @Override
    public int getItemCount() {
        return orderDetails.size();
    }

    static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, quantityTextView, priceTextView, totalTextView;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.product_name);
            quantityTextView = itemView.findViewById(R.id.quantity);
            priceTextView = itemView.findViewById(R.id.product_price);
            totalTextView = itemView.findViewById(R.id.total_amount);
        }
    }

    public static class OrderDetail {
        private String productName;
        private int quantity;
        private double productPrice;
        private double totalAmount;

        public OrderDetail(String productName, int quantity, double productPrice, double totalAmount) {
            this.productName = productName;
            this.quantity = quantity;
            this.productPrice = productPrice;
            this.totalAmount = totalAmount;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getProductPrice() {
            return productPrice;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }
}