package com.stonedonkey.shackdroid;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

public class BackupAgent extends BackupAgentHelper {

    static final String PREFS = "user_preferences";	  // The name of the SharedPreferences file
    static final String PREFS_BACKUP_KEY = "prefs";   // A key to uniquely identify the set of backup data

    static final String FILE_WATCH_CACHE = "watch.cache";
    static final String FILE_SHACK_MESSAGE_CACHE = "shackmessage.cache";
    static final String FILE_STATS_CACHE = "stats.cache";
    static final String FILES_BACKUP_KEY = "caches";
    
    private static boolean tryToBackupData = true;

    
    @Override
	public void onCreate() {
		super.onCreate();
		
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this,getDefaultSharedPreferencesName());
		addHelper(PREFS_BACKUP_KEY, helper);
		
		 //SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
	     
	
	     //FileBackupHelper caches = new FileBackupHelper(this, FILE_WATCH_CACHE, FILE_SHACK_MESSAGE_CACHE,FILE_STATS_CACHE,FILES_BACKUP_KEY);
	     //addHelper(FILES_BACKUP_KEY,caches);
	     
	}
    
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,ParcelFileDescriptor newState) throws IOException {
		super.onBackup(oldState, data, newState);
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,	ParcelFileDescriptor newState) throws IOException {
		super.onRestore(data, appVersionCode, newState);

	
	}
   private String getDefaultSharedPreferencesName() {
        // Transdroid uses the PreferenceManager.getDefaultPreferences() which, according
        // to the Android source code, equals the package name + _preferences
        return this.getPackageName() + "_preferences";
    }

	

	
	
}
