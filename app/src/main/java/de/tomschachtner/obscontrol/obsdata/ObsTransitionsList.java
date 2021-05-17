package de.tomschachtner.obscontrol.obsdata;

import java.util.ArrayList;

public class ObsTransitionsList {
    String currentTransition;

    public String getCurrentTransition() {
            return currentTransition;
        }

    public void setCurrentTransition(String currentTransition) {
        this.currentTransition = currentTransition;
    }
    public ArrayList<String> transitions;

    public ObsTransitionsList() {
        transitions = new ArrayList<>();
    }
}
