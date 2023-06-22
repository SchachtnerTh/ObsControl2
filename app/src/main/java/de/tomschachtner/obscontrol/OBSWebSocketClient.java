package de.tomschachtner.obscontrol;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.tomschachtner.obscontrol.obsdata.OBSAudioSource;
import de.tomschachtner.obscontrol.obsdata.ObsScene;
import de.tomschachtner.obscontrol.obsdata.ObsSceneItem;
import de.tomschachtner.obscontrol.obsdata.ObsScenesList;
import de.tomschachtner.obscontrol.obsdata.ObsSourceTypesList;
import de.tomschachtner.obscontrol.obsdata.ObsTransitionsList;

public class OBSWebSocketClient
        extends WebSocketClient {

    //region Constants
    public final static int OBS_OP_HELLO = 0;
    public final static int OBS_OP_IDENTIFY = 1;
    public final static int OBS_OP_IDENTIFIED = 2;
    public final static int OBS_OP_REIDENTIFY = 3;
    public final static int OBS_OP_EVENT = 5;
    public final static int OBS_OP_REQUEST = 6;
    public final static int OBS_OP_REQRESPONSE = 7;

    public final static int OBS_EVENT_SUBSCR_NONE = 0;
    public final static int OBS_EVENT_SUBSCR_GENERAL = 1;
    public final static int OBS_EVENT_SUBSCR_CONFIG = 2;
    public final static int OBS_EVENT_SUBSCR_SCENES = 4;
    public final static int OBS_EVENT_SUBSCR_INPUTS = 8;
    public final static int OBS_EVENT_SUBSCR_TRANSITIONS = 16;
    public final static int OBS_EVENT_SUBSCR_FILTERS = 32;
    public final static int OBS_EVENT_SUBSCR_OUTPUTS = 64;
    public final static int OBS_EVENT_SUBSCR_SCENEITEMS = 128;
    public final static int OBS_EVENT_SUBSCR_MEDIAINPUTS = 256;
    public final static int OBS_EVENT_SUBSCR_VENDORS = 512;
    public final static int OBS_EVENT_SUBSCR_UI = 1024;
    public final static int OBS_EVENT_SUBSCR_ALL = OBS_EVENT_SUBSCR_GENERAL |
            OBS_EVENT_SUBSCR_CONFIG |
            OBS_EVENT_SUBSCR_SCENES |
            OBS_EVENT_SUBSCR_INPUTS |
            OBS_EVENT_SUBSCR_TRANSITIONS |
            OBS_EVENT_SUBSCR_FILTERS |
            OBS_EVENT_SUBSCR_OUTPUTS |
            OBS_EVENT_SUBSCR_SCENEITEMS |
            OBS_EVENT_SUBSCR_MEDIAINPUTS |
            OBS_EVENT_SUBSCR_VENDORS |
            OBS_EVENT_SUBSCR_UI;
    public final static String OBS_REQ_GET_SCENE_LIST = "GetSceneList";
    public final static String OBS_REQ_GET_SCENE_ITEMS_LIST = "GetSceneItemList";
    public final static String OBS_REQ_SET_CURRENT_PREVIEW_SCENE = "SetCurrentPreviewScene";
    public final static String OBS_REQ_GET_CURRENT_PREVIEW_SCENE = "GetCurrentPreviewScene";
    public final static String OBS_REQ_SET_SCENE_ITEM_ENABLED = "SetSceneItemEnabled";
    public final static String OBS_REQ_TOGGLE_STREAM = "ToggleStream";
    public final static String OBS_REQ_TOGGLE_RECORDING = "ToggleRecord";
    public final static String OBS_REQ_TRIGGER_STUDIO_MODE_TRANSITION = "TriggerStudioModeTransition";
    public final static String OBS_REQ_GET_INPUT_LIST = "GetInputList";
    public final static String OBS_REQ_GET_INPUT_SETTINGS = "GetInputSettings";
    public final static String OBS_REQ_GET_INPUT_MUTE = "GetInputMute";
    public final static String OBS_REQ_GET_INPUT_VOLUME = "GetInputVolume";
    public final static String OBS_REQ_SET_INPUT_VOLUME = "SetInputVolume";
    public final static String OBS_REQ_SET_INPUT_MUTE = "SetInputMute";
    public final static String OBS_REQ_GET_VERSION = "GetVersion";
    public final static String OBS_REQ_GET_SOURCE_SCREENSHOT = "GetSourceScreenshot";
    public final static String OBS_EVT_INPUT_VOLUME_CHANGED = "InputVolumeChanged";
    public final static String OBS_EVT_INPUT_MUTE_STATE_CHANGED = "InputMuteStateChanged";
    public final static String OBS_EVT_CURRENT_PREVIEW_SCENE_CHANGED = "CurrentPreviewSceneChanged";
    public final static String OBS_EVT_CURRENT_PROGRAM_SCENE_CHANGED = "CurrentProgramSceneChanged";

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

    // No audio support
    private final String OBS_INPUT_MONITOR_CAPTURE = "monitor_capture";
    private final String OBS_INPUT_IMAGE_SOURCE = "image_source";
    private final String OBS_INPUT_SLIDESHOW = "slideshow";
    private final String OBS_INPUT_TEXT_GDIPLUS_V2 = "text_gdiplus_v2";

    // audio support
    private final String OBS_INPUT_DSHOW_INPUT = "dshow_input";
    private final String OBS_INPUT_WASAPI_INPUT_CAPTURE = "wasapi_input_capture";
    private final String OBS_INPUT_WASAPI_OUTPUT_CAPTURE = "wasapi_output_capture";
    private final String OBS_INPUT_BROWSER_SOURCE = "browser_source";

    public static final String TAG = "ObsWebSocketClient_TS";

    //endregion

    // region Variables
    public MainActivity mainAct;
    public MainActivity.status connStatus;
    public ObsScenesList obsScenes;
    public ObsScene currentPreviewScene;
    public ObsTransitionsList transitionsList;
    public ObsSourceTypesList obsSourceTypesList;
    public ArrayList<OBSAudioSource> obsAudioSources;
    private boolean userChangesVolume = false;
    private Map<UUID, String> ScenesToItems;
    Handler mainHandler;
    private boolean isStreaming = false;
    private boolean isRecording = false;
    private boolean bPendingTransition = false; // used to signal that a transition is currently going on
    int targetUpdateStatus = SCENES
            + SCENE_ITEMS
            + ACTIVE_SCENE
            + PREVIEW_SCENE
            + ITEMS_ACTIVE
            + AUDIO_SOURCES
            + STREAMING
            + RECORDING
            + TRANS_CHANGED;
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
    String pendingSourceActiveName;
    int currentUpdateStatus = 0;
    boolean fillFullStatus;
    UUID getCurrentPreviewScreenReqUUID=null;
    UUID triggerStudioModeTransitionUUID = null;
    UUID setSceneItemRenderUUID = null;
    UUID toggleStreamUUID=null;
    UUID toggleRecordUUID=null;
    UUID getInputListUUID=null;
    // endregion

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

    //region Utility methods
    private JSONObject CreateBasicRequest(String reqType, String reqId, Object reqData) {
        JSONObject dataObject = new JSONObject();
        JSONObject request = new JSONObject();
            //currentRequestType = reqType;
            //currentRequestId = reqId;
            try {
                dataObject.put("requestType", reqType);
                dataObject.put("requestId", reqId);
                if (reqData != null) {
                    dataObject.put("requestData", reqData);
                }

                request.put("op", OBS_OP_REQUEST);
                request.put("d", dataObject);
            } catch (JSONException e) {
                e.printStackTrace();
                return (JSONObject) null;
            }
            return request;
    }

    private JSONObject CreateBasicRequest(String reqType, String reqId) {
        return CreateBasicRequest(reqType,reqId, null);
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
     * If authentication is required, perform authentication here
     *
     * @param jso return JSON data from the web service with authentication information included
     */
    private void checkAndDoAuth(JSONObject jso) {
        // Check, if auth is necessary
        try {
            if (!jso.has("authentication")) {
                Log.d(TAG, "No authentication needed.");
                return;
            }
            JSONObject jsAuth = jso.getJSONObject("authentication");
            Log.d("TEST", "Authentication required.");
            // Check, if there's a password saved in the preferences.
            // If not, show them and give the user a chance to set the password
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mainAct);
            String passwd;
            passwd = sp.getString("password_preference", "");

            // We are sure to have a password stored in the Preferences.
            // Use it to authenticate
            String salt = jsAuth.getString("salt");
            String challenge = jsAuth.getString("challenge");

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

                JSONObject authResponse, authRespData;

                authRespData = new JSONObject();
                authRespData.put("rpcVersion", 1);
                authRespData.put("authentication", auth_response);
                authRespData.put("eventSubscriptions", OBS_EVENT_SUBSCR_ALL);

                authResponse = new JSONObject();
                authResponse.put("op", OBS_OP_IDENTIFY);
                authResponse.put("d", authRespData);

                send(authResponse.toString());

            } catch (NoSuchAlgorithmException nsae) {
                Log.e("TEST", "NoSuchAlgorithmException thrown: " + nsae.getLocalizedMessage());
            } catch (JSONException e) {
                e.printStackTrace();
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
            if (jso.has("negotiatedRpcVersion")) {
                // Authentication succeeded
                if (jso.getInt("negotiatedRpcVersion") != 1) {
                    AlertInMainAct("Falsche RPC-Protokoll-Version.");
                    throw new UnsupportedOperationException();
                }

                // Sämtliche Szenen abholen usw TODO
                updateScenes();

            } else {
                AlertInMainAct("Authentifizierung fehlgeschlagen.\nBitte Passwort in den Einstellungen setzen und neu anmelden.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //endregion


    public void updateScenes() {
        fillFullStatus = true; // Hole alle Szenen
        getScenesList_req();
        //getSceneItemsList_req(UUID.randomUUID(), "Szene 2");
        getPreviewScene_req();
        getInputList_req();
        //getAudioSourcesList_req();
        //getStreamingStatus_req();
        //getTransitionsList_req();
    }

    public void updateVolumes() {
        fillFullStatus = true;
        getInputList_req();
    }

    /**
     * Ein Request wird an OBS geschickt, dass alle verfügbaren Szenen übermittelt werden sollen
     */
    public void getScenesList_req() {
        JSONObject jso = this.CreateBasicRequest(OBS_REQ_GET_SCENE_LIST, "SceneList_SCT");
        send(jso.toString());
    }

    /**
     * Ein Request wird an OBS geschickt, dass alle verfügbaren Scene Items übermittelt werden sollen
     */
    public void getSceneItemsList_req(UUID thisInstance, String sceneName) {
        //JSONObject jso = this.CreateBasicRequest(OBS_REQ_GET_SCENE_ITEMS_LIST, thisInstance.toString());
        JSONObject payload = new JSONObject();
        try {
            payload.put("sceneName", sceneName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jso2 = this.CreateBasicRequest(
                OBS_REQ_GET_SCENE_ITEMS_LIST,
                thisInstance.toString(),
                payload);
        send(jso2.toString());
    }

    public void getSceneItemsList_resp(JSONObject jso, String referencedScene) {
        // Erst mal rausfinden, zu welcher Szene die zurückgelieferten SceneItems überhaupt gehören
        try {
            JSONArray sceneItems = jso.getJSONArray("sceneItems");
            for (int i = 0; i < sceneItems.length(); i++) {
                JSONObject sceneItem = sceneItems.getJSONObject(i);
                ObsSceneItem obsSceneItem = new ObsSceneItem(sceneItem);
                int sceneID = obsScenes.findSceneIdByName(referencedScene);
                if (sceneID != -1) // Wenn die Szene nicht gefunden wurde, weil sie z. B. leer war,
                    // macht es natürlich keinen Sinn, diese Szene zu suchen.
                    obsScenes.scenes.get(sceneID).sceneItems.add(obsSceneItem);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }



/*        JSONArray sceneItems = scene.getJSONArray("sources");
        for (int j = 0; j < sceneItems.length(); j++) {
            JSONObject sceneItem = sceneItems.getJSONObject(j);
            Log.d("TEST", "-- Source: " + sceneItem.getString("name"));
            ObsSceneItem obsSceneItem = new ObsSceneItem(sceneItem);
            obsScene.sceneItems.add(obsSceneItem);
        }
*/

    }

    /**
     * Eine Struktur mit allen im ausgewählten OBS-Profil verfügbaren Szenen wird angelegt
     *
     * @param jso Struktur, die alle OBS-Szenen enthält
     */
//    private void getScenesList_resp(JSONObject jso) {
//        try {
//            obsScenes = new ObsScenesList();
//            obsScenes.setCurrentScene(jso.getString("currentProgramSceneName"));
//            JSONArray scenes = jso.getJSONArray("scenes");
//            for (int i = 0; i < scenes.length(); i++) {
//                JSONObject scene = scenes.getJSONObject(i);
//                Log.d("TEST", "Szene: " + scene.getString("sceneName"));
//                ObsScene obsScene = new ObsScene();
//                obsScene.name = scene.getString("sceneName");
//                // Get SceneItemList
//                UUID thisGUID = java.util.UUID.randomUUID();
//
//                JSONArray sceneItems = scene.getJSONArray("sources");
//                for (int j = 0; j < sceneItems.length(); j++) {
//                    JSONObject sceneItem = sceneItems.getJSONObject(j);
//                    Log.d("TEST", "-- Source: " + sceneItem.getString("name"));
//                    ObsSceneItem obsSceneItem = new ObsSceneItem(sceneItem);
//                    obsScene.sceneItems.add(obsSceneItem);
//                }
//                obsScenes.scenes.add(obsScene);
//            }
//            if (fillFullStatus) {
//                currentUpdateStatus |= SCENES | SCENE_ITEMS | ACTIVE_SCENE | ITEMS_ACTIVE;
//                checkInvalidate();
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Eine Struktur mit allen im ausgewählten OBS-Profil verfügbaren Szenen wird angelegt
     *
     * @param jso Struktur, die alle OBS-Szenen enthält
     */
    private void getScenesList_resp2(JSONObject jso) {
        try {
            obsScenes = new ObsScenesList();
            obsScenes.setCurrentScene(jso.getString("currentProgramSceneName"));
            JSONArray scenes = jso.getJSONArray("scenes");
            ScenesToItems = new HashMap<>();
            for (int i = 0; i < scenes.length(); i++) {
                JSONObject scene = scenes.getJSONObject(i);
                Log.d("TEST", "Szene: " + scene.getString("sceneName"));
                ObsScene obsScene = new ObsScene();
                obsScene.name = scene.getString("sceneName");
                obsScenes.scenes.add(obsScene);
                UUID thisScene = UUID.randomUUID();
                ScenesToItems.put(thisScene, obsScene.name);
                getSceneItemsList_req(thisScene, obsScene.name);
            }
            if (fillFullStatus) {
                currentUpdateStatus |= SCENES | SCENE_ITEMS | ACTIVE_SCENE | ITEMS_ACTIVE;
                checkInvalidate();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //getSceneItemsList_req(UUID.randomUUID(), "Szene 2");
        //scenesValid = true;
        //invalidateAdapters();
        mainAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mainAct.scenesFragment.sceneButtonsAdapter != null)
                    mainAct.scenesFragment.sceneButtonsAdapter.notifyDataSetChanged();
                if (mainAct.scenesFragment.sceneButtonsAdapter != null)
                    mainAct.scenesFragment.sourceButtonsAdapter.notifyDataSetChanged();
            }
        });
    }

    //public boolean scenesValid = false;

    private void invalidateControls() {
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
        jso = new JSONObject();
        getCurrentPreviewScreenReqUUID = UUID.randomUUID();
        jso = CreateBasicRequest(
                OBS_REQ_GET_CURRENT_PREVIEW_SCENE,
                getCurrentPreviewScreenReqUUID.toString());
        send(jso.toString());
    }

    private void getPreviewScene_resp(JSONObject jso, UUID lastCall) {
        try {
            if (lastCall.equals(getCurrentPreviewScreenReqUUID)) {
                getCurrentPreviewScreenReqUUID = null;
                String currenPreviewSceneName = jso.getString("currentPreviewSceneName");
                obsScenes.setCurrentPreviewScene(currenPreviewSceneName);
            }
            for (int i = 0; i < obsScenes.scenes.size(); i++) {
                if (obsScenes.scenes.get(i).name.equals(obsScenes.getCurrentPreviewScene())) {
                    currentPreviewScene = obsScenes.scenes.get(i);
                }
            }
            //invalidateControls();
            invalidateAdapters();
            //if (fillFullStatus) {
            //    currentUpdateStatus |= PREVIEW_SCENE;
            //    checkInvalidate();
            //}
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
        JSONObject jsonPayload;
        jsonPayload = new JSONObject();
        try {
            jsonPayload.put("sceneName", newScene);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        JSONObject jso = CreateBasicRequest(
                OBS_REQ_SET_CURRENT_PREVIEW_SCENE,
                UUID.randomUUID().toString(),
                jsonPayload);
        send(jso.toString());
    }


    /**
     * Ein Request wird an OBS geschickt, dass die aktuelle Preview-Szene mit dem aktuellen Übergang
     * mit der aktuellen aktiven Szene getauscht werden soll.
     */
    public void doTransitionToProgram() {
        JSONObject jso;
        triggerStudioModeTransitionUUID = UUID.randomUUID();
        jso = CreateBasicRequest(
                OBS_REQ_TRIGGER_STUDIO_MODE_TRANSITION,
                triggerStudioModeTransitionUUID.toString()
        );
        send(jso.toString());
    }

    /**
     * Um ein Scene Item umschalten zu können, muss erst ermittelt werden, wie sein aktueller Status
     * ist. Dann wird der komplementäre Status gesetzt.
     *
     * @param sceneItem
     */
    public void toggleSceneItemVisibility(int sceneItemId) {
        boolean isVisible = false;
        for (int i = 0; i < currentPreviewScene.sceneItems.size(); i++) {
            if (currentPreviewScene.sceneItems.get(i).sceneItemId == sceneItemId) {
                isVisible = currentPreviewScene.sceneItems.get(i).sceneItemEnabled;
            }
        }
        setSceneItemVisible_req(currentPreviewScene.name, sceneItemId, !isVisible);
    }


    /**
     * Ein Request wird an OBS geschickt, um ein Scene Item ab- oder anzuschalten
     *
     * @param scene      Die Szene, von der ein Item ab- oder angeschaltet werden soll
     * @param sceneItem  Das Scene Item, das umgeschaltet werden soll
     * @param setVisible true, wenn das Item sichtbar sein soll; false, wenn nicht
     */
    private void setSceneItemVisible_req(String scene, int sceneItemId, boolean setVisible) {
        JSONObject jso;
        setSceneItemRenderUUID = UUID.randomUUID();

        try {
            JSONObject payload = new JSONObject();
            payload.put("sceneName", scene);
            payload.put("sceneItemId", sceneItemId);
            payload.put("sceneItemEnabled", setVisible);

            jso = new JSONObject();
            jso = CreateBasicRequest(
                    OBS_REQ_SET_SCENE_ITEM_ENABLED,
                    setSceneItemRenderUUID.toString(),
                    payload);

            send(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSceneItemVisible_resp(UUID lastcall) {
        if (setSceneItemRenderUUID.equals(lastcall)) {
            setSceneItemRenderUUID = null;
            updateScenes();
        }
    }


    /**
     * Ein Request wird an OBS geschickt, um das Streamen ein- oder auszuschalten
     */
    public void toggleStreaming() {
        toggleStreamUUID = UUID.randomUUID();
        JSONObject jso;
        jso = CreateBasicRequest(
                OBS_REQ_TOGGLE_STREAM,
                toggleStreamUUID.toString());
        send(jso.toString());
    }



    /**
     * Ein Request wird an OBS geschickt, um die Aufnahme ein- oder auszuschalten
     */
    public void toggleRecording() {
        toggleRecordUUID = UUID.randomUUID();
        JSONObject jso;
        jso = new JSONObject();
        jso = CreateBasicRequest(
                OBS_REQ_TOGGLE_RECORDING,
                toggleRecordUUID.toString()
        );
        send(jso.toString());
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
//    public void getAudioSourcesList_req() {
//        JSONObject jso;
//        try {
//            jso = new JSONObject();
//            jso.put("request-type", "GetSourcesList");
//            jso.put("message-id", "GetSourcesList_SCT");
//            send(jso.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    int numberOfAudioSources;
    boolean numberOfAudioSourcesValid = false;
    int audioSourcesDone;

    public void getInputList_req() {
        getInputListUUID = UUID.randomUUID();
        JSONObject jso = new JSONObject();
        jso = CreateBasicRequest(
                OBS_REQ_GET_INPUT_LIST,
                getInputListUUID.toString()
        );
        send(jso.toString());
    }

//    private void getAudioSourcesList_resp(JSONObject jso, int stage) {
//        mainAct.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                switch (stage) {
//                    case 1:
//                        if (targetUpdateStatus == AUDIO_VOLUMES) { // if user changes volume, not all audio sources have to be reread...
//                            for (OBSAudioSource source :
//                                    obsAudioSources) {
//                                JSONObject jso2 = new JSONObject();
//                                try {
//                                    jso2.put("request-type", "GetVolume");
//                                    jso2.put("source", source.name);
//                                    jso2.put("useDecibel", "true");
//                                    jso2.put("message-id", "GetVolume_SCT");
//                                    send(jso2.toString());
//                                } catch (JSONException jsonException) {
//                                    jsonException.printStackTrace();
//                                }
//                            }
//                        } else {
//                            numberOfAudioSources = 0; // clear the number of audio sources
//                            numberOfAudioSourcesValid = false;
//                            audioSourcesDone = 0;
//                            JSONArray jsonArray;
//                            try {
//                                jsonArray = jso.getJSONArray("sources");
//                                obsAudioSources.clear();
//                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    JSONObject source = jsonArray.getJSONObject(i);
//                                    String typeId = source.getString("typeId");
//                                    for (ObsSourceType obsSourceType : obsSourceTypesList.obsSources) {
//                                        if (obsSourceType.typeId.equals(typeId) && obsSourceType.caps.hasAudio) {
//                                            OBSAudioSource obsAudioSource = new OBSAudioSource();
//                                            obsAudioSource.name = source.getString("name");
//                                            obsAudioSource.typeId = typeId;
//                                            obsAudioSource.type = source.getString("type");
//                                            obsAudioSources.add(obsAudioSource);
//                                            JSONObject jso2 = new JSONObject();
//                                            numberOfAudioSources++;
//                                            jso2.put("request-type", "GetVolume");
//                                            jso2.put("source", obsAudioSource.name);
//                                            jso2.put("useDecibel", "true");
//                                            jso2.put("message-id", "GetVolume_SCT");
//                                            send(jso2.toString());
//                                        }
//                                    }
//                                }
//                                numberOfAudioSourcesValid = true;
//
//                            } catch (JSONException jsonException) {
//                                jsonException.printStackTrace();
//                            }
//                        }
//                        break;
//                    case 2:
//                        for (OBSAudioSource obsAudioSource :
//                                obsAudioSources) {
//                            try {
//                                if (obsAudioSource.name.equals(jso.getString("name"))) {
//                                    mainAct.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//
//                                            try {
//                                                obsAudioSource.volume = (int) (jso.getDouble("volume") * 1000);
//                                                obsAudioSource.isMuted = jso.getBoolean("muted");
//                                                //invalidateAdapters();
//                                                audioSourcesDone++;
//                                                if (numberOfAudioSourcesValid && (audioSourcesDone >= numberOfAudioSources) && fillFullStatus) {
//                                                    if ((targetUpdateStatus & AUDIO_SOURCES) > 0) {
//                                                        currentUpdateStatus |= AUDIO_SOURCES;
//                                                    }
//                                                    if ((targetUpdateStatus & AUDIO_VOLUMES) > 0) {
//                                                        currentUpdateStatus |= AUDIO_VOLUMES;
//                                                    }
//                                                    audioSourcesDone = 0;
//                                                    checkInvalidate();
//                                                }
//
//
//                                                // check if audio source is available in currently active scene
////                                                boolean found = false;
////                                                for (ObsScene scene :
////                                                        obsScenes.scenes) {
////                                                    if (scene.name.equals(obsScenes.getCurrentScene())) {
////                                                        for (ObsSceneItem item :
////                                                                scene.sceneItems) {
////                                                            if (item.name.equals(obsAudioSource.name)) {
////                                                                found = true;
////                                                            }
////                                                        }
////                                                    }
////                                                }
//
////                                                if (!found) {
////                                                    for (int i = 0; i < obsAudioSources.size(); i++) {
////                                                        if (obsAudioSources.get(i).name.equals(obsAudioSource.name)) {
////                                                            obsAudioSources.remove(i);
////                                                        }
////                                                    }
////                                                }
//
//                                            } catch (JSONException jsonException) {
//                                                jsonException.printStackTrace();
//                                            }
//                                        }
//                                    });
//                                }
//                            } catch (JSONException jsonException) {
//                                jsonException.printStackTrace();
//                            }
//                        }
//                        break;
//                    // TODO enhance Audio structure to contain information about mute status and volume level
//                }
//            }
//        });
//    }

    /**
     * Die Audioquelle an Position @param position wird stummgeschaltet oder die Stummschaltung
     * wird aufgehoben.
     *
     * @param position Position der Audioquelle in der internen Datenstruktur
     * @param checked  true, wenn die Quelle stummgeschaltet werden soll; false, wenn die Stummschaltung aufgehoben werden soll.
     */
    public void setMute(int position, boolean checked) {
        JSONObject jso;
        JSONObject payload = new JSONObject();
        try {
            payload.put("inputName", obsAudioSources.get(position).name);
            payload.put("inputMuted", checked);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        jso = CreateBasicRequest(
                OBS_REQ_SET_INPUT_MUTE,
                UUID.randomUUID().toString(),
                payload);
        send(jso.toString());
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

    public void sendHotkey(String hotkeyName, int shift, int alt, int ctrl, int cmd) {
        JSONObject jso = new JSONObject();
        JSONObject modifiers = new JSONObject();
        try {
            jso.put("request-type", "TriggerHotkeyBySequence");
            jso.put("message-id", "hotkey_by_sequence_SCT");
            jso.put("keyId", hotkeyName);
            modifiers.put("shift", shift);
            modifiers.put("alt", alt);
            modifiers.put("control", ctrl);
            modifiers.put("command", cmd);
            jso.put("keyModifiers", modifiers);
            send(jso.toString());
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
        // TODO: Make sure that studio mode is on and switch it on if it is off
        JSONObject jso = null;
        try {
            jso = new JSONObject(message);

            // We now first check if this is both a valid message and it has the correct version number
            if (jso.has("op") && jso.has("d")) {
                Log.d(TAG, "Basically correct JSON format. Now checking version number.");
            } else {
                Log.e(TAG, "Invalid JSON format.");
                Toast.makeText(mainAct, "Ungültiges JSON-Format", Toast.LENGTH_SHORT).show();
                mainAct.finish();
            }

            JSONObject dataObj = jso.getJSONObject("d");
            int rpcVersion;
            if (dataObj.has("rpcVersion")) {
                rpcVersion = dataObj.getInt("rpcVersion");
                if (rpcVersion == 1) {
                    Log.d(TAG, "Correct RPC version number sent.");
                } else {
                    Log.e(TAG, "Wrong RPC version number " + rpcVersion + " sent. Aborting.");
                    Toast.makeText(mainAct, "Ungültige RPC-Version", Toast.LENGTH_SHORT).show();
                    mainAct.finish();
                }
            }

            // We now know that basic properties are correct and we are good to go.
            // Let's inspect the actual message by looking at its opcode

            switch (jso.getInt("op")) {
                case OBS_OP_HELLO:
                    checkAndDoAuth(dataObj);
                    break;
                case OBS_OP_IDENTIFIED:
                    processAuthResult(dataObj);
                    break;
                case OBS_OP_REQRESPONSE:
                    ProcessResponse(dataObj);
                    break;
                case OBS_OP_EVENT:
                    ProcessEvent(dataObj);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + jso.getInt("op"));
            }

//            final int targetFullUpdateStatus = SCENES
//                    + SCENE_ITEMS
//                    + ACTIVE_SCENE
//                    + PREVIEW_SCENE
//                    + ITEMS_ACTIVE
//                    + AUDIO_SOURCES
//                    + STREAMING
//                    + RECORDING
//                    + TRANS_CHANGED;

//            if (jso.has("message-id")) {
//                String msgId = jso.getString("message-id");
//                switch (msgId) {
//                    case "SetScene_SCT":
//                    case "transProgram_SCT":
//                        Log.d("TEST", jso.toString());
//                        TimeUnit.MILLISECONDS.sleep(200);
//                        //updateScenes();
//                        break;
//                    //case "GetPreviewScene_SCT":
//                    //    Log.d("TEST", jso.toString());
//                    //    getPreviewScene_resp(jso, lastCall);
//                    //    // getStreamingStatus_req();
//                    //    // invalidateAdapters();
//                    //    break;
//                    case "changeScreenItemRender_SCT":
//                    case "SetMute_SCT":
//                        Log.d("TEST", jso.toString());
//                        //updateScenes();
//                        break;
//                    case "getStreamingStatus_SCT":
//                        Log.d("TEST", jso.toString());
//                        getStreamingStatus_resp(jso);
//                        // invalidateAdapters();
//                        break;
//                    case "toggleStreaming_SCT":
//                    case "toggleRecording_SCT":
//                        Log.d("TEST", jso.toString());
//                        this.targetUpdateStatus = RECORDING + STREAMING;
//                        updateScenes();
//                        break;
//                    case "translitionsList_SCT":
//                        Log.d("TEST", jso.toString());
//                        //getTransitionsList_resp(jso);
//                        // invalidateAdapters();
//                        break;
//                    case "transProgramCustom_SCT":
//                        Log.d("TEST", jso.toString());
//                        // Während des Übergangs sollen keine weiteren Updates am UI gemacht werden
//                        // Die Semaphore bPendingTransition sorgt dafür.
//                        // Außerdem wird damit erkannt, dass der angeforderte Übergang abgeschlossen
//                        // ist, wenn eine entsprechende Statusmeldung von OBS verschickt wird
//                        bPendingTransition = true;
//                        // Is not thread-safe: There could be another transaction kicking in while this transaction is still running...
//                        TimeUnit.MILLISECONDS.sleep(200);
//                        break;
//                    case "NewCurrentTransition_SCT":
//                        Log.d("TEST", jso.toString());
//                        //getTransitionsList_req();
//                        break;
//                    case "GetSourcesList_SCT":
//                        Log.d("TEST", jso.toString());
//                        getAudioSourcesList_resp(jso, 1);
//                        break;
//                    case "GetVolume_SCT":
//                        Log.d("TEST", jso.toString());
//                        getAudioSourcesList_resp(jso, 2);
//                        break;
//                    case "GetSourceTypesList_SCT":
//                        Log.d("TEST", jso.toString());
//                        obsSourceTypesList = new ObsSourceTypesList(jso);
////                        getAudioSourcesList();
//                        break;
//                    case "GetSourceActive_SCT":
//                        Log.d("TEST", jso.toString());
//                        getAudioSourcesList_resp(jso, 3);
//                        break;
//                    case "SetVolume_SCT":
//                        Log.d("TEST", jso.toString());
//                        break;
//                    case "hotkey_by_sequence_SCT":
//                        Log.d("TEST", jso.toString());
//                        Log.d("TEST", "Hotkey sent!");
//                        break;
//                    case "TakeSourceScreenshot_SCT":
//                        Log.d("TEST", jso.toString());
//                        updatePreview_resp(jso);
//                        break;
//
//                    default:
//                        ToastInMainAct("Unbekannte Antwort vom WebService!");
//                }
//            } else if (jso.has("update-type")) {
//                //ToastInMainAct("Update from OBS");
//                Log.e("TEST", jso.toString());
//                switch (jso.getString("update-type")) {
//                    case "TransitionEnd":
//                        if (bPendingTransition) {
//                            backToOriginalDefaultTransition();
//                            TimeUnit.MILLISECONDS.sleep(200);
//                            this.targetUpdateStatus = targetFullUpdateStatus;
//                            //updateScenes();
//                        }
//                        break;
//                    case "SwitchTransition":
//                        if (!bPendingTransition) {
//                            targetUpdateStatus = targetFullUpdateStatus;
//                            updateScenes(); // war vorher: getTransitionsList_req();
//                        }
//                        break;
//                    case "SourceVolumeChanged":
//                        if (!bPendingTransition && !userChangesVolume) {
//                            targetUpdateStatus = AUDIO_VOLUMES;
//                            updateVolumes();
//                        }
//                        break;
//                    default:
//                        if (!bPendingTransition) {
//                            if (userChangesVolume) {
//                                targetUpdateStatus = AUDIO_VOLUMES;
//                                updateVolumes();
//                            } else {
//                                targetUpdateStatus = targetFullUpdateStatus;
//                                updateScenes();
//                            }
//
//                        }
//                        //getAudioSourcesList();
//                        // TODO: GetSourceActive mit einbauen (Audioquelle darf nur angezeigt werden, wenn true
//                }
//            }

        } catch (JSONException e) {
            //e.printStackTrace();
            if (jso != null) Log.e("TEST", jso.toString());
        }

    }

    private void ProcessEvent(JSONObject dataObj) {
        Log.i(TAG, "Event catched. Probably need to handle... (Detail: " + dataObj.toString() + ")");
        JSONObject eventData = null;
        try {
            eventData = dataObj.getJSONObject("eventData");
            int eventIntent = dataObj.getInt("eventIntent");
            String eventType = dataObj.getString("eventType");
            switch (eventType) {
                case OBS_EVT_INPUT_VOLUME_CHANGED:
                    handleInputVolumeChanged(eventData);
                    break;
                case OBS_EVT_INPUT_MUTE_STATE_CHANGED:
                    handleMuteStateChanged(eventData);
                    break;
                case OBS_EVT_CURRENT_PREVIEW_SCENE_CHANGED:
                case OBS_EVT_CURRENT_PROGRAM_SCENE_CHANGED:
                    updateScenes();
                    break;
                default:
                    Log.i(TAG, "Unhandled event: " + eventData.toString());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        

        // TODO: Take care of events


    }

    private void handleMuteStateChanged(JSONObject eventData) {
        String inputName = null;
        boolean inputMuted = false;
        try {
            inputName = eventData.getString("inputName");
            inputMuted = eventData.getBoolean("inputMuted");
            for (OBSAudioSource src : obsAudioSources) {
                if (src.name.equals(inputName)) {
                    src.isMuted = inputMuted;
                    invalidateControls();
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleInputVolumeChanged(JSONObject eventData) {
        String inputName;
        double db;
        try {
            inputName = eventData.getString("inputName");
            db = eventData.getDouble("inputVolumeDb");
            for (OBSAudioSource src : obsAudioSources) {
                if (src.name.equals(inputName)) {
                    src.volume = (int)(Math.exp(db/20) * 1000);
                    invalidateControls();
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // Log.e(TAG, "** AUDIO: " + db + ", " + Math.exp(db/20));
    }

    private void ProcessResponse(JSONObject dataObj) {
            JSONObject responseData = null;
        JSONObject requestStatus;
            try {
                UUID lastcall;
                switch (dataObj.getString("requestType")) {
                    case OBS_REQ_GET_SCENE_LIST:
                        responseData = dataObj.getJSONObject("responseData");
                        getScenesList_resp2(responseData);
                        break;
                    case OBS_REQ_GET_SCENE_ITEMS_LIST:
                        responseData = dataObj.getJSONObject("responseData");
                        String referencedScene = ScenesToItems.get(UUID.fromString(dataObj.getString("requestId")));
                        getSceneItemsList_resp(responseData, referencedScene);
                        break;
                    case OBS_REQ_SET_CURRENT_PREVIEW_SCENE:
                        updateScenes();
                        break;
                    case OBS_REQ_GET_CURRENT_PREVIEW_SCENE:
                        lastcall = UUID.fromString(dataObj.getString("requestId"));
                        responseData = dataObj.getJSONObject("responseData");
                        getPreviewScene_resp(responseData, lastcall);
                        break;
                    case OBS_REQ_SET_SCENE_ITEM_ENABLED:
                        lastcall = UUID.fromString(dataObj.getString("requestId"));
                        //responseData = dataObj.getJSONObject("responseData");
                        setSceneItemVisible_resp(lastcall);
                        break;
                    case OBS_REQ_TOGGLE_RECORDING:
                        lastcall = UUID.fromString(dataObj.getString("requestId"));
                        responseData = dataObj.getJSONObject("responseData");
                        toggleRecording_resp(responseData, lastcall);
                        break;
                    case OBS_REQ_TOGGLE_STREAM:
                        lastcall = UUID.fromString(dataObj.getString("requestId"));
                        responseData = dataObj.getJSONObject("responseData");
                        toggleStream_resp(responseData, lastcall);
                        break;
                    case OBS_REQ_TRIGGER_STUDIO_MODE_TRANSITION:
                        //checkInvalidate();
                        updateScenes();
                        //invalidateAdapters();
                        //invalidateControls();
                        break;
                    case OBS_REQ_GET_INPUT_LIST:
                        lastcall = UUID.fromString(dataObj.getString("requestId"));
                        responseData = dataObj.getJSONObject("responseData");
                        getInputList_resp(responseData, lastcall);
                        break;
                    case OBS_REQ_GET_INPUT_MUTE:
                        requestStatus = dataObj.getJSONObject("requestStatus");
                        lastcall = UUID.fromString(dataObj.getString("requestId"));
                        if (requestStatus.getInt("code") == 604)
                        {
                            Log.i(TAG, dataObj.toString());
                            Log.i(TAG, "The source does not support Audio");
                            removeInputNoAudio(lastcall);
                        } else {

                            responseData = dataObj.getJSONObject("responseData");
                            getInputMute_resp(responseData, lastcall);
                        }
                        break;
                    case OBS_REQ_GET_INPUT_VOLUME:
                        requestStatus = dataObj.getJSONObject("requestStatus");
                        lastcall = UUID.fromString(dataObj.getString("requestId"));
                        if (requestStatus.getInt("code") == 604)
                        {
                            Log.i(TAG, dataObj.toString());
                            Log.i(TAG, "The source does not support Audio");
                            removeInputNoAudio(lastcall);
                        } else {

                            responseData = dataObj.getJSONObject("responseData");
                            getInputVolume_resp(responseData, lastcall);
                        }
                        break;
                    case OBS_REQ_SET_INPUT_VOLUME:
                        // already handled by event
                        break;
                    case OBS_REQ_GET_VERSION:
                        responseData = dataObj.getJSONObject("responseData");
                        lastcall = UUID.fromString(dataObj.getString("requestId"));
                        updatePreview_resp(responseData, lastcall);
                        break;
                    case OBS_REQ_GET_SOURCE_SCREENSHOT:
                        responseData = dataObj.getJSONObject("responseData");
                        lastcall = UUID.fromString(dataObj.getString("requestId"));
                        updatePreview_resp2(responseData, lastcall);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

    private void updatePreview_resp2(JSONObject responseData, UUID lastcall) {
        //previewImage
        //Log.d(TAG,responseData.toString());
        String base64Img = null;
        try {
            base64Img = responseData.getString("imageData");
            String base64ImgRaw = base64Img.substring(base64Img.indexOf(","));
            byte[] decodedString = android.util.Base64.decode(base64ImgRaw, android.util.Base64.DEFAULT);
            final Bitmap newImage = BitmapFactory.decodeByteArray(decodedString,0, decodedString.length);
            mainAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    previewImage.setImageBitmap(newImage);
                }
            });

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }

    private void updatePreview_resp(JSONObject responseData, UUID lastcall) {
        if (lastcall.equals(GetVersionForScreenShotUUID)) {
            GetVersionForScreenShotUUID = null;
            // preview Screen mode
            try {
                JSONArray imageFormats = responseData.getJSONArray("supportedImageFormats");
                boolean jpegFound=false;
                for (int i = 0; i < imageFormats.length(); i++) {
                    if (imageFormats.getString(i).equals("jpeg")) {
                        jpegFound = true;
                        break;
                    }
                }
                String IMG_FMT_JPEG = "jpeg";
                if (jpegFound) {
                    JSONObject jso = new JSONObject();
                    JSONObject payload = new JSONObject();
                    payload.put("sourceName", obsScenes.getCurrentScene());
                    payload.put("imageFormat", IMG_FMT_JPEG);
                    jso = CreateBasicRequest(
                            OBS_REQ_GET_SOURCE_SCREENSHOT,
                            UUID.randomUUID().toString(),
                            payload);
                    send(jso.toString());
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            Log.d(TAG, responseData.toString());
        }
    }

    private void removeInputNoAudio(UUID lastcall) {
        String inputSource = ScenesToItems.get(lastcall);
        if (inputSource != null) {
            ScenesToItems.remove(lastcall);
            int position = -1;
            for (int i = 0; i < obsAudioSources.size(); i++) {
                if (obsAudioSources.get(i).name.equals(inputSource)) {
                    position = i;
                }
            }
            if (position != -1) {
                obsAudioSources.remove(position);
                //invalidateControls();
            }
        }
    }

    private void getInputVolume_resp(JSONObject responseData, UUID lastcall) {
        String audioSourceName = ScenesToItems.get(lastcall);
        for (OBSAudioSource src : obsAudioSources) {
            if (src.name.equals(audioSourceName)) {
                ScenesToItems.remove(lastcall);
                Log.d(TAG, responseData.toString());
                try {
                    double inputVolumeDb = responseData.getDouble("inputVolumeDb");
                    src.volume = (int) (Math.exp(inputVolumeDb / 20) * 1000.0);
                    src.volumeDb = inputVolumeDb;
                    //invalidateControls();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void getInputMute_resp(JSONObject responseData, UUID lastcall) {
        String audioSourceName = ScenesToItems.get(lastcall);
        for (OBSAudioSource src : obsAudioSources) {
            if (src.name.equals(audioSourceName)) {
                ScenesToItems.remove(lastcall);
                Log.d(TAG, responseData.toString());
                try {
                    src.isMuted = responseData.getBoolean("inputMuted");
                    //invalidateControls();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
ArrayList<OBSAudioSource> tempObsAudioSources;
    public boolean fillingAudio = false;
    private void getInputList_resp(JSONObject responseData, UUID lastcall) {
        if (lastcall.equals(getInputListUUID)) {
            Log.d(TAG, responseData.toString());
                    obsAudioSources.clear();
            try {
                JSONArray obsInputs = responseData.getJSONArray("inputs");
                for (int i = 0; i < obsInputs.length(); i++) {
                    OBSAudioSource audioSource = new OBSAudioSource();
                    JSONObject jso = obsInputs.getJSONObject(i);
                    audioSource.name = jso.getString("inputName");
                    audioSource.type = jso.getString("inputKind");
                    if (audioSource.type.equals(OBS_INPUT_DSHOW_INPUT) ||
                            audioSource.type.equals(OBS_INPUT_WASAPI_INPUT_CAPTURE) ||
                            audioSource.type.equals(OBS_INPUT_WASAPI_OUTPUT_CAPTURE) ||
                            audioSource.type.equals(OBS_INPUT_BROWSER_SOURCE)) {
                        obsAudioSources.add(audioSource);
                        UUID audioUUID = UUID.randomUUID();
                        //audioSource.requestId = audioUUID;
                        ScenesToItems.put(audioUUID, audioSource.name);
                        JSONObject payload = new JSONObject();
                        payload.put("inputName", audioSource.name);
                        JSONObject listRequest = new JSONObject();
                        listRequest = CreateBasicRequest(
                                OBS_REQ_GET_INPUT_MUTE,
                                audioUUID.toString(),
                                payload
                        );
                        send(listRequest.toString());
                        audioUUID = UUID.randomUUID();
                        ScenesToItems.put(audioUUID, audioSource.name);
                        //reuse old payload from above
                        listRequest = CreateBasicRequest(
                                OBS_REQ_GET_INPUT_VOLUME,
                                audioUUID.toString(),
                                payload
                        );
                        send(listRequest.toString());
                    }
                }
                    // check inputKind and do not add if no audio support // TODO
                //mainAct.transitionsVolumesFragment.obsAudioSourcesSlidersAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doTransitionToProgram_resp(JSONObject responseData, UUID lastcall) {
        Log.i(TAG, responseData.toString());
    }

    private void toggleStream_resp(JSONObject responseData, UUID lastcall) {
        if (lastcall.equals(toggleStreamUUID)) {
            toggleStreamUUID = null;
            try {
                isStreaming = responseData.getBoolean("outputActive");
                invalidateAdapters();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void toggleRecording_resp(JSONObject responseData, UUID lastcall) {
        if (lastcall.equals(toggleRecordUUID)) {
            toggleRecordUUID = null;
            try {
                isRecording = responseData.getBoolean("outputActive");
                invalidateAdapters();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*private void processResponse(JSONObject dataObj) {
    }*/

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

    UUID setInputVolumeUUID=null;

    public void setVolume(int adapterPosition, int volume) {
        JSONObject jso = new JSONObject();
        setInputVolumeUUID = UUID.randomUUID();
        double inputVolumeDb;
        if (volume == 0) {
            inputVolumeDb = -100;
        } else {
            inputVolumeDb = 20 * Math.log(volume / 1000.0);
        }

        JSONObject payload = new JSONObject();
        try {
            payload.put("inputName", obsAudioSources.get(adapterPosition).name);
            payload.put("inputVolumeDb", inputVolumeDb);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        jso = CreateBasicRequest(
                OBS_REQ_SET_INPUT_VOLUME,
                setInputVolumeUUID.toString(),
                payload
        );
        send(jso.toString());
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

    ImageView previewImage;

    UUID GetVersionForScreenShotUUID=null;
    public void updatePreview(ImageView previewImage) {
        this.previewImage = previewImage;
        JSONObject jso = new JSONObject();
        GetVersionForScreenShotUUID = UUID.randomUUID();
        jso = CreateBasicRequest(OBS_REQ_GET_VERSION, GetVersionForScreenShotUUID.toString());
        send(jso.toString());
    }
//        String currentScene = obsScenes.getCurrentScene();
//        JSONObject payload = new JSONObject();

//        payload.put("sourceName", currentScene);
//        try {
//            Log.e("TEST", "Take Screenshot");
//            jso.put("request-type", "TakeSourceScreenshot");
//            jso.put("embedPictureFormat", "png");
//            jso.put("compressionQuality", "-1");
//            jso.put("message-id", "TakeSourceScreenshot_SCT");
//            send(jso.toString());
//        } catch (JSONException jsonException) {
//            jsonException.printStackTrace();
//        }

//    }

    private void updatePreview_resp(JSONObject jso) {
        try {
            String imgData = jso.getString("img");
            imgData = imgData.replace("data:image/png;base64,", "");
            byte[] imageAsBytes = Base64.getDecoder().decode(imgData.getBytes());
            mainAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    previewImage.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                }
            });
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
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
