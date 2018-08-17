package edudcball.wpi.users.enotesandroid;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ColorConversions {

    private final Map<Integer, JSONObject> colorToJSON = new HashMap<>();
    private Context context;

    public static void init(Context context){
        getInstance().context = context;
        Map<Integer, JSONObject> map = getInstance().colorToJSON;
        map.put(ContextCompat.getColor(context, R.color.noteYellow), makeColorJSON("#ddaf00", "#ffe062"));
        map.put(ContextCompat.getColor(context, R.color.noteOrange), makeColorJSON("#e88a19", "#ffa63d"));
        map.put(ContextCompat.getColor(context, R.color.noteRed), makeColorJSON("#f15656", "#f97e7e"));
        map.put(ContextCompat.getColor(context, R.color.noteGreen), makeColorJSON("#1ea723", "#53ce57"));
        map.put(ContextCompat.getColor(context, R.color.noteBlue), makeColorJSON("#43a5ec", "#7fc8f5"));
        map.put(ContextCompat.getColor(context, R.color.notePurple), makeColorJSON("#b34ace", "#e083f7"));
    }

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

    public static JSONObject getJSONFromColor(int color){
        if(getInstance().colorToJSON.containsKey(color)) {
            return getInstance().colorToJSON.get(color);
        }
        return null;
    }


    private ColorConversions(){}

    private static class SingletonHelper{
        private static final ColorConversions instance = new ColorConversions();
    }

    private static ColorConversions getInstance(){ return SingletonHelper.instance; }
}
