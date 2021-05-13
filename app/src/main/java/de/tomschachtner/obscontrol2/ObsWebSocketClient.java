package de.tomschachtner.obscontrol2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

public class ObsWebSocketClient extends WebSocketClient {

    public static final String TAG = "ObsWebSocketClient_TS";
    public MainActivity mainAct;
    public status connStatus;

    public void getScenesList() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "GetSceneList");
            jso.put("message-id", "SceneList_SCT");
            send(jso.toString());
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }
    }

    public enum status {
        OPEN,
        CLOSED
    };

    boolean SettingsActivityRunningForPassword = false;

    /** Constructs a WebSocketClient with a Callback mechanism
     *
     */
    public ObsWebSocketClient(URI serverUri, MainActivity activity) {
        super(serverUri);
        mainAct = activity;
        connStatus = status.CLOSED;
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri the server URI to connect to
     */
    public ObsWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri     the server URI to connect to
     * @param protocolDraft The draft which should be used for this connection
     */
    public ObsWebSocketClient(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri   the server URI to connect to
     * @param httpHeaders Additional HTTP-Headers
     * @since 1.3.8
     */
    public ObsWebSocketClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri     the server URI to connect to
     * @param protocolDraft The draft which should be used for this connection
     * @param httpHeaders   Additional HTTP-Headers
     * @since 1.3.8
     */
    public ObsWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
        super(serverUri, protocolDraft, httpHeaders);
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri      the server URI to connect to
     * @param protocolDraft  The draft which should be used for this connection
     * @param httpHeaders    Additional HTTP-Headers
     * @param connectTimeout The Timeout for the connection
     */
    public ObsWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    /**
     * Called after an opening handshake has been performed and the given websocket is ready to be written on.
     *
     * @param handshakedata The handshake of the websocket instance
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "in onOpen()");
        Log.i(TAG, "HTTP Status: " + handshakedata.getHttpStatus());
        Log.i(TAG, "Status Msg: " + handshakedata.getHttpStatusMessage());
//        mainAct.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(mainAct, "onOpen in ObsWebSocket", Toast.LENGTH_SHORT).show();
//
//            }
//        });
        connStatus = status.OPEN;
        mainAct.onConnectedToWebService();
    }

    /**
     * Callback for string messages received from the remote host
     *
     * @param message The UTF-8 decoded message that was received.
     * @see #onMessage(ByteBuffer)
     **/
    @Override
    public void onMessage(String message) {
        try {
            JSONObject jso = new JSONObject(message);
            String msgId = jso.getString("message-id");
            switch (msgId)
            {
                case "isAuthRequired_SCT":
                    // Benutzer muss sich authentisieren
                    // ToastInMainAct("Benutzer muss sich authentisieren!");
                    checkAndDoAuth(jso);
                    break;
                case "Authenticate_SCT":
                    ToastInMainAct(jso.toString());
                    Log.d("TEST", jso.toString());
                    processAuthResult(jso);
                    getScenesList();
                    break;
                case "SceneList_SCT":
                    Log.d("TEST", jso.toString());
                    break;
                default:
                    ToastInMainAct("Unbekannte Antwort vom WebService!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void processAuthResult(JSONObject jso) {
        try {
            if (jso.getString("status").equals("error")) {
                if (jso.getString("error").equals("Authentication Failed.")) {
                    AlertInMainAct("Authentifizierung fehlgeschlagen.\nBitte Passwort in den Einstellungen setzen und neu anmelden.");
                }
            } else Log.d("TEST", jso.getString("status"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkAndDoAuth(JSONObject jso) {
        // Check, if auth is necessary
        try {
            if (jso.getBoolean("authRequired")) {
                Log.d("TEST", "Authentication required.");
                // Check, if there's a password saved in the preferences.
                // If not, show them and give the user a chance to set the password
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mainAct);
                String passwd;
                passwd = sp.getString("password_preference", "");

                // We are sure to have a password stored in the Preferences.
                // Use it to authenticate
                String salt = jso.getString("salt");
                String challenge = jso.getString("challenge");
                String secret_string = passwd + salt;
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] secret_hash = digest.digest(secret_string.getBytes(StandardCharsets.UTF_8));
                    Log.d("TEST", "secret_hash: " + secret_hash.toString());
                    String secret = Base64.getEncoder().encodeToString(secret_hash);
                    Log.d("TEST", "secret: " + secret);
                    String auth_response_string = secret + challenge;
                    Log.d("TEST", "auth_response_string: " + auth_response_string);
                    byte[] auth_response_hash = digest.digest(auth_response_string.getBytes(StandardCharsets.UTF_8));
                    Log.d("TEST", "auth_response_hash: " + auth_response_hash.toString());
                    String auth_response = Base64.getEncoder().encodeToString(auth_response_hash);
                    Log.d("TEST", "auth_response: " + auth_response);

                    JSONObject authResponse;
                    try {
                        authResponse = new JSONObject();
                        authResponse.put("request-type", "Authenticate");
                        authResponse.put("message-id", "Authenticate_SCT");
                        authResponse.put("auth", auth_response);
                        send(authResponse.toString());
                    }
                    catch (JSONException jse)
                    {
                        jse.printStackTrace();
                    }

                }
                catch (NoSuchAlgorithmException nsae) {
                    Log.e("TEST", "NoSuchAlgorithmException thrown: " + nsae.getLocalizedMessage());
                }
                Log.d("TEST", sp.getString("password_preference", "** NOTHING **"));
            } else {
                Log.d("TEST", "Authentication not required.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void AlertInMainAct(String alertMsg) {
        mainAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alert = new AlertDialog.Builder(mainAct).create();
                alert.setTitle("Fehler");
                alert.setMessage(alertMsg);
                alert.setButton(AlertDialog.BUTTON_NEUTRAL,
                        "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                alert.show();
            }
        });
    }

    private void ToastInMainAct(String toastMsg) {
        mainAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainAct.onMessageFromWebService(toastMsg);
            }
        });
    }

    /**
     * Called after the websocket connection has been closed.
     *
     * @param code   The codes can be looked up here: {@link CloseFrame}
     * @param reason Additional information string
     * @param remote
     **/
    @Override
    public void onClose(int code, String reason, boolean remote) {
        connStatus = status.CLOSED;
        mainAct.onDisconnectedFromWebService();
    }

    /**
     * Called when errors occurs. If an error causes the websocket connection to fail {@link #onClose(int, String, boolean)} will be called additionally.<br>
     * This method will be called primarily because of IO or protocol errors.<br>
     * If the given exception is an RuntimeException that probably means that you encountered a bug.<br>
     *
     * @param ex The exception causing this error
     **/
    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "Error during connect: " + ex.getLocalizedMessage());
        if (ex instanceof ConnectException) mainAct.onConnectErrorFromWebService(ex.getLocalizedMessage());
    }

    public void checkAuthentication() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "GetAuthRequired");
            jso.put("message-id", "isAuthRequired_SCT");
            send(jso.toString());
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }
    }
}
