package com.heaven.wing.adapter;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.amap.api.maps.model.MarkerOptions;
import com.heaven.wing.entity.PathRecord;
import com.heaven.wing.entity.Trace;
import com.heaven.wing.entity.ViewMarker;
import com.heaven.wing.util.ParseUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * 数据库相关操作，用于存取轨迹记录
 * 
 */
public class DbAdapter {
	public static final String KEY_ROWID = "id";
	public static final String KEY_DISTANCE = "distance";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_SPEED = "averagespeed";
	public static final String KEY_LINE = "pathline";
	public static final String KEY_STRAT = "stratpoint";
	public static final String KEY_END = "endpoint";
	public static final String KEY_DATE = "date";
	private final static String DATABASE_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/recordPath";
	static final String DATABASE_NAME = DATABASE_PATH + "/" + "record.db";
	private static final int DATABASE_VERSION = 1;
	private static final String RECORD_TABLE = "record";
	private static final String RECORD_CREATE = "create table if not exists record("
			+ KEY_ROWID
			+ " integer primary key autoincrement,"
			+ "stratpoint STRING,"
			+ "endpoint STRING,"
			+ "pathline STRING,"
			+ "distance STRING,"
			+ "duration STRING,"
			+ "averagespeed STRING,"
			+ "date STRING" + ");";

	public static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(RECORD_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}


	List<PathRecord> records = new ArrayList<PathRecord>();

	private Context mCtx = null;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;



	// constructor
	public DbAdapter(Context ctx) {
		this.mCtx = ctx;
		dbHelper = new DatabaseHelper(mCtx);
	}

	public DbAdapter open() throws SQLException {

		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public Cursor getall() {
		return db.rawQuery("SELECT * FROM record", null);
	}

	// remove an entry
	public boolean delete(long rowId) {

		return db.delete(RECORD_TABLE, "id=" + rowId, null) > 0;
	}

	/**
	 * 数据库存入一条轨迹
	 * 
	 * @param distance
	 * @param duration
	 * @param averagespeed
	 * @param pathline
	 * @param stratpoint
	 * @param endpoint
	 * @param date
	 * @return
	 */
	public long createrecord(String distance, String duration,
							 String averagespeed, String pathline, String stratpoint,
							 String endpoint, String date) {
		ContentValues args = new ContentValues();
		args.put("distance", distance);
		args.put("duration", duration);
		args.put("averagespeed", averagespeed);
		args.put("pathline", pathline);
		args.put("stratpoint", stratpoint);
		args.put("endpoint", endpoint);
		args.put("date", date);
		return db.insert(RECORD_TABLE, null, args);
	}

	public int getDateFromBmob(){
		BmobQuery<Trace> query = new BmobQuery<Trace>();
		//返回50条数据，如果不加上这条语句，默认返回10条数据
		query.setLimit(50);
		//执行查询方法
		query.findObjects(new FindListener<Trace>() {

			@Override
			public void done(List<Trace> object, BmobException e) {
				if(e==null){
//					Log.e("bmob","查询成功：共"+object.size()+"条数据：" + object.toString());

				}else{
//					Log.e("bmob","失败："+e.getMessage()+","+e.getErrorCode());
				}

//				Message message = handler.obtainMessage();
//				message.what = 0;
//				//以消息为载体
//				message.obj = recordList;//这里的list就是查询出list
//				//向handler发送消息
//				handler.sendMessage(message);
			}
		});

		return 1;
	}




//	private Handler handler = new Handler(){
//		@Override
//		public void handleMessage(Message msg){
//			switch(msg.what){
//				case 1:
//					records = (List<PathRecord>)msg.obj;
//					Log.e("Test", "handler"+records.toString());
//					break;
//			}
//		}
//	};
//
//	class BmobThread extends Thread{
//		@Override
//		public void run(){
//			Message msg = new Message();
//
////			msg.what = getData();
//			handler.sendMessage(msg);
//		}
//	}
//
//	public int getData() throws InterruptedException {
//		BmobQuery<Trace> query = new BmobQuery<Trace>();
//		query.setLimit(50);
//		//返回50条数据，如果不加上这条语句，默认返回10条数据
//		query.findObjects(new FindListener<Trace>() {
//
//			@Override
//			public void done(List<Trace> object, BmobException e) {
//				if(e==null){
////					List<PathRecord> pathRecord = new ArrayList<PathRecord>();
//					for (int i = 0; i < object.size(); i++) {
//						PathRecord record = new PathRecord();
//						record.setAveragespeed(object.get(i).getAveragespeed());
//						record.setDate(object.get(i).getDate());
//						record.setId(object.get(i).getObjectId());
//						record.setDistance(String.valueOf(object.get(i).getDistance()));
//						record.setDuration(object.get(i).getDuration());
//						record.setEndpoint(Util.parseLocation(object.get(i).getEndpoint()));
//						record.setStartpoint(Util.parseLocation(object.get(i).getStartpoint()));
//						record.setPathline(Util.parseLocations(object.get(i).getPathline()));
//						records.add(record);
//					}
//
//				}else{
//					Log.e("bmob","失败："+e.getMessage()+","+e.getErrorCode());
//				}
////				Message message = handler.obtainMessage();
////				message.what = 0;
////				//以消息为载体
////				message.obj = recordList;//这里的list就是查询出list
////				//向handler发送消息
////				handler.sendMessage(message);
//			}
//		});
//
//		return 1;
//	}



//	public List<PathRecord> queryRecordAllFromBmob() throws InterruptedException {
//		getData();
//		Log.e("Test", "返回前");
//		Log.e("Test", records.toString());
//		return records;
//	}
	/**
	 * 查询所有轨迹记录
	 * 
	 * @return
	 */
	public List<PathRecord> queryRecordAll() {
		List<PathRecord> allRecord = new ArrayList<PathRecord>();
		Cursor allRecordCursor = db.query(RECORD_TABLE, getColumns(), null,
				null, null, null, null);
		while (allRecordCursor.moveToNext()) {
			PathRecord record = new PathRecord();
			record.setId(BmobUser.getCurrentUser().getObjectId());
			record.setDistance(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbAdapter.KEY_DISTANCE)));
			record.setDuration(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbAdapter.KEY_DURATION)));
			record.setDate(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbAdapter.KEY_DATE)));
			String lines = allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbAdapter.KEY_LINE));
			record.setPathline(ParseUtil.parseLocations(lines));
			record.setStartpoint(ParseUtil.parseLocation(allRecordCursor
					.getString(allRecordCursor
							.getColumnIndex(DbAdapter.KEY_STRAT))));
			record.setEndpoint(ParseUtil.parseLocation(allRecordCursor
					.getString(allRecordCursor
							.getColumnIndex(DbAdapter.KEY_END))));
			allRecord.add(record);
		}
		Collections.reverse(allRecord);
		return allRecord;
	}
	/**
	 * 按照id查询
	 * 
	 * @param mRecordItemId
	 * @return
	 */
	public PathRecord queryRecordById(int mRecordItemId) {
		String where = KEY_ROWID + "=?";
		String[] selectionArgs = new String[] { String.valueOf(mRecordItemId) };
		Cursor cursor = db.query(RECORD_TABLE, getColumns(), where,
				selectionArgs, null, null, null);
		PathRecord record = new PathRecord();
		if (cursor.moveToNext()) {
			record.setId(BmobUser.getCurrentUser().getObjectId());
			record.setDistance(cursor.getString(cursor
					.getColumnIndex(DbAdapter.KEY_DISTANCE)));
			record.setDuration(cursor.getString(cursor
					.getColumnIndex(DbAdapter.KEY_DURATION)));
			record.setDate(cursor.getString(cursor
					.getColumnIndex(DbAdapter.KEY_DATE)));
			String lines = cursor.getString(cursor
					.getColumnIndex(DbAdapter.KEY_LINE));
			record.setPathline(ParseUtil.parseLocations(lines));
			record.setStartpoint(ParseUtil.parseLocation(cursor.getString(cursor
					.getColumnIndex(DbAdapter.KEY_STRAT))));
			record.setEndpoint(ParseUtil.parseLocation(cursor.getString(cursor
					.getColumnIndex(DbAdapter.KEY_END))));
		}
		return record;
	}

	private String[] getColumns() {
		return new String[] { KEY_ROWID, KEY_DISTANCE, KEY_DURATION, KEY_SPEED,
				KEY_LINE, KEY_STRAT, KEY_END, KEY_DATE };
	}

	public void saveMarkerToBmob(MarkerOptions marker, String userId){
		ViewMarker viewMarker = new ViewMarker();
		viewMarker.setPosition(marker.getPosition().toString());
		viewMarker.setUserId(userId);
		viewMarker.save(new SaveListener<String>() {

			@Override
			public void done(String objectId, BmobException e) {
				if(e==null){
					Log.e("bmob","创建marker数据成功：" + objectId);
				}else{
					Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
				}
			}
		});
	}
}

//		getData();

//		Handler handler=new Handler(){
//			@Override
//			public void handleMessage(Message msg) {
//				List<Trace> object = null;
//				switch (msg.what){
//					case 0:
//						object = (List<Trace>) msg.obj;
//						break;
//				}
//				for (int i = 0; i < object.size(); i++) {
//					PathRecord record = new PathRecord();
//					record.setAveragespeed(object.get(i).getAveragespeed());
//					record.setDate(object.get(i).getDate());
//					record.setId(object.get(i).getId());
//					record.setDistance(String.valueOf(object.get(i).getDistance()));
//					record.setDuration(object.get(i).getDuration());
//					record.setEndpoint(Util.parseLocation(object.get(i).getEndpoint()));
//					record.setStartpoint(Util.parseLocation(object.get(i).getStartpoint()));
//					record.setPathline(Util.parseLocations(object.get(i).getPathline()));
//					records.add(record);
//				}
//				Log.e("bmob2",records.toString());
//			}
//		};
//if(records.size() == 0){
////			Log.e("bmob2","查询数据为空：" + records.toString());
//		}