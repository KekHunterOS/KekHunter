package com.offsec.nethunter.SQL;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;

import com.offsec.nethunter.BuildConfig;
import com.offsec.nethunter.models.NethunterModel;
import com.offsec.nethunter.utils.NhPaths;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

 /*
    SQLiteOpenHelper class for nethunter fragment.
 */
public class NethunterSQL extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NethunterFragment";
    private static NethunterSQL instance;
    private static final String TAG = "NethunterSQL";
    private static final String TABLE_NAME = DATABASE_NAME;
    private static ArrayList<String> COLUMNS = new ArrayList<>();
    private static final String[][] nethunterData = {
            {"1", "Kernel Version", "uname -a", "\\n", "1"},
            {"2", "Busybox Version", NhPaths.BUSYBOX + " | head -n1", "\\n", "1"},
            {"3", "Root Status", "su -v", "\\n", "1"},
            {"4", "HID status", "ls /dev/hidg* || echo \"HID interface not found.\"", "\\n", "1"},
            {"5", "Nethunter Terminal Status", "[ \"$(pm list packages | grep 'com.offsec.nhterm')\" ] && echo \"Nethunter Terminal is installed.\" || echo \"Nethunter Terminal is NOT yet installed.\"", "\\n", "1"},
            {"6", "Network Interface Status", " ip -o addr show | " + NhPaths.BUSYBOX + " awk '{print $2, $3, $4}'", "\\n", "1"},
            {"7", "External IP", NhPaths.BUSYBOX + " wget -qO - icanhazip.com || curl ipv4.icanhazip.com", "\\n", "0"}
    };

    public synchronized static NethunterSQL getInstance(Context context){
        if (instance == null) {
            instance = new NethunterSQL(context.getApplicationContext());
        }
        return instance;
    }

    private NethunterSQL(Context context) {
        super(context, DATABASE_NAME, null, 1);
        // Add your default column here;
        COLUMNS.add("id");
        COLUMNS.add("TitleName");
        COLUMNS.add("CommandforResult");
        COLUMNS.add("Delimiter");
        COLUMNS.add("RunOnCreate");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMNS.get(0) + " INTEGER, " +
                COLUMNS.get(1) + " TEXT, " + COLUMNS.get(2) +  " TEXT, " + COLUMNS.get(3) + " INTEGER, " + COLUMNS.get(4) + " TEXT)");
        ContentValues initialValues = new ContentValues();
        db.beginTransaction();
        for (String[] data: nethunterData){
            initialValues.put(COLUMNS.get(0), data[0]);
            initialValues.put(COLUMNS.get(1), data[1]);
            initialValues.put(COLUMNS.get(2), data[2]);
            initialValues.put(COLUMNS.get(3), data[3]);
            initialValues.put(COLUMNS.get(4), data[4]);
            db.insert(TABLE_NAME, null, initialValues);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public ArrayList<NethunterModel> bindData(ArrayList<NethunterModel> nethunterModelArrayList) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);
        while (cursor.moveToNext()) {
            nethunterModelArrayList.add(new NethunterModel(
                    cursor.getString(cursor.getColumnIndex(COLUMNS.get(1))),
                    cursor.getString(cursor.getColumnIndex(COLUMNS.get(2))),
                    cursor.getString(cursor.getColumnIndex(COLUMNS.get(3))),
                    cursor.getString(cursor.getColumnIndex(COLUMNS.get(4))),
                    "".split("\\n")
            ));
        }
        cursor.close();
        db.close();
        return nethunterModelArrayList;
    }

    public void addData(int targetPositionId, ArrayList<String> addData){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues initialValues = new ContentValues();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + COLUMNS.get(0) + " + 1 WHERE " + COLUMNS.get(0) + " >= " + targetPositionId + ";");
        initialValues.put(COLUMNS.get(0), targetPositionId);
        initialValues.put(COLUMNS.get(1), addData.get(0));
        initialValues.put(COLUMNS.get(2), addData.get(1));
        initialValues.put(COLUMNS.get(3), addData.get(2));
        initialValues.put(COLUMNS.get(4), addData.get(3));
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
                COLUMNS.get(4) + " = '" + editData.get(3).replace("'", "''") + "'" +
                " WHERE " + COLUMNS.get(0) + " = " + (targetPosition + 1));
        db.close();
    }

    public void resetData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMNS.get(0) + " INTEGER, " +
                COLUMNS.get(1) + " TEXT, " + COLUMNS.get(2) +  " TEXT, " + COLUMNS.get(3) + " INTEGER, " + COLUMNS.get(4) + " TEXT)");
        ContentValues initialValues = new ContentValues();
        db.beginTransaction();
        for (String[] data: nethunterData){
            initialValues.put(COLUMNS.get(0), data[0]);
            initialValues.put(COLUMNS.get(1), data[1]);
            initialValues.put(COLUMNS.get(2), data[2]);
            initialValues.put(COLUMNS.get(3), data[3]);
            initialValues.put(COLUMNS.get(4), data[4]);
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
                //return "db is successfully backup to " + storedDBpath;
                //NhPaths.showMessage(context, "db is successfully backup to " + storedDBpath);
            }
        } catch (Exception e) {
            //new AlertDialog.Builder(context).setTitle("Failed to backup the DB.").setMessage(e.getMessage()).create().show();
            e.printStackTrace();
            return e.toString();
        }
        return null;
    }

    public String restoreData(String storedDBpath) {
        if (!new File(storedDBpath).exists()){
            //new AlertDialog.Builder(context).setTitle("Failed to restore the DB.").setMessage("db file not found.").create().show();
            return "db file not found.";
        }
        if (!verifyDB(storedDBpath)) {
            //new AlertDialog.Builder(context).setTitle("Failed to restore the DB.").setMessage("invalid columns format.").create().show();
            return "invalid columns format.";
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
                    //NhPaths.showMessage(context, "db is successfully restored to " + currentDBPath);
                    //return "db is successfully restored to " + currentDBPath;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return null;
    }

    private boolean verifyDB(String storedDBpath){
        SQLiteDatabase tempDB = SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor c = tempDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_NAME + "'", null);
        if (c.getCount()==1){
            c.close();
            c = tempDB.query(TABLE_NAME, null, null, null, null, null, null);
            String[] tempColumnNames = c.getColumnNames();
            c.close();
            if (tempColumnNames.length != COLUMNS.size()) {
                tempDB.close();
                return false;
            }
            for (int i = 0; i < tempColumnNames.length; i++){
                if (!tempColumnNames[i].equals(COLUMNS.get(i))){
                    tempDB.close();
                    return false;
                }
            }
            tempDB.close();
            return true;
        }
        tempDB.close();
        return false;
    }
}
