package com.stonedonkey.shackdroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class ShackDroidNotesManager {

	public static final String THREAD_ID = "threadID";
	public static final String MESSAGE_PREVIEW = "messagePreview";
	public static final String POSTER_NAME = "posterName";
	public static final String POST_DATE = "postDate";
	public static final String POST_CATEGORY = "postCategory";
	
    //private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table ShackDroidNotes (_id integer primary key autoincrement, "
                    + "threadID text not null, messagePreview text not null," 
                    + "posterName text not null, postDate text not null,"
                    + "postCategory text not null)";
	
    private static final String DATABASE_NAME = "ShackDroidNotes";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;
    
    public static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
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
    
	public long CreateNote(Integer threadID, String messagePreview,
			String posterName, String postDate, String postCategory) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(THREAD_ID, threadID);
        initialValues.put(MESSAGE_PREVIEW, messagePreview);
        initialValues.put(POSTER_NAME, posterName);
        initialValues.put(POST_DATE, postDate);
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);

	

	}

}
