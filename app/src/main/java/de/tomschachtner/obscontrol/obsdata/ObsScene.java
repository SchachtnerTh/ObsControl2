package de.tomschachtner.obscontrol.obsdata;

import java.util.ArrayList;

public class ObsScene {
    public String name;
    public ArrayList<ObsSource> sources;

    public ObsScene() {
        sources = new ArrayList<>();
    }
}
