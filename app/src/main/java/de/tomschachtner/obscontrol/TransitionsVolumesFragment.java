package de.tomschachtner.obscontrol;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransitionsVolumesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransitionsVolumesFragment
       extends Fragment
        implements OBSTransitionsButtonsAdapter.OnTransitionsClickListener, OBSTransitionsButtonsAdapter.OnTransitionsLongClickListener {

    public TransitionsVolumesFragment() {
        // Required empty public constructor
    }

    MainActivity theActivity;
    OBSWebSocketClient client;

    RecyclerView obsTransitionsButtons;
    OBSTransitionsButtonsAdapter transitionsButtonsAdapter;

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

        client.getTransitionsList();

        int numberOfScenesColumns = 4;
        obsTransitionsButtons.setLayoutManager(new GridLayoutManager(getContext(), numberOfScenesColumns));
        transitionsButtonsAdapter = new OBSTransitionsButtonsAdapter(getContext(), client.transitionsList);
        transitionsButtonsAdapter.setTransitionsClickListener(this);
        transitionsButtonsAdapter.setTransitionsLongClickListener(this);
        theActivity.mOBSWebSocketClient.setOnObsTransitionsChangedListener(transitionsButtonsAdapter);
        obsTransitionsButtons.setAdapter(transitionsButtonsAdapter);


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
}