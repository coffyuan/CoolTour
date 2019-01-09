package com.heaven.wing.entity;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SQLQueryListener;

/**
 * Created by 刘康斌 on 2018/11/13.
 */

public class User extends BmobUser {
    private String sex;
    private BmobFile headPic;
    private List<Note> noteList = new ArrayList<>();

    public User(){
    }
    public User(BmobFile user_pic) {
        headPic = user_pic;
    }

    public User(String objectId){setObjectId(objectId);}



    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public BmobFile getHeadPic() {
        return headPic;
    }

    public void setHeadPic(BmobFile headPic) {
        this.headPic = headPic;
    }

    public List<Note> getNoteList() {
        return noteList;
    }

    public void findNoteByUser(String sql) {
        new BmobQuery<Note>().doSQLQuery(sql, new SQLQueryListener<Note>() {
            @Override
            public void done(BmobQueryResult<Note> bmobQueryResult, BmobException e) {
                if (e==null) {
                    List<Note> list = (List<Note>)bmobQueryResult.getResults();
                    if(list != null && list.size() > 0) {
                        for(int i = 0;i < list.size();i++) {
                            noteList.add(list.get(i));
                        }
                    }else {
                       System.out.print("暂无游记");
                    }
                }
            }
        });
    }
}
