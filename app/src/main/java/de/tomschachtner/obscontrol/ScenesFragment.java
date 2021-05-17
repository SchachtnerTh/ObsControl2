package de.tomschachtner.obscontrol;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.tabs.TabItem;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScenesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScenesFragment
        extends Fragment
        implements OBSSceneButtonsAdapter.OnSceneClickListener, OBSSourceButtonsAdapter.OnSourceClickListener {

    TextView connectStatusIndicator;
    public TextView currentSceneName;
    public RecyclerView obsScenesButtons;
    public RecyclerView obsSourcesButtons;
    public Button transition;
    public ImageButton streamButton;
    public ImageButton recordButton;
    public TabItem tabSzenen;
    public TabItem tabTransVol;
    public TabItem tabHotkeys;
    OBSSceneButtonsAdapter sceneButtonsAdapter;
    OBSSourceButtonsAdapter sourceButtonsAdapter;

    MainActivity theActivity;


    // TODO: Rename and change types of parameters
    private String host;
    private String port;

    public ScenesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        theActivity = (MainActivity)getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.fragment_scenes, container, false);
        // link variables to the UI elements
        obsScenesButtons = cl.findViewById(R.id.scenes_button_list);
        transition = cl.findViewById(R.id.transition_to_program);
        connectStatusIndicator = cl.findViewById(R.id.connect_status);
        currentSceneName = cl.findViewById(R.id.aktive_szene_display);
        obsSourcesButtons = cl.findViewById(R.id.sources_button_list);
        streamButton = cl.findViewById(R.id.stream_button);
        recordButton = cl.findViewById(R.id.record_button);
        tabHotkeys = cl.findViewById(R.id.tab_hotkeys);
        tabSzenen = cl.findViewById(R.id.tab_szenen);
        tabTransVol = cl.findViewById(R.id.tab_transvol);



        transition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                theActivity.mOBSWebSocketClient.doTransitionToProgram();
            }
        });

        streamButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                theActivity.mOBSWebSocketClient.toggleStreaming();
                return true;
            }
        });

        recordButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                theActivity.mOBSWebSocketClient.toggleRecording();
                return true;
            }
        });

        int numberOfScenesColumns = 4;
        obsScenesButtons.setLayoutManager(new GridLayoutManager(getContext(), numberOfScenesColumns));
        sceneButtonsAdapter = new OBSSceneButtonsAdapter(getContext(), theActivity.mOBSWebSocketClient.obsScenes);
        sceneButtonsAdapter.setSceneClickListener(this);
        theActivity.mOBSWebSocketClient.setOnObsScenesChangedListener(sceneButtonsAdapter);
        obsScenesButtons.setAdapter(sceneButtonsAdapter);

        int numberOfSourcesColumns = 4;
        obsSourcesButtons.setLayoutManager(new GridLayoutManager(getContext(), numberOfSourcesColumns));
        sourceButtonsAdapter = new OBSSourceButtonsAdapter(getContext(), theActivity.mOBSWebSocketClient.currentPreviewScene);
        sourceButtonsAdapter.setSourceClickListener(this);
        theActivity.mOBSWebSocketClient.setOnObsSourcesChangedListener(sourceButtonsAdapter);
        obsSourcesButtons.setAdapter(sourceButtonsAdapter);
        updateViewContents();
        return cl;
    }

    private void updateViewContents() {
        if (theActivity.mOBSWebSocketClient != null) {
            setConnectStatusIndicator(theActivity.mOBSWebSocketClient.connStatus);
            currentSceneName.setText(theActivity.mOBSWebSocketClient.obsScenes.getCurrentScene());
        } else {
            setConnectStatusIndicator(MainActivity.status.CLOSED);
            currentSceneName.setText("-- keine --");
        }
    }

    @Override
    public void onSceneClick(View view, int position) {
        Log.i("TEST", "You clicked number " + sceneButtonsAdapter.getItem(position) + ", which is at cell position " + position);
        theActivity.mOBSWebSocketClient.switchActiveScene(sceneButtonsAdapter.getItem(position));
    }

    @Override
    public void onSourceClick(View view, int position) {
        Log.i("TEST", "You clicked number " + sourceButtonsAdapter.getItem(position) + ", which is at cell position " + position);
        theActivity.mOBSWebSocketClient.toggleSourceVisibility(sourceButtonsAdapter.getItem(position));
    }

    public void setConnectStatusIndicator(MainActivity.status statusIndicator) {
        if (connectStatusIndicator != null) {
            switch (statusIndicator) {
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
    }

    /**
     * called after a new scenes list was received from OBS
     */
    public void newScenesAvailable() {
        int numberOfColumns = 4;
        obsScenesButtons.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        // TODO: Is this following block necessary? When can we get into it?
        if (sceneButtonsAdapter == null) {
            sceneButtonsAdapter = new OBSSceneButtonsAdapter(getContext(), theActivity.mOBSWebSocketClient.obsScenes);
            sceneButtonsAdapter.setSceneClickListener(this);
            theActivity.mOBSWebSocketClient.setOnObsScenesChangedListener(sceneButtonsAdapter);
            obsScenesButtons.setAdapter(sceneButtonsAdapter);
        }
    }

// reuse @link: https://stackoverflow.com/questions/51948208/how-to-reuse-a-fragment
}