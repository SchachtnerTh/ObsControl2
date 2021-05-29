package de.tomschachtner.obscontrol.obsdata;

import java.util.ArrayList;

public class ObsScene {
    public String name;
    public ArrayList<ObsSceneItem> sceneItems;

    public ObsScene() {
        sceneItems = new ArrayList<>();
    }
}
