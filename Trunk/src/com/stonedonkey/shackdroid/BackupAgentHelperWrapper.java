package com.stonedonkey.shackdroid;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.ParcelFileDescriptor;

// Lifted from Transdroid
// http://code.google.com/p/transdroid/source/browse/trunk/src/org/transdroid/service/BackupAgentHelperWrapper.java

public class BackupAgentHelperWrapper {
	
	  private BackupAgentHelper instance;
      private static boolean checkAvailability = true;
      private static boolean isAvailable = false;
      
      /**
       * Class initialization which will fail when the helper class doesn't exist
       */
      static {
              if (checkAvailability) {
                      try {
                              Class.forName("android.app.backup.BackupAgentHelper");
                              checkAvailability = false;
                              isAvailable = true;
                      } catch (Exception e) {
                      }
              }
      }
      
      public static void checkAvailable() { }
      
      public BackupAgentHelperWrapper() {
              if (isAvailable) {
                      instance = new BackupAgentHelper();
              }
      }

      protected Context getBackupAgentInstance() {
              if (isAvailable) {
                      return instance;
              }
              return null;
      }

      public void onCreate() {
              if (isAvailable) {
                      onCreate();
              }
      }
      
      public void onDestroy() {
              if (isAvailable) {
                      onDestroy();
              }
      }
      
      public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, 
                      ParcelFileDescriptor newState) throws IOException {
              if (isAvailable) {
                      instance.onBackup(oldState, data, newState);
              }
      }
      
      public void onRestore(BackupDataInput data, int appVersionCode, 
                      ParcelFileDescriptor newState) throws IOException {
              if (isAvailable) {
                      instance.onRestore(data, appVersionCode, newState);
              }
      }

  public void addHelper(String keyPrefix, SharedPreferencesBackupHelper helper) {
              if (isAvailable) {
                      instance.addHelper(keyPrefix, helper);
              }
      }

  public String getPackageName() {
      if (isAvailable) {
              instance.getPackageName();
      }
      return null;
  }

}
