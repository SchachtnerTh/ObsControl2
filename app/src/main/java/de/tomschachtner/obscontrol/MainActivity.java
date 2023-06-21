package de.tomschachtner.obscontrol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MainActivity
        extends AppCompatActivity {
    public Context ctx = this;

    public final static int MANAGE_HOTKEYS_INTENT = 1;
    public final static int SAVE_HOTKEY_TO_XML_INTENT = 2;
    public final static int LOAD_HOTKEY_FROM_XML_INTENT = 3;

    public void onConnectErrorFromWebService(String localizedMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alert = new AlertDialog.Builder(ctx, R.style.myDialog).create();
                alert.setTitle("Fehler");
                alert.setMessage("Fehler beim Verbinden mit dem Webservice.\nFehlermeldung: " + localizedMessage);
                alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
            }
        });
    }


    enum status {
        OPEN,
        CONNECTING,
        CLOSED
    }


    private status connectionStatus = status.CLOSED;
    private static final String TAG = "TS";
    public OBSWebSocketClient mOBSWebSocketClient;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem logInMenuItem = menu.findItem(R.id.logon);
        if (mOBSWebSocketClient == null) {
            logInMenuItem.setEnabled(false);
        } else {
            // on first start of the app, mOBSWebSocketClient might not have been initialized, as
            // the SettingsActivity activity will be shown to give the user the opportunity to con-
            // figure server settings and passwort.
            // in this case, we might find ourselves within this callback and without a valid
            // mOBSWebSocketClient property. In this case mark the menu item as "not available"
            logInMenuItem.setEnabled(mOBSWebSocketClient.connStatus == status.CLOSED);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSettings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.logon:
                verbindenMitWebService();
                break;
            case R.id.version:
                AlertDialog.Builder dlgVersion = new AlertDialog.Builder(this);
                dlgVersion.setMessage("Versionsnummer: " + BuildConfig.VERSION_NAME + "\n" +
                        "Buildnummer: " + BuildConfig.VERSION_CODE + "\n" +
                        "Build type: " + BuildConfig.BUILD_TYPE + "\n" +
                        "Application ID: " + BuildConfig.APPLICATION_ID);
                dlgVersion.setTitle("Versionsinformationen");
                dlgVersion.setPositiveButton("OK", null);
                dlgVersion.setCancelable(true);
                dlgVersion.create().show();
                break;
            case R.id.manageHotkeys:
//                Intent i2 = new Intent(this, HotkeyConfigActivity.class);
//                startActivity(i2);
                Intent i3 = new Intent(this, AddNewHotkeyActivity.class);
                startActivityForResult(i3, MANAGE_HOTKEYS_INTENT);
                break;
            case R.id.exportHotkeys:
                Intent saveXMLFileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                saveXMLFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                saveXMLFileIntent.setType("text/xml");
                saveXMLFileIntent.putExtra(Intent.EXTRA_TITLE, "hotkeys.xml");
                startActivityForResult(saveXMLFileIntent, SAVE_HOTKEY_TO_XML_INTENT);
                break;
            case R.id.importHotkeys:
                Intent readXMLFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                readXMLFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                readXMLFileIntent.setType("text/xml");
                readXMLFileIntent.putExtra(Intent.EXTRA_TITLE, "hotkeys.xml");
                startActivityForResult(readXMLFileIntent, LOAD_HOTKEY_FROM_XML_INTENT);
                //Toast.makeText(this,"Das funktioniert leider noch nicht!", Toast.LENGTH_SHORT).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MANAGE_HOTKEYS_INTENT:
                if (resultCode == 1) {
                    hotkeysFragment.hotkeysButtonsAdapter.notifyDataSetChanged();
                }
                if (resultCode == 0) {
                    AlertDialog dlg = new AlertDialog.Builder(this).create();
                    dlg.setTitle("Hotkey bereits vorhanden");
                    dlg.setMessage("Der angegebene Hotkey ist bereits registriert. Registrieren Sie einen anderen Hotkey oder löschen Sie den bereits registrierten.");
                    dlg.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    dlg.show();
                }
                break;
            case SAVE_HOTKEY_TO_XML_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Document dom;
                    String xmlText;
                    try {
                        OutputStream output = getContentResolver().openOutputStream(uri);
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        try {
                            DocumentBuilder dbld = dbf.newDocumentBuilder();
                            dom = dbld.newDocument();
                            Element rootElement = dom.createElement("hotkeys");
                            OBSHotkeysDatabaseHelper dbHelper = new OBSHotkeysDatabaseHelper(this);
                            SQLiteDatabase db = dbHelper.getReadableDatabase();
                            Cursor c = dbHelper.getDefinedHotkeys(db);
                            while (c.moveToNext()) {
                                Element e = dom.createElement("hotkey");
                                e.setAttribute("key", c.getString(c.getColumnIndex(OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_HOTKEY)));
                                e.setAttribute("shift", c.getString(c.getColumnIndex(OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_MOD_SHIFT)));
                                e.setAttribute("alt", c.getString(c.getColumnIndex(OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_MOD_ALT)));
                                e.setAttribute("ctrl", c.getString(c.getColumnIndex(OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_MOD_CTRL)));
                                e.setAttribute("cmd", c.getString(c.getColumnIndex(OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_MOD_CMD)));
                                e.appendChild(dom.createTextNode(c.getString(c.getColumnIndex(OBSHotkeysDBContract.OBSHotkeyTbl.COLUMN_NAME_NAME))));
                                //e.setNodeValue();
                                rootElement.appendChild(e);
                            }
                            c.close();
                            dom.appendChild(rootElement);
                            try {
                                Transformer tr = TransformerFactory.newInstance().newTransformer();
                                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                                xmlText = "";
                                tr.transform(new DOMSource(dom), new StreamResult(output));
                                output.flush();
                                output.close();
                            } catch (TransformerConfigurationException e) {
                                e.printStackTrace();
                            } catch (TransformerException e) {
                                e.printStackTrace();
                            }
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                    }
                    catch (IOException ioe) {
                        Toast.makeText(this, "Fehler beim Schreiben der XML-Datei.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case LOAD_HOTKEY_FROM_XML_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Document dom;
                    String xmlText;

                    try {
                        InputStream input = getContentResolver().openInputStream(uri);
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        try {
                            DocumentBuilder dbld = dbf.newDocumentBuilder();
                            try {
                                dom = dbld.parse(input);
                                Element rootElement = dom.getDocumentElement();
                                NodeList nodes = rootElement.getElementsByTagName("hotkey");
                                int hotkeysCount = nodes.getLength();
                                OBSHotkeysDatabaseHelper dbHelper = new OBSHotkeysDatabaseHelper(this);
                                SQLiteDatabase db = dbHelper.getReadableDatabase();
                                if (hotkeysCount > 0) {
                                    dbHelper.removeAllHotkeys(db);
                                    for (int i = 0; i < hotkeysCount; i++) {
                                        Element e = (Element) nodes.item(i);
                                        String hotkey = e.getAttribute("key");
                                        Boolean bShift = e.getAttribute("shift").equals("1") ? true : false;
                                        Boolean bAlt = e.getAttribute("alt").equals("1") ? true : false;
                                        Boolean bCtrl = e.getAttribute("ctrl").equals("1") ? true : false;
                                        Boolean bCmd = e.getAttribute("cmd").equals("1") ? true : false;
                                        String name = e.getFirstChild().getNodeValue();
                                        dbHelper.addNewHotkey(
                                                db,
                                                hotkey,
                                                name,
                                                i,
                                                bShift,
                                                bAlt,
                                                bCtrl,
                                                bCmd
                                        );


                                    }
                                    hotkeysFragment.hotkeysButtonsAdapter.notifyDataSetChanged();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (SAXException e) {
                                e.printStackTrace();
                            }
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    // delete existing hotkey configuration
                    // parse xml data
                    // create new hotkey database entries
                }
        }
    }

    private ViewStub clientView;
    private LinearLayout rootLayout;
    public TabLayout categoryTabs;

    ScenesFragment scenesFragment;
    TransitionsVolumesFragment transitionsVolumesFragment;
    HotkeysFragment hotkeysFragment;
    PreviewFragment previewFragment;

    View vScenesSources;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TEST", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("TEST", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TEST", "onDestroy");
    }

    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("connected", (mOBSWebSocketClient == null) ? false : true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String host = sp.getString("ws_host_value", null);
        String port = sp.getString("ws_port_value", null);

        String strFontSize = sp.getString("key_font_size", "");
//        String fontsize = sp.getString("key_font_size", "(float) 0.0");
        if (strFontSize.equals("")) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("key_font_size", "14.0");
            editor.commit();
        }

        if (host == null || port == null) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else {

            // configure the layout for this activity
            //setContentView(R.layout.activity_main);

            setContentView(R.layout.activity_root_layout);
            boolean wakelock = sp.getBoolean("wakelock", false);
            if (wakelock)
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //rootLayout = findViewById(R.id.root_layout);
            scenesFragment = new ScenesFragment();
            transitionsVolumesFragment = new TransitionsVolumesFragment();
            hotkeysFragment = new HotkeysFragment();
            previewFragment = new PreviewFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.simple_frame_layout, scenesFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            ft.commit();


            //clientView = (ViewStub)findViewById(R.id.child_view);
            //clientView.setLayoutResource(R.layout.scenes_tab);

            //vScenesSources = clientView.inflate();


            categoryTabs = findViewById(R.id.category_tabs);

            categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    Fragment fragment = null;
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    switch (tab.getPosition()) {
                        case 0:
                            Log.d("TEST", "Scenes tab.");
                            ft.replace(R.id.simple_frame_layout, scenesFragment);
                            break;
                        case 1:
                            Log.d("TEST", "Trans/Vol tab.");
                            ft.replace(R.id.simple_frame_layout, transitionsVolumesFragment);
                            break;
                        case 2:
                            Log.d("TEST", "Hotkeys tab.");
                            ft.replace(R.id.simple_frame_layout, hotkeysFragment);
                            break;
                        case 3:
                            Log.d("TEST", "Preview tab.");
                            ft.replace(R.id.simple_frame_layout, previewFragment);
                            break;
                        default:
                            Log.d("TEST", "no tab.");
                    }

                    //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            URI webSocketURI;
            try {
                webSocketURI = new URI("ws://" + host + ":" + port + "/");
            } catch (URISyntaxException e) {
                webSocketURI = null;
                e.printStackTrace();
            }

            mOBSWebSocketClient = new OBSWebSocketClient(webSocketURI, this);

            //setConnectStatusIndicator(MainActivity.status.CLOSED);

            verbindenMitWebService();
        }

    }


    public void verbindenMitWebService() {
        mOBSWebSocketClient.connect();
        scenesFragment.setConnectStatusIndicator(status.CONNECTING);
    }


    void onConnectedToWebService() {
        //Toast.makeText(this, "Yeah!", Toast.LENGTH_SHORT).show();
        //mOBSWebSocketClient.checkAuthentication();
        invalidateOptionsMenu();
        scenesFragment.setConnectStatusIndicator(status.OPEN);
        //mOBSWebSocketClient.getScenesList();
    }

    public void onMessageFromWebService(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.i(TAG, message);
    }

    public void onDisconnectedFromWebService() {
        //Toast.makeText(this, "Yeah!", Toast.LENGTH_SHORT).show();
        invalidateOptionsMenu();
        ///scenesFragment.setConnectStatusIndicator(status.CLOSED);
    }
}