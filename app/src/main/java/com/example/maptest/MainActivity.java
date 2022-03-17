package com.example.maptest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.*;
/*
  맥주소 or uuid비교 후 major, minor 값 비교해서 지도의 해당되는 위치에 뿌려주기.
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,BeaconConsumer {
    private MapView mapView;

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource mLocationSource;
    private NaverMap naverMap;
    private BeaconManager beaconManager;
    private static final String TAG = "Beacontest";
    //    private Beacon beacon;
    TextView textView;
    private List<Beacon> beaconList = new ArrayList<>();
    private RssiCompare comp = new RssiCompare();
    private BeaconCoordinate bc;
    private LatLng cor;
    private Marker marker;
    private int beaconchk = 0;

    private Location location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        beaconManager = BeaconManager.getInstanceForApplication(this);
//        textView = (TextView) findViewById(R.id.tv_message);//비콘검색후 검색내용 뿌려주기위한 textview

        // ibeacon layout
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        //ALTBEACON      m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25

        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code와 권한획득 여부 확인
        if (mLocationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!mLocationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        // 지도 유형 위성사진으로 설정
//        System.out.println("맵 세팅");
//        naverMap = this.naverMap;
        //위치 세팅
        naverMap.setLocationSource(mLocationSource); //현재 위치 추적기능
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

//        naverMap.addOnLocationChangeListener(location ->
//                Toast.makeText(this,
//                        location.getLatitude() + ", " + location.getLongitude()+", " + location.getBearing(),
//                        Toast.LENGTH_SHORT).show());
        // 현재 위치 버튼 안보이게 설정
        naverMap.setMapType(NaverMap.MapType.Basic);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
    }

/*    private void setMarker(Marker marker, LatLng cor, int resourceID, int zIndex) {
        //원근감 표시
        marker.setIconPerspectiveEnabled(true);
        //아이콘 지정
        marker.setIcon(OverlayImage.fromResource(resourceID));
        //마커의 투명도
        marker.setAlpha(0.8f);
        //마커 위치
        marker.setPosition(cor);
        //마커 우선순위
        marker.setZIndex(zIndex);
        //마커 표시
    }*/

    public void onLocationChanged(Location location, LatLng cor) {
        if (naverMap == null)  { // || location == null
            Log.i(TAG, " return "+ naverMap + " "+ location);
            return;
        }
        Log.i(TAG, " "+cor +" "+beaconchk + " "+ location);
        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true); // 오버레이 활성화
        locationOverlay.setPosition(cor); // 현재 위치 조정
//        locationOverlay.setBearing(location.getBearing());

        naverMap.moveCamera(CameraUpdate.scrollTo(cor));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

//    @Override
//    public void onDenied(int i, String[] strings) {
//    }
//
//    @Override
//    public void onGranted(int i, String[] strings) {
//    }

    /*
    비콘 테스트용
     */
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    beaconchk = 5; // 비콘을 불러오면 5번까지 재탐색
                    //비콘 리스트에 비콘 정보 저장
                    for (Beacon beacon : beacons) {
                        beaconList.add(beacon);
                    }
                    //rssi값 오름차순으로 비콘리스트 정렬
                    Collections.sort(beaconList, comp);
                    //가장 짧은 거리의 비콘 정보 가져오기
                    bc = new BeaconCoordinate((beaconList.get(0).getId3().toInt()));
                    //비콘정보를 통해 좌표 수정
                    cor = bc.setCoordinate();
                } else { //비콘 못찾았으면 -1
                    beaconchk -= 1;
                }
                int as;
                if(beaconchk>=1){ //비콘값이 한개라도 나오면
                    //위치 변경
                    onLocationChanged(location, cor);
                    //비콘이 잡히면 트래킹모드 끔.
                    naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("beacon", null, null, null));
        } catch (RemoteException e) {}
    }
//    public void OnButtonClicked(View view){
//        // 아래에 있는 handleMessage를 부르는 함수. 맨 처음에는 0초간격이지만 한번 호출되고 나면
//        // 1초마다 불러온다.
//        handler.sendEmptyMessage(0);
//    }
//    Handler handler = new Handler() {
//        public void handleMessage(Message msg) {
//            textView.setText("");
//
//            // 비콘의 아이디와 거리를 측정하여 textView에 넣는다.
////            Arrays.sort(beaconList, (o1, o2) ->(o1.getC));
//            Collections.sort(beaconList, comp); //rssi 기준으로 정렬
//
////            for(Beacon beacon : beaconList){
//////                textView.append("adress : " + beacon.getBluetoothAddress() + " / " + "minor : " + beacon.getId3() + " / "+ "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
////            }
//
//            // 자기 자신을 1초마다 호출
//            handler.sendEmptyMessageDelayed(0, 500);//0.5초
//        }
//    };

}