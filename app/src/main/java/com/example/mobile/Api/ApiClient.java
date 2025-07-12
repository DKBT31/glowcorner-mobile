package com.example.mobile.Api;

import com.example.mobile.Models.ProductResponse;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setLenient(); // Enable lenient parsing to handle malformed JSON
            gsonBuilder.registerTypeAdapter(ProductResponse.class, new ProductResponse.DataDeserializer());

            // Add logging interceptor for debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .build();
        }
        return retrofit;
    }
}