package com.keithvongola.android.moneydiary.service;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

import com.keithvongola.android.moneydiary.databases.MoneyDBHelper;
import com.keithvongola.android.moneydiary.databases.MoneyProvider;

import java.io.IOException;

public class MyBackUpAgent extends BackupAgentHelper {
    static final String PREFS_BACKUP_KEY = "myprefs";
    static final String DATABASE_BACKUP_KEY = "mydb";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper preferencesBackupHelper =
                new SharedPreferencesBackupHelper(this,
                        this.getPackageName() + "_preferences");

        FileBackupHelper dbBackupHelper = new FileBackupHelper(this,
                "../databases/"+ MoneyDBHelper.DB_NAME);

        addHelper(PREFS_BACKUP_KEY, preferencesBackupHelper);
        addHelper(DATABASE_BACKUP_KEY, dbBackupHelper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
        synchronized (MoneyProvider.dbLock) {
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                          ParcelFileDescriptor newState) throws IOException {
        synchronized (MoneyProvider.dbLock) {
            super.onRestore(data, appVersionCode, newState);
        }
    }

}
