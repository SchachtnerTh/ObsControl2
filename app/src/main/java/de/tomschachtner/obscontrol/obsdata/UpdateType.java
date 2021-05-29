package de.tomschachtner.obscontrol.obsdata;

import java.util.EnumSet;

public enum UpdateType {
    SCENES        (0b00000001),
    SCENE_ITEMS   (0b00000010),
    AUDIO_SOURCES (0b00000100),
    TRANS_CHANGED (0b00001000),
    TRANS_DONE    (0b00010000),
    STREAMING     (0b00100000),
    RECORDING     (0b01000000);

    private int v;

    private UpdateType(int v) {
        this.v = v;
    }

    public static EnumSet<UpdateType> fromInt(int codeId) {
        EnumSet<UpdateType> codesList = EnumSet.noneOf(UpdateType.class);
        for (UpdateType code : values()) {
            if ((codeId & code.intValue()) != 0) {
                codesList.add(code);
            }
        }
        return codesList;
    }

    public static int toInt(EnumSet<UpdateType> codesList) throws IllegalAccessException {
        if (codesList == null || codesList.isEmpty()) {
            throw new IllegalAccessException("Null or empty EnumSet<UpdateType>");
        }
        int returnValue = 0;
        for (UpdateType code : codesList) {
            returnValue |= code.intValue();
        }
        return returnValue;
    }

    public int intValue() {
        return v;
    }

    public long longValue() {
        return v;
    }
}
