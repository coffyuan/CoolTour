/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.heaven.wing.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.heaven.wing.R;
import com.heaven.wing.entity.Photo;
import com.heaven.wing.entity.Scene;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.toolsfinal.DeviceUtils;

public class ChoosePhotoListAdapter extends BaseAdapter {
    private List<PhotoInfo> mList;
    private LayoutInflater mInflater;
    private int mScreenWidth;
    private String sceneId;

    public ChoosePhotoListAdapter(Activity activity, List<PhotoInfo> list,String sceneId) {
        this.mList = list;
        this.mInflater = LayoutInflater.from(activity);
        this.mScreenWidth = DeviceUtils.getScreenPix(activity).widthPixels;
        this.sceneId = sceneId;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.ic_gf_default_photo)
                .showImageForEmptyUri(R.drawable.ic_gf_default_photo)
                .showImageOnLoading(R.drawable.ic_gf_default_photo).build();

        ImageView ivPhoto = (ImageView) mInflater.inflate(R.layout.adapter_photo_list_item, null);
        setHeight(ivPhoto);

        PhotoInfo photoInfo = mList.get(position);
        ImageLoader.getInstance().displayImage("file:/" + photoInfo.getPhotoPath(), ivPhoto, options);
        System.out.println("path"+photoInfo.getPhotoPath());

        final BmobFile file = new BmobFile(new File(photoInfo.getPhotoPath()));
        file.upload(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    //上传图片
                    saveFile(file);
                    //设置景点状态
                    setSceneState();
                }
            }
        });
        return ivPhoto;
    }

    private void setHeight(final View convertView) {
        int height = mScreenWidth / 3 - 8;
        convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }

    //上传图片
    private void saveFile(final BmobFile file) {
        Photo photo = new Photo();
        photo.setPhoto(file);
        photo.setUserId(BmobUser.getCurrentUser().getObjectId());
        photo.setSceneId(sceneId);
        photo.save(new SaveListener<String>(){
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    System.out.println("上传成功"+file.getUrl());
                } else {
                    System.out.println("上传失败"+file.getUrl());
                }
            }
        });
    }
    //设置景点状态
    public void setSceneState(){
        Scene scene = new Scene();
        scene.setState(true);
        scene.update(sceneId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    Log.e("test","更新景点状态成功");
                }else{
                    Log.e("test","更新景点失败");
                }
            }
        });

    }
}
