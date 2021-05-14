package de.tomschachtner.obscontrol2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MyButtonListAdapter.OnItemClickListener {
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

    @Override
    public void onItemClick(View view, int position) {
        Log.i("TEST", "You clicked number " + adapter.getItem(position) + ", which is at cell position " + position);
        mOBSWebSocketClient.switchActiveScene(adapter.getItem(position));
    }

    /**
     * called after a new scenes list was received from OBS
     */
    public void newScenesAvailable() {
        RecyclerView vButtonList = findViewById(R.id.button_list);
        int numberOfColumns = 4;
        vButtonList.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        if (adapter == null) {
            adapter = new MyButtonListAdapter(this, mOBSWebSocketClient.obsScenes);
            adapter.setClickListener(this);
            mOBSWebSocketClient.setOnObsScenesChangedListener(adapter);
            vButtonList.setAdapter(adapter);
        }
    }

    enum status {
        OPEN,
        CONNECTING,
        CLOSED
    }
    private status connectionStatus = status.CLOSED;
    private static final String TAG = "TS";
    ObsWebSocketClient mOBSWebSocketClient;

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
        logInMenuItem.setEnabled(mOBSWebSocketClient.connStatus == ObsWebSocketClient.status.CLOSED);
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

    TextView connectStatusIndicator;
    public TextView currentSceneName;
    MyButtonListAdapter adapter;
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
            setContentView(R.layout.activity_main);
            RecyclerView vButtonList = findViewById(R.id.button_list);
            Button transition = findViewById(R.id.transition_to_program);
            transition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOBSWebSocketClient.doTransitionToProgram();
                }
            });

            URI webSocketURI;
            try {
                webSocketURI = new URI("ws://" + host + ":" + port + "/");
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }

            mOBSWebSocketClient = new ObsWebSocketClient(webSocketURI, this);

            int numberOfColumns = 4;
            vButtonList.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
            adapter = new MyButtonListAdapter(this, mOBSWebSocketClient.obsScenes);
            adapter.setClickListener(this);
            mOBSWebSocketClient.setOnObsScenesChangedListener(adapter);

            vButtonList.setAdapter(adapter);
            connectStatusIndicator = findViewById(R.id.connect_status);
            currentSceneName = findViewById(R.id.aktive_szene_display);
            setConnectStatusIndicator(status.CLOSED);

            verbindenMitWebService();
        }
    }

    private void setConnectStatusIndicator(status statusIndicator) {
        switch (statusIndicator){
            case CLOSED:
                connectStatusIndicator.setText("OFFLINE");
                connectStatusIndicator.setBackgroundColor(Color.RED);
                break;
            case OPEN:
                connectStatusIndicator.setText("ONLINE");
                connectStatusIndicator.setBackgroundColor(Color.GREEN);
                break;
            case CONNECTING:
                connectStatusIndicator.setText("Verbinden");
                connectStatusIndicator.setBackgroundColor(Color.YELLOW);
                break;
            default:
                connectStatusIndicator.setText("UNDEFINED!");
                connectStatusIndicator.setBackgroundColor(Color.RED);
        }
    }

    private void verbindenMitWebService() {

        mOBSWebSocketClient.connect();
        setConnectStatusIndicator(status.CONNECTING);
    }

    void onConnectedToWebService() {
        //Toast.makeText(this, "Yeah!", Toast.LENGTH_SHORT).show();
        mOBSWebSocketClient.checkAuthentication();
        invalidateOptionsMenu();
        setConnectStatusIndicator(status.OPEN);
        //mOBSWebSocketClient.getScenesList();
    }

    public void onMessageFromWebService(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.i(TAG, message);
    }

    public void onDisconnectedFromWebService() {
        //Toast.makeText(this, "Yeah!", Toast.LENGTH_SHORT).show();
        invalidateOptionsMenu();
        setConnectStatusIndicator(status.CLOSED);
    }
}