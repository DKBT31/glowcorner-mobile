package com.example.mobile.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Models.Promotion;
import com.example.mobile.R;
import com.example.mobile.manager.ManagerPromotionsActivity;

import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {
    private Context context;
    private List<Promotion> promotionList;
    private OnPromotionActionListener listener;

    public interface OnPromotionActionListener {
        void onUpdatePromotion(String promotionId);
        void onDeletePromotion(String promotionId);
    }

    public PromotionAdapter(Context context, List<Promotion> promotionList, OnPromotionActionListener listener) {
        this.context = context;
        this.promotionList = promotionList;
        this.listener = listener;
    }

    @Override
    public PromotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.promotion_item, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PromotionViewHolder holder, int position) {
        Promotion promotion = promotionList.get(position);
        holder.promotionIdTextView.setText("ID: " + promotion.getPromotionId());
        holder.promotionNameTextView.setText("Name: " + promotion.getPromotionName());
        holder.promotionDiscountTextView.setText("Discount: " + promotion.getDiscount() + "%");
        holder.promotionDatesTextView.setText("Dates: " + promotion.getStartDate() + " - " + promotion.getEndDate());

        // Display productIDs as a comma-separated list
        List<String> productIDs = promotion.getProductIDs();
        if (productIDs != null && !productIDs.isEmpty()) {
            String productsText = "Products: " + String.join(", ", productIDs);
            holder.promotionProductsTextView.setText(productsText);
        } else {
            holder.promotionProductsTextView.setText("Products: None");
        }

        holder.btnUpdatePromotion.setOnClickListener(v -> {
            if (context instanceof ManagerPromotionsActivity && !((ManagerPromotionsActivity) context).isFinishing() && listener != null) {
                holder.btnUpdatePromotion.setEnabled(false);
                listener.onUpdatePromotion(promotion.getPromotionId());
                holder.btnUpdatePromotion.postDelayed(() -> holder.btnUpdatePromotion.setEnabled(true), 1000);
            }
        });

        holder.btnDeletePromotion.setOnClickListener(v -> {
            if (context instanceof ManagerPromotionsActivity && !((ManagerPromotionsActivity) context).isFinishing() && listener != null) {
                holder.btnDeletePromotion.setEnabled(false);
                listener.onDeletePromotion(promotion.getPromotionId());
                holder.btnDeletePromotion.postDelayed(() -> holder.btnDeletePromotion.setEnabled(true), 1000);
            }
        });
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    public static class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView promotionIdTextView, promotionNameTextView, promotionDiscountTextView, promotionDatesTextView, promotionProductsTextView;
        ImageButton btnUpdatePromotion, btnDeletePromotion;

        public PromotionViewHolder(View itemView) {
            super(itemView);
            promotionIdTextView = itemView.findViewById(R.id.promotion_id);
            promotionNameTextView = itemView.findViewById(R.id.promotion_name);
            promotionDiscountTextView = itemView.findViewById(R.id.promotion_discount);
            promotionDatesTextView = itemView.findViewById(R.id.promotion_dates);
            promotionProductsTextView = itemView.findViewById(R.id.promotion_products); // New TextView
            btnUpdatePromotion = itemView.findViewById(R.id.btn_update_promotion);
            btnDeletePromotion = itemView.findViewById(R.id.btn_delete_promotion);
        }
    }
}