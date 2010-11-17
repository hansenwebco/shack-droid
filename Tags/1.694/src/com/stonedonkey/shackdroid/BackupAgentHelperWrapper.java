package com.stonedonkey.shackdroid;

import android.app.backup.BackupAgentHelper;

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
      
     

  public String getPackageName() {
      if (isAvailable) {
              instance.getPackageName();
      }
      return null;
  }

}
