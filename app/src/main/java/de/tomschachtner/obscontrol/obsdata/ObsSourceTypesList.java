package de.tomschachtner.obscontrol.obsdata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ObsSourceTypesList {
    public ArrayList<ObsSourceType> obsSources;

    public ObsSourceTypesList(JSONObject jsonObject) {
        obsSources = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("types");
            for (int i = 0; i < jsonArray.length(); i++) {
                ObsSourceType obsSourceType = new ObsSourceType(jsonArray.getJSONObject(i));
                obsSources.add(obsSourceType);
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }
}
