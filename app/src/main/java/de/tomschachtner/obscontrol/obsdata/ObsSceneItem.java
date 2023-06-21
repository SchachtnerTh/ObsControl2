package de.tomschachtner.obscontrol.obsdata;

import org.json.JSONException;
import org.json.JSONObject;

public class ObsSceneItem {
    /**
     * Constructs an ObsSceneItem class instance with all available properties
     * @param alignment The point on the source that the item is manipulated from.
     *                  The sum of 1=Left or 2=Right, and 4=Top or 8=Bottom,
     *                  or omit to center on that axis.
     * @param cx (unknown)
     * @param cy (unknown)
     * @param id Scene item ID
     * @param locked Whether or not this Scene Item is locked and can't be moved around
     * @param muted Whether or not this Scene Item is muted.
     * @param name The name of this Scene Item.
     * @param render Whether or not this Scene Item is set to "visible".
     * @param source_cx (unknown)
     * @param source_cy (unknown)
     * @param type Source type. Value is one of the following:
     *             "input", "filter", "transition", "scene" or "unknown"
     * @param volume (unknown)
     * @param x (unknown)
     * @param y (unknown)
     */
    public ObsSceneItem(
            String inputKind,
            boolean isGroup,
            String sceneItemBlendMode,
            boolean sceneItemEnabled,
            int sceneItemId,
            boolean sceneItemLocked,
            String sourceName,
            String sourceType) {
        this.inputKind=inputKind;
        this.isGroup=isGroup;
        this.sceneItemBlendMode=sceneItemBlendMode;
        this.sceneItemEnabled=sceneItemEnabled;
        this.sceneItemId=sceneItemId;
        this.sceneItemLocked=sceneItemLocked;
        this.sourceName=sourceName;
        this.sourceType=sourceType;
    }

    public ObsSceneItem(JSONObject jsonObject) {
        try {
            this.inputKind=jsonObject.getString("inputKind");
            //this.isGroup=jsonObject.getBoolean("isGroup");

            this.sceneItemBlendMode=jsonObject.getString("sceneItemBlendMode");
            this.sceneItemEnabled=jsonObject.getBoolean("sceneItemEnabled");
            this.sceneItemId=jsonObject.getInt("sceneItemId");
            this.sceneItemIndex=jsonObject.getInt("sceneItemIndex");
            this.sceneItemLocked=jsonObject.getBoolean("sceneItemLocked");
            this.sourceType=jsonObject.getString("sourceType");
            this.sourceName=jsonObject.getString("sourceName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    String inputKind;
    boolean isGroup;
    String sceneItemBlendMode;
    public boolean sceneItemEnabled;
    public int sceneItemId;
    int sceneItemIndex;
    boolean sceneItemLocked;
    public String sourceName;
    String sourceType;
}
