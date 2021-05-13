package de.tomschachtner.obscontrol2.obsdata;

import org.json.JSONException;
import org.json.JSONObject;

public class ObsSource {
    /**
     * Constructs an ObsSource class instance with all available properties
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
    public ObsSource (
            int alignment, 
            int cx, 
            int cy, 
            int id, 
            boolean locked, 
            boolean muted, 
            String name, 
            boolean render,
            int source_cx,
            int source_cy,
            String type,
            int volume,
            int x,
            int y) {
        this.alignment=alignment;
        this.cx=cx;
        this.cy=cy;
        this.id=id;
        this.locked=locked;
        this.muted=muted;
        this.name=name;
        this.render=render;
        this.source_cx=source_cx;
        this.source_cy=source_cy;
        this.type=type;
        this.volume=volume;
        this.x=x;
        this.y=y;
    }

    public ObsSource(JSONObject jsonObject) {
        try {
            this.alignment=jsonObject.getInt("alignment");
            this.cx=jsonObject.getInt("cx");
            this.cy=jsonObject.getInt("cy");
            this.id=jsonObject.getInt("id");
            this.locked=jsonObject.getBoolean("locked");
            this.muted=jsonObject.getBoolean("muted");
            this.name=jsonObject.getString("name");
            this.render=jsonObject.getBoolean("render");
            this.source_cx=jsonObject.getInt("source_cx");
            this.source_cy=jsonObject.getInt("source_cy");
            this.type=jsonObject.getString("type");
            this.volume=jsonObject.getInt("volume");
            this.x=jsonObject.getInt("x");
            this.y=jsonObject.getInt("y");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    
    int alignment;
    int cx;
    int cy;
    int id;
    boolean locked;
    boolean muted;
    String name;
    boolean render;
    int source_cx;
    int source_cy;
    String type;
    int volume;
    int x;
    int y;
}
