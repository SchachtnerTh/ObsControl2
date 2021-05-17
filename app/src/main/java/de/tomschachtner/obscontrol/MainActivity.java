package de.tomschachtner.obscontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity
        extends AppCompatActivity
         {
    public Context ctx = this;
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
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private ViewStub clientView;
    private LinearLayout rootLayout;
    public TabLayout categoryTabs;

    ScenesFragment scenesFragment;
    TransitionsVolumesFragment transitionsVolumesFragment;
    HotkeysFragment hotkeysFragment;

    View vScenesSources;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String host = sp.getString("ws_host_value", null);
        String port = sp.getString("ws_port_value", null);
        if (host == null || port == null) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else {

            // configure the layout for this activity
            //setContentView(R.layout.activity_main);

            setContentView(R.layout.activity_root_layout);
            //rootLayout = findViewById(R.id.root_layout);
            scenesFragment = new ScenesFragment();
            transitionsVolumesFragment = new TransitionsVolumesFragment();
            hotkeysFragment = new HotkeysFragment();
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
                webSocketURI=null;
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
        mOBSWebSocketClient.checkAuthentication();
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