package de.tomschachtner.obscontrol;

import android.provider.BaseColumns;

public final class OBSHotkeysDBContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private OBSHotkeysDBContract() {}

    /* Inner class that defines the table contents */
    public static class OBSHotkeyTbl implements BaseColumns {
        public static final String TABLE_NAME = "hotkey";
        public static final String COLUMN_NAME_ORDER = "reihenfolge";
        public static final String COLUMN_NAME_HOTKEY = "hotkey";
        public static final String COLUMN_NAME_MOD_SHIFT = "shift";
        public static final String COLUMN_NAME_MOD_ALT = "alt";
        public static final String COLUMN_NAME_MOD_CTRL = "ctrl";
        public static final String COLUMN_NAME_MOD_CMD = "cmd";
        public static final String COLUMN_NAME_NAME = "name";
    }

    public static class OBSHotkeyList implements BaseColumns {
        public static final String TABLE_NAME = "hotkey_list";
        public static final String COLUMN_NAME_HOTKEY = "hotkey";
    }

    public static final String SQL_CREATE_HOTKEY_TABLE =
            "CREATE TABLE " + OBSHotkeyTbl.TABLE_NAME + " (" +
                    OBSHotkeyTbl._ID + " INTEGER PRIMARY KEY," +
                    OBSHotkeyTbl.COLUMN_NAME_ORDER + " INTEGER," +
                    OBSHotkeyTbl.COLUMN_NAME_HOTKEY + " TEXT," +
                    OBSHotkeyTbl.COLUMN_NAME_MOD_SHIFT + " INTEGER," +
                    OBSHotkeyTbl.COLUMN_NAME_MOD_ALT + " INTEGER," +
                    OBSHotkeyTbl.COLUMN_NAME_MOD_CTRL + " INTEGER," +
                    OBSHotkeyTbl.COLUMN_NAME_MOD_CMD + " INTEGER," +
                    OBSHotkeyTbl.COLUMN_NAME_NAME + " TEXT)";

    public static final String SQL_CREATE_HOTKEYLIST_TABLE =
            "CREATE TABLE " + OBSHotkeyList.TABLE_NAME + " (" +
                    OBSHotkeyList._ID + " INTEGER PRIMARY KEY," +
                    OBSHotkeyList.COLUMN_NAME_HOTKEY + " Text)";

    public static final String SQL_DELETE_HOTKEY_TABLE =
            "DROP TABLE IF EXISTS " + OBSHotkeyTbl.TABLE_NAME;

    public static final String SQL_DELETE_HOTKEYLIST_TABLE =
            "DROP TABLE IF EXISTS " + OBSHotkeyList.TABLE_NAME;
}
