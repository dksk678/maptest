package com.example.maptest.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FireSensor {
    @SerializedName("list")
    public List<Sensor> list;


    public class Sensor {
        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
