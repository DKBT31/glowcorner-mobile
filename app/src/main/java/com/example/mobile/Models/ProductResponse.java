package com.example.mobile.Models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class ProductResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("description")
    private String description;

    @SerializedName("data")
    private List<Product> data;

    @SerializedName("role")
    private String role;

    @SerializedName("redirectUrl")
    private String redirectUrl;

    @SerializedName("success")
    private boolean success;

    // Getters
    public int getStatus() { return status; }
    public String getDescription() { return description; }
    public List<Product> getData() { return data; }
    public String getRole() { return role; }
    public String getRedirectUrl() { return redirectUrl; }
    public boolean isSuccess() { return success; }

    // Custom deserializer for the data field
    public static class DataDeserializer implements JsonDeserializer<ProductResponse> {
        @Override
        public ProductResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ProductResponse response = new ProductResponse();
            JsonElement statusElement = json.getAsJsonObject().get("status");
            if (statusElement != null) {
                response.status = statusElement.getAsInt();
            }
            JsonElement descriptionElement = json.getAsJsonObject().get("description");
            if (descriptionElement != null) {
                response.description = descriptionElement.getAsString();
            }
            JsonElement roleElement = json.getAsJsonObject().get("role");
            if (roleElement != null && !roleElement.isJsonNull()) {
                response.role = roleElement.getAsString();
            }
            JsonElement redirectUrlElement = json.getAsJsonObject().get("redirectUrl");
            if (redirectUrlElement != null && !redirectUrlElement.isJsonNull()) {
                response.redirectUrl = redirectUrlElement.getAsString();
            }
            JsonElement successElement = json.getAsJsonObject().get("success");
            if (successElement != null) {
                response.success = successElement.getAsBoolean();
            }

            JsonElement dataElement = json.getAsJsonObject().get("data");
            if (dataElement != null) {
                if (dataElement.isJsonArray()) {
                    response.data = context.deserialize(dataElement, new com.google.gson.reflect.TypeToken<List<Product>>(){}.getType());
                } else if (dataElement.isJsonObject()) {
                    Product product = context.deserialize(dataElement, Product.class);
                    response.data = Collections.singletonList(product);
                }
            }
            return response;
        }
    }
}