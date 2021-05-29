package de.tomschachtner.obscontrol.obsdata;

import org.json.JSONException;
import org.json.JSONObject;

public class ObsSourceType {
    public Capabilities caps;
    public String displayName;
    public SourceType type;
    public String typeId;

    public class Capabilities {
        public boolean canInteract;
        public boolean doNotDuplicate;
        public boolean doNotSelfMonitor;
        public boolean hasAudio;
        public boolean hasVideo;
        public boolean isAsync;
        public boolean isComposite;
        public boolean isDeprecated;
        Capabilities(JSONObject jsonObject) {
            try {
                this.canInteract = jsonObject.getBoolean("canInteract");
                this.doNotDuplicate = jsonObject.getBoolean("doNotDuplicate");
                this.doNotSelfMonitor = jsonObject.getBoolean("doNotSelfMonitor");
                this.hasAudio = jsonObject.getBoolean("hasAudio");
                this.hasVideo = jsonObject.getBoolean("hasVideo");
                this.isAsync = jsonObject.getBoolean("isAsync");
                this.isComposite = jsonObject.getBoolean("isComposite");
                this.isDeprecated = jsonObject.getBoolean("isDeprecated");
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }


        }
    }

    private enum SourceType {
        INPUT,
        FILTER,
        TRANSITION,
        OTHER
    }

    ObsSourceType(JSONObject jsonObject) {
        try {
            this.displayName = jsonObject.getString("displayName");
            switch (jsonObject.getString("type")) {
                case "filter": type = SourceType.FILTER; break;
                case "input": type = SourceType.INPUT; break;
                case "transition": type = SourceType.TRANSITION; break;
                case "other": type = SourceType.OTHER;break;
            }
            this.typeId = jsonObject.getString("typeId");
            this.caps = new Capabilities(jsonObject.getJSONObject("caps"));
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
     }
}
