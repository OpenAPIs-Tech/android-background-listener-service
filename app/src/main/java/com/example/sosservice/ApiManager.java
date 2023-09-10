package com.example.sosservice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    private static ApiManager sInstance = null;
    private static final String BASE_URL = "https://api.example.com/"; // Replace with your API base URL
    private ApiService apiService;
    private BlockingQueue<String> dataQueue = new LinkedBlockingQueue<>();


    private ApiManager() {
        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
            sendDataFromQueueToServer();
        }
    }

    public static synchronized ApiManager getInstance() {
        if (sInstance == null) {
            sInstance = new ApiManager();
        }

        return sInstance;
    }

    public void sendDataToServer(String data) {
        try {
            if ( data != null && !data.isEmpty()) {
                dataQueue.put(data); // Add data to the queue
            }
        } catch (InterruptedException e) {
            // Handle the exception, if necessary
        }
    }

    private void sendDataFromQueueToServer() {
        // Create a background thread or use a timer to periodically check the queue
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) { // You can add a condition to stop this loop when needed
                    try {
                        String data = dataQueue.take(); // Get data from the queue (blocks if the queue is empty)

                        // Now you can make the API call using the data
                        Call<SampleModel> call = apiService.getApiResponse(data);

                        call.enqueue(new Callback<SampleModel>() {
                            @Override
                            public void onResponse(Call<SampleModel> call, Response<SampleModel> response) {
                                if (response.isSuccessful()) {
                                    SampleModel responseData = response.body();
                                    // Handle the API response data here
                                } else {
                                    // Handle the API error
                                }
                            }

                            @Override
                            public void onFailure(Call<SampleModel> call, Throwable t) {
                                // Handle network or other errors
                            }
                        });
                    } catch (InterruptedException e) {
                        // Handle the exception, if necessary
                    }
                }
            }
        }).start();
    }
}
