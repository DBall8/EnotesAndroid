package edudcball.wpi.users.enotesandroid.noteDataTypes;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.activities.MainActivity;

/**
 * A class for converting between color id codes and their JSON forms used on the web version
 */
public class NoteLookupTable {

    public enum NoteColor{
        YELLOW,
        ORANGE,
        RED,
        GREEN,
        BLUE,
        PURPLE
    }

    public enum NoteFont {
        ARIAL,
        PALATINO,
        COURIER
    }

    private static final Map<NoteColor, NoteColorContainer> colorTable = new HashMap<>(); // map from color id to JSON
    private static final Map<NoteFont, String> fontTable = new HashMap<>();

    public static void init(Context context){
        // color table
        colorTable.put(NoteColor.YELLOW, new NoteColorContainer(context, NoteColor.YELLOW));
        colorTable.put(NoteColor.ORANGE, new NoteColorContainer(context, NoteColor.ORANGE));
        colorTable.put(NoteColor.RED, new NoteColorContainer(context, NoteColor.RED));
        colorTable.put(NoteColor.GREEN, new NoteColorContainer(context, NoteColor.GREEN));
        colorTable.put(NoteColor.BLUE, new NoteColorContainer(context, NoteColor.BLUE));
        colorTable.put(NoteColor.PURPLE, new NoteColorContainer(context, NoteColor.PURPLE));

        // font table
        fontTable.put(NoteFont.ARIAL, context.getResources().getString(R.string.arial));
        fontTable.put(NoteFont.PALATINO, context.getResources().getString(R.string.palatino));
        fontTable.put(NoteFont.COURIER, context.getResources().getString(R.string.courier));
    }

    /**
     * Builds a JSON object with the format that matches the format used on the web server
     * @param head the color of the note head
     * @param body the color of the note body
     * @return the JSON object with the given values for head and body
     */
    static JSONObject makeColorJSON(String head, String body){
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
    public static JSONObject getColorJSON(NoteColor color){
        if(getInstance().colorTable.containsKey(color)) {
            return getInstance().colorTable.get(color).jsonVal;
        }
        return null;
    }

    public static int getColorInt(NoteColor color){
        if(getInstance().colorTable.containsKey(color)) {
            return getInstance().colorTable.get(color).intVal;
        }
        return 0;
    }

    public static String getColorStr(NoteColor color){
        if(getInstance().colorTable.containsKey(color)) {
            return getInstance().colorTable.get(color).strVal;
        }
        return null;
    }

    public static NoteColor getColorFromInt(int color){
        for(Map.Entry<NoteColor, NoteColorContainer> cursor: getInstance().colorTable.entrySet()){
            if(cursor.getValue().intVal == color){
                return cursor.getKey();
            }
        }
        return MainActivity.getSettings().getDefaultColor();
    }

    public static NoteColor getColorFromStr(String color){
        for(Map.Entry<NoteColor, NoteColorContainer> cursor: getInstance().colorTable.entrySet()){
            if(cursor.getValue().strVal.equals(color)){
                return cursor.getKey();
            }
        }
        return MainActivity.getSettings().getDefaultColor();
    }

    public static String getFontString(NoteFont font){
        if(getInstance().fontTable.containsKey(font)) {
            return getInstance().fontTable.get(font);
        }
        return null;
    }

    public static NoteFont getFontFromStr(String str){
        for(Map.Entry<NoteFont, String> cursor: getInstance().fontTable.entrySet()){
            if(cursor.getValue().equals(str)){
                return cursor.getKey();
            }
        }

        return null;
    }



    // ----------------- Code for creating a protected singleton ----------------------------------
    private NoteLookupTable(){}

    private static class SingletonHelper{
        private static final NoteLookupTable instance = new NoteLookupTable();
    }

    private static NoteLookupTable getInstance(){ return SingletonHelper.instance; }
}
