package com.heaven.wing.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.heaven.wing.R;
import com.heaven.wing.adapter.PathAdapter;
import com.heaven.wing.entity.Path;
import com.heaven.wing.entity.PathRecord;
import com.heaven.wing.entity.Trace;
import com.heaven.wing.util.ParseUtil;
import com.heaven.wing.util.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

//主界面
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView startBt;

    private RecyclerView recyclerView;
    private PathAdapter myAdapter;
    private List<Path> cardList = new ArrayList<>();
    //从bmob数据库取出的当前用户的历史轨迹信息列表
    private List<PathRecord> mRecordList = new ArrayList<PathRecord>();

    //得到当前用户的id
    private final static String USER_ID = BmobUser.getCurrentUser().getObjectId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getLayoutInflater().inflate(R.layout.app_start_new_tour, toolbar);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());

        //历史轨迹列表
        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        int BigSpace = getResources().getDimensionPixelSize(R.dimen.BigSpace);
        int MiddleSpace = getResources().getDimensionPixelSize(R.dimen.MiddleSpace);
        int SmallSpace = getResources().getDimensionPixelSize(R.dimen.SmallSpace);
        recyclerView.addItemDecoration(new SpaceItemDecoration(BigSpace,MiddleSpace,SmallSpace));
//        myAdapter = new PathAdapter(this, initData(cardList));
//        recyclerView.setAdapter(myAdapter);

        getPathData();

        //开始新的旅途按钮
        startBt = (TextView)findViewById(R.id.start_new_tour);
        startBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),JourneyActivity.class);
                startActivity(intent);
            }
        });


        //旋转开关按钮
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.app_popupwindow, null);
        final PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setContentView(contentView);

        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = (TextView)headerView.findViewById(R.id.username);
        userNameTextView.setText(BmobUser.getCurrentUser().getUsername());

//        startBt = (Button) findViewById(R.id.start_button);
//        startBt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(startBt.getText().equals("开始旅程")){
//                    startBt.setText("结束旅程");
//                }else if(startBt.getText().equals("结束旅程")) {
//                    Path path = new Path();
//                    path.setPathId("001");
//                    path.setPathId("100");
//                    path.setTitle("庐山行");
//                    path.save(new SaveListener<String>() {
//                        @Override
//                        public void done(String objectId,BmobException e) {
//                            if(e==null){
//                                Toast.makeText(MainActivity.this,"添加数据成功，返回objectId为："+objectId,Toast.LENGTH_SHORT).show();
//                            }else{
//                                Toast.makeText(MainActivity.this,"创建数据失败："+e.getMessage(),Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                    startBt.setText("开始旅程");
//                }
//            }
//        });
    }

    public void getPathData(){
        BmobQuery<Trace> query = new BmobQuery<Trace>();
        query.addWhereEqualTo("userId",USER_ID);
        query.findObjects(new FindListener<Trace>() {
            @Override
            public void done(List<Trace> object, BmobException e) {
                if(e == null){
                    Log.e("test","获取数据成功" + object.size());
                        for (int i = 0; i < object.size(); i++) {
                            PathRecord record = new PathRecord();
                            record.setAveragespeed(object.get(i).getAveragespeed());
                            record.setDate(object.get(i).getDate());
                            record.setId(object.get(i).getObjectId());
                            record.setUserId(object.get(i).getUserId());
                            record.setDistance(String.valueOf(object.get(i).getDistance()));
                            record.setDuration(object.get(i).getDuration());
                            record.setEndpoint(ParseUtil.parseLocation(object.get(i).getEndpoint()));
                            record.setStartpoint(ParseUtil.parseLocation(object.get(i).getStartpoint()));
                            record.setPathline(ParseUtil.parseLocations(object.get(i).getPathline()));
                            record.setCity(object.get(i).getCity());
                            Log.e("test","城市:" + record.getCity());
                            mRecordList.add(record);
                    }
                    Log.e("test","获取数据成功" + mRecordList.size());
                    myAdapter = new PathAdapter(MainActivity.this, mRecordList);
                 //
                    recyclerView.setAdapter(myAdapter);
                }else{
                    Log.e("test","获取数据失败");
                }
            }
        });
    }

    //开关抽屉
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    //注销
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            Intent intent = new Intent();
//            intent.setClass(getApplicationContext(),JourneyActivity.class);
//            startActivity(intent);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
       int id = item.getItemId();
       if (id == R.id.nav_journey) {
            Intent intent = new Intent(MainActivity.this, JourneyActivity.class);
            MainActivity.this.startActivity(intent);
        } else if (id == R.id.nav_note) {
           Log.e("test","noteActivity clicked!");
            Intent intent = new Intent(MainActivity.this, NoteListActivity.class);
            MainActivity.this.startActivity(intent);
        } else if (id == R.id.nav_info) {
           Intent intent = new Intent(MainActivity.this, InfoActivity.class);
           MainActivity.this.startActivity(intent);
        } else if (id == R.id.nav_setter) {
           Intent intent = new Intent(MainActivity.this, SettingActivity.class);
           MainActivity.this.startActivity(intent);
        } else if (id == R.id.nav_share) {
           BmobUser.logOut();
           Intent intent = new Intent(MainActivity.this, LoginActivity.class);
           intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
           MainActivity.this.startActivity(intent);
        } else if (id == R.id.nav_send) {
           BmobUser.logOut();
           Intent intent = new Intent(MainActivity.this, LoginActivity.class);
           intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
           MainActivity.this.startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Log.e("test","GravityCompat.START:" + GravityCompat.START);
//        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}