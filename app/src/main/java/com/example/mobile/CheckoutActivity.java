package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";
    
    private Button payButton;
    private LinearLayout processingLayout;
    private TextView displayTotalTextView;
    private EditText cardNumberInput, expiryInput, cvcInput;
    
    private String clientSecret;
    private double totalAmount;
    private double discountedAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "=== CheckoutActivity onCreate START ===");
        
        try {
            Log.d(TAG, "Setting content view...");
            setContentView(R.layout.activity_checkout);
            Log.d(TAG, "✓ Content view set successfully");

            // Initialize views
            Log.d(TAG, "Finding views...");
            payButton = findViewById(R.id.payButton);
            processingLayout = findViewById(R.id.processing_layout);
            displayTotalTextView = findViewById(R.id.display_total_amount);
            cardNumberInput = findViewById(R.id.card_number_input);
            expiryInput = findViewById(R.id.expiry_input);
            cvcInput = findViewById(R.id.cvc_input);
            
            Log.d(TAG, "Views found - PayButton: " + (payButton != null) + 
                       ", ProcessingLayout: " + (processingLayout != null) + 
                       ", DisplayTotal: " + (displayTotalTextView != null) +
                       ", CardNumber: " + (cardNumberInput != null) +
                       ", Expiry: " + (expiryInput != null) +
                       ", CVC: " + (cvcInput != null));

            if (payButton == null) {
                Log.e(TAG, "❌ PayButton is null!");
                Toast.makeText(this, "Layout error: Pay button not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            if (displayTotalTextView == null) {
                Log.e(TAG, "❌ DisplayTotalTextView is null!");
                Toast.makeText(this, "Layout error: Total amount view not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Retrieve data from intent
            Log.d(TAG, "Getting intent data...");
            Intent intent = getIntent();
            if (intent == null) {
                Log.e(TAG, "❌ Intent is null!");
                Toast.makeText(this, "No payment data received", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            clientSecret = intent.getStringExtra("clientSecret");
            totalAmount = intent.getDoubleExtra("totalAmount", 0.0);
            discountedAmount = intent.getDoubleExtra("discountedAmount", 0.0);

            Log.d(TAG, "Intent data - ClientSecret: " + (clientSecret != null ? "✓ Present" : "❌ NULL"));
            Log.d(TAG, "Intent data - Total: $" + totalAmount + ", Discounted: $" + discountedAmount);

            // Display the order total
            double displayAmount = discountedAmount > 0 ? discountedAmount : totalAmount;
            displayTotalTextView.setText("$" + String.format("%.2f", displayAmount));
            Log.d(TAG, "✓ Display amount set: $" + String.format("%.2f", displayAmount));

            // Validate amounts
            if (totalAmount <= 0) {
                Log.e(TAG, "❌ Invalid total amount: " + totalAmount);
                Toast.makeText(this, "Invalid order amount: $" + totalAmount, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Set up payment button
            payButton.setOnClickListener(v -> {
                Log.d(TAG, "💳 Pay button clicked");
                handlePayment();
            });
            
            // Set up card input formatting
            setupCardInputFormatting();
            
            Log.d(TAG, "✅ CheckoutActivity setup completed successfully");
            Toast.makeText(this, "Checkout loaded successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "❌ CRITICAL ERROR in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Critical error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
        
        Log.d(TAG, "=== CheckoutActivity onCreate END ===");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "=== CheckoutActivity onResume ===");
        try {
            // Verify all views are still valid
            if (payButton == null || displayTotalTextView == null) {
                Log.e(TAG, "❌ Views became null in onResume!");
                finish();
                return;
            }
            Log.d(TAG, "✓ onResume completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR in onResume: " + e.getMessage(), e);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "=== CheckoutActivity onPause ===");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "=== CheckoutActivity onDestroy ===");
    }

    private void handlePayment() {
        try {
            Log.d(TAG, "=== PAYMENT VALIDATION START ===");
            
            // Validate card inputs
            String cardNumber = cardNumberInput != null ? cardNumberInput.getText().toString().trim().replaceAll(" ", "") : "";
            String expiry = expiryInput != null ? expiryInput.getText().toString().trim() : "";
            String cvc = cvcInput != null ? cvcInput.getText().toString().trim() : "";
            
            Log.d(TAG, "Card inputs - Number: " + (cardNumber.length() > 0 ? "✓" : "❌") + 
                       ", Expiry: " + (expiry.length() > 0 ? "✓" : "❌") + 
                       ", CVC: " + (cvc.length() > 0 ? "✓" : "❌"));
            
            // Simple validation
            if (cardNumber.length() < 13) {
                Toast.makeText(this, "Please enter a valid card number (13-16 digits)", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!expiry.matches("\\d{2}/\\d{2}")) {
                Toast.makeText(this, "Please enter a valid expiry date (MM/YY)", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (cvc.length() < 3) {
                Toast.makeText(this, "Please enter a valid CVC (3-4 digits)", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d(TAG, "✅ All card inputs validated successfully");
            
            // Show payment confirmation dialog
            showPaymentConfirmation();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in handlePayment: " + e.getMessage(), e);
            Toast.makeText(this, "Payment validation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void resetPaymentButton() {
        payButton.setEnabled(true);
        payButton.setText("Pay Now");
        if (processingLayout != null) {
            processingLayout.setVisibility(View.GONE);
        }
    }

    private void updateOrderStatusToCompleted() {
        Log.d(TAG, "=== ORDER STATUS UPDATE START ===");
        
        String userID = SignInActivity.getStoredValue(this, "userID");
        String jwtToken = SignInActivity.getStoredValue(this, "jwtToken");

        // Get the orderID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String orderID = prefs.getString("orderID", null);

        Log.d(TAG, "Order update - UserID: " + (userID != null ? "✓" : "❌") + 
                   ", Token: " + (jwtToken != null ? "✓" : "❌") + 
                   ", OrderID: " + (orderID != null ? orderID : "❌ NULL"));

        if (orderID == null || orderID.isEmpty()) {
            Log.e(TAG, "❌ No orderID found in SharedPreferences");
            showSuccessPageDirectly();
            return;
        }

        if (jwtToken == null || jwtToken.isEmpty()) {
            Log.e(TAG, "❌ No JWT token found");
            showSuccessPageDirectly();
            return;
        }

        // Call API to update order status - Use correct endpoint with api/ prefix
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        
        // Add Authorization header
        String authHeader = jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken;
        
        Log.d(TAG, "Making API call to update order " + orderID + " to COMPLETED status");
        Log.d(TAG, "🔑 Auth header: " + (authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader));
        Log.d(TAG, "🌐 API endpoint: PUT /api/orders/staff/" + orderID);
        
        // Create JSON body for status update with proper content type
        String statusJson = "{\"status\":\"COMPLETED\"}";
        RequestBody statusBody = RequestBody.create(statusJson, okhttp3.MediaType.get("application/json; charset=utf-8"));
        Call<ResponseBody> call = apiService.updateOrderStatus(authHeader, orderID, statusBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Order status update response: " + response.code());
                
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Order status updated successfully to COMPLETED");
                    
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "No body";
                        Log.d(TAG, "Response body: " + responseBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response body: " + e.getMessage());
                    }
                    
                    runOnUiThread(() -> {
                        Toast.makeText(CheckoutActivity.this, "✅ Order completed successfully!", Toast.LENGTH_SHORT).show();
                        showSuccessPageDirectly();
                    });
                } else {
                    Log.e(TAG, "⚠️ Failed to update order status: " + response.code());
                    Log.e(TAG, "⚠️ Response message: " + response.message());
                    
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Error response: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    
                    runOnUiThread(() -> {
                        Toast.makeText(CheckoutActivity.this, "⚠️ Order status update failed (payment still successful)", Toast.LENGTH_LONG).show();
                        showSuccessPageDirectly();
                    });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Error updating order status: " + t.getMessage(), t);
                runOnUiThread(() -> {
                    Toast.makeText(CheckoutActivity.this, "❌ Status update failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    showSuccessPageDirectly();
                });
            }
        });
    }

    private void goToSuccessPage() {
        Log.d(TAG, "🎉 Navigating to success page");
        Intent successIntent = new Intent(CheckoutActivity.this, SuccessActivity.class);
        successIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(successIntent);
        finish();
    }

    private void setupCardInputFormatting() {
        // Card number formatting (add spaces every 4 digits)
        if (cardNumberInput != null) {
            cardNumberInput.addTextChangedListener(new TextWatcher() {
                private boolean isFormatting = false;
                
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (isFormatting) return;
                    
                    isFormatting = true;
                    String input = s.toString().replaceAll(" ", "");
                    
                    if (input.length() > 16) {
                        input = input.substring(0, 16);
                    }
                    
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < input.length(); i++) {
                        if (i > 0 && i % 4 == 0) {
                            formatted.append(" ");
                        }
                        formatted.append(input.charAt(i));
                    }
                    
                    cardNumberInput.setText(formatted.toString());
                    cardNumberInput.setSelection(formatted.length());
                    isFormatting = false;
                }
            });
        }
        
        // Expiry date formatting (add slash after MM)
        if (expiryInput != null) {
            expiryInput.addTextChangedListener(new TextWatcher() {
                private boolean isFormatting = false;
                
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (isFormatting) return;
                    
                    isFormatting = true;
                    String input = s.toString().replaceAll("/", "");
                    
                    if (input.length() > 4) {
                        input = input.substring(0, 4);
                    }
                    
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < input.length(); i++) {
                        if (i == 2) {
                            formatted.append("/");
                        }
                        formatted.append(input.charAt(i));
                    }
                    
                    expiryInput.setText(formatted.toString());
                    expiryInput.setSelection(formatted.length());
                    isFormatting = false;
                }
            });
        }
    }

    private void showPaymentConfirmation() {
        double displayAmount = discountedAmount > 0 ? discountedAmount : totalAmount;
        
        new AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage("Are you sure you want to proceed with payment of $" + String.format("%.2f", displayAmount) + "?")
            .setPositiveButton("Yes, Pay Now", (dialog, which) -> {
                dialog.dismiss();
                processPayment();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
                resetPaymentButton();
            })
            .setCancelable(false)
            .show();
    }

    private void processPayment() {
        // Simulate payment processing
        Log.d(TAG, "🔄 Simulating payment processing...");
        payButton.postDelayed(() -> {
            Log.d(TAG, "✅ Payment simulation completed");
            showPaymentSuccess();
        }, 3000);
    }

    private void showPaymentSuccess() {
        try {
            Log.d(TAG, "=== SHOWING PAYMENT SUCCESS DIALOG ===");
            
            // Hide processing layout first
            if (processingLayout != null) {
                processingLayout.setVisibility(View.GONE);
            }
            
            double displayAmount = discountedAmount > 0 ? discountedAmount : totalAmount;
            
            // Save the actual paid amount for SuccessActivity
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("paidAmount", (float) displayAmount);
            editor.putFloat("originalAmount", (float) totalAmount);
            editor.putFloat("discountedAmount", (float) discountedAmount);
            editor.apply();
            
            Log.d(TAG, "💾 Saved payment amounts - Paid: $" + displayAmount + 
                      ", Original: $" + totalAmount + ", Discounted: $" + discountedAmount);
            
            // Create professional success dialog that looks polished
            AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
            
            String successMessage = "🎉 Payment Successful!\n\n" +
                    "💰 Amount Paid: $" + String.format("%.2f", displayAmount) + "\n" +
                    "💳 Payment Method: Card\n" +
                    "📅 Date: " + new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date()) + "\n\n" +
                    "✅ Your order has been confirmed and is being processed.\n\n" +
                    "Thank you for shopping with GlowCorner!";
            
            AlertDialog successDialog = builder
                .setTitle("Order Confirmed ✓")
                .setMessage(successMessage)
                .setPositiveButton("Continue Shopping 🛍️", (dialog, which) -> {
                    Log.d(TAG, "User clicked Continue Shopping - updating order status and going home");
                    dialog.dismiss();
                    updateOrderStatusToCompleted();
                    // Small delay to ensure status update starts
                    payButton.postDelayed(() -> goToHomePage(), 800);
                })
                .setNegativeButton("View Success Page 📄", (dialog, which) -> {
                    Log.d(TAG, "User clicked View Success Page - updating order status and showing success");
                    dialog.dismiss();
                    updateOrderStatusToCompleted();
                    // Small delay to ensure status update starts
                    payButton.postDelayed(() -> showSuccessPageDirectly(), 800);
                })
                .setCancelable(false)
                .create();
                
            // Show dialog and customize button colors
            successDialog.show();
            
            // Make the dialog look more professional
            successDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
            successDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_blue_dark, null));
            
            Log.d(TAG, "✅ Professional success dialog displayed and waiting for user interaction");
                
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing payment success: " + e.getMessage(), e);
            // Fallback - update status and go directly to success page
            updateOrderStatusToCompleted();
            payButton.postDelayed(() -> showSuccessPageDirectly(), 1000);
        }
    }
    
    private void showSuccessPageDirectly() {
        Log.d(TAG, "🎉 Navigating directly to success page");
        Intent successIntent = new Intent(CheckoutActivity.this, SuccessActivity.class);
        successIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(successIntent);
        finish();
    }
    
    private void goToHomePage() {
        Log.d(TAG, "🏠 Navigating to home page");
        Intent homeIntent = new Intent(CheckoutActivity.this, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }
}
