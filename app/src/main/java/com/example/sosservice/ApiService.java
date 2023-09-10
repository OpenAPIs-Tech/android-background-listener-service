package com.example.sosservice;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("v1/sample_end_point")
    Call<SampleModel> getApiResponse(@Query("data")  String data);
}
