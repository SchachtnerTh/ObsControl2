package de.tomschachtner.obscontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static de.tomschachtner.obscontrol.OBSHotkeysDBContract.*;

public class OBSHotkeysDatabaseHelper
    extends SQLiteOpenHelper
{
    private static final String TAG = "OBSHotkeysDatabaseHelper";
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "OBSHotkeys.db";

    public OBSHotkeysDatabaseHelper(Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
//        SQLiteDatabase db = getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put(OBSHotkeyTbl.COLUMN_NAME_HOTKEY, "OBS_KEY_F12");
//        cv.put(OBSHotkeyTbl.COLUMN_NAME_MOD_ALT, 0);
//        cv.put(OBSHotkeyTbl.COLUMN_NAME_MOD_CMD, 0);
//        cv.put(OBSHotkeyTbl.COLUMN_NAME_MOD_CTRL, 0);
//        cv.put(OBSHotkeyTbl.COLUMN_NAME_MOD_SHIFT, 0);
//        cv.put(OBSHotkeyTbl.COLUMN_NAME_NAME, "Test");
//        cv.put(OBSHotkeyTbl.COLUMN_NAME_ORDER, 0);
//        db.insert(OBSHotkeyTbl.TABLE_NAME, null, cv);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_HOTKEY_TABLE);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL(SQL_DELETE_HOTKEY_TABLE);
        onCreate(sqLiteDatabase);
    }

    public String getHotkeyName(SQLiteDatabase db, int orderPosition) {
        String[] projection = {
                OBSHotkeyTbl._ID,
                OBSHotkeyTbl.COLUMN_NAME_NAME,
                OBSHotkeyTbl.COLUMN_NAME_HOTKEY,
                OBSHotkeyTbl.COLUMN_NAME_MOD_SHIFT,
                OBSHotkeyTbl.COLUMN_NAME_MOD_ALT,
                OBSHotkeyTbl.COLUMN_NAME_MOD_CTRL,
                OBSHotkeyTbl.COLUMN_NAME_MOD_CMD,
                OBSHotkeyTbl.COLUMN_NAME_ORDER
        };
        String selection = OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_ORDER + " = ?";
        String[] selectionArgs = { String.valueOf(orderPosition) };
        String sortOrder = OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_ORDER + " ASC";

        Cursor cursor = db.query(
                OBSHotkeysDBContract.OBSHotkeyTbl.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(OBSHotkeyTbl.COLUMN_NAME_NAME));
    }

    public long getItemsCount(SQLiteDatabase db) {
        return DatabaseUtils.queryNumEntries(db, OBSHotkeyTbl.TABLE_NAME);
    }

    public String getHotkeyKey(SQLiteDatabase db, int id) {
        String[] projection = {
                OBSHotkeyTbl._ID,
                OBSHotkeyTbl.COLUMN_NAME_NAME,
                OBSHotkeyTbl.COLUMN_NAME_HOTKEY,
                OBSHotkeyTbl.COLUMN_NAME_MOD_SHIFT,
                OBSHotkeyTbl.COLUMN_NAME_MOD_ALT,
                OBSHotkeyTbl.COLUMN_NAME_MOD_CTRL,
                OBSHotkeyTbl.COLUMN_NAME_MOD_CMD,
                OBSHotkeyTbl.COLUMN_NAME_ORDER
        };
        String selection = OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_ORDER + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        String sortOrder = OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_ORDER + " ASC";

        Cursor cursor = db.query(
                OBSHotkeysDBContract.OBSHotkeyTbl.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(OBSHotkeyTbl.COLUMN_NAME_HOTKEY));
    }
}
