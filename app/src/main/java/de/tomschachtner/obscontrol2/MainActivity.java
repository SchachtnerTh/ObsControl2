package de.tomschachtner.obscontrol2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
                alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",new DialogInterface.OnClickListener() {
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
    }

    enum status {
        OPEN,
        CONNECTING,
        CLOSED
    };
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
        if (mOBSWebSocketClient.connStatus == ObsWebSocketClient.status.CLOSED) {
            logInMenuItem.setEnabled(true);
        } else {
            logInMenuItem.setEnabled(false);
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

    MyButtonListAdapter adapter;
    String[] data = {"1","2","3","4","5","6","7","8","9","10"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        RecyclerView vButtonList = findViewById(R.id.button_list);
        int numberOfColumns = 4;
        vButtonList.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new MyButtonListAdapter(this, data);
        adapter.setClickListener(this);
        vButtonList.setAdapter(adapter);

        verbindenMitWebService();

    }

    private void verbindenMitWebService() {
        URI webSocketURI;
        try {
            webSocketURI = new URI("ws://192.168.178.100:4444/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mOBSWebSocketClient = new ObsWebSocketClient(webSocketURI, this);
        mOBSWebSocketClient.connect();
    }

    void onConnectedToWebService() {
        //Toast.makeText(this, "Yeah!", Toast.LENGTH_SHORT).show();
        mOBSWebSocketClient.checkAuthentication();
        invalidateOptionsMenu();
        //mOBSWebSocketClient.getScenesList();
    }

    public void onMessageFromWebService(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.i(TAG, message);
    }

    public void onDisconnectedFromWebService() {
        //Toast.makeText(this, "Yeah!", Toast.LENGTH_SHORT).show();
        invalidateOptionsMenu();
    }
}