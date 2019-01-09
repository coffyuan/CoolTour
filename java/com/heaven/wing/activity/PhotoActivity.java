package com.heaven.wing.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.heaven.wing.R;
import com.heaven.wing.adapter.PhotoAdapter;
import com.heaven.wing.entity.Photo;
import com.heaven.wing.entity.Scene;
import com.heaven.wing.listener.UILPauseOnScrollListener;
import com.heaven.wing.model.UILImageLoader;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.PauseOnScrollListener;
import cn.finalteam.galleryfinal.ThemeConfig;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by 刘康斌 on 2018/11/8.
 */

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener,PhotoAdapter.OnItemClickListener{

    /*
    * 删除功能相关变量
    * */
    private static final int MYLIVE_MODE_CHECK = 0;
    private static final int MYLIVE_MODE_EDIT = 1;

    @InjectView(R.id.tv_select_num)
    TextView mTvSelectNum;
    @InjectView(R.id.btn_delete)
    Button mBtnDelete;
    @InjectView(R.id.select_all)
    TextView mSelectAll;
    @InjectView(R.id.ll_mycollection_bottom_dialog)
    LinearLayout mLlMycollectionBottomDialog;
    @InjectView(R.id.btn_editor)
    TextView mBtnEditor;
    private int mEditMode = MYLIVE_MODE_CHECK;
    private boolean isSelectAll = false;
    private boolean editorStatus = false;
    private int index = 0;

    /*
    * 图片上传及加载功能变量
    * */
    private final int REQUEST_CODE_GALLERY = 1001;
    private final int PHOTO_MAX_SIZE = 9;

    RecyclerView recyclerView;
    PhotoAdapter myAdapter;
    String userId,sceneId,traceId;

    private List<PhotoInfo> mPhotoList;//为从相册选取的图片列表
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy
                .Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy
                .Builder()
                .detectAll()
                .penaltyLog()
                .build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//返回箭头
        getLayoutInflater().inflate(R.layout.app_edit_toobar, toolbar);
        setTitle("照片");
        ButterKnife.inject(this);//一个注解框架，可用于变量初始化

        @SuppressWarnings("unchecked")
        List<Photo> photoList = (List<Photo>)getIntent().getSerializableExtra("photoList");
        if(photoList == null){
            photoList = new ArrayList<>();
            Log.e("test","photoList:" + photoList.toString());
        }
        sceneId = getIntent().getStringExtra("sceneId");
        traceId = getIntent().getStringExtra("traceId");

        mPhotoList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        myAdapter = new PhotoAdapter(PhotoActivity.this,mPhotoList,photoList,userId,sceneId);
        recyclerView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();

        //内有上传照片
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "可从相册上传照片", Snackbar.LENGTH_LONG)
                        .setAction("上传照片", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //公共配置都可以在application中配置，这里只是为了代码演示而写在此处
                                ThemeConfig themeConfig = null;

                                //打开相册的主题色
                                themeConfig = ThemeConfig.DEFAULT;

                                FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
                                functionConfigBuilder.setMutiSelectMaxSize(PHOTO_MAX_SIZE)
                                        .setEnableEdit(true)
                                        .setEnableRotate(true)//可以旋转
                                        .setEnableCrop(true)//正方形剪辑
                                        .setEnableCamera(true)//选择照片时显示相机
                                        .setEnablePreview(true)//启动预览功能
                                        .setSelected(mPhotoList);//添加过滤集合,添加已选列表

                                final FunctionConfig functionConfig = functionConfigBuilder.build();

                                cn.finalteam.galleryfinal.ImageLoader imageLoader;
                                PauseOnScrollListener pauseOnScrollListener = null;

                                //默认格式UIL
                                imageLoader = new UILImageLoader();
                                pauseOnScrollListener = new UILPauseOnScrollListener(false, true);

                                CoreConfig coreConfig = new CoreConfig.Builder(PhotoActivity.this, imageLoader, themeConfig)
                                        .setFunctionConfig(functionConfig)
                                        .setPauseOnScrollListener(pauseOnScrollListener)
                                        .setNoAnimcation(false)
                                        .build();
                                GalleryFinal.init(coreConfig);
                                //多选打开相册
                                GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY, functionConfig, mOnHanlderResultCallback);
                            }
                        }).show();
            }
        });

        initImageLoader(this);
        initFresco();
        x.Ext.init(getApplication());
        initListener();
    }

    //回调
    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null) {
                mPhotoList.addAll(resultList);
                //将选取的图片上传到数据库
                for(int i = 0;i < mPhotoList.size();i++) {
                    final BmobFile file = new BmobFile(new File(mPhotoList.get(i).getPhotoPath()));
                    file.upload(new UploadFileListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                saveFile(file);
                            }
                        }
                    });
                }
                myAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            Toast.makeText(PhotoActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    };

    //初始化图片加载库
    private void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        ImageLoader.getInstance().init(config.build());
    }

    //初始化图片加载库
    private void initFresco() {
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setBitmapsConfig(Bitmap.Config.ARGB_8888)
                .build();
        Fresco.initialize(this, config);
    }

    //上传图片到数据库
    private Photo saveFile(final BmobFile file) {
       Photo photo = new Photo();
       photo.setPhoto(file);
       photo.setUserId(userId);
       photo.setSceneId(sceneId);
        photo.setTraceId(traceId);
       // photo.setTraceId();
       photo.save(new SaveListener<String>(){
           @Override
           public void done(String s, BmobException e) {
               if (e == null) {
                   System.out.println("上传成功"+file.getUrl());
                   Toast.makeText(getApplicationContext(),"上传成功！",Toast.LENGTH_SHORT).show();
                   setSceneState(sceneId);
               } else {
                   System.out.println("上传失败"+file.getUrl());
                   Toast.makeText(getApplicationContext(),"上传失败！",Toast.LENGTH_SHORT).show();
               }
           }
       });
       return photo;
   }
    public void setSceneState(String sceneId){
        Scene scene = new Scene();
        scene.setState(true);
        scene.update(sceneId,new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    Log.e("photoActivity","更新景点状态成功!");
                }
            }
        });
    }
    /**
     * 根据选择的数量是否为0来判断按钮的是否可点击.
     *
     * @param size
     */
    private void setBtnBackground(int size) {
        if (size != 0) {
            mBtnDelete.setBackgroundResource(R.drawable.button_shape);
            mBtnDelete.setEnabled(true);
            mBtnDelete.setTextColor(Color.WHITE);
        } else {
            mBtnDelete.setBackgroundResource(R.drawable.button_noclickable_shape);
            mBtnDelete.setEnabled(false);
            mBtnDelete.setTextColor(ContextCompat.getColor(this, R.color.color_b7b8bd));
        }
    }

    private void initListener() {
        myAdapter.setOnItemClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mSelectAll.setOnClickListener(this);
        mBtnEditor.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_delete:
                deleteVideo();
                break;
            case R.id.select_all:
                selectAllMain();
                break;
            case R.id.btn_editor:
                updataEditMode();
                break;
            default:
                break;
        }
    }

    /**
     * 全选和反选
     */
    private void selectAllMain() {
        if (myAdapter == null) return;
        if (!isSelectAll) {
            for (int i = 0, j = myAdapter.getMyLiveList().size(); i < j; i++) {
                myAdapter.getMyLiveList().get(i).setSelect(true);
            }
            index = myAdapter.getMyLiveList().size();
            mBtnDelete.setEnabled(true);
            mSelectAll.setText("取消全选");
            isSelectAll = true;
        } else {
            for (int i = 0, j = myAdapter.getMyLiveList().size(); i < j; i++) {
                myAdapter.getMyLiveList().get(i).setSelect(false);
            }
            index = 0;
            mBtnDelete.setEnabled(false);
            mSelectAll.setText("全选");
            isSelectAll = false;
        }
        myAdapter.notifyDataSetChanged();
        setBtnBackground(index);
        mTvSelectNum.setText(String.valueOf(index));
    }

    /**
     * 删除逻辑
     */
    private void deleteVideo() {
        if (index == 0){
            mBtnDelete.setEnabled(false);
            return;
        }
        final AlertDialog builder = new AlertDialog.Builder(this)
                .create();
        builder.show();
        if (builder.getWindow() == null) return;
        builder.getWindow().setContentView(R.layout.pop_info);//设置弹出框加载的布局
        TextView msg = (TextView) builder.findViewById(R.id.tv_msg);
        Button cancle = (Button) builder.findViewById(R.id.btn_cancle);
        Button sure = (Button) builder.findViewById(R.id.btn_sure);
        if (msg == null || cancle == null || sure == null) return;

        if (index == 1) {
            msg.setText("删除后不可恢复，是否删除该条目？");
        } else {
            msg.setText("删除后不可恢复，是否删除这" + index + "个条目？");
        }
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = myAdapter.getMyLiveList().size(), j =0 ; i > j; i--) {
                    Photo photo = myAdapter.getMyLiveList().get(i-1);
                    if (photo.isSelect()) {
                        myAdapter.getMyLiveList().remove(photo);
                        Photo photo1= new Photo();
                        photo1.setObjectId(photo.getObjectId());
                        photo1.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e==null){
                                    Toast.makeText(PhotoActivity.this,"删除成功",Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(PhotoActivity.this,"删除失败",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        index--;
                    }
                }
                index = 0;
                mTvSelectNum.setText(String.valueOf(0));
                setBtnBackground(index);
                if (myAdapter.getMyLiveList().size() == 0){
                    mLlMycollectionBottomDialog.setVisibility(View.GONE);
                }
                myAdapter.notifyDataSetChanged();
                builder.dismiss();
            }
        });
    }
    private void updataEditMode() {
        mEditMode = mEditMode == MYLIVE_MODE_CHECK ? MYLIVE_MODE_EDIT : MYLIVE_MODE_CHECK;
        if (mEditMode == MYLIVE_MODE_EDIT) {
            mBtnEditor.setText("取消");
            mLlMycollectionBottomDialog.setVisibility(View.VISIBLE);
            editorStatus = true;
        } else {
            mBtnEditor.setText("编辑");
            mLlMycollectionBottomDialog.setVisibility(View.GONE);
            editorStatus = false;
            clearAll();
        }
        myAdapter.setEditMode(mEditMode);
    }


    private void clearAll() {
        mTvSelectNum.setText(String.valueOf(0));
        isSelectAll = false;
        mSelectAll.setText("全选");
        setBtnBackground(0);
    }

    @Override
    public void onItemClickListener(int pos, List<Photo> list) {
        if (editorStatus) {
            Photo photo = list.get(pos);
            boolean isSelect = photo.isSelect();
            if (!isSelect) {
                index++;
                photo.setSelect(true);
                if (index == list.size()) {
                    isSelectAll = true;
                    mSelectAll.setText("取消全选");
                }

            } else {
                photo.setSelect(false);
                index--;
                isSelectAll = false;
                mSelectAll.setText("全选");
            }
            setBtnBackground(index);
            mTvSelectNum.setText(String.valueOf(index));
            myAdapter.notifyDataSetChanged();
        }
    }
}
