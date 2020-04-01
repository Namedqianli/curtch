package pers.hhz.curtch;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager;

import java.net.Socket;

public class MyApplication extends Application {

    private Socket socket = null;
    private String sosNumber1;
    private String sosNumber2;
    private String sosNumber3;
    private String sosNumber4;
    private String sosNumber5;

    @Override
    public void onCreate() {
        super.onCreate();
        //QMUI初始化
        QMUISwipeBackActivityManager.init(this);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        //ToastUtils初始化
        ToastUtils.init(this);
    }

    public Socket getSocket(){
        return socket;
    }

    public void setSocket(Socket socket){
        this.socket = socket;
    }

    public String getSosNumber1() {
        return sosNumber1;
    }

    public void setSosNumber1(String sosNumber1) {
        this.sosNumber1 = sosNumber1;
    }

    public String getSosNumber2() {
        return sosNumber2;
    }

    public void setSosNumber2(String sosNumber2) {
        this.sosNumber2 = sosNumber2;
    }

    public String getSosNumber3() {
        return sosNumber3;
    }

    public void setSosNumber3(String sosNumber3) {
        this.sosNumber3 = sosNumber3;
    }

    public String getSosNumber4() {
        return sosNumber4;
    }

    public void setSosNumber4(String sosNumber4) {
        this.sosNumber4 = sosNumber4;
    }

    public String getSosNumber5() {
        return sosNumber5;
    }

    public void setSosNumber5(String sosNumber5) {
        this.sosNumber5 = sosNumber5;
    }
}
