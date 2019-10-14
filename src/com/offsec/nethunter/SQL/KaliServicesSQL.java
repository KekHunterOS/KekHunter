package com.offsec.nethunter.SQL;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;

import com.offsec.nethunter.utils.NhPaths;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class KaliServicesSQL extends SQLiteOpenHelper {
	private Context context;
	private static final String DATABASE_NAME = "FragmentKaliService";
	private static final String TAG = "KaliServicesSQL";
	private static final String TABLE_NAME = "FragmentKaliService";
	private final ArrayList<String> COLUMNS = new ArrayList<>();
	private static final String[][] kaliserviceData = {
			{"1", "SSH", "service ssh start", "service ssh stop", "sshd", "0"},
			{"2", "APACHE2", "service apache2 start", "service apache2 stop", "apache2", "0"},
			{"3", "POSTGRESQL", "service postgresql start", "service postgresql stop", "postgres", "0"},
			{"4", "DNSMASQ", "service dnsmasq start", "service dnsmasq stop", "dnsmasq", "0"}
	};

	public KaliServicesSQL(Context context) {
		super(context, DATABASE_NAME, null, 1);
		// Add your default column here;f
		this.context = context;
		COLUMNS.add("id");
		COLUMNS.add("ServiceName");
		COLUMNS.add("CommandforStartService");
		COLUMNS.add("CommandforStopService");
		COLUMNS.add("CommandforCheckServiceStatus");
		COLUMNS.add("RunOnChrootStart");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMNS.get(0) + " INTEGER, " +
				COLUMNS.get(1) + " TEXT, " + COLUMNS.get(2) +  " TEXT, " +
				COLUMNS.get(3) + " TEXT, " + COLUMNS.get(4) + " TEXT, " +
				COLUMNS.get(5) + " INTEGER)");
		ContentValues initialValues = new ContentValues();
		for (String[] data: kaliserviceData){
			initialValues.put(COLUMNS.get(0), data[0]);
			initialValues.put(COLUMNS.get(1), data[1]);
			initialValues.put(COLUMNS.get(2), data[2]);
			initialValues.put(COLUMNS.get(3), data[3]);
			initialValues.put(COLUMNS.get(4), data[4]);
			initialValues.put(COLUMNS.get(5), data[5]);
			db.insert(TABLE_NAME, null, initialValues);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		this.onCreate(db);
	}

	public ArrayList<ArrayList<String>> getData() {
		ArrayList<ArrayList<String>> Data = new ArrayList<>();
		ArrayList<String> Id = new ArrayList<>();
		ArrayList<String> ServiceName = new ArrayList<>();
		ArrayList<String> CommandforStartService = new ArrayList<>();
		ArrayList<String> CommandforStopService = new ArrayList<>();
		ArrayList<String> CommandforCheckServiceStatus = new ArrayList<>();
		ArrayList<String> RunOnChrootStart = new ArrayList<>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);
		while (cursor.moveToNext()) {
			Id.add(cursor.getString(cursor.getColumnIndex(COLUMNS.get(0))));
			ServiceName.add(cursor.getString(cursor.getColumnIndex(COLUMNS.get(1))));
			CommandforStartService.add(cursor.getString(cursor.getColumnIndex(COLUMNS.get(2))));
			CommandforStopService.add(cursor.getString(cursor.getColumnIndex(COLUMNS.get(3))));
			CommandforCheckServiceStatus.add(cursor.getString(cursor.getColumnIndex(COLUMNS.get(4))));
			RunOnChrootStart.add(cursor.getString(cursor.getColumnIndex(COLUMNS.get(5))));
		}
		Data.add(Id);
		Data.add(ServiceName);
		Data.add(CommandforStartService);
		Data.add(CommandforStopService);
		Data.add(CommandforCheckServiceStatus);
		Data.add(RunOnChrootStart);
		cursor.close();
		db.close();
		return Data;
	}

	public void addData(int targetPositionId, ArrayList<String> Data){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues initialValues = new ContentValues();
		db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + COLUMNS.get(0) + " + 1 WHERE " + COLUMNS.get(0) + " >= " + targetPositionId + ";");
		initialValues.put(COLUMNS.get(0), Data.get(0));
		initialValues.put(COLUMNS.get(1), Data.get(1));
		initialValues.put(COLUMNS.get(2), Data.get(2));
		initialValues.put(COLUMNS.get(3), Data.get(3));
		initialValues.put(COLUMNS.get(4), Data.get(4));
		initialValues.put(COLUMNS.get(5), Data.get(5));
		db.insert(TABLE_NAME, null, initialValues);
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
					(originalPosition + 1)  + " AND " + COLUMNS.get(0) + " < " + (targetPosition + 1) + ";");
			db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + (targetPosition) + " WHERE " + COLUMNS.get(0) + " = -1;");
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
		for (String[] data: kaliserviceData){
			initialValues.put(COLUMNS.get(0), data[0]);
			initialValues.put(COLUMNS.get(1), data[1]);
			initialValues.put(COLUMNS.get(2), data[2]);
			initialValues.put(COLUMNS.get(3), data[3]);
			initialValues.put(COLUMNS.get(4), data[4]);
			initialValues.put(COLUMNS.get(5), data[5]);
			db.insert(TABLE_NAME, null, initialValues);
		}
		db.close();
	}

	public boolean backupData(String storedDBpath) {
		try {
			String currentDBPath = Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/" + getDatabaseName();
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
				NhPaths.showMessage(context, "db is successfully backup to " + storedDBpath);
			}
		} catch (Exception e) {
			new AlertDialog.Builder(context).setTitle("Failed to backup the DB.").setMessage(e.getMessage()).create().show();
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean restoreData(String storedDBpath) {
		if (!new File(storedDBpath).exists()){
			new AlertDialog.Builder(context).setTitle("Failed to restore the DB.").setMessage("db file not found.").create().show();
			return false;
		}
		if (!verifyDB(storedDBpath)) {
			new AlertDialog.Builder(context).setTitle("Failed to restore the DB.").setMessage("invalid columns format.").create().show();
			return false;
		}
		try {
			String currentDBPath = Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/" + getDatabaseName();
			if (Environment.getExternalStorageDirectory().canWrite()) {
				File currentDB = new File(currentDBPath);
				File backupDB = new File(storedDBpath);
				if (backupDB.exists()) {
					FileChannel src = new FileInputStream(backupDB).getChannel();
					FileChannel dst = new FileOutputStream(currentDB).getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
					NhPaths.showMessage(context, "db is successfully restored to " + currentDBPath);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
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