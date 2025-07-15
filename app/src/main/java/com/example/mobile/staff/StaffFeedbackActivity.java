package com.example.mobile.staff;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Feedback;
import com.example.mobile.Models.FeedbackResponse;
import com.example.mobile.Adapter.FeedbackAdapter;
import com.example.mobile.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;

public class StaffFeedbackActivity extends AppCompatActivity {
    private static final String TAG = "StaffFeedbackActivity";
    private RecyclerView recyclerView;
    private FeedbackAdapter feedbackAdapter;
    private List<Feedback> feedbackList = new ArrayList<>();
    private List<Feedback> fullFeedbackList = new ArrayList<>(); // Store full list for filtering
    private EditText filterInput;
    private Button filterButton;
    private Button resetButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.staff_feedback);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading layout", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.feedback_recycler_view);
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView not found in layout");
            Toast.makeText(this, "UI initialization error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackAdapter = new FeedbackAdapter(feedbackList, this::deleteFeedback, this::updateFeedback);
        recyclerView.setAdapter(feedbackAdapter);

        // Initialize filter input and buttons
        filterInput = findViewById(R.id.filter_input);
        filterButton = findViewById(R.id.filter_button);
        resetButton = findViewById(R.id.reset_button);
        if (filterInput == null || filterButton == null || resetButton == null) {
            Log.e(TAG, "Filter input or buttons not found in layout");
            Toast.makeText(this, "UI initialization error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set filter button click listener
        filterButton.setOnClickListener(v -> {
            String query = filterInput.getText().toString().trim();
            filterFeedbacks(query);
        });

        // Set reset button click listener
        resetButton.setOnClickListener(v -> {
            filterInput.setText("");
            feedbackList.clear();
            feedbackList.addAll(fullFeedbackList);
            feedbackAdapter.notifyDataSetChanged();
            Log.d(TAG, "Reset filter, restored full feedback list: " + feedbackList.size());
        });

        // Initialize ApiService using ApiClient
        try {
            apiService = ApiClient.getClient().create(ApiService.class);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ApiService: " + e.getMessage(), e);
            Toast.makeText(this, "Network setup error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Fetch feedbacks
        fetchFeedbacks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload full feedback list when activity resumes if filter is empty
        if (filterInput.getText().toString().trim().isEmpty()) {
            fetchFeedbacks();
        }
    }

    private void fetchFeedbacks() {
        Call<FeedbackResponse> call = apiService.getFeedbacks();
        call.enqueue(new Callback<FeedbackResponse>() {
            @Override
            public void onResponse(Call<FeedbackResponse> call, Response<FeedbackResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    feedbackList.clear();
                    fullFeedbackList.clear();
                    List<Feedback> data = response.body().getData();
                    if (data != null) {
                        feedbackList.addAll(data);
                        fullFeedbackList.addAll(data); // Store full list for filtering
                        feedbackAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Feedbacks loaded: " + feedbackList.size());
                    } else {
                        Log.w(TAG, "Feedback data is null");
                        Toast.makeText(StaffFeedbackActivity.this, "No feedback data received", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Failed to load feedbacks, response code: " + response.code() + ", message: " + response.message());
                    ResponseBody errorBody = response.errorBody();
                    if (errorBody != null) {
                        try {
                            Log.w(TAG, "Error body: " + errorBody.string());
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                    Toast.makeText(StaffFeedbackActivity.this, "Failed to load feedbacks", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FeedbackResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching feedbacks: " + t.getMessage(), t);
                Toast.makeText(StaffFeedbackActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterFeedbacks(String query) {
        if (query.isEmpty()) {
            feedbackList.clear();
            feedbackList.addAll(fullFeedbackList);
            feedbackAdapter.notifyDataSetChanged();
            Log.d(TAG, "Filter cleared, restored full feedback list: " + feedbackList.size());
            return;
        }

        List<Feedback> filteredList = new ArrayList<>();
        if (query.matches("F\\d+")) { // Filter by feedbackID
            for (Feedback feedback : fullFeedbackList) {
                if (feedback.getFeedbackID().equals(query)) {
                    filteredList.add(feedback);
                }
            }
            Log.d(TAG, "Filtered by feedbackID: " + query + ", results: " + filteredList.size());
        } else if (query.matches("\\d+")) { // Filter by customerID
            for (Feedback feedback : fullFeedbackList) {
                if (feedback.getCustomerID().equals(query)) {
                    filteredList.add(feedback);
                }
            }
            Log.d(TAG, "Filtered by customerID: " + query + ", results: " + filteredList.size());
        } else {
            Toast.makeText(this, "Invalid filter format", Toast.LENGTH_SHORT).show();
            return;
        }

        feedbackList.clear();
        feedbackList.addAll(filteredList);
        feedbackAdapter.notifyDataSetChanged();
    }

    private void deleteFeedback(String feedbackID) {
        Call<Void> call = apiService.deleteFeedback(feedbackID);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    feedbackList.removeIf(f -> f.getFeedbackID().equals(feedbackID));
                    fullFeedbackList.removeIf(f -> f.getFeedbackID().equals(feedbackID));
                    feedbackAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Feedback deleted: " + feedbackID);
                    Toast.makeText(StaffFeedbackActivity.this, "Feedback deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "Failed to delete feedback, response code: " + response.code());
                    ResponseBody errorBody = response.errorBody();
                    if (errorBody != null) {
                        try {
                            Log.w(TAG, "Error body: " + errorBody.string());
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                    Toast.makeText(StaffFeedbackActivity.this, "Failed to delete feedback", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error deleting feedback: " + t.getMessage(), t);
                Toast.makeText(StaffFeedbackActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFeedback(String feedbackID) {
        // Placeholder for update logic
        Log.d(TAG, "Update feedback clicked: " + feedbackID);
        Toast.makeText(this, "Update feedback: " + feedbackID, Toast.LENGTH_SHORT).show();
    }
}