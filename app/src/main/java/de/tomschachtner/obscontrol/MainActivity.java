package de.tomschachtner.obscontrol;

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
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity
        extends AppCompatActivity
        implements OBSSceneButtonsAdapter.OnSceneClickListener, OBSSourceButtonsAdapter.OnSourceClickListener {
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
    public void onSceneClick(View view, int position) {
        Log.i("TEST", "You clicked number " + sceneButtonsAdapter.getItem(position) + ", which is at cell position " + position);
        mOBSWebSocketClient.switchActiveScene(sceneButtonsAdapter.getItem(position));
    }

    @Override
    public void onSourceClick(View view, int position) {
        Log.i("TEST", "You clicked number " + sourceButtonsAdapter.getItem(position) + ", which is at cell position " + position);
        mOBSWebSocketClient.toggleSourceVisibility(sourceButtonsAdapter.getItem(position));
    }

    /**
     * called after a new scenes list was received from OBS
     */
    public void newScenesAvailable() {
        int numberOfColumns = 4;
        obsScenesButtons.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        // TODO: Is this following block necessary? When can we get into it?
        if (sceneButtonsAdapter == null) {
            sceneButtonsAdapter = new OBSSceneButtonsAdapter(this, mOBSWebSocketClient.obsScenes);
            sceneButtonsAdapter.setSceneClickListener(this);
            mOBSWebSocketClient.setOnObsScenesChangedListener(sceneButtonsAdapter);
            obsScenesButtons.setAdapter(sceneButtonsAdapter);
        }
    }


    enum status {
        OPEN,
        CONNECTING,
        CLOSED
    }
    private status connectionStatus = status.CLOSED;
    private static final String TAG = "TS";
    OBSWebSocketClient mOBSWebSocketClient;

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
            logInMenuItem.setEnabled(mOBSWebSocketClient.connStatus == OBSWebSocketClient.status.CLOSED);
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

    TextView connectStatusIndicator;
    public TextView currentSceneName;
    public RecyclerView obsScenesButtons;
    public RecyclerView obsSourcesButtons;
    public Button transition;
    public ImageButton streamButton;
    public ImageButton recordButton;
    OBSSceneButtonsAdapter sceneButtonsAdapter;
    OBSSourceButtonsAdapter sourceButtonsAdapter;
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

            ViewStub stub = (ViewStub)findViewById(R.id.child_view);
            stub.setLayoutResource(R.layout.activity_main);
            vScenesSources = stub.inflate();

            // link variables to the UI elements
            obsScenesButtons = findViewById(R.id.scenes_button_list);
            transition = findViewById(R.id.transition_to_program);
            connectStatusIndicator = findViewById(R.id.connect_status);
            currentSceneName = findViewById(R.id.aktive_szene_display);
            obsSourcesButtons = findViewById(R.id.sources_button_list);
            streamButton = findViewById(R.id.stream_button);
            recordButton = findViewById(R.id.record_button);


            transition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOBSWebSocketClient.doTransitionToProgram();
                }
            });

            streamButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOBSWebSocketClient.toggleStreaming();
                    return true;
                }
            });

            recordButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOBSWebSocketClient.toggleRecording();
                    return true;
                }
            });

            URI webSocketURI;
            try {
                webSocketURI = new URI("ws://" + host + ":" + port + "/");
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }

            mOBSWebSocketClient = new OBSWebSocketClient(webSocketURI, this);

            int numberOfScenesColumns = 4;
            obsScenesButtons.setLayoutManager(new GridLayoutManager(this, numberOfScenesColumns));
            sceneButtonsAdapter = new OBSSceneButtonsAdapter(this, mOBSWebSocketClient.obsScenes);
            sceneButtonsAdapter.setSceneClickListener(this);
            mOBSWebSocketClient.setOnObsScenesChangedListener(sceneButtonsAdapter);
            obsScenesButtons.setAdapter(sceneButtonsAdapter);

            int numberOfSourcesColumns = 4;
            obsSourcesButtons.setLayoutManager(new GridLayoutManager(this, numberOfSourcesColumns));
            sourceButtonsAdapter = new OBSSourceButtonsAdapter(this, mOBSWebSocketClient.currentPreviewScene);
            sourceButtonsAdapter.setSourceClickListener(this);
            mOBSWebSocketClient.setOnObsSourcesChangedListener(sourceButtonsAdapter);
            obsSourcesButtons.setAdapter(sourceButtonsAdapter);

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