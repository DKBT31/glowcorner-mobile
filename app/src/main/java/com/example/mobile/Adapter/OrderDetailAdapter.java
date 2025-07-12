package com.example.mobile.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Models.OrderDetail;
import com.example.mobile.R;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private Context context;
    private List<OrderDetail> orderDetailList;

    public OrderDetailAdapter(Context context, List<OrderDetail> orderDetailList) {
        this.context = context;
        this.orderDetailList = orderDetailList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OrderDetail orderDetail = orderDetailList.get(position);
        holder.productNameTextView.setText(orderDetail.getProductName());
        holder.quantityTextView.setText("Quantity: " + orderDetail.getQuantity());

        // Show discounted price if available, otherwise show regular price
        Double discountedTotal = orderDetail.getDiscountedTotalAmount();
        if (discountedTotal != null && discountedTotal > 0) {
            double discountedPricePerUnit = discountedTotal / orderDetail.getQuantity();
            holder.priceTextView.setText("Price: $" + String.format("%.2f", discountedPricePerUnit) +
                    " (was $" + String.format("%.2f", orderDetail.getProductPrice()) + ")");
        } else {
            holder.priceTextView.setText("Price: $" + String.format("%.2f", orderDetail.getProductPrice()));
        }
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, quantityTextView, priceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.product_name);
            quantityTextView = itemView.findViewById(R.id.quantity);
            priceTextView = itemView.findViewById(R.id.product_price);
        }
    }
}
