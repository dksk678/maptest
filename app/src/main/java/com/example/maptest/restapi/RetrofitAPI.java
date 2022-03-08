package com.example.maptest.restapi;

import com.example.maptest.data.FireSensor;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitAPI {
    @GET("firelist")
    Call<FireSensor> getData();
}
