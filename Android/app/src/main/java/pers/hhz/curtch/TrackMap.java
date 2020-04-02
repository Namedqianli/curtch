package pers.hhz.curtch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TrackMap extends Fragment {

    private MapView mapView;
    private BaiduMap baiduMap;
    private LocationClient locationClient;
    private double latitude;            //Latitude获取纬度信息
    private double longitude;           //Longitude获取经度信息
    private boolean isFirstLoc;         //第一次定位
    private boolean isFirstLocation;
    private Trace trace;                //轨迹
    private LBSTraceClient lbsTraceClient;
    private Thread thread;
    private Handler handler;
    private BufferedReader bufferedReader;
    private LatLng point1;
    private LatLng point2;
    private boolean threadFlag;
    private Thread threadCall;
    /**
     * 初始化监听器
     */
    private OnTraceListener onTraceListener = new OnTraceListener() {
        @Override
        public void onBindServiceCallback(int i, String s) {

        }

        /**
         * 开启服务回调
         * @param i
         * @param s
         */
        @Override
        public void onStartTraceCallback(int i, String s) {
            //开启采集
            lbsTraceClient.startGather(onTraceListener);
        }

        @Override
        public void onStopTraceCallback(int i, String s) {

        }

        @Override
        public void onStartGatherCallback(int i, String s) {

        }

        @Override
        public void onStopGatherCallback(int i, String s) {

        }

        @Override
        public void onPushCallback(byte b, PushMessage pushMessage) {

        }

        @Override
        public void onInitBOSCallback(int i, String s) {

        }
    };
    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                //取出消息
                String buffer = msg.obj.toString();
                Log.d("debug001", buffer);
                String result[] = buffer.split("#");
                switch (result[0]){
                    case "0":
                        if(isFirstLocation){
                            point2 = new LatLng(Double.valueOf(result[2]), Double.valueOf(result[1]));
                            MapStatus.Builder builder = new MapStatus.Builder()
                                    .target(point2) //地图缩放中心点
                                    .zoom(17f);
                            isFirstLocation = false;
                        }else {
                            point1 = point2;
                            point2 = new LatLng(Double.valueOf(result[2]), Double.valueOf(result[1]));
                            List<LatLng> points = new ArrayList<LatLng>();
                            points.add(point1);
                            points.add(point2);
                            //设置折线的属性
                            OverlayOptions mOverlayOptions = new PolylineOptions()
                                    .width(10)
                                    .color(0xAAFF0000)
                                    .points(points);
                            //绘制折线
                            Overlay mPolyline = baiduMap.addOverlay(mOverlayOptions);
                            mPolyline.setZIndex(3);
                        }
                        MyLocationData locData = new MyLocationData.Builder()
                                .latitude(point2.latitude)
                                .longitude(point2.longitude).build();
                        baiduMap.setMyLocationData(locData);
                        break;
                    case "1":
                        if(threadFlag == false){
                            threadFlag = true;
                            threadCall = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String number = ((MyApplication)getActivity().getApplication()).getSosNumber1();
                                    if(number != null){
                                        callPhone(number);
                                        try {
                                            Thread.sleep(10000);
                                            Log.d("sleep","sleep20");
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if(phoneIsInUse()){
                                        threadFlag = false;
                                        return;
                                    }
                                    number = ((MyApplication)getActivity().getApplication()).getSosNumber2();
                                    if(number != null){
                                        callPhone(number);
                                        try {
                                            Thread.sleep(10000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if(phoneIsInUse()){
                                        threadFlag = false;
                                        return;
                                    }
                                    number = ((MyApplication)getActivity().getApplication()).getSosNumber3();
                                    if(number != null){
                                        callPhone(number);
                                        try {
                                            Thread.sleep(10000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if(phoneIsInUse()){
                                        threadFlag = false;
                                        return;
                                    }
                                    number = ((MyApplication)getActivity().getApplication()).getSosNumber4();
                                    if(number != null){
                                        callPhone(number);
                                        try {
                                            Thread.sleep(10000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if(phoneIsInUse()){
                                        threadFlag = false;
                                        return;
                                    }
                                    number = ((MyApplication)getActivity().getApplication()).getSosNumber5();
                                    if(number != null){
                                        callPhone(number);
                                        try {
                                            Thread.sleep(10000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if(phoneIsInUse()){
                                        threadFlag = false;
                                        return;
                                    }
                                    threadFlag = false;
                                }
                            });
                            threadCall.start();
                        }
                        break;
                }
            }
        };
    }
    /**
     * 拨打电话（直接拨打电话）
     * @param phoneNum 电话号码
     */
    public void callPhone(String phoneNum){
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }
    /**
     * 是否正在电话通话中
     */
    private boolean phoneIsInUse() {
        TelephonyManager mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        int state = mTelephonyManager.getCallState();
        return state != TelephonyManager.CALL_STATE_IDLE;
    }
    /**
     * 初始化线程
     */
    private void initThread() throws IOException {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    String buffer = new String("");
                    //Log.d("debug001", "thread is running");
                    try {
                        if (bufferedReader.ready()) {
                            try {
                                buffer = "";
                                buffer = bufferedReader.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (buffer.length() != 0) {
                                Message message = handler.obtainMessage();
                                message.obj = buffer;
                                handler.sendMessage(message);
                            }
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
    /**
     * 定位事件监听
     */
    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //mapView 销毁后不再处理新接收的位置
            if(bdLocation == null || mapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            latitude = bdLocation.getLatitude();
            longitude = bdLocation.getLongitude();
            baiduMap.setMyLocationData(locData);
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            //第一次定位需要更新下地图显示状态
            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatus.Builder builder = new MapStatus.Builder()
                        .target(latLng) //地图缩放中心点
                        .zoom(17f);     //缩放倍数 百度地图支持缩放21级 部分特殊图层为20级
                //改变地图状态
                baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            baiduMap.setMyLocationData(locData);
            locationClient.stop();
            baiduMap.clear();
        }
    }

    /**
     * 检查并获取所需要的权限
     */
    protected void judgePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝

            // sd卡权限
            String[] SdCardPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(getContext(), SdCardPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(getActivity(), SdCardPermission, 100);
            }

            //手机状态权限
            String[] readPhoneStatePermission = {Manifest.permission.READ_PHONE_STATE};
            if (ContextCompat.checkSelfPermission(getContext(), readPhoneStatePermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(getActivity(), readPhoneStatePermission, 200);
            }

            //定位权限
            String[] locationPermission = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (ContextCompat.checkSelfPermission(getContext(), locationPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(getActivity(), locationPermission, 300);
            }

            String[] ACCESS_COARSE_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
            if (ContextCompat.checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(getActivity(), ACCESS_COARSE_LOCATION, 400);
            }

            String[] READ_EXTERNAL_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(getActivity(), READ_EXTERNAL_STORAGE, 500);
            }

            String[] WRITE_EXTERNAL_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(getActivity(), WRITE_EXTERNAL_STORAGE, 600);
            }

            String[] CALL_PHONE = {Manifest.permission.CALL_PHONE};
            if (ContextCompat.checkSelfPermission(getContext(), CALL_PHONE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(getActivity(), CALL_PHONE, 600);
            }
        }
    }

    /**
     * 初始化
     */
    private void init() throws IOException {
        threadFlag = false;
        //获取输入流
        Socket socket = ((MyApplication)getActivity().getApplication()).getSocket();
        InputStream ips;
        InputStreamReader ipsr;
        try {
            ips = socket.getInputStream();
            ipsr = new InputStreamReader(ips);
            bufferedReader = new BufferedReader(ipsr);
        }catch (IOException e){
            e.printStackTrace();
        }
        //初始化地图定位
        //检查申请权限
        judgePermission();
        //第一次定位
        isFirstLoc = true;
        mapView = (MapView)getActivity().findViewById(R.id.bmapView);
        //获取地图实例
        baiduMap = mapView.getMap();
        //开启定位图层
        baiduMap.setMyLocationEnabled(true);
        //初始化定位
        locationClient = new LocationClient(getContext());
        //定位方式参数设置
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setOpenGps(true);          //打开gps
        locationClientOption.setCoorType("bd0911");     //设置坐标类型
        locationClientOption.setScanSpan(1000);         //周期性请求定位，1秒返回一次位置
        locationClientOption.setNeedDeviceDirect(true); //获取方位
        locationClient.setLocOption(locationClientOption);
        //定位初始化
        MyLocationListener myLocationListener = new MyLocationListener();
        locationClient.registerLocationListener(myLocationListener);
        //开始定位
        locationClient.start();
        //初始化轨迹
        //初始化轨迹服务
        long serviceId = 218935;
        //设备标识
        String entityName = "HHZTrace";
        // 是否需要对象存储服务，默认为：false，关闭对象存储服务。注：鹰眼 Android SDK v3.0以上版本支持随轨迹上传图像等对象数据，若需使用此功能，该参数需设为 true，且需导入bos-android-sdk-1.0.2.jar。
        boolean isNeedObjectStorage = false;
        // 初始化轨迹服务
        trace = new Trace(serviceId, entityName, isNeedObjectStorage);
        // 初始化轨迹服务客户端
        lbsTraceClient = new LBSTraceClient(getActivity().getApplicationContext());
        //设置定位和回传周期
        //定位周期（单位：秒）
        int gatherInerval = 5;
        //打包回传周期（单位：秒）
        int packInterval = 10;
        //设置定位和打包周期
        lbsTraceClient.setInterval(gatherInerval, packInterval);
        //开启服务
        lbsTraceClient.startTrace(trace, onTraceListener);
        //开启采集
        lbsTraceClient.startGather(onTraceListener);
        initHandler();
        initThread();
        isFirstLocation = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_map, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        mapView = null;
    }
}
