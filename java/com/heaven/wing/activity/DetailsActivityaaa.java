//package com.heaven.wing.activity;
//
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.support.v7.widget.Toolbar;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.RadioButton;
//import android.widget.Toast;
//
//import com.baoyz.actionsheet.ActionSheet;
//import com.facebook.drawee.backends.pipeline.Fresco;
//import com.facebook.imagepipeline.core.ImagePipelineConfig;
//import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
//import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import cn.finalteam.galleryfinal.CoreConfig;
//import cn.finalteam.galleryfinal.FunctionConfig;
//import cn.finalteam.galleryfinal.GalleryFinal;
//import cn.finalteam.galleryfinal.PauseOnScrollListener;
//import cn.finalteam.galleryfinal.ThemeConfig;
//import cn.finalteam.galleryfinal.model.PhotoInfo;
//import cn.finalteam.galleryfinal.widget.HorizontalListView;
//import sample.listener.UILPauseOnScrollListener;
//import sample.loader.UILImageLoader;
//import org.xutils.x;
//
//public class DetailsActivity extends AppCompatActivity {
//
//    private final int REQUEST_CODE_CAMERA = 1000;
//    private final int REQUEST_CODE_GALLERY = 1001;
//    private final int REQUEST_CODE_CROP = 1002;
//    private final int REQUEST_CODE_EDIT = 1003;
//    private final int PHOTO_MAX_SIZE = 9;
//    @Bind(R.id.lv_photo)
//    HorizontalListView mLvPhoto;
//
//    private List<PhotoInfo> mPhotoList;
//    private ChoosePhotoListAdapter mChoosePhotoListAdapter;
//    private Button btn_add_photo;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_details);
//        ButterKnife.bind(this);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        mLvPhoto = (HorizontalListView) findViewById(R.id.lv_photo);
//        mPhotoList = new ArrayList<>();
//        mChoosePhotoListAdapter = new ChoosePhotoListAdapter(this, mPhotoList);
//        mLvPhoto.setAdapter(mChoosePhotoListAdapter);
//        btn_add_photo = (Button) findViewById(R.id.btn_add_photo);
//
//        btn_add_photo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //公共配置都可以在application中配置，这里只是为了代码演示而写在此处
//                ThemeConfig themeConfig = null;
//
//                //打开相册的主题色
//                themeConfig = ThemeConfig.DEFAULT;
//
//                FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
//                functionConfigBuilder.setMutiSelectMaxSize(PHOTO_MAX_SIZE)
//                        .setEnableEdit(true)
//                        .setEnableRotate(true)//可以旋转
//                        .setEnableCrop(true)//正方形剪辑
//                        .setEnableCamera(true)//选择照片时显示相机
//                        .setEnablePreview(true)//启动预览功能
//                        .setSelected(mPhotoList);//添加过滤集合
//
//                final FunctionConfig functionConfig = functionConfigBuilder.build();
//
//                cn.finalteam.galleryfinal.ImageLoader imageLoader;
//                PauseOnScrollListener pauseOnScrollListener = null;
//
//                //默认格式UIL
//                imageLoader = new UILImageLoader();
//                pauseOnScrollListener = new UILPauseOnScrollListener(false, true);
//
//                CoreConfig coreConfig = new CoreConfig.Builder(DetailsActivity.this, imageLoader, themeConfig)
//                        .setFunctionConfig(functionConfig)
//                        .setPauseOnScrollListener(pauseOnScrollListener)
//                        .setNoAnimcation(false)
//                        .build();
//                GalleryFinal.init(coreConfig);
//
//                ActionSheet.createBuilder(DetailsActivity.this, getSupportFragmentManager())
//                        .setCancelButtonTitle("取消(Cancel)")
//                        .setOtherButtonTitles("打开相册(Open Gallery)", "拍照(Camera)", "裁剪(Crop)", "编辑(Edit)")
//                        .setCancelableOnTouchOutside(true)
//                        .setListener(new ActionSheet.ActionSheetListener() {
//                            @Override
//                            public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
//
//                            }
//
//                            @Override
//                            public void onOtherButtonClick(ActionSheet actionSheet, int index) {
//                                String path = "/sdcard/pk1-2.jpg";
//                                switch (index) {
//                                    case 0:
//                                        GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY, functionConfig, mOnHanlderResultCallback);
//                                        break;
//                                    case 1:
//                                        GalleryFinal.openCamera(REQUEST_CODE_CAMERA, functionConfig, mOnHanlderResultCallback);
//                                        break;
//                                    case 2:
//                                        if (new File(path).exists()) {
//                                            GalleryFinal.openCrop(REQUEST_CODE_CROP, functionConfig, path, mOnHanlderResultCallback);
//                                        } else {
//                                            Toast.makeText(DetailsActivity.this, "图片不存在", Toast.LENGTH_SHORT).show();
//                                        }
//                                        break;
//                                    case 3:
//                                        if (new File(path).exists()) {
//                                            GalleryFinal.openEdit(REQUEST_CODE_EDIT, functionConfig, path, mOnHanlderResultCallback);
//                                        } else {
//                                            Toast.makeText(DetailsActivity.this, "图片不存在", Toast.LENGTH_SHORT).show();
//                                        }
//                                        break;
//                                    default:
//                                        break;
//                                }
//                            }
//                        })
//                        .show();
//            }
//        });
//        initImageLoader(this);
//        initFresco();
//        x.Ext.init(getApplication());
//    }
//
//    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
//        @Override
//        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
//            if (resultList != null) {
//                mPhotoList.addAll(resultList);
//                mChoosePhotoListAdapter.notifyDataSetChanged();
//            }
//        }
//
//        @Override
//        public void onHanlderFailure(int requestCode, String errorMsg) {
//            Toast.makeText(DetailsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
//        }
//    };
//
//    private void initImageLoader(Context context) {
//        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
//        config.threadPriority(Thread.NORM_PRIORITY - 2);
//        config.denyCacheImageMultipleSizesInMemory();
//        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
//        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
//        config.tasksProcessingOrder(QueueProcessingType.LIFO);
//        config.writeDebugLogs(); // Remove for release app
//
//        ImageLoader.getInstance().init(config.build());
//    }
//
//    private void initFresco() {
//        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
//                .setBitmapsConfig(Bitmap.Config.ARGB_8888)
//                .build();
//        Fresco.initialize(this, config);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int itemId = item.getItemId();
//        if (itemId == R.id.action_clean_cache) {
//            GalleryFinal.cleanCacheFile();
//            Toast.makeText(this, "清理成功(Clear success)", Toast.LENGTH_SHORT).show();
//        } else {
//            //startActivity(new Intent(this, FuncationActivity.class));
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//}