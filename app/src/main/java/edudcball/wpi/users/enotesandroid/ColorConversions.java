package edudcball.wpi.users.enotesandroid;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for converting between color id codes and their JSON forms used on the web version
 */
public class ColorConversions {

    private final Map<Integer, JSONObject> colorToJSON = new HashMap<>(); // map from color id to JSON

    /**
     * Builds the color conversion table
     * @param context the context to use to load the color IDs
     */
    public static void init(Context context){
        Map<Integer, JSONObject> map = getInstance().colorToJSON;
        map.put(ContextCompat.getColor(context, R.color.noteYellow), makeColorJSON("#ddaf00", "#ffe062"));
        map.put(ContextCompat.getColor(context, R.color.noteOrange), makeColorJSON("#e88a19", "#ffa63d"));
        map.put(ContextCompat.getColor(context, R.color.noteRed), makeColorJSON("#f15656", "#f97e7e"));
        map.put(ContextCompat.getColor(context, R.color.noteGreen), makeColorJSON("#1ea723", "#53ce57"));
        map.put(ContextCompat.getColor(context, R.color.noteBlue), makeColorJSON("#43a5ec", "#7fc8f5"));
        map.put(ContextCompat.getColor(context, R.color.notePurple), makeColorJSON("#b34ace", "#e083f7"));
    }

    /**
     * Builds a JSON object with the format that matches the format used on the web server
     * @param head the color of the note head
     * @param body the color of the note body
     * @return the JSON object with the given values for head and body
     */
    private static JSONObject makeColorJSON(String head, String body){
        JSONObject json = new JSONObject();
        try {
            json.put("head", head);
            json.put("body", body);
            return json;
        } catch(JSONException e){
            Log.d("MYAPP", "Failed to create color JSON: " + e.getMessage());
            return json;
        }
    }

    /**
     * Retrieves the JSON object corresponding to the given color code
     * @param color
     * @return
     */
    public static JSONObject getJSONFromColor(int color){
        if(getInstance().colorToJSON.containsKey(color)) {
            return getInstance().colorToJSON.get(color);
        }
        return null;
    }


    // ----------------- Code for creating a protected singleton ----------------------------------
    private ColorConversions(){}

    private static class SingletonHelper{
        private static final ColorConversions instance = new ColorConversions();
    }

    private static ColorConversions getInstance(){ return SingletonHelper.instance; }
}
