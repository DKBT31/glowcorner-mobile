package com.example.mobile.Api;

import com.example.mobile.Models.CartResponse;
import com.example.mobile.Models.LoginRequest;
import com.example.mobile.Models.OrderResponse;
import com.example.mobile.Models.ProductResponse;
import com.example.mobile.Models.Promotion;
import com.example.mobile.Models.RegisterRequest;
import com.example.mobile.Models.RoutineResponse;
import com.example.mobile.Models.User;
import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
        @POST("auth/login")
        Call<ApiResponse> login(@Body LoginRequest loginRequest);

        @POST("auth/register")
        Call<ResponseBody> registerSimple(@Body RegisterRequest registerRequest);

        // --------------------------------------------------------------------------------------------------
        // Get products
        @GET("api/products")
        Call<ProductResponse> getProducts();

        @DELETE("api/products/{productID}")
        Call<Void> deleteProduct(@Path("productID") String productID);

        @POST("api/products")
        Call<ProductResponse> createProduct(@Body JsonObject product, @Body MultipartBody.Part image);

        @Multipart
        @PUT("api/products/{id}")
        Call<ProductResponse> updateProduct(
                        @Path("id") String id,
                        @Part("product") JsonObject productData,
                        @Part MultipartBody.Part image);

        @GET("api/customer/products/skinType/{skinType}")
        Call<ProductResponse> getProductsBySkinType(@Path("skinType") String skinType);

        @GET("api/customer/products/category/{category}")
        Call<ProductResponse> getProductsByCategory(@Path("category") String category);

        @GET("api/customer/products/{productID}")
        Call<ProductResponse> getProductById(@Path("productID") String productID);

        @GET("api/customer/products/name/{productName}")
        Call<ProductResponse> getProductsByName(@Path("productName") String productName);

        // --------------------------------------------------------------------------------------------------
        // Add to cart
        @POST("api/cart/{userID}/add/{productID}")
        Call<ResponseBody> addToCart(@Path("userID") String userID, @Path("productID") String productID,
                        @Query("quantity") int quantity);

        @DELETE("api/cart/{userId}/remove/{productId}")
        Call<ResponseBody> removeFromCart(@Path("userId") String userId, @Path("productId") String productId);

        // Show cart
        @GET("api/cart/{userID}") // Adjust endpoint as needed
        Call<CartResponse> getCart(@Path("userID") String userID);

        // --------------------------------------------------------------------------------------------------
        // Get user profile
        @GET("api/user/{userID}")
        Call<ResponseBody> getUserProfile(@Path("userID") String userID);

        // Update user profile
        @Multipart
        @PUT("api/user/{userID}")
        Call<ResponseBody> updateUserProfile(
                        @Path("userID") String userID,
                        @Part("fullName") String fullName,
                        @Part("email") String email,
                        @Part("phone") String phone,
                        @Part("address") String address,
                        @Part("skinType") String skinType,
                        @Part MultipartBody.Part image);

        // All user
        @GET("api/manager/users")
        Call<List<User>> getManagerUsers(); // Changed to return List<User>

        @DELETE("api/manager/users/{userId}")
        Call<Void> deleteManagerUser(@Path("userId") String userId);

        // --------------------------------------------------------------------------------------------------
        @GET("api/orders/staff")
        Call<ResponseBody> getOrders(@Header("Authorization") String authHeader);

        @POST("/api/orders/customer/{userID}/create")
        Call<OrderResponse> createOrder(@Header("Authorization") String token, @Path("userID") String userID,
                        @Body JsonObject orderData);

        // Get orders by user ID
        @GET("api/orders/customer/{userID}")
        Call<ResponseBody> getCustomerOrders(@Header("Authorization") String token, @Path("userID") String userID);

        @GET("api/orders/staff/{orderId}")
        Call<ResponseBody> getOrderDetails(@Path("orderId") String orderId);

        @PUT("api/orders/staff/{orderId}")
        Call<ResponseBody> updateOrderStatus(@Header("Authorization") String authHeader,
                        @Path("orderId") String orderId, @Body RequestBody status);

        @DELETE("api/orders/{orderId}")
        Call<ResponseBody> deleteOrder(@Header("Authorization") String authHeader, @Path("orderId") String orderId);

        // --------------------------------------------------------------------------------------------------
        // Get Quizzes
        @GET("api/quizzes")
        Call<ResponseBody> getQuizzes();

        // Get Skintype by Quiz ID
        @GET("api/skin-care-routines")
        Call<ResponseBody> getSkinCareRoutines();

        @GET("api/skin-care-routines/{routineId}")
        Call<RoutineResponse> getRoutineById(@Path("routineId") String routineId);

        @POST("api/skin-care-routines/{routineId}/apply-to-user/{userId}")
        Call<RoutineResponse> applyRoutineToUser(
                        @Path("routineId") String routineId,
                        @Path("userId") String userId);

        // --------------------------------------------------------------------------------------------------
        // Promotions
        @GET("api/promotions")
        Call<ResponseBody> getPromotions(@Header("Authorization") String authHeader);

        @GET("api/promotions/search")
        Call<ResponseBody> searchPromotions(@Header("Authorization") String authHeader, @Query("name") String name,
                        @Query("description") String description);

        @GET("api/promotions/{promotionID}")
        Call<ResponseBody> getPromotionById(@Header("Authorization") String authHeader,
                        @Path("promotionID") String promotionID);

        @GET("api/promotions/active/product")
        Call<ResponseBody> getActivePromotionsByProduct(@Header("Authorization") String authHeader,
                        @Query("productID") String productID);

        @PUT("api/promotions/{promotionID}")
        Call<ResponseBody> updatePromotion(@Header("Authorization") String authHeader,
                        @Path("promotionID") String promotionID, @Body Promotion promotion);

        @DELETE("api/promotions/{promotionID}")
        Call<ResponseBody> deletePromotion(@Header("Authorization") String authToken,
                        @Path("promotionID") String promotionId);

        // --------------------------------------------------------------------------------------------------
        // Stripe Payment API
        @POST("api/payment/stripe/create-intent")
        Call<ResponseBody> createPaymentIntent(@Body JsonObject paymentRequest);
}