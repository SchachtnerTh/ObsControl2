package de.tomschachtner.obscontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Editable;

import androidx.core.content.ContextCompat;

import static de.tomschachtner.obscontrol.OBSHotkeysDBContract.*;

public class OBSHotkeysDatabaseHelper
    extends SQLiteOpenHelper
{
    private static final String TAG = "OBSHotkeysDatabaseHelper";
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "OBSHotkeys.db";

    // region key table data
    String[] obsKeys = new String[] {"OBS_KEY_RETURN",
            "OBS_KEY_ENTER",
            "OBS_KEY_ESCAPE",
            "OBS_KEY_TAB",
            "OBS_KEY_BACKTAB",
            "OBS_KEY_BACKSPACE",
            "OBS_KEY_INSERT",
            "OBS_KEY_DELETE",
            "OBS_KEY_PAUSE",
            "OBS_KEY_PRINT",
            "OBS_KEY_SYSREQ",
            "OBS_KEY_CLEAR",
            "OBS_KEY_HOME",
            "OBS_KEY_END",
            "OBS_KEY_LEFT",
            "OBS_KEY_UP",
            "OBS_KEY_RIGHT",
            "OBS_KEY_DOWN",
            "OBS_KEY_PAGEUP",
            "OBS_KEY_PAGEDOWN",
            "OBS_KEY_SHIFT",
            "OBS_KEY_CONTROL",
            "OBS_KEY_META",
            "OBS_KEY_ALT",
            "OBS_KEY_ALTGR",
            "OBS_KEY_CAPSLOCK",
            "OBS_KEY_NUMLOCK",
            "OBS_KEY_SCROLLLOCK",
            "OBS_KEY_F1",
            "OBS_KEY_F2",
            "OBS_KEY_F3",
            "OBS_KEY_F4",
            "OBS_KEY_F5",
            "OBS_KEY_F6",
            "OBS_KEY_F7",
            "OBS_KEY_F8",
            "OBS_KEY_F9",
            "OBS_KEY_F10",
            "OBS_KEY_F11",
            "OBS_KEY_F12",
            "OBS_KEY_F13",
            "OBS_KEY_F14",
            "OBS_KEY_F15",
            "OBS_KEY_F16",
            "OBS_KEY_F17",
            "OBS_KEY_F18",
            "OBS_KEY_F19",
            "OBS_KEY_F20",
            "OBS_KEY_F21",
            "OBS_KEY_F22",
            "OBS_KEY_F23",
            "OBS_KEY_F24",
            "OBS_KEY_F25",
            "OBS_KEY_F26",
            "OBS_KEY_F27",
            "OBS_KEY_F28",
            "OBS_KEY_F29",
            "OBS_KEY_F30",
            "OBS_KEY_F31",
            "OBS_KEY_F32",
            "OBS_KEY_F33",
            "OBS_KEY_F34",
            "OBS_KEY_F35",
            "OBS_KEY_MENU",
            "OBS_KEY_HYPER_L",
            "OBS_KEY_HYPER_R",
            "OBS_KEY_HELP",
            "OBS_KEY_DIRECTION_L",
            "OBS_KEY_DIRECTION_R",
            "OBS_KEY_SPACE",
            "OBS_KEY_EXCLAM",
            "OBS_KEY_QUOTEDBL",
            "OBS_KEY_NUMBERSIGN",
            "OBS_KEY_DOLLAR",
            "OBS_KEY_PERCENT",
            "OBS_KEY_AMPERSAND",
            "OBS_KEY_APOSTROPHE",
            "OBS_KEY_PARENLEFT",
            "OBS_KEY_PARENRIGHT",
            "OBS_KEY_ASTERISK",
            "OBS_KEY_PLUS",
            "OBS_KEY_COMMA",
            "OBS_KEY_MINUS",
            "OBS_KEY_PERIOD",
            "OBS_KEY_SLASH",
            "OBS_KEY_0",
            "OBS_KEY_1",
            "OBS_KEY_2",
            "OBS_KEY_3",
            "OBS_KEY_4",
            "OBS_KEY_5",
            "OBS_KEY_6",
            "OBS_KEY_7",
            "OBS_KEY_8",
            "OBS_KEY_9",
            "OBS_KEY_NUMEQUAL",
            "OBS_KEY_NUMASTERISK",
            "OBS_KEY_NUMPLUS",
            "OBS_KEY_NUMCOMMA",
            "OBS_KEY_NUMMINUS",
            "OBS_KEY_NUMPERIOD",
            "OBS_KEY_NUMSLASH",
            "OBS_KEY_NUM0",
            "OBS_KEY_NUM1",
            "OBS_KEY_NUM2",
            "OBS_KEY_NUM3",
            "OBS_KEY_NUM4",
            "OBS_KEY_NUM5",
            "OBS_KEY_NUM6",
            "OBS_KEY_NUM7",
            "OBS_KEY_NUM8",
            "OBS_KEY_NUM9",
            "OBS_KEY_COLON",
            "OBS_KEY_SEMICOLON",
            "OBS_KEY_QUOTE",
            "OBS_KEY_LESS",
            "OBS_KEY_EQUAL",
            "OBS_KEY_GREATER",
            "OBS_KEY_QUESTION",
            "OBS_KEY_AT",
            "OBS_KEY_A",
            "OBS_KEY_B",
            "OBS_KEY_C",
            "OBS_KEY_D",
            "OBS_KEY_E",
            "OBS_KEY_F",
            "OBS_KEY_G",
            "OBS_KEY_H",
            "OBS_KEY_I",
            "OBS_KEY_J",
            "OBS_KEY_K",
            "OBS_KEY_L",
            "OBS_KEY_M",
            "OBS_KEY_N",
            "OBS_KEY_O",
            "OBS_KEY_P",
            "OBS_KEY_Q",
            "OBS_KEY_R",
            "OBS_KEY_S",
            "OBS_KEY_T",
            "OBS_KEY_U",
            "OBS_KEY_V",
            "OBS_KEY_W",
            "OBS_KEY_X",
            "OBS_KEY_Y",
            "OBS_KEY_Z",
            "OBS_KEY_BRACKETLEFT",
            "OBS_KEY_BACKSLASH",
            "OBS_KEY_BRACKETRIGHT",
            "OBS_KEY_ASCIICIRCUM",
            "OBS_KEY_UNDERSCORE",
            "OBS_KEY_QUOTELEFT",
            "OBS_KEY_BRACELEFT",
            "OBS_KEY_BAR",
            "OBS_KEY_BRACERIGHT",
            "OBS_KEY_ASCIITILDE",
            "OBS_KEY_NOBREAKSPACE",
            "OBS_KEY_EXCLAMDOWN",
            "OBS_KEY_CENT",
            "OBS_KEY_STERLING",
            "OBS_KEY_CURRENCY",
            "OBS_KEY_YEN",
            "OBS_KEY_BROKENBAR",
            "OBS_KEY_SECTION",
            "OBS_KEY_DIAERESIS",
            "OBS_KEY_COPYRIGHT",
            "OBS_KEY_ORDFEMININE",
            "OBS_KEY_GUILLEMOTLEFT",
            "OBS_KEY_NOTSIGN",
            "OBS_KEY_HYPHEN",
            "OBS_KEY_REGISTERED",
            "OBS_KEY_MACRON",
            "OBS_KEY_DEGREE",
            "OBS_KEY_PLUSMINUS",
            "OBS_KEY_TWOSUPERIOR",
            "OBS_KEY_THREESUPERIOR",
            "OBS_KEY_ACUTE",
            "OBS_KEY_MU",
            "OBS_KEY_PARAGRAPH",
            "OBS_KEY_PERIODCENTERED",
            "OBS_KEY_CEDILLA",
            "OBS_KEY_ONESUPERIOR",
            "OBS_KEY_MASCULINE",
            "OBS_KEY_GUILLEMOTRIGHT",
            "OBS_KEY_ONEQUARTER",
            "OBS_KEY_ONEHALF",
            "OBS_KEY_THREEQUARTERS",
            "OBS_KEY_QUESTIONDOWN",
            "OBS_KEY_AGRAVE",
            "OBS_KEY_AACUTE",
            "OBS_KEY_ACIRCUMFLEX",
            "OBS_KEY_ATILDE",
            "OBS_KEY_ADIAERESIS",
            "OBS_KEY_ARING",
            "OBS_KEY_AE",
            "OBS_KEY_CCEDILLA",
            "OBS_KEY_EGRAVE",
            "OBS_KEY_EACUTE",
            "OBS_KEY_ECIRCUMFLEX",
            "OBS_KEY_EDIAERESIS",
            "OBS_KEY_IGRAVE",
            "OBS_KEY_IACUTE",
            "OBS_KEY_ICIRCUMFLEX",
            "OBS_KEY_IDIAERESIS",
            "OBS_KEY_ETH",
            "OBS_KEY_NTILDE",
            "OBS_KEY_OGRAVE",
            "OBS_KEY_OACUTE",
            "OBS_KEY_OCIRCUMFLEX",
            "OBS_KEY_OTILDE",
            "OBS_KEY_ODIAERESIS",
            "OBS_KEY_MULTIPLY",
            "OBS_KEY_OOBLIQUE",
            "OBS_KEY_UGRAVE",
            "OBS_KEY_UACUTE",
            "OBS_KEY_UCIRCUMFLEX",
            "OBS_KEY_UDIAERESIS",
            "OBS_KEY_YACUTE",
            "OBS_KEY_THORN",
            "OBS_KEY_SSHARP",
            "OBS_KEY_DIVISION",
            "OBS_KEY_YDIAERESIS",
            "OBS_KEY_MULTI_KEY",
            "OBS_KEY_CODEINPUT",
            "OBS_KEY_SINGLECANDIDATE",
            "OBS_KEY_MULTIPLECANDIDATE",
            "OBS_KEY_PREVIOUSCANDIDATE",
            "OBS_KEY_MODE_SWITCH",
            "OBS_KEY_KANJI",
            "OBS_KEY_MUHENKAN",
            "OBS_KEY_HENKAN",
            "OBS_KEY_ROMAJI",
            "OBS_KEY_HIRAGANA",
            "OBS_KEY_KATAKANA",
            "OBS_KEY_HIRAGANA_KATAKANA",
            "OBS_KEY_ZENKAKU",
            "OBS_KEY_HANKAKU",
            "OBS_KEY_ZENKAKU_HANKAKU",
            "OBS_KEY_TOUROKU",
            "OBS_KEY_MASSYO",
            "OBS_KEY_KANA_LOCK",
            "OBS_KEY_KANA_SHIFT",
            "OBS_KEY_EISU_SHIFT",
            "OBS_KEY_EISU_TOGGLE",
            "OBS_KEY_HANGUL",
            "OBS_KEY_HANGUL_START",
            "OBS_KEY_HANGUL_END",
            "OBS_KEY_HANGUL_HANJA",
            "OBS_KEY_HANGUL_JAMO",
            "OBS_KEY_HANGUL_ROMAJA",
            "OBS_KEY_HANGUL_JEONJA",
            "OBS_KEY_HANGUL_BANJA",
            "OBS_KEY_HANGUL_PREHANJA",
            "OBS_KEY_HANGUL_POSTHANJA",
            "OBS_KEY_HANGUL_SPECIAL",
            "OBS_KEY_DEAD_GRAVE",
            "OBS_KEY_DEAD_ACUTE",
            "OBS_KEY_DEAD_CIRCUMFLEX",
            "OBS_KEY_DEAD_TILDE",
            "OBS_KEY_DEAD_MACRON",
            "OBS_KEY_DEAD_BREVE",
            "OBS_KEY_DEAD_ABOVEDOT",
            "OBS_KEY_DEAD_DIAERESIS",
            "OBS_KEY_DEAD_ABOVERING",
            "OBS_KEY_DEAD_DOUBLEACUTE",
            "OBS_KEY_DEAD_CARON",
            "OBS_KEY_DEAD_CEDILLA",
            "OBS_KEY_DEAD_OGONEK",
            "OBS_KEY_DEAD_IOTA",
            "OBS_KEY_DEAD_VOICED_SOUND",
            "OBS_KEY_DEAD_SEMIVOICED_SOUND",
            "OBS_KEY_DEAD_BELOWDOT",
            "OBS_KEY_DEAD_HOOK",
            "OBS_KEY_DEAD_HORN",
            "OBS_KEY_BACK",
            "OBS_KEY_FORWARD",
            "OBS_KEY_STOP",
            "OBS_KEY_REFRESH",
            "OBS_KEY_VOLUMEDOWN",
            "OBS_KEY_VOLUMEMUTE",
            "OBS_KEY_VOLUMEUP",
            "OBS_KEY_BASSBOOST",
            "OBS_KEY_BASSUP",
            "OBS_KEY_BASSDOWN",
            "OBS_KEY_TREBLEUP",
            "OBS_KEY_TREBLEDOWN",
            "OBS_KEY_MEDIAPLAY",
            "OBS_KEY_MEDIASTOP",
            "OBS_KEY_MEDIAPREVIOUS",
            "OBS_KEY_MEDIANEXT",
            "OBS_KEY_MEDIARECORD",
            "OBS_KEY_MEDIAPAUSE",
            "OBS_KEY_MEDIATOGGLEPLAYPAUSE",
            "OBS_KEY_HOMEPAGE",
            "OBS_KEY_FAVORITES",
            "OBS_KEY_SEARCH",
            "OBS_KEY_STANDBY",
            "OBS_KEY_OPENURL",
            "OBS_KEY_LAUNCHMAIL",
            "OBS_KEY_LAUNCHMEDIA",
            "OBS_KEY_LAUNCH0",
            "OBS_KEY_LAUNCH1",
            "OBS_KEY_LAUNCH2",
            "OBS_KEY_LAUNCH3",
            "OBS_KEY_LAUNCH4",
            "OBS_KEY_LAUNCH5",
            "OBS_KEY_LAUNCH6",
            "OBS_KEY_LAUNCH7",
            "OBS_KEY_LAUNCH8",
            "OBS_KEY_LAUNCH9",
            "OBS_KEY_LAUNCHA",
            "OBS_KEY_LAUNCHB",
            "OBS_KEY_LAUNCHC",
            "OBS_KEY_LAUNCHD",
            "OBS_KEY_LAUNCHE",
            "OBS_KEY_LAUNCHF",
            "OBS_KEY_LAUNCHG",
            "OBS_KEY_LAUNCHH",
            "OBS_KEY_MONBRIGHTNESSUP",
            "OBS_KEY_MONBRIGHTNESSDOWN",
            "OBS_KEY_KEYBOARDLIGHTONOFF",
            "OBS_KEY_KEYBOARDBRIGHTNESSUP",
            "OBS_KEY_KEYBOARDBRIGHTNESSDOWN",
            "OBS_KEY_POWEROFF",
            "OBS_KEY_WAKEUP",
            "OBS_KEY_EJECT",
            "OBS_KEY_SCREENSAVER",
            "OBS_KEY_WWW",
            "OBS_KEY_MEMO",
            "OBS_KEY_LIGHTBULB",
            "OBS_KEY_SHOP",
            "OBS_KEY_HISTORY",
            "OBS_KEY_ADDFAVORITE",
            "OBS_KEY_HOTLINKS",
            "OBS_KEY_BRIGHTNESSADJUST",
            "OBS_KEY_FINANCE",
            "OBS_KEY_COMMUNITY",
            "OBS_KEY_AUDIOREWIND",
            "OBS_KEY_BACKFORWARD",
            "OBS_KEY_APPLICATIONLEFT",
            "OBS_KEY_APPLICATIONRIGHT",
            "OBS_KEY_BOOK",
            "OBS_KEY_CD",
            "OBS_KEY_CALCULATOR",
            "OBS_KEY_TODOLIST",
            "OBS_KEY_CLEARGRAB",
            "OBS_KEY_CLOSE",
            "OBS_KEY_COPY",
            "OBS_KEY_CUT",
            "OBS_KEY_DISPLAY",
            "OBS_KEY_DOS",
            "OBS_KEY_DOCUMENTS",
            "OBS_KEY_EXCEL",
            "OBS_KEY_EXPLORER",
            "OBS_KEY_GAME",
            "OBS_KEY_GO",
            "OBS_KEY_ITOUCH",
            "OBS_KEY_LOGOFF",
            "OBS_KEY_MARKET",
            "OBS_KEY_MEETING",
            "OBS_KEY_MENUKB",
            "OBS_KEY_MENUPB",
            "OBS_KEY_MYSITES",
            "OBS_KEY_NEWS",
            "OBS_KEY_OFFICEHOME",
            "OBS_KEY_OPTION",
            "OBS_KEY_PASTE",
            "OBS_KEY_PHONE",
            "OBS_KEY_CALENDAR",
            "OBS_KEY_REPLY",
            "OBS_KEY_RELOAD",
            "OBS_KEY_ROTATEWINDOWS",
            "OBS_KEY_ROTATIONPB",
            "OBS_KEY_ROTATIONKB",
            "OBS_KEY_SAVE",
            "OBS_KEY_SEND",
            "OBS_KEY_SPELL",
            "OBS_KEY_SPLITSCREEN",
            "OBS_KEY_SUPPORT",
            "OBS_KEY_TASKPANE",
            "OBS_KEY_TERMINAL",
            "OBS_KEY_TOOLS",
            "OBS_KEY_TRAVEL",
            "OBS_KEY_VIDEO",
            "OBS_KEY_WORD",
            "OBS_KEY_XFER",
            "OBS_KEY_ZOOMIN",
            "OBS_KEY_ZOOMOUT",
            "OBS_KEY_AWAY",
            "OBS_KEY_MESSENGER",
            "OBS_KEY_WEBCAM",
            "OBS_KEY_MAILFORWARD",
            "OBS_KEY_PICTURES",
            "OBS_KEY_MUSIC",
            "OBS_KEY_BATTERY",
            "OBS_KEY_BLUETOOTH",
            "OBS_KEY_WLAN",
            "OBS_KEY_UWB",
            "OBS_KEY_AUDIOFORWARD",
            "OBS_KEY_AUDIOREPEAT",
            "OBS_KEY_AUDIORANDOMPLAY",
            "OBS_KEY_SUBTITLE",
            "OBS_KEY_AUDIOCYCLETRACK",
            "OBS_KEY_TIME",
            "OBS_KEY_HIBERNATE",
            "OBS_KEY_VIEW",
            "OBS_KEY_TOPMENU",
            "OBS_KEY_POWERDOWN",
            "OBS_KEY_SUSPEND",
            "OBS_KEY_CONTRASTADJUST",
            "OBS_KEY_MEDIALAST",
            "OBS_KEY_CALL",
            "OBS_KEY_CAMERA",
            "OBS_KEY_CAMERAFOCUS",
            "OBS_KEY_CONTEXT1",
            "OBS_KEY_CONTEXT2",
            "OBS_KEY_CONTEXT3",
            "OBS_KEY_CONTEXT4",
            "OBS_KEY_FLIP",
            "OBS_KEY_HANGUP",
            "OBS_KEY_NO",
            "OBS_KEY_SELECT",
            "OBS_KEY_YES",
            "OBS_KEY_TOGGLECALLHANGUP",
            "OBS_KEY_VOICEDIAL",
            "OBS_KEY_LASTNUMBERREDIAL",
            "OBS_KEY_EXECUTE",
            "OBS_KEY_PRINTER",
            "OBS_KEY_PLAY",
            "OBS_KEY_SLEEP",
            "OBS_KEY_ZOOM",
            "OBS_KEY_CANCEL",
            "OBS_KEY_BACKSLASH_RT102",
            "OBS_KEY_OPEN",
            "OBS_KEY_FIND",
            "OBS_KEY_REDO",
            "OBS_KEY_UNDO",
            "OBS_KEY_FRONT",
            "OBS_KEY_PROPS",
            "OBS_KEY_VK_CANCEL",
            "OBS_KEY_0x07",
            "OBS_KEY_0x0A",
            "OBS_KEY_0x0B",
            "OBS_KEY_0x0E",
            "OBS_KEY_0x0F",
            "OBS_KEY_0x16",
            "OBS_KEY_VK_JUNJA",
            "OBS_KEY_VK_FINAL",
            "OBS_KEY_0x1A",
            "OBS_KEY_VK_ACCEPT",
            "OBS_KEY_VK_MODECHANGE",
            "OBS_KEY_VK_SELECT",
            "OBS_KEY_VK_PRINT",
            "OBS_KEY_VK_EXECUTE",
            "OBS_KEY_VK_HELP",
            "OBS_KEY_0x30",
            "OBS_KEY_0x31",
            "OBS_KEY_0x32",
            "OBS_KEY_0x33",
            "OBS_KEY_0x34",
            "OBS_KEY_0x35",
            "OBS_KEY_0x36",
            "OBS_KEY_0x37",
            "OBS_KEY_0x38",
            "OBS_KEY_0x39",
            "OBS_KEY_0x3A",
            "OBS_KEY_0x3B",
            "OBS_KEY_0x3C",
            "OBS_KEY_0x3D",
            "OBS_KEY_0x3E",
            "OBS_KEY_0x3F",
            "OBS_KEY_0x40",
            "OBS_KEY_0x41",
            "OBS_KEY_0x42",
            "OBS_KEY_0x43",
            "OBS_KEY_0x44",
            "OBS_KEY_0x45",
            "OBS_KEY_0x46",
            "OBS_KEY_0x47",
            "OBS_KEY_0x48",
            "OBS_KEY_0x49",
            "OBS_KEY_0x4A",
            "OBS_KEY_0x4B",
            "OBS_KEY_0x4C",
            "OBS_KEY_0x4D",
            "OBS_KEY_0x4E",
            "OBS_KEY_0x4F",
            "OBS_KEY_0x50",
            "OBS_KEY_0x51",
            "OBS_KEY_0x52",
            "OBS_KEY_0x53",
            "OBS_KEY_0x54",
            "OBS_KEY_0x55",
            "OBS_KEY_0x56",
            "OBS_KEY_0x57",
            "OBS_KEY_0x58",
            "OBS_KEY_0x59",
            "OBS_KEY_0x5A",
            "OBS_KEY_VK_LWIN",
            "OBS_KEY_VK_RWIN",
            "OBS_KEY_VK_APPS",
            "OBS_KEY_0x5E",
            "OBS_KEY_VK_SLEEP",
            "OBS_KEY_VK_SEPARATOR",
            "OBS_KEY_0x88",
            "OBS_KEY_0x89",
            "OBS_KEY_0x8A",
            "OBS_KEY_0x8B",
            "OBS_KEY_0x8C",
            "OBS_KEY_0x8D",
            "OBS_KEY_0x8E",
            "OBS_KEY_0x8F",
            "OBS_KEY_VK_OEM_FJ_JISHO",
            "OBS_KEY_VK_OEM_FJ_LOYA",
            "OBS_KEY_VK_OEM_FJ_ROYA",
            "OBS_KEY_0x97",
            "OBS_KEY_0x98",
            "OBS_KEY_0x99",
            "OBS_KEY_0x9A",
            "OBS_KEY_0x9B",
            "OBS_KEY_0x9C",
            "OBS_KEY_0x9D",
            "OBS_KEY_0x9E",
            "OBS_KEY_0x9F",
            "OBS_KEY_VK_LSHIFT",
            "OBS_KEY_VK_RSHIFT",
            "OBS_KEY_VK_LCONTROL",
            "OBS_KEY_VK_RCONTROL",
            "OBS_KEY_VK_LMENU",
            "OBS_KEY_VK_RMENU",
            "OBS_KEY_VK_BROWSER_BACK",
            "OBS_KEY_VK_BROWSER_FORWARD",
            "OBS_KEY_VK_BROWSER_REFRESH",
            "OBS_KEY_VK_BROWSER_STOP",
            "OBS_KEY_VK_BROWSER_SEARCH",
            "OBS_KEY_VK_BROWSER_FAVORITES",
            "OBS_KEY_VK_BROWSER_HOME",
            "OBS_KEY_VK_VOLUME_MUTE",
            "OBS_KEY_VK_VOLUME_DOWN",
            "OBS_KEY_VK_VOLUME_UP",
            "OBS_KEY_VK_MEDIA_NEXT_TRACK",
            "OBS_KEY_VK_MEDIA_PREV_TRACK",
            "OBS_KEY_VK_MEDIA_STOP",
            "OBS_KEY_VK_MEDIA_PLAY_PAUSE",
            "OBS_KEY_VK_LAUNCH_MAIL",
            "OBS_KEY_VK_LAUNCH_MEDIA_SELECT",
            "OBS_KEY_VK_LAUNCH_APP1",
            "OBS_KEY_VK_LAUNCH_APP2",
            "OBS_KEY_0xB8",
            "OBS_KEY_0xB9",
            "OBS_KEY_0xC1",
            "OBS_KEY_0xC2",
            "OBS_KEY_0xC3",
            "OBS_KEY_0xC4",
            "OBS_KEY_0xC5",
            "OBS_KEY_0xC6",
            "OBS_KEY_0xC7",
            "OBS_KEY_0xC8",
            "OBS_KEY_0xC9",
            "OBS_KEY_0xCA",
            "OBS_KEY_0xCB",
            "OBS_KEY_0xCC",
            "OBS_KEY_0xCD",
            "OBS_KEY_0xCE",
            "OBS_KEY_0xCF",
            "OBS_KEY_0xD0",
            "OBS_KEY_0xD1",
            "OBS_KEY_0xD2",
            "OBS_KEY_0xD3",
            "OBS_KEY_0xD4",
            "OBS_KEY_0xD5",
            "OBS_KEY_0xD6",
            "OBS_KEY_0xD7",
            "OBS_KEY_0xD8",
            "OBS_KEY_0xD9",
            "OBS_KEY_0xDA",
            "OBS_KEY_VK_OEM_8",
            "OBS_KEY_0xE0",
            "OBS_KEY_VK_OEM_AX",
            "OBS_KEY_VK_ICO_HELP",
            "OBS_KEY_VK_ICO_00",
            "OBS_KEY_VK_PROCESSKEY",
            "OBS_KEY_VK_ICO_CLEAR",
            "OBS_KEY_VK_PACKET",
            "OBS_KEY_0xE8",
            "OBS_KEY_VK_OEM_RESET",
            "OBS_KEY_VK_OEM_JUMP",
            "OBS_KEY_VK_OEM_PA1",
            "OBS_KEY_VK_OEM_PA2",
            "OBS_KEY_VK_OEM_PA3",
            "OBS_KEY_VK_OEM_WSCTRL",
            "OBS_KEY_VK_OEM_CUSEL",
            "OBS_KEY_VK_OEM_ATTN",
            "OBS_KEY_VK_OEM_FINISH",
            "OBS_KEY_VK_OEM_COPY",
            "OBS_KEY_VK_OEM_AUTO",
            "OBS_KEY_VK_OEM_ENLW",
            "OBS_KEY_VK_ATTN",
            "OBS_KEY_VK_CRSEL",
            "OBS_KEY_VK_EXSEL",
            "OBS_KEY_VK_EREOF",
            "OBS_KEY_VK_PLAY",
            "OBS_KEY_VK_ZOOM",
            "OBS_KEY_VK_NONAME",
            "OBS_KEY_VK_PA1",
            "OBS_KEY_VK_OEM_CLEAR"};
    // endregion

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
        sqLiteDatabase.execSQL(SQL_CREATE_HOTKEYLIST_TABLE);
        fillHotkeyListTable(sqLiteDatabase);
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
        sqLiteDatabase.execSQL(SQL_DELETE_HOTKEYLIST_TABLE);
        onCreate(sqLiteDatabase);

    }

    public int getHotkeyModifier(SQLiteDatabase db, int modifier, int orderPosition) {
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

        int ret=0;

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            switch (modifier) {
                case 1: // shift
                    ret = cursor.getInt(cursor.getColumnIndex(OBSHotkeyTbl.COLUMN_NAME_MOD_SHIFT));
                    break;
                case 2: // alt
                    ret = cursor.getInt(cursor.getColumnIndex(OBSHotkeyTbl.COLUMN_NAME_MOD_ALT));
                    break;
                case 3: // ctrl
                    ret = cursor.getInt(cursor.getColumnIndex(OBSHotkeyTbl.COLUMN_NAME_MOD_CTRL));
                    break;
                case 4: // cmd
                    ret = cursor.getInt(cursor.getColumnIndex(OBSHotkeyTbl.COLUMN_NAME_MOD_CMD));
                    break;
            }
            return ret;
        } else
        {
            return 0;
        }
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

        if (cursor.getCount()>0) {
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(OBSHotkeyTbl.COLUMN_NAME_NAME));
        } else
        {
            return null;
        }
    }

    public long getItemsCount(SQLiteDatabase db) {
        return DatabaseUtils.queryNumEntries(db, OBSHotkeyTbl.TABLE_NAME);
    }

    public int getMaxItemNumber(SQLiteDatabase db) {
        Cursor c = db.query(OBSHotkeyTbl.TABLE_NAME, new String[]{"COUNT(" + OBSHotkeyTbl.COLUMN_NAME_ORDER + ") AS Anzahl"}, null,null,null,null,null);
        int numItems, newId;
        try {
            if (c.getCount() > 0) {
                c.moveToFirst();
                numItems = c.getInt(c.getColumnIndex("Anzahl"));
            } else {
                numItems = 0;
            }
        }
        catch (CursorIndexOutOfBoundsException cioobe) {
            newId = -1;
            return newId;
        }
        if (numItems > 0) {
            c = db.query(OBSHotkeyTbl.TABLE_NAME, new String[] { "MAX(" + OBSHotkeyTbl.COLUMN_NAME_ORDER + ") AS MaxID"}, null, null, null, null,null);
            c.moveToFirst();
            int maxId = c.getInt(0);
            newId = maxId + 1;
        }
        else {
            newId = 0;
        }
        return newId;
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

    public void fillHotkeyListTable(SQLiteDatabase db) {
        for (String key :
                obsKeys) {
            ContentValues cv = new ContentValues();
            cv.put(OBSHotkeyList.COLUMN_NAME_HOTKEY, key);
            db.insert(OBSHotkeyList.TABLE_NAME, null, cv);
        }
    }

    public Cursor getAvailableHotkeys(SQLiteDatabase db) {
//        return db.rawQuery("SELECT " +
//                OBSHotkeyList._ID +
//                ", " +
//                OBSHotkeyList.COLUMN_NAME_HOTKEY +
//                " FROM " +
//                OBSHotkeyList.TABLE_NAME +
//                " WHERE " +
//                OBSHotkeyList.COLUMN_NAME_HOTKEY +
//                " NOT IN (SELECT " +
//                OBSHotkeyTbl.COLUMN_NAME_HOTKEY +
//                " FROM " +
//                OBSHotkeyTbl.TABLE_NAME + ")", null);
        String[] projection = new String[] {
                OBSHotkeyTbl._ID,
                OBSHotkeyList.COLUMN_NAME_HOTKEY
        };
        return db.query(
                OBSHotkeyList.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
    }

    public boolean addNewHotkey(SQLiteDatabase db,
            String hotkey,
            String name,
            int reihenfolge,
            boolean bShift,
            boolean bAlt,
            boolean bCtrl,
            boolean bCmd) {
        // Erst prüfen, ob es diesen Hotkey vielleicht schon gibt...
        Cursor c = db.query(
                OBSHotkeyTbl.TABLE_NAME,
                new String[] { "COUNT (" + OBSHotkeyTbl._ID + ")" },
                OBSHotkeyTbl.COLUMN_NAME_HOTKEY +
                        " = ? AND " + OBSHotkeyTbl.COLUMN_NAME_MOD_SHIFT +
                        " = ? AND " + OBSHotkeyTbl.COLUMN_NAME_MOD_ALT +
                        " = ? AND " + OBSHotkeyTbl.COLUMN_NAME_MOD_CTRL +
                        " = ? AND " + OBSHotkeyTbl.COLUMN_NAME_MOD_CMD +
                        " = ?",
                new String[] {
                        hotkey,
                        String.valueOf(bShift?1:0),
                        String.valueOf(bAlt?1:0),
                        String.valueOf(bCtrl?1:0),
                        String.valueOf(bCmd?1:0)
                },
                null,
                null,
                null
        );
        c.moveToFirst();
        int found_id = c.getInt(0);
        if (found_id == 0) {
            ContentValues cv = new ContentValues();
            cv.put(OBSHotkeyTbl.COLUMN_NAME_NAME, name);
            cv.put(OBSHotkeyTbl.COLUMN_NAME_HOTKEY, hotkey);
            cv.put(OBSHotkeyTbl.COLUMN_NAME_ORDER, reihenfolge);
            cv.put(OBSHotkeyTbl.COLUMN_NAME_MOD_SHIFT, bShift);
            cv.put(OBSHotkeyTbl.COLUMN_NAME_MOD_ALT, bAlt);
            cv.put(OBSHotkeyTbl.COLUMN_NAME_MOD_CTRL, bCtrl);
            cv.put(OBSHotkeyTbl.COLUMN_NAME_MOD_CMD, bCmd);
            db.insert(
                    OBSHotkeyTbl.TABLE_NAME,
                    null,
                    cv
            );
            return true;
        } else {
            return false;
        }
    }

    public void moveHotkeyPosition(SQLiteDatabase db, int fromPosition, int toPosition) {
        if (fromPosition > toPosition) {
            int id_from;
            Cursor c = db.query(
                    OBSHotkeyTbl.TABLE_NAME,
                    new String[]{OBSHotkeyTbl._ID},
                    OBSHotkeyTbl.COLUMN_NAME_ORDER + " = ?",
                    new String[]{String.valueOf(fromPosition)},
                    null,
                    null,
                    null);
            c.moveToFirst();
            id_from = c.getInt(0);
            db.execSQL("UPDATE " + OBSHotkeyTbl.TABLE_NAME + " SET " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " = " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " + 1 WHERE " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " < " + String.valueOf(fromPosition) + " AND " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " >= " + String.valueOf(toPosition));
            ContentValues cv = new ContentValues();
            cv.put(OBSHotkeyTbl.COLUMN_NAME_ORDER, toPosition);
            db.update(OBSHotkeyTbl.TABLE_NAME, cv,OBSHotkeyTbl._ID + " = ?",new String[] { String.valueOf(id_from) });
        }
        if (fromPosition < toPosition) {
            int id_from;
            Cursor c = db.query(
                    OBSHotkeyTbl.TABLE_NAME,
                    new String[]{OBSHotkeyTbl._ID},
                    OBSHotkeyTbl.COLUMN_NAME_ORDER + " = ?",
                    new String[]{String.valueOf(fromPosition)},
                    null,
                    null,
                    null);
            c.moveToFirst();
            id_from = c.getInt(0);
            db.execSQL("UPDATE " + OBSHotkeyTbl.TABLE_NAME + " SET " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " = " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " - 1 WHERE " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " > " + String.valueOf(fromPosition) + " AND " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " < " + String.valueOf(toPosition));
            ContentValues cv = new ContentValues();
            cv.put(OBSHotkeyTbl.COLUMN_NAME_ORDER, toPosition - 1);
            db.update(OBSHotkeyTbl.TABLE_NAME, cv,OBSHotkeyTbl._ID + " = ?",new String[] { String.valueOf(id_from) });

        }
    }

    public void removeHotkey(SQLiteDatabase db, int position) {
        db.delete(
                OBSHotkeyTbl.TABLE_NAME,
                OBSHotkeyTbl.COLUMN_NAME_ORDER + " = ?",
                new String[] { String.valueOf(position) }
        );
        db.execSQL("UPDATE " + OBSHotkeyTbl.TABLE_NAME + " SET " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " = " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " - 1 WHERE " + OBSHotkeyTbl.COLUMN_NAME_ORDER + " > " + String.valueOf(position));
    }
}
