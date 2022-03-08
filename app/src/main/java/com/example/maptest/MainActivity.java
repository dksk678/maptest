package com.example.maptest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private FusedLocationSource mLocationSource;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private NaverMap mNaverMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        naverMapBasicSettings();
    }

    public void naverMapBasicSettings() {
        mapView.getMapAsync(this);
    }
    //맵 설정(유형, 오버레이, 버튼)
    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {

        mNaverMap = naverMap;

        // 현재 위치 버튼 안보이게 설정
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap.setLocationSource(mLocationSource);

        // 권한확인. 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        // 지도 유형 설정
        naverMap.setMapType(NaverMap.MapType.Basic);
        List<LatLng>[] latlist = getJson("yongin_dongwon_royal.json");

        for(int i=0; i<latlist.length; i++){
            PolygonOverlay polygonOverlay = new PolygonOverlay();

            polygonOverlay.setCoords(latlist[i]);
            polygonOverlay.setOutlineWidth(2);
            polygonOverlay.setMap(naverMap);

        }
        latlist = getJson("yongin_dongwon_royal_hole.json");
        for(int i=0; i<latlist.length; i++){
            PolygonOverlay polygonOverlay = new PolygonOverlay();

            polygonOverlay.setHoles(Collections.singletonList(latlist[i]));
        }
    }
    //GPS 현재위치
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // request code와 권한획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }

    public List<LatLng>[] getJson(String filename){
        AssetManager assetManager = getAssets();
        List<LatLng>[] list = new List[0];
        try{
            InputStream is = assetManager.open(filename);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);

            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while(line != null){
                buffer.append(line + "\n");
                line=reader.readLine();
            }
            String jsonData = buffer.toString();


            JSONArray jsonArray = new JSONArray(jsonData);
            list = new ArrayList[jsonArray.length()];

            for(int i=0; i<jsonArray.length(); i++){
                list[i] = new ArrayList<>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject geometry = (JSONObject) jsonObject.get("geometry");
                JSONArray jsonArray1 = geometry.getJSONArray("coordinates");

                for(int j=0; j<jsonArray1.getJSONArray(0).length(); j++){
                    String str = jsonArray1.getJSONArray(0).get(j).toString();
                    String str2 = str.substring(1, str.length()-2);
                    String[] strarr = str2.split(",");
                    list[i].add(new LatLng(Double.parseDouble(strarr[1]),Double.parseDouble(strarr[0])));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}