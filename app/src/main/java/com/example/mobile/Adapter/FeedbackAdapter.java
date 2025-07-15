package com.example.mobile.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Models.Feedback;
import com.example.mobile.R;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {
    private List<Feedback> feedbackList;
    private final Context context;
    private final OnDeleteClickListener deleteClickListener;
    private final OnUpdateClickListener updateClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(String feedbackID);
    }

    public interface OnUpdateClickListener {
        void onUpdateClick(String feedbackID);
    }

    public FeedbackAdapter(List<Feedback> feedbackList, OnDeleteClickListener deleteClickListener, OnUpdateClickListener updateClickListener) {
        this.feedbackList = feedbackList;
        this.context = null; // Context not needed for basic operations
        this.deleteClickListener = deleteClickListener;
        this.updateClickListener = updateClickListener;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.staff_item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.feedbackIdText.setText(feedback.getFeedbackID());
        holder.customerIdText.setText(feedback.getCustomerID());
        holder.ratingText.setText(String.valueOf(feedback.getRating()));
        holder.commentText.setText(feedback.getComment());
        holder.dateText.setText(feedback.getFeedbackDate());

        holder.deleteButton.setOnClickListener(v -> deleteClickListener.onDeleteClick(feedback.getFeedbackID()));
        holder.updateButton.setOnClickListener(v -> updateClickListener.onUpdateClick(feedback.getFeedbackID()));
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public void updateList(List<Feedback> newList) {
        this.feedbackList = newList;
        notifyDataSetChanged();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView feedbackIdText, customerIdText, ratingText, commentText, dateText;
        ImageButton deleteButton, updateButton;

        FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            feedbackIdText = itemView.findViewById(R.id.feedback_id_text);
            customerIdText = itemView.findViewById(R.id.customer_id_text);
            ratingText = itemView.findViewById(R.id.rating_text);
            commentText = itemView.findViewById(R.id.comment_text);
            dateText = itemView.findViewById(R.id.date_text);
            deleteButton = itemView.findViewById(R.id.delete_button);
            updateButton = itemView.findViewById(R.id.update_button);
        }
    }
}