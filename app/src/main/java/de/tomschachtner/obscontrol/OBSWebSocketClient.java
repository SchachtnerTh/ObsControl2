package de.tomschachtner.obscontrol;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.tomschachtner.obscontrol.obsdata.ObsScene;
import de.tomschachtner.obscontrol.obsdata.ObsScenesList;
import de.tomschachtner.obscontrol.obsdata.ObsSource;

public class OBSWebSocketClient extends WebSocketClient {

    public static final String TAG = "ObsWebSocketClient_TS";
    public MainActivity mainAct;
    public status connStatus;

    public ObsScenesList obsScenes;
    public ObsScene currentPreviewScene;
    private boolean isStreaming = false;
    private boolean isRecording = false;

    public void updateScenes() {
        getScenesList();
    }

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

    public void getPreviewScene() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "GetPreviewScene");
            jso.put("message-id", "GetPreviewScene_SCT");
            send(jso.toString());
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }
    }

    public void switchActiveScene(String newScene) {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "SetPreviewScene");
            jso.put("message-id", "SetScene_SCT");
            jso.put("scene-name", newScene);
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void doTransitionToProgram() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "TransitionToProgram");
            jso.put("message-id", "transProgram_SCT");
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void toggleSourceVisibility(String source) {
        boolean isVisible=false;
        for (int i = 0; i < currentPreviewScene.sources.size(); i++) {
            if (currentPreviewScene.sources.get(i).name.equals(source)) {
                isVisible = currentPreviewScene.sources.get(i).render;
            }
        }
        setSourceVisible(currentPreviewScene.name,source,!isVisible);
    }

    public void toggleStreaming() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "StartStopStreaming");
            jso.put("message-id", "toggleStreaming_SCT");
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void toggleRecording() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "StartStopRecording");
            jso.put("message-id", "toggleRecording_SCT");
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public enum status {
        OPEN,
        CLOSED
    }

    boolean SettingsActivityRunningForPassword = false;

    /** Constructs a WebSocketClient with a Callback mechanism
     *
     */
    public OBSWebSocketClient(URI serverUri, MainActivity activity) {
        super(serverUri);
        mainAct = activity;
        connStatus = status.CLOSED;
        obsScenes = new ObsScenesList();
        currentPreviewScene = new ObsScene(); // normally not needed, but on app start, this might not yet
        // been initialized correctly, so we define a dummy empty scene so that app does not crash
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri the server URI to connect to
     */
    public OBSWebSocketClient(URI serverUri) {
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
    public OBSWebSocketClient(URI serverUri, Draft protocolDraft) {
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
    public OBSWebSocketClient(URI serverUri, Map<String, String> httpHeaders) {
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
    public OBSWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
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
    public OBSWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
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
        JSONObject jso=null;
        try {
            jso = new JSONObject(message);
            if (jso.has("message-id")) {
                String msgId = jso.getString("message-id");
                switch (msgId) {
                    case "isAuthRequired_SCT":
                        // Benutzer muss sich authentisieren
                        // ToastInMainAct("Benutzer muss sich authentisieren!");
                        checkAndDoAuth(jso);
                        break;
                    case "Authenticate_SCT":
                        ToastInMainAct(jso.toString());
                        Log.d("TEST", jso.toString());
                        processAuthResult(jso);
                        updateScenes();
                        break;
                    case "SceneList_SCT":
                        Log.d("TEST", jso.toString());
                        createUpdateOBSScenes(jso);
                        getPreviewScene();
                        break;
                    case "SetScene_SCT":
                    case "transProgram_SCT":
                        Log.d("TEST", jso.toString());
                        TimeUnit.MILLISECONDS.sleep(200);
                        //updateScenes();
                        break;
                    case "GetPreviewScene_SCT":
                        Log.d("TEST", jso.toString());
                        setPreviewScene(jso);
                        getStreamingStatus();
                        invalidateAdapters();
                        break;
                    case "changeScreenItemRender_SCT":
                        Log.d("TEST", jso.toString());
                        //updateScenes();
                        break;
                    case "getStreamingStatus_SCT":
                        Log.d("TEST", jso.toString());
                        updateStreamingStatus(jso);
                        invalidateAdapters();
                        break;
                    case "toggleStreaming_SCT":
                    case "toggleRecording_SCT":
                        Log.d("TEST", jso.toString());
                        updateScenes();
                        break;
                    default:
                        ToastInMainAct("Unbekannte Antwort vom WebService!");
                }
            } else if (jso.has("update-type")) {
                //ToastInMainAct("Update from OBS");
                updateScenes();
            }

        } catch (JSONException | InterruptedException e) {
            //e.printStackTrace();
            if (jso != null) Log.e("TEST", jso.toString());
        }

    }

    private void updateStreamingStatus(JSONObject jso) {
        try {
            isStreaming = jso.getBoolean("streaming");
            isRecording = jso.getBoolean("recording");
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    private void getStreamingStatus() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "GetStreamingStatus");
            jso.put("message-id", "getStreamingStatus_SCT");
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPreviewScene(JSONObject jso) {
        try {
            obsScenes.setCurrentPreviewScene(jso.getString("name"));
            for (int i = 0; i < obsScenes.scenes.size(); i++) {
                if (obsScenes.scenes.get(i).name.equals(obsScenes.getCurrentPreviewScene())) {
                    currentPreviewScene = obsScenes.scenes.get(i);
                }
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    private void setSourceVisible(String scene, String source, boolean setVisible) {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "SetSceneItemRender");
            jso.put("message-id", "changeScreenItemRender_SCT");
            jso.put("scene-name", scene);
            jso.put("source", source);
            jso.put("render", setVisible);
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Eine Struktur mit allen im ausgewählten OBS-Profil verfügbaren Szenen wird angelegt
     * @param jso Struktur, die alle OBS-Szenen enthält
     */
    private void createUpdateOBSScenes(JSONObject jso) {
        try {
            obsScenes = new ObsScenesList();
            obsScenes.setCurrentScene(jso.getString("current-scene"));
            JSONArray scenes = jso.getJSONArray("scenes");
            for (int i = 0; i < scenes.length(); i++) {
                JSONObject scene = scenes.getJSONObject(i);
                Log.d("TEST", "Szene: " + scene.getString("name"));
                ObsScene obsScene = new ObsScene();
                obsScene.name = scene.getString("name");
                JSONArray sources = scene.getJSONArray("sources");
                for (int j = 0; j < sources.length(); j++) {
                    JSONObject source = sources.getJSONObject(j);
                    Log.d("TEST", "-- Source: " + source.getString("name"));
                    ObsSource obsSource = new ObsSource(source);
                    obsScene.sources.add(obsSource);
                }
//                if (obsScene.name.equals(obsScenes.getCurrentPreviewScene())) {
//                    // if this scene is the currently active scene, also save it in the member
//                    // variable currentScene for later re-use by the sources button list view.
//                    currentPreviewScene = obsScene;
//                }
                obsScenes.scenes.add(obsScene);
            }

//            mainAct.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mainAct.newScenesAvailable();
//                }
//            });
            //mainAct.newScenesAvailable();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void invalidateAdapters() {
        if (obsScenes.getCurrentPreviewScene() == null) {
            Log.e("TEST", "Preview scene is null!");
        }
        mainAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mObsScenesChangedListener.onObsScenesChanged(obsScenes);
                mainAct.currentSceneName.setText(obsScenes.getCurrentScene());
                mObsSourcesChangedListener.onObsSourcesChanged(currentPreviewScene);
                mainAct.streamButton.setBackgroundResource(isStreaming ? R.drawable.on_air : R.drawable.not_on_air);
                mainAct.recordButton.setBackgroundResource(isRecording ? R.drawable.on_air : R.drawable.not_on_air);
           }
        });
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
                    Log.d("TEST", "secret_hash: " + Arrays.toString(secret_hash));
                    String secret = Base64.getEncoder().encodeToString(secret_hash);
                    Log.d("TEST", "secret: " + secret);
                    String auth_response_string = secret + challenge;
                    Log.d("TEST", "auth_response_string: " + auth_response_string);
                    byte[] auth_response_hash = digest.digest(auth_response_string.getBytes(StandardCharsets.UTF_8));
                    Log.d("TEST", "auth_response_hash: " + Arrays.toString(auth_response_hash));
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
     * @param remote (unknown)
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

    /**
     * Create an interface to be able to inform the RecyclerView adapter about any changes if
     * necessary
     */
    public interface ObsScenesChangedListener {
        void onObsScenesChanged(ObsScenesList obsScenesList);
    }

    /**
     * Create an interface to be able to inform the RecyclerView adapter for the Sources about any
     * changes if necessary
     */
    public interface ObsSourcesChangedListener {
        void onObsSourcesChanged(ObsScene currentScene);
    }

    /**
     * This variable holds the listener that is informed about changes of the scenes list
     */
    ObsScenesChangedListener mObsScenesChangedListener;

    public void setOnObsScenesChangedListener(ObsScenesChangedListener listener) {
        this.mObsScenesChangedListener = listener;
    }

    /**
     * This variable holds the listener that is informed about changes of the sources list
     */
    ObsSourcesChangedListener mObsSourcesChangedListener;

    public void setOnObsSourcesChangedListener(ObsSourcesChangedListener listener) {
        this.mObsSourcesChangedListener = listener;
    }
}
