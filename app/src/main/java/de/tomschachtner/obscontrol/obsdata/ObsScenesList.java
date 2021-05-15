package de.tomschachtner.obscontrol.obsdata;

import java.util.ArrayList;

public class ObsScenesList {
    String currentScene;

    public String getCurrentPreviewScene() {
        return currentPreviewScene;
    }

    public void setCurrentPreviewScene(String currentPreviewScene) {
        this.currentPreviewScene = currentPreviewScene;
    }

    String currentPreviewScene;
    public ArrayList<ObsScene> scenes;

    public ObsScenesList() {
        scenes = new ArrayList<>();
    }
    public void setCurrentScene(String currentScene) {
        this.currentScene = currentScene;
    }

    public String getCurrentScene() {
        return currentScene;
    }
}
