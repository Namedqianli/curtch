package pers.hhz.curtch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomNavigationView bottomNavigationView;  //底部导航栏
    private ImageView imageViewTrackMap;    //导航栏：地图
    private ImageView imageViewSetAndInfo;  //导航栏：设置
    private Fragment[] fragments;       //页面
    private Fragment fragmentTrackMap;  //地图页面
    private Fragment fragmentSetInfo;   //设置和信息页
    private int lastFragment;           //保存上一个页面id

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_view, fragmentTrackMap).show(fragmentTrackMap).commit();
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
        }
    }
}
