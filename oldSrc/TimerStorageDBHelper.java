package com.ciscavate.android.phototimer.old;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class TimerStorageDBHelper extends SQLiteOpenHelper {
	// create table t1 (t1key INTEGER PRIMARY KEY,data TEXT,num double,timeEnter DATE);

	private static final String DATABASE_NAME = "PhotoTimers";
	private static final int DATABASE_VERSION = 1;

	private static final String TIMERS_TABLE_NAME = "timers";
	private static final String ID_DEF = "id";
	private static final String TOTAL_TIME_DEF = "total_time";

	private static final String REMAINING_TIME_DEF = "remaining_time";
	
	// TODO update with PhotoTimer & AppState to support all the data needed to serialize..
	private static final String TIMERS_TABLE_CREATE =
			"CREATE TABLE " + TIMERS_TABLE_NAME + " (" +
					ID_DEF + " INTEGER PRIMARY KEY, " +
					TOTAL_TIME_DEF + " INTEGER, " +
					REMAINING_TIME_DEF + " INTEGER" +
			");";

	// TODO create an instance, then use call getWritableDatabase() and getReadableDatabase()
	// to get, uh, a database.
	public TimerStorageDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TIMERS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}
