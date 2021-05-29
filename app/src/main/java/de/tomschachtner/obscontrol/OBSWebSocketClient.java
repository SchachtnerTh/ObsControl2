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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.tomschachtner.obscontrol.obsdata.OBSAudioSource;
import de.tomschachtner.obscontrol.obsdata.ObsScene;
import de.tomschachtner.obscontrol.obsdata.ObsSceneItem;
import de.tomschachtner.obscontrol.obsdata.ObsScenesList;
import de.tomschachtner.obscontrol.obsdata.ObsSourceType;
import de.tomschachtner.obscontrol.obsdata.ObsSourceTypesList;
import de.tomschachtner.obscontrol.obsdata.ObsTransitionsList;

public class OBSWebSocketClient extends WebSocketClient {

    public static final String TAG = "ObsWebSocketClient_TS";
    public MainActivity mainAct;
    public MainActivity.status connStatus;

    public ObsScenesList obsScenes;
    public ObsScene currentPreviewScene;
    public ObsTransitionsList transitionsList;

    public ObsSourceTypesList obsSourceTypesList;
    public ArrayList<OBSAudioSource> obsAudioSources;
    private boolean userChangesVolume = false;

    /**
     * This variable holds the listener that is informed about changes of the scenes list
     */
    ObsScenesChangedListener mObsScenesChangedListener;
    /**
     * This variable holds the listener that is informed about changes of the sources list
     */
    ObsSourcesChangedListener mObsSourcesChangedListener;
    ObsTransitionsChangedListener mObsTransitionsChangedListener;
    ObsAudioChangedListener mObsAudioChangedListener;

    private boolean isStreaming = false;
    private boolean isRecording = false;
    private boolean bPendingTransition = false; // used to signal that a transition is currently going on
    String pendingSourceActiveName;

    private final int SCENES = 0b00000000001;
    private final int ACTIVE_SCENE = 0b00000000010;
    private final int PREVIEW_SCENE = 0b00000000100;
    private final int SCENE_ITEMS = 0b00000001000;
    private final int ITEMS_ACTIVE = 0b00000010000;
    private final int AUDIO_SOURCES = 0b00000100000;
    private final int TRANS_CHANGED = 0b00001000000;
    private final int TRANS_DONE = 0b00010000000;
    private final int STREAMING = 0b00100000000;
    private final int RECORDING = 0b01000000000;
    private final int AUDIO_VOLUMES = 0b10000000000;

    int targetUpdateStatus = SCENES
            + SCENE_ITEMS
            + ACTIVE_SCENE
            + PREVIEW_SCENE
            + ITEMS_ACTIVE
            + AUDIO_SOURCES
            + STREAMING
            + RECORDING
            + TRANS_CHANGED;

    //region Constructors

    /**
     * Constructs a WebSocketClient with a Callback mechanism
     */
    public OBSWebSocketClient(URI serverUri, MainActivity activity) {
        super(serverUri);
        mainAct = activity;
        connStatus = MainActivity.status.CLOSED;
        obsScenes = new ObsScenesList();
        currentPreviewScene = new ObsScene(); // normally not needed, but on app start, this might not yet
        // been initialized correctly, so we define a dummy empty scene so that app does not crash
        transitionsList = new ObsTransitionsList();

        obsAudioSources = new ArrayList<>();
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
    //endregion

    //region OBSWebSocket lifecycle methods

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
        connStatus = MainActivity.status.OPEN;
        mainAct.onConnectedToWebService();
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
        connStatus = MainActivity.status.CLOSED;
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
        if (ex instanceof ConnectException)
            mainAct.onConnectErrorFromWebService(ex.getLocalizedMessage());
    }
    //endregion

    //region authentication stuff

    /**
     * Checks if authentication is required to connect to the web service
     */
    public void checkAuthentication() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "GetAuthRequired");
            jso.put("message-id", "isAuthRequired_SCT");
            send(jso.toString());
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
    }

    /**
     * If authentication is required, perform authentication here
     *
     * @param jso return JSON data from the web service with authentication information included
     */
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
                    } catch (JSONException jse) {
                        jse.printStackTrace();
                    }

                } catch (NoSuchAlgorithmException nsae) {
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

    /**
     * After authentication, check the response from the web service (i. e. if the authentication
     * process was successful or not.
     *
     * @param jso JSON object returned from the web service after the authentication attempt
     */
    private void processAuthResult(JSONObject jso) {
        try {
            if (jso.getString("status").equals("error")) {
                if (jso.getString("error").equals("Authentication Failed.")) {
                    AlertInMainAct("Authentifizierung fehlgeschlagen.\nBitte Passwort in den Einstellungen setzen und neu anmelden.");
                }
            } else {
                Log.d("TEST", jso.getString("status"));
                JSONObject jso2;
                try {
                    jso2 = new JSONObject();
                    jso2.put("request-type", "GetSourceTypesList");
                    jso2.put("message-id", "GetSourceTypesList_SCT");
                    send(jso2.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //endregion

    int currentUpdateStatus = 0;
    boolean fillFullStatus;

    public void updateScenes() {
        fillFullStatus = true;
        getScenesList_req();
        getPreviewScene_req();
        getAudioSourcesList_req();
        getStreamingStatus_req();
        getTransitionsList_req();
    }

    public void updateVolumes() {
        fillFullStatus = true;
        getAudioSourcesList_req();
    }

    /**
     * Ein Request wird an OBS geschickt, dass alle verfügbaren Szenen übermittelt werden sollen
     */
    public void getScenesList_req() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "GetSceneList");
            jso.put("message-id", "SceneList_SCT");
            send(jso.toString());
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
    }

    /**
     * Eine Struktur mit allen im ausgewählten OBS-Profil verfügbaren Szenen wird angelegt
     *
     * @param jso Struktur, die alle OBS-Szenen enthält
     */
    private void getScenesList_resp(JSONObject jso) {
        try {
            obsScenes = new ObsScenesList();
            obsScenes.setCurrentScene(jso.getString("current-scene"));
            JSONArray scenes = jso.getJSONArray("scenes");
            for (int i = 0; i < scenes.length(); i++) {
                JSONObject scene = scenes.getJSONObject(i);
                Log.d("TEST", "Szene: " + scene.getString("name"));
                ObsScene obsScene = new ObsScene();
                obsScene.name = scene.getString("name");
                JSONArray sceneItems = scene.getJSONArray("sources");
                for (int j = 0; j < sceneItems.length(); j++) {
                    JSONObject sceneItem = sceneItems.getJSONObject(j);
                    Log.d("TEST", "-- Source: " + sceneItem.getString("name"));
                    ObsSceneItem obsSceneItem = new ObsSceneItem(sceneItem);
                    obsScene.sceneItems.add(obsSceneItem);
                }
                obsScenes.scenes.add(obsScene);
            }
            if (fillFullStatus) {
                currentUpdateStatus |= SCENES | SCENE_ITEMS | ACTIVE_SCENE | ITEMS_ACTIVE;
                checkInvalidate();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkInvalidate() {
        if (currentUpdateStatus == targetUpdateStatus) {
            fillFullStatus = false;
            currentUpdateStatus = 0;
            mainAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String selectedTab = mainAct.categoryTabs.getTabAt(mainAct.categoryTabs.getSelectedTabPosition()).getText().toString();
                    if (selectedTab.toUpperCase().equals("SZENEN")) {
                        mObsScenesChangedListener.onObsScenesChanged(obsScenes);
                        mainAct.scenesFragment.currentSceneName.setText(obsScenes.getCurrentScene());
                        mObsSourcesChangedListener.onObsSourcesChanged(currentPreviewScene);
                        mainAct.scenesFragment.streamButton.setBackgroundResource(isStreaming ? R.drawable.on_air : R.drawable.not_on_air);
                        mainAct.scenesFragment.recordButton.setBackgroundResource(isRecording ? R.drawable.on_air : R.drawable.not_on_air);
                    }
                    if (selectedTab.toUpperCase().equals("TRANS/VOL")) {
                        mObsTransitionsChangedListener.onObsTransitionsChanged(transitionsList);
                        mObsAudioChangedListener.onObsAudioChanged(obsAudioSources);
                    }
                }
            });
            // TODO: check which tab is active and invalidate all adapters on that tab
        }
    }

    /**
     * Ein Request wird on OBS geschickt, dass die aktuell Preview-Szene übermittelt werden soll
     */
    public void getPreviewScene_req() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "GetPreviewScene");
            jso.put("message-id", "GetPreviewScene_SCT");
            send(jso.toString());
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
    }

    private void getPreviewScene_resp(JSONObject jso) {
        try {
            obsScenes.setCurrentPreviewScene(jso.getString("name"));
            for (int i = 0; i < obsScenes.scenes.size(); i++) {
                if (obsScenes.scenes.get(i).name.equals(obsScenes.getCurrentPreviewScene())) {
                    currentPreviewScene = obsScenes.scenes.get(i);
                }
            }
            if (fillFullStatus) {
                currentUpdateStatus |= PREVIEW_SCENE;
                checkInvalidate();
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    /**
     * Ein Request wird an OBS geschickt, dass die Preview-Szene umgeschaltet werden soll
     *
     * @param newScene
     */
    public void switchPreviewScene(String newScene) {
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

    /**
     * Ein Request wird an OBS geschickt, dass die aktuelle Preview-Szene mit dem aktuellen Übergang
     * mit der aktuellen aktiven Szene getauscht werden soll.
     */
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

    /**
     * Um ein Scene Item umschalten zu können, muss erst ermittelt werden, wie sein aktueller Status
     * ist. Dann wird der komplementäre Status gesetzt.
     *
     * @param sceneItem
     */
    public void toggleSceneItemVisibility(String sceneItem) {
        boolean isVisible = false;
        for (int i = 0; i < currentPreviewScene.sceneItems.size(); i++) {
            if (currentPreviewScene.sceneItems.get(i).name.equals(sceneItem)) {
                isVisible = currentPreviewScene.sceneItems.get(i).render;
            }
        }
        setSceneItemVisible_req(currentPreviewScene.name, sceneItem, !isVisible);
    }

    /**
     * Ein Request wird an OBS geschickt, um ein Scene Item ab- oder anzuschalten
     *
     * @param scene      Die Szene, von der ein Item ab- oder angeschaltet werden soll
     * @param sceneItem  Das Scene Item, das umgeschaltet werden soll
     * @param setVisible true, wenn das Item sichtbar sein soll; false, wenn nicht
     */
    private void setSceneItemVisible_req(String scene, String sceneItem, boolean setVisible) {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "SetSceneItemRender");
            jso.put("message-id", "changeScreenItemRender_SCT");
            jso.put("scene-name", scene);
            jso.put("source", sceneItem);
            jso.put("render", setVisible);
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ein Request wird an OBS geschickt, um das Streamen ein- oder auszuschalten
     */
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

    /**
     * Ein Request wird an OBS geschickt, um die Aufnahme ein- oder auszuschalten
     */
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

    /**
     * Ein Request wird an OBS geschickt, um eine Liste aller verfügbaren Übergänge (transistions)
     * zu erhalten
     */
    public void getTransitionsList_req() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "GetTransitionList");
            jso.put("message-id", "translitionsList_SCT");
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mit den Antworten, die von OBS kommen, wird die interne Datenstruktur alle Übergänge befüllt
     *
     * @param jso
     */
    private void getTransitionsList_resp(JSONObject jso) {
        try {
            transitionsList.setCurrentTransition(jso.getString("current-transition"));
            JSONArray transitionsArr = jso.getJSONArray("transitions");
            transitionsList.transitions.clear();
            for (int i = 0; i < transitionsArr.length(); i++) {
                transitionsList.transitions.add(transitionsArr.getJSONObject(i).getString("name"));
            }
            if (fillFullStatus) {
                currentUpdateStatus |= TRANS_CHANGED;
                checkInvalidate();
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    /**
     * Ein anderer als der Standard-Übergang soll durchgeführt werden.
     *
     * @param customTransitionName Der Name für diesen Übergang (wird aber nirgends sonst verwendet)
     */
    public void makeCustomTransition(String customTransitionName) {
        JSONObject jso;
        try {

            JSONObject trans = new JSONObject();
            trans.put("name", customTransitionName);
            jso = new JSONObject();
            jso.put("request-type", "TransitionToProgram");
            jso.put("message-id", "transProgramCustom_SCT");
            jso.put("with-transition.name", "CustomTransition");
            jso.put("with-transition", trans);
//            doUIUpdates = false;
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Nach einem Nicht-Standard-Übergang wird auch der Standard-Übergang umgestellt.
     * Diese Funktion macht das rückgängig, indem es den vorherigen Standard-Übergang aus der
     * internen Datenstruktur aller Übergänge verwendet und in OBS den Standard-Übergang
     * entsprechend setzt.
     * Durch die Semaphore wird sicher gestellt, dass die Übergangs-Buttons in der App nicht
     * zwischenzeitlich den Standardübergang ändern und dann gleich wieder zurückändern.
     */
    private void backToOriginalDefaultTransition() {
        setCurrentTransition(transitionsList.getCurrentTransition());
        bPendingTransition = false;
    }

    /**
     * Es wird ein Request an OBS geschickt, dass der Standard-Übergang durch einen anderen ausge-
     * tauscht werden soll
     *
     * @param newCurrentTransition
     */
    public void setCurrentTransition(String newCurrentTransition) {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "SetCurrentTransition");
            jso.put("message-id", "NewCurrentTransition_SCT");
            jso.put("transition-name", newCurrentTransition);
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Es wird ein Request an OBS geschickt, um eine Liste aller Audioquellen zu erhalten
     * Dabei werden im ersten Schritt alle Quellen zurückgeliefert, dann wird aber weiter abgeprüft,
     * ob die Quelle auch eine Audioquelle ist. In einem dritten Schritt wird geprüft, ob die Audio-
     * quelle auch gerade aktiv ist.
     */
    public void getAudioSourcesList_req() {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "GetSourcesList");
            jso.put("message-id", "GetSourcesList_SCT");
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int numberOfAudioSources;
    boolean numberOfAudioSourcesValid = false;
    int audioSourcesDone;

    private void getAudioSourcesList_resp(JSONObject jso, int stage) {
        mainAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (stage) {
                    case 1:
                        if (targetUpdateStatus == AUDIO_VOLUMES) { // if user changes volume, not all audio sources have to be reread...
                            for (OBSAudioSource source :
                                    obsAudioSources) {
                                JSONObject jso2 = new JSONObject();
                                try {
                                    jso2.put("request-type", "GetVolume");
                                    jso2.put("source", source.name);
                                    jso2.put("useDecibel", "true");
                                    jso2.put("message-id", "GetVolume_SCT");
                                    send(jso2.toString());
                                } catch (JSONException jsonException) {
                                    jsonException.printStackTrace();
                                }
                            }
                        } else {
                            numberOfAudioSources = 0; // clear the number of audio sources
                            numberOfAudioSourcesValid = false;
                            audioSourcesDone = 0;
                            JSONArray jsonArray;
                            try {
                                jsonArray = jso.getJSONArray("sources");
                                obsAudioSources.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject source = jsonArray.getJSONObject(i);
                                    String typeId = source.getString("typeId");
                                    for (ObsSourceType obsSourceType : obsSourceTypesList.obsSources) {
                                        if (obsSourceType.typeId.equals(typeId) && obsSourceType.caps.hasAudio) {
                                            OBSAudioSource obsAudioSource = new OBSAudioSource();
                                            obsAudioSource.name = source.getString("name");
                                            obsAudioSource.typeId = typeId;
                                            obsAudioSource.type = source.getString("type");
                                            obsAudioSources.add(obsAudioSource);
                                            JSONObject jso2 = new JSONObject();
                                            numberOfAudioSources++;
                                            jso2.put("request-type", "GetVolume");
                                            jso2.put("source", obsAudioSource.name);
                                            jso2.put("useDecibel", "true");
                                            jso2.put("message-id", "GetVolume_SCT");
                                            send(jso2.toString());
                                        }
                                    }
                                }
                                numberOfAudioSourcesValid = true;

                            } catch (JSONException jsonException) {
                                jsonException.printStackTrace();
                            }
                        }
                        break;
                    case 2:
                        for (OBSAudioSource obsAudioSource :
                                obsAudioSources) {
                            try {
                                if (obsAudioSource.name.equals(jso.getString("name"))) {
                                    mainAct.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            try {
                                                obsAudioSource.volume = (int) (jso.getDouble("volume") * 1000);
                                                obsAudioSource.isMuted = jso.getBoolean("muted");
                                                //invalidateAdapters();
                                                audioSourcesDone++;
                                                if (numberOfAudioSourcesValid && (audioSourcesDone >= numberOfAudioSources) && fillFullStatus) {
                                                    if ((targetUpdateStatus & AUDIO_SOURCES) > 0) {
                                                        currentUpdateStatus |= AUDIO_SOURCES;
                                                    }
                                                    if ((targetUpdateStatus & AUDIO_VOLUMES) > 0) {
                                                        currentUpdateStatus |= AUDIO_VOLUMES;
                                                    }
                                                    audioSourcesDone = 0;
                                                    checkInvalidate();
                                                }


                                                // check if audio source is available in currently active scene
//                                                boolean found = false;
//                                                for (ObsScene scene :
//                                                        obsScenes.scenes) {
//                                                    if (scene.name.equals(obsScenes.getCurrentScene())) {
//                                                        for (ObsSceneItem item :
//                                                                scene.sceneItems) {
//                                                            if (item.name.equals(obsAudioSource.name)) {
//                                                                found = true;
//                                                            }
//                                                        }
//                                                    }
//                                                }

//                                                if (!found) {
//                                                    for (int i = 0; i < obsAudioSources.size(); i++) {
//                                                        if (obsAudioSources.get(i).name.equals(obsAudioSource.name)) {
//                                                            obsAudioSources.remove(i);
//                                                        }
//                                                    }
//                                                }

                                            } catch (JSONException jsonException) {
                                                jsonException.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            } catch (JSONException jsonException) {
                                jsonException.printStackTrace();
                            }
                        }
                        break;
                    // TODO enhance Audio structure to contain information about mute status and volume level
                }
            }
        });
    }

    /**
     * Die Audioquelle an Position @param position wird stummgeschaltet oder die Stummschaltung
     * wird aufgehoben.
     *
     * @param position Position der Audioquelle in der internen Datenstruktur
     * @param checked  true, wenn die Quelle stummgeschaltet werden soll; false, wenn die Stummschaltung aufgehoben werden soll.
     */
    public void setMute(int position, boolean checked) {
        JSONObject jso;
        try {
            jso = new JSONObject();
            jso.put("request-type", "SetMute");
            jso.put("source", obsAudioSources.get(position).name);
            jso.put("mute", checked ? true : false);
            jso.put("message-id", "SetMute_SCT");
            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prüfen, ob Streaming oder Recording gerade aktiv ist. Ein entsprechender Request wird an OBS
     * geschickt.
     */
    private void getStreamingStatus_req() {
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

    /**
     * Die Antwort von OBS darauf, ob Streaming oder Recording aktiv ist
     *
     * @param jso JSON, das von OBS zurückgegeben wurde mit der Antwort
     */
    private void getStreamingStatus_resp(JSONObject jso) {
        try {
            isStreaming = jso.getBoolean("streaming");
            isRecording = jso.getBoolean("recording");
            if (fillFullStatus) {
                currentUpdateStatus |= RECORDING | STREAMING;
                checkInvalidate();
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    /**
     * Callback for string messages received from the remote host
     *
     * @param message The UTF-8 decoded message that was received.
     * @see #onMessage(ByteBuffer)
     **/
    @Override
    public void onMessage(String message) {
        JSONObject jso = null;
        try {
            jso = new JSONObject(message);
            final int targetFullUpdateStatus = SCENES
                    + SCENE_ITEMS
                    + ACTIVE_SCENE
                    + PREVIEW_SCENE
                    + ITEMS_ACTIVE
                    + AUDIO_SOURCES
                    + STREAMING
                    + RECORDING
                    + TRANS_CHANGED;
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
                        this.targetUpdateStatus = targetFullUpdateStatus;
                        updateScenes();
                        break;
                    case "SceneList_SCT":
                        Log.d("TEST", jso.toString());
                        getScenesList_resp(jso);
                        getPreviewScene_req();
                        break;
                    case "SetScene_SCT":
                    case "transProgram_SCT":
                        Log.d("TEST", jso.toString());
                        TimeUnit.MILLISECONDS.sleep(200);
                        //updateScenes();
                        break;
                    case "GetPreviewScene_SCT":
                        Log.d("TEST", jso.toString());
                        getPreviewScene_resp(jso);
                        // getStreamingStatus_req();
                        // invalidateAdapters();
                        break;
                    case "changeScreenItemRender_SCT":
                    case "SetMute_SCT":
                        Log.d("TEST", jso.toString());
                        //updateScenes();
                        break;
                    case "getStreamingStatus_SCT":
                        Log.d("TEST", jso.toString());
                        getStreamingStatus_resp(jso);
                        // invalidateAdapters();
                        break;
                    case "toggleStreaming_SCT":
                    case "toggleRecording_SCT":
                        Log.d("TEST", jso.toString());
                        this.targetUpdateStatus = RECORDING + STREAMING;
                        updateScenes();
                        break;
                    case "translitionsList_SCT":
                        Log.d("TEST", jso.toString());
                        getTransitionsList_resp(jso);
                        // invalidateAdapters();
                        break;
                    case "transProgramCustom_SCT":
                        Log.d("TEST", jso.toString());
                        // Während des Übergangs sollen keine weiteren Updates am UI gemacht werden
                        // Die Semaphore bPendingTransition sorgt dafür.
                        // Außerdem wird damit erkannt, dass der angeforderte Übergang abgeschlossen
                        // ist, wenn eine entsprechende Statusmeldung von OBS verschickt wird
                        bPendingTransition = true;
                        // Is not thread-safe: There could be another transaction kicking in while this transaction is still running...
                        TimeUnit.MILLISECONDS.sleep(200);
                        break;
                    case "NewCurrentTransition_SCT":
                        Log.d("TEST", jso.toString());
                        getTransitionsList_req();
                        break;
                    case "GetSourcesList_SCT":
                        Log.d("TEST", jso.toString());
                        getAudioSourcesList_resp(jso, 1);
                        break;
                    case "GetVolume_SCT":
                        Log.d("TEST", jso.toString());
                        getAudioSourcesList_resp(jso, 2);
                        break;
                    case "GetSourceTypesList_SCT":
                        Log.d("TEST", jso.toString());
                        obsSourceTypesList = new ObsSourceTypesList(jso);
//                        getAudioSourcesList();
                        break;
                    case "GetSourceActive_SCT":
                        Log.d("TEST", jso.toString());
                        getAudioSourcesList_resp(jso, 3);
                        break;
                    case "SetVolume_SCT":
                        Log.d("TEST", jso.toString());
                        break;
                    default:
                        ToastInMainAct("Unbekannte Antwort vom WebService!");
                }
            } else if (jso.has("update-type")) {
                //ToastInMainAct("Update from OBS");
                Log.e("TEST", jso.toString());
                switch (jso.getString("update-type")) {
                    case "TransitionEnd":
                        if (bPendingTransition) {
                            backToOriginalDefaultTransition();
                            TimeUnit.MILLISECONDS.sleep(200);
                            this.targetUpdateStatus = targetFullUpdateStatus;
                            //updateScenes();
                        }
                        break;
                    case "SwitchTransition":
                        if (!bPendingTransition) {
                            targetUpdateStatus = targetFullUpdateStatus;
                            updateScenes(); // war vorher: getTransitionsList_req();
                        }
                        break;
                    case "SourceVolumeChanged":
                        if (!bPendingTransition && !userChangesVolume) {
                            targetUpdateStatus = AUDIO_VOLUMES;
                            updateVolumes();
                        }
                        break;
                    default:
                        if (!bPendingTransition) {
                            if (userChangesVolume) {
                                targetUpdateStatus = AUDIO_VOLUMES;
                                updateVolumes();
                            } else {
                                targetUpdateStatus = targetFullUpdateStatus;
                                updateScenes();
                            }

                        }
                        //getAudioSourcesList();
                        // TODO: GetSourceActive mit einbauen (Audioquelle darf nur angezeigt werden, wenn true
                }
            }

        } catch (JSONException | InterruptedException e) {
            //e.printStackTrace();
            if (jso != null) Log.e("TEST", jso.toString());
        }

    }

    public void invalidateAdapters() {
//        if (doUIUpdates) {
        if (obsScenes.getCurrentPreviewScene() == null) {
            Log.e("TEST", "Preview scene is null!");
        }
        mainAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mObsScenesChangedListener.onObsScenesChanged(obsScenes);
                mainAct.scenesFragment.currentSceneName.setText(obsScenes.getCurrentScene());
                mObsSourcesChangedListener.onObsSourcesChanged(currentPreviewScene);
                mainAct.scenesFragment.streamButton.setBackgroundResource(isStreaming ? R.drawable.on_air : R.drawable.not_on_air);
                mainAct.scenesFragment.recordButton.setBackgroundResource(isRecording ? R.drawable.on_air : R.drawable.not_on_air);
                if (mObsTransitionsChangedListener != null)
                    mObsTransitionsChangedListener.onObsTransitionsChanged(transitionsList);
                //Log.e("TEST", obsAudioSources.toString());
                // TODO: Remove check for null on obsAudioSources
//                if (mObsAudioChangedListener != null) mObsAudioChangedListener.onObsAudioChanged(obsAudioSources);
            }
        });
        //       }
    }

    public void startUserVolumeChange() {
        userChangesVolume = true;
    }

    public void stopUserVolumeChange() {
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        userChangesVolume = false;
    }

    public void setVolume(int adapterPosition, int volume) {
        JSONObject jso = new JSONObject();
        try {
            Log.e("TEST", String.valueOf(adapterPosition));
            jso.put("request-type", "SetVolume");
            jso.put("source", obsAudioSources.get(adapterPosition).name);
            jso.put("volume", volume / 1000.0);
            jso.put("useDecibel", "true");
            jso.put("message-id", "SetVolume_SCT");
            send(jso.toString());
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    //region alert and Toast mechanisms
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
//endregion

//region Listeners (interfaces)
    /*
     * Listeners allow us to call methods in other classes
     */

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

    public interface ObsTransitionsChangedListener {
        void onObsTransitionsChanged(ObsTransitionsList transitions);
    }

    public interface ObsAudioChangedListener {
        void onObsAudioChanged(ArrayList<OBSAudioSource> audioSources);

    }
    //endregion

    //region convenience  methods to set listeners
    public void setOnObsScenesChangedListener(ObsScenesChangedListener listener) {
        this.mObsScenesChangedListener = listener;
    }

    public void setOnObsSourcesChangedListener(ObsSourcesChangedListener listener) {
        this.mObsSourcesChangedListener = listener;
    }

    public void setOnObsTransitionsChangedListener(ObsTransitionsChangedListener
                                                           listener) {
        this.mObsTransitionsChangedListener = listener;
    }

    public void setOnObsAudioChangedListener(ObsAudioChangedListener listener) {
        this.mObsAudioChangedListener = listener;
    }
    //endregion

}
