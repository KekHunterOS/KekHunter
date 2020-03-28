package com.offsec.nethunter.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.offsec.nethunter.BuildConfig;
import com.offsec.nethunter.models.CustomCommandsModel;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class CustomCommandsSQL extends SQLiteOpenHelper {
    private static CustomCommandsSQL instance;
    private static final String DATABASE_NAME = "CustomCommandsFragment";
    private static final String TAG = "CustomCommandsSQL";
    private static final String TABLE_NAME = DATABASE_NAME;
    private static ArrayList<String> COLUMNS = new ArrayList<>();
    private static final String[][] customcommandsData = {
            {"1", "Update Kali Metapackages",
                    "apt update && apt-get -y upgrade",
                    "kali", "interactive", "0"},
            {"2", "Set USB gadget mode: adb",
                    "su -c setprop sys.usb.config \"win,adb\";exit",
                    "android", "interactive", "0"},
            {"3", "Set USB gadget mode: HID,MTP,adb for Win",
                    "su -c setprop sys.usb.config \"win,mtp,hid,adb\";exit",
                    "android", "interactive", "0"},
            {"4", "Launch Wifite",
                    "wifite",
                    "kali", "interactive", "0"},
            {"5", "Start wlan0 in monitor mode",
                    "ip link set wlan0 down; echo 4 > /sys/module/wlan/parameters/con_mode;ip link set wlan0 up",
                    "kali", "interactive", "0"},
            {"6", "Stop wlan0 monitor mode",
                    "ip link set wlan0 down; echo 0 > /sys/module/wlan/parameters/con_mode;ip link set wlan0 up",
                    "kali", "interactive", "0"},
            {"7", "Start wlan1 in monitor mode",
                    "ip link set wlan1 down && iw wlan1 set monitor control && ip link set wlan1 up",
                    "kali", "interactive", "0"}
    };

    public synchronized static CustomCommandsSQL getInstance(Context context){
        if (instance == null) {
            instance = new CustomCommandsSQL(context.getApplicationContext());
        }
        return instance;
    }

    private CustomCommandsSQL(Context context) {
        super(context, DATABASE_NAME, null, 3);
        // Add your default column here;
        COLUMNS.add("id");
        COLUMNS.add("CommandLabel");
        COLUMNS.add("Command");
        COLUMNS.add("RuntimeEnv");
        COLUMNS.add("ExecutionMode");
        COLUMNS.add("RunOnBoot");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMNS.get(0) + " INTEGER, " +
                COLUMNS.get(1) + " TEXT, " + COLUMNS.get(2) +  " TEXT, " +
                COLUMNS.get(3) + " TEXT, " + COLUMNS.get(4) + " TEXT, " +
                COLUMNS.get(5) + " INTEGER)");
        // For devices update from db version 2 to 3 only;
        if (new File(NhPaths.APP_DATABASE_PATH + "/KaliLaunchers").exists()) {
            convertOldDBtoNewDB(db);
        // else create default value;
        } else {
            ContentValues initialValues = new ContentValues();
            db.beginTransaction();
            for (String[] data : customcommandsData) {
                initialValues.put(COLUMNS.get(0), data[0]);
                initialValues.put(COLUMNS.get(1), data[1]);
                initialValues.put(COLUMNS.get(2), data[2]);
                initialValues.put(COLUMNS.get(3), data[3]);
                initialValues.put(COLUMNS.get(4), data[4]);
                initialValues.put(COLUMNS.get(5), data[5]);
                db.insert(TABLE_NAME, null, initialValues);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<CustomCommandsModel> bindData(ArrayList<CustomCommandsModel> customCommandsModelArrayList) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);
        while (cursor.moveToNext()) {
            customCommandsModelArrayList.add(new CustomCommandsModel(
                    cursor.getString(cursor.getColumnIndex(COLUMNS.get(1))),
                    cursor.getString(cursor.getColumnIndex(COLUMNS.get(2))),
                    cursor.getString(cursor.getColumnIndex(COLUMNS.get(3))),
                    cursor.getString(cursor.getColumnIndex(COLUMNS.get(4))),
                    cursor.getString(cursor.getColumnIndex(COLUMNS.get(5)))
            ));
        }
        cursor.close();
        db.close();
        return customCommandsModelArrayList;
    }

    public void addData(int targetPositionId, ArrayList<String> Data){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues initialValues = new ContentValues();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + COLUMNS.get(0) + " + 1 WHERE " + COLUMNS.get(0) + " >= " + targetPositionId + ";");
        initialValues.put(COLUMNS.get(0), targetPositionId);
        initialValues.put(COLUMNS.get(1), Data.get(0));
        initialValues.put(COLUMNS.get(2), Data.get(1));
        initialValues.put(COLUMNS.get(3), Data.get(2));
        initialValues.put(COLUMNS.get(4), Data.get(3));
        initialValues.put(COLUMNS.get(5), Data.get(4));
        db.beginTransaction();
        db.insert(TABLE_NAME, null, initialValues);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void deleteData(ArrayList<Integer> selectedTargetIds){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMNS.get(0) + " in (" + TextUtils.join(",", selectedTargetIds) + ");");
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);

        while (cursor.moveToNext()) {
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + cursor.getPosition() + " + 1 WHERE " + COLUMNS.get(0) + " = " + cursor.getInt(0) + ";");
        }
        cursor.close();
        db.close();
    }

    public void moveData(Integer originalPosition, Integer targetPosition){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = 0 - 1 WHERE " + COLUMNS.get(0) + " = " + (originalPosition + 1) + ";");
        if (originalPosition < targetPosition){
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + COLUMNS.get(0) + " - 1 WHERE " + COLUMNS.get(0) + " > " +
                    (originalPosition + 1)  + " AND " + COLUMNS.get(0) + " < " + (targetPosition + 2) + ";");
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + (targetPosition + 1) + " WHERE " + COLUMNS.get(0) + " = -1;");
        } else {
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + COLUMNS.get(0) + " + 1 WHERE " + COLUMNS.get(0) + " > " +
                    targetPosition  + " AND " + COLUMNS.get(0) + " < " + (originalPosition + 1) + ";");
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + (targetPosition + 1) + " WHERE " + COLUMNS.get(0) + " = -1;");
        }
        db.close();
    }

    public void editData(Integer targetPosition, ArrayList<String> editData){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(1) + " = '" + editData.get(0).replace("'", "''") + "', " +
                COLUMNS.get(2) + " = '" + editData.get(1).replace("'", "''") + "', " +
                COLUMNS.get(3) + " = '" + editData.get(2).replace("'", "''") + "', " +
                COLUMNS.get(4) + " = '" + editData.get(3).replace("'", "''") + "', " +
                COLUMNS.get(5) + " = '" + editData.get(4).replace("'", "''") + "'" +
                " WHERE " + COLUMNS.get(0) + " = " + (targetPosition + 1));
        db.close();
    }


    public void resetData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMNS.get(0) + " INTEGER, " +
                COLUMNS.get(1) + " TEXT, " + COLUMNS.get(2) + " TEXT, " + COLUMNS.get(3) + " TEXT, " +
                COLUMNS.get(4) + " TEXT, " + COLUMNS.get(5) + " INTEGER)");
        ContentValues initialValues = new ContentValues();
        db.beginTransaction();
        for (String[] data: customcommandsData){
            initialValues.put(COLUMNS.get(0), data[0]);
            initialValues.put(COLUMNS.get(1), data[1]);
            initialValues.put(COLUMNS.get(2), data[2]);
            initialValues.put(COLUMNS.get(3), data[3]);
            initialValues.put(COLUMNS.get(4), data[4]);
            initialValues.put(COLUMNS.get(5), data[5]);
            db.insert(TABLE_NAME, null, initialValues);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public String backupData(String storedDBpath) {
        try {
            String currentDBPath = Environment.getDataDirectory() + "/data/" + BuildConfig.APPLICATION_ID + "/databases/" + getDatabaseName();
            if (Environment.getExternalStorageDirectory().canWrite()) {
                File currentDB = new File(currentDBPath);
                File backupDB = new File(storedDBpath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return null;
    }

    public String restoreData(String storedDBpath) {
        if (!new File(storedDBpath).exists()){
            return "db file not found.";
        }
        if (SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READONLY).getVersion()
            > this.getReadableDatabase().getVersion()) {
            return "db cannot be restored.\nReason: the db version of your backup db is larger than the current db version.";
        }
        if (!verifyDB(storedDBpath)) {
            if (!isOldDB(storedDBpath)) {
                return "Invalid DB format.";
            } else {
                if (restoreOldDBtoNewDB(storedDBpath)) {
                    return null;
                } else {
                    return "Failed to convert to the new DB format.";
                }
            }
        }
        try {
            String currentDBPath = Environment.getDataDirectory() + "/data/" + BuildConfig.APPLICATION_ID + "/databases/" + getDatabaseName();
            if (Environment.getExternalStorageDirectory().canWrite()) {
                File currentDB = new File(currentDBPath);
                File backupDB = new File(storedDBpath);
                if (backupDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            return e.toString();
        }
        return null;
    }

    private boolean verifyDB(String storedDBpath){
        SQLiteDatabase tempDB = SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READWRITE);
        if (ifTableExists(tempDB, TABLE_NAME)) {
            Cursor c = tempDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_NAME + "'", null);
            if (c.getCount()==1){
                c.close();
                c = tempDB.query(TABLE_NAME, null, null, null, null, null, null);
                String[] tempColumnNames = c.getColumnNames();
                c.close();
                if (tempColumnNames.length == COLUMNS.size()) {
                    for (int i = 0; i < tempColumnNames.length; i++){
                        if (!tempColumnNames[i].equals(COLUMNS.get(i))){
                            tempDB.close();
                            return false;
                        }
                    }
                    tempDB.close();
                    return true;
                }
            }
        }
        tempDB.close();
        return false;
    }

    private boolean isOldDB(String storedDBpath) {
        SQLiteDatabase tempDB = SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READWRITE);
        String oldDBTableName = "LAUNCHERS";
        if (ifTableExists(tempDB, oldDBTableName)) {
            Cursor c = tempDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + oldDBTableName + "'", null);
            if (c.getCount() == 1) {
                c.close();
                c = tempDB.query(oldDBTableName, null, null, null, null, null, null);
                String[] tempColumnNames = c.getColumnNames();
                c.close();
                if (tempColumnNames.length == COLUMNS.size()) {
                    if (tempColumnNames[0].equals("ID") &&
                            tempColumnNames[1].equals("BTN_LABEL") &&
                            tempColumnNames[2].equals("COMMAND") &&
                            tempColumnNames[3].equals("EXEC_MODE") &&
                            tempColumnNames[4].equals("SEND_TO_SHELL") &&
                            tempColumnNames[5].equals("RUN_AT_BOOT")) {
                        tempDB.close();
                        return true;
                    }
                }
            }
        }
        tempDB.close();
        return false;
    }

    //Convert the old db of customcommands sql to the new one.
    private boolean restoreOldDBtoNewDB(String storedDBpath) {
        SQLiteDatabase tempDB = SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READWRITE);
        try {
            SQLiteDatabase currentDB = this.getWritableDatabase();
            currentDB.execSQL("ATTACH DATABASE ? AS oldDB",new String[]{storedDBpath});
            currentDB.execSQL("DELETE FROM " + TABLE_NAME + ";");
            currentDB.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMNS.get(0) + "," +
                    COLUMNS.get(1) + "," + COLUMNS.get(2) + "," +
                    COLUMNS.get(3) + "," + COLUMNS.get(4) + "," +
                    COLUMNS.get(5) + ")" + " SELECT " + "ID,BTN_LABEL,COMMAND,SEND_TO_SHELL,EXEC_MODE,RUN_AT_BOOT" +
                    " FROM oldDB.LAUNCHERS;");
            currentDB.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(3) + " = LOWER(" + COLUMNS.get(3) + ");");
            currentDB.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(4) + " = LOWER(" + COLUMNS.get(4) + ");");
            currentDB.execSQL("DETACH DATABASE oldDB;");
            tempDB.close();
            currentDB.close();
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        tempDB.close();
        return false;
    }

    private void convertOldDBtoNewDB(SQLiteDatabase currentDB) {
        currentDB.execSQL("ATTACH DATABASE ? AS oldDB", new String[]{NhPaths.APP_DATABASE_PATH + "/KaliLaunchers"});
        currentDB.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMNS.get(0) + "," +
                COLUMNS.get(1) + "," + COLUMNS.get(2) + "," +
                COLUMNS.get(3) + "," + COLUMNS.get(4) + "," +
                COLUMNS.get(5) + ")" + " SELECT " + "ID,BTN_LABEL,COMMAND,SEND_TO_SHELL,EXEC_MODE,RUN_AT_BOOT" +
                " FROM oldDB.LAUNCHERS;");
        currentDB.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(3) + " = LOWER(" + COLUMNS.get(3) + ");");
        currentDB.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(4) + " = LOWER(" + COLUMNS.get(4) + ");");
        SQLiteDatabase.deleteDatabase(new File(NhPaths.APP_DATABASE_PATH + "/KaliLaunchers"));
    }

    private boolean ifTableExists (SQLiteDatabase tempDB, String tableName) {
        boolean tableExists = false;
        try {
            Cursor c = tempDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'", null);
            if (c.getCount()==1) {
                tableExists = true;
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return tableExists;
    }
}