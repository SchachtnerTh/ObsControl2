package de.tomschachtner.obscontrol.obsdata;

import java.util.UUID;

public class OBSAudioSource {
    public String name;
    public String type;
    public String typeId;
    public boolean isMuted;
    public int volume;
    public double volumeDb;
    public boolean needsUpdate;
    public UUID requestId;
}
