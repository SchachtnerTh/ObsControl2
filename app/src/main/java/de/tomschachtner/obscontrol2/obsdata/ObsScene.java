package de.tomschachtner.obscontrol2.obsdata;

import java.util.ArrayList;

public class ObsScene {
    public String name;
    public ArrayList<ObsSource> sources;

    public ObsScene() {
        sources = new ArrayList<>();
    }
}
