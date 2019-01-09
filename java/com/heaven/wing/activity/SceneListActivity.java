package com.heaven.wing.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.heaven.wing.R;
import com.heaven.wing.adapter.NoteAdapter;
import com.heaven.wing.entity.Note;
import com.heaven.wing.util.DividerItemDecoration;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by Eric on 2018/11/29.
 */

public class SceneListActivity extends AppCompatActivity implements View.OnClickListener,NoteAdapter.OnItemClickListener{
    private static final int MYLIVE_MODE_CHECK = 0;
    private static final int MYLIVE_MODE_EDIT = 1;

    @InjectView(R.id.list)
    RecyclerView recyclerView;
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

    private NoteAdapter myAdapter = null;
    private int mEditMode = MYLIVE_MODE_CHECK;
    private boolean isSelectAll = false;
    private boolean editorStatus = false;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notelist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("游记");
        getLayoutInflater().inflate(R.layout.app_edit_toobar, toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//返回箭头

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        ButterKnife.inject(this);

        loadData();
    }

    public void loadData() {
        BmobQuery<Note> query = new BmobQuery<>();
        query.addWhereEqualTo("userId", BmobUser.getCurrentUser().getObjectId());
        query.setLimit(50);
        query.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                if(e==null){
                    recyclerView.setLayoutManager(new LinearLayoutManager(SceneListActivity.this));
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    DividerItemDecoration itemDecorationHeader = new DividerItemDecoration(SceneListActivity.this, DividerItemDecoration.VERTICAL_LIST);//RecyclerView的通用间隔条(vivider)
                    itemDecorationHeader.setDividerDrawable(ContextCompat.getDrawable(SceneListActivity.this, R.drawable.divider_main_bg_height_1));
                    myAdapter = new NoteAdapter(SceneListActivity.this,list);
                    recyclerView.addItemDecoration(itemDecorationHeader);
                    myAdapter.notifyAdapter(list, false);
                    recyclerView.setAdapter(myAdapter);
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
                initListener();
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
                    Note note = myAdapter.getMyLiveList().get(i-1);
                    if (note.isSelect()) {
                        myAdapter.getMyLiveList().remove(note);
                        Note note1 = new Note();
                        note1.setObjectId(note.getObjectId());
                        note1.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e==null){
                                    Toast.makeText(SceneListActivity.this,"删除成功",Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(SceneListActivity.this,"删除失败",Toast.LENGTH_LONG).show();
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
    public void onItemClickListener(int pos, List<Note> list) {
        if (editorStatus) {
            Note note = list.get(pos);
            boolean isSelect = note.isSelect();
            if (!isSelect) {
                index++;
                note.setSelect(true);
                if (index == list.size()) {
                    isSelectAll = true;
                    mSelectAll.setText("取消全选");
                }

            } else {
                note.setSelect(false);
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
