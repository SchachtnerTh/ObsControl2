package de.tomschachtner.obscontrol;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransitionsVolumesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransitionsVolumesFragment
       extends Fragment
        implements OBSTransitionsButtonsAdapter.OnTransitionsClickListener, OBSTransitionsButtonsAdapter.OnTransitionsLongClickListener, OBSAudioSourcesSlidersAdapter.OnMuteButtonChangedListener, OBSAudioSourcesSlidersAdapter.OnVolumeChangedListener {

    public TransitionsVolumesFragment() {
        // Required empty public constructor
    }

    MainActivity theActivity;
    OBSWebSocketClient client;

    RecyclerView obsTransitionsButtons;
    OBSTransitionsButtonsAdapter transitionsButtonsAdapter;

    RecyclerView volumesSliderView;
    OBSAudioSourcesSlidersAdapter obsAudioSourcesSlidersAdapter;

//    RecyclerView obsAudioSourcesSliders;
//    OBSAudioSourcesSlidersAdapter obsAudioSourcesSlidersAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        theActivity = (MainActivity)getActivity();
        client = theActivity.mOBSWebSocketClient;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.fragment_transitions_volumes, container, false);

        obsTransitionsButtons = cl.findViewById(R.id.transitions_view);
        volumesSliderView = cl.findViewById(R.id.volumeSliderView);

        client.getTransitionsList_req();
        client.getAudioSourcesList_req();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String strColumns = sp.getString("columns", "4");

        int numberOfTransitionsColumns = Integer.parseInt(strColumns);

        obsTransitionsButtons.setLayoutManager(new GridLayoutManager(getContext(), numberOfTransitionsColumns));
        transitionsButtonsAdapter = new OBSTransitionsButtonsAdapter(getContext(), client.transitionsList);
        transitionsButtonsAdapter.setTransitionsClickListener(this);
        transitionsButtonsAdapter.setTransitionsLongClickListener(this);
        theActivity.mOBSWebSocketClient.setOnObsTransitionsChangedListener(transitionsButtonsAdapter);
        obsTransitionsButtons.setAdapter(transitionsButtonsAdapter);

        volumesSliderView.setLayoutManager(new LinearLayoutManager(getContext()));
        obsAudioSourcesSlidersAdapter = new OBSAudioSourcesSlidersAdapter(getContext(), client.obsAudioSources);
        obsAudioSourcesSlidersAdapter.setMuteButtonChangedListener(this);
        obsAudioSourcesSlidersAdapter.setVolumeChangedListener(this);
        theActivity.mOBSWebSocketClient.setOnObsAudioChangedListener(obsAudioSourcesSlidersAdapter);
        volumesSliderView.setAdapter(obsAudioSourcesSlidersAdapter);

        return cl;
    }

    @Override
    public void onTransitionClick(View view, int position) {
        Log.i("TEST", "You clicked number " + transitionsButtonsAdapter.getItem(position) + ", which is at cell position " + position);
        client.makeCustomTransition(transitionsButtonsAdapter.getItem(position));
        // TODO: perform transition on this handler
        // TODO: perform change of default transition on long press

    }

    @Override
    public void onTransitionLongClick(View view, int position) {
        Log.i("TEST", "You clicked number " + transitionsButtonsAdapter.getItem(position) + ", which is at cell position " + position);
        client.setCurrentTransition(transitionsButtonsAdapter.getItem(position));
    }


    @Override
    public void onVolumeChanged(View view, int volume, boolean fromUser, int adapterPosition) {
        if (adapterPosition == -1) {
            String name = ((SeekBar)view).getTag().toString();
            for (int i = 0; i < client.obsAudioSources.size(); i++) {
                if (client.obsAudioSources.get(i).name.equals(name)) adapterPosition = i;
            }
        }
        if (fromUser) {
            client.setVolume(adapterPosition, volume);
        }
    }

    @Override
    public void onVolumeStartTracking(View view, int position) {
        Log.d("TEST", "In onVolumeStartTracking");
        client.startUserVolumeChange();
    }

    @Override
    public void onVolumeStopTracking(View view, int position) {
        Log.d("TEST", "In onVolumeStopTracking");
        client.stopUserVolumeChange();
    }

    @Override
    public void onMuteButtonChanged(View view, int position) {
        Log.i("TEST", "You pressed the MUTE button. Current slider number: " + position);
        client.setMute(position, ((CompoundButton)view).isChecked());
    }
}