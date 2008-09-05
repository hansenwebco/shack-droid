package com.stonedonkey.shackdroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ShackDroidNotesManager {

	public static final String THREAD_ID = "threadID";
	public static final String MESSAGE_PREVIEW = "messagePreview";
	public static final String POSTER_NAME = "posterName";
	public static final String POST_DATE = "postDate";
	public static final String POST_CATEGORY = "postCategory";


	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context _context;

	
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table ShackDroidNotes (_id integer primary key autoincrement, "
			+ "threadID text not null, messagePreview text not null,"
			+ "posterName text not null, postDate text not null,"
			+ "postCategory text not null)";

	private static final String DATABASE_NAME = "ShackDroid";
	private static final String DATABASE_TABLE = "ShackDroidNotes";
	private static final int DATABASE_VERSION = 2;

	public static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(DATABASE_CREATE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}

	}

	public long CreateNote(String threadID, String messagePreview,
			String posterName, String postDate, String postCategory) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(THREAD_ID, threadID);
		initialValues.put(MESSAGE_PREVIEW, messagePreview);
		initialValues.put(POSTER_NAME, posterName);
		initialValues.put(POST_DATE, postDate);
		initialValues.put(POST_CATEGORY, postCategory);

		return mDb.insert(DATABASE_TABLE, null, initialValues);

	}

	public boolean DeleteNote(long rowId) {
		return mDb.delete(DATABASE_TABLE, "_id=" + rowId, null) > 0;
	}
	public Cursor GetAllNotes()
	{
	
	        //return mDb.query(DATABASE_TABLE, new String[] {"_id", THREAD_ID,
	         //       MESSAGE_PREVIEW, POSTER_NAME,POST_DATE}, null, null, null, null, null);
		
		return mDb.query(DATABASE_TABLE,new String[] { MESSAGE_PREVIEW }, null, null, null, null, null, null);
	}
	
	public ShackDroidNotesManager(Context context) {
		this._context = context;
	}
	
    public ShackDroidNotesManager open() throws SQLException {
        mDbHelper = new DatabaseHelper(_context);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
}
