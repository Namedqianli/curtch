package pers.hhz.curtch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.media.Image;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomNavigationView bottomNavigationView;  //底部导航栏
    private ImageView imageViewTrackMap;    //导航栏：地图
    private ImageView imageViewSetAndInfo;  //导航栏：设置
    private Fragment[] fragments;       //页面
    private Fragment fragmentTrackMap;  //地图页面
    private Fragment fragmentSetInfo;   //设置和信息页
    private int lastFragment;           //保存上一个页面id

    private EditText editTextIp;
    private EditText editTextPort;
    private Socket socket;
    private Toast toast;

    private MKOfflineMap mkOfflineMap;

    /**
     * 初始化连接页面
     */
    private void initConnect() {
        editTextIp = findViewById(R.id.ipAdress);
        editTextPort = findViewById(R.id.port);
    }
    /**
     * 连接
     */
    private void connection(){
        String host;
        int port = 0;
        boolean flag = true;
        //如果IP地址或端口号为空
        if(editTextPort.getEditableText().toString().length() == 0 || editTextIp.getText().toString().length() == 0){

            toast.makeText(this, "请输入ip地址或端口号", toast.LENGTH_LONG).show();  //显示提示
            return;
        }
        host = editTextIp.getText().toString();
        port = Integer.parseInt(editTextPort.getText().toString());
        String s = String.valueOf(port);
        //toast.makeText(this, "成功连接", toast.LENGTH_LONG).show();     //成功连接
        try {
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, 200);     //设置连接时间，超时则抛出异常
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (SocketTimeoutException e){
            e.printStackTrace();
        }
        catch (ConnectException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(!socket.isConnected()){
                flag = false;
                toast.makeText(this, "连接失败,请重试", toast.LENGTH_LONG).show();
            }
        }
        if(flag){
            ((MyApplication)getApplication()).setSocket(socket);
            setContentView(R.layout.activity_main);
            init();
        }
    }
    /**
     * 初始化各个页面
     */
    private void initFragment() {
        fragmentTrackMap = new TrackMap();
        fragmentSetInfo = new SetAndInfo();
        fragments = new Fragment[]{fragmentTrackMap, fragmentSetInfo};
        lastFragment = 0;
    }
    /**
     * 页面的切换
     * @param lastFragment
     * @param index
     */
    private void switchFragment(int lastFragment, int index){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //隐藏上一个页面
        transaction.hide(fragments[lastFragment]);
        if(fragments[index].isAdded() == false){
            transaction.add(R.id.main_view, fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }

    /**
     * 初始化
     */
    private void init(){
        //导航栏设置
        bottomNavigationView = findViewById(R.id.navigation_view);
        LayoutInflater.from(MainActivity.this).inflate(R.layout.navigation_view, bottomNavigationView, true);
        bottomNavigationView.setVisibility(View.VISIBLE);
        //切换监听
        imageViewTrackMap = findViewById(R.id.radius_image_view_TrackMap);
        imageViewSetAndInfo = findViewById(R.id.radius_image_view_set_info);
        imageViewTrackMap.setOnClickListener(this);
        imageViewSetAndInfo.setOnClickListener(this);
        initFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_view, fragmentTrackMap).show(fragmentTrackMap).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);
        //使sockt能在主线程创建连接
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                .build());
        initConnect();
        findViewById(R.id.connect).setOnClickListener(this);
        //下载离线地图
        mkOfflineMap = new MKOfflineMap();
        mkOfflineMap.init(new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int i, int i1) {
                switch (i) {
                    case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                        MKOLUpdateElement update = mkOfflineMap.getUpdateInfo(i1);
                        // 处理下载进度更新提示
                        if (update != null) {
                            Log.d("download", String.format("%s : %d%%", update.cityName,
                                    update.ratio));
                        }
                    }
                    break;
                    case MKOfflineMap.TYPE_NEW_OFFLINE:
                        // 有新离线地图安装
                        Log.d("OfflineDemo", String.format("add offlinemap num:%d", i1));
                        break;
                    case MKOfflineMap.TYPE_VER_UPDATE:
                        // 版本更新提示
                        // MKOLUpdateElement e = mOffline.getUpdateInfo(state);
                        break;
                    default:
                        break;
                }
            }
        });
        int cityId = 0;
        ArrayList<MKOLSearchRecord> records = mkOfflineMap.searchCity("桂林市");
        if (records != null && records.size() == 1) {
            cityId = records.get(0).cityID;
        }
        mkOfflineMap.start(cityId);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.radius_image_view_TrackMap:

                if(lastFragment != 0){
                    switchFragment(lastFragment, 0);
                    lastFragment = 0;
                }
                break;
            case R.id.radius_image_view_set_info:
                if(lastFragment != 1){
                    switchFragment(lastFragment, 1);
                    lastFragment = 1;
                }
                break;
            case R.id.connect:
                connection();
                break;
        }
    }
}
