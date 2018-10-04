package edudcball.wpi.users.enotesandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.AsyncTasks.userTasks.UpdateSettingsTask;
import edudcball.wpi.users.enotesandroid.activities.MainActivity;
import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;

public class Settings {

    public static final String baseURL = "https://enotes.site";//"http://10.0.2.2:8080";//

    private NoteLookupTable.NoteFont defaultFont = NoteLookupTable.NoteFont.ARIAL;
    private NoteLookupTable.NoteColor defaultColor = NoteLookupTable.NoteColor.YELLOW;
    private int defaultFontSize = 12;
    private SortBy sortBy = SortBy.RECENT;
    private Size iconSize = Size.MEDIUM;
    private Size textSize = Size.MEDIUM;

    private SharedPreferences preferences;

    public enum Size{
        SMALL((short)0),
        MEDIUM((short)1),
        LARGE((short)2);

        short val;
        static Map<Short, Size> map = new HashMap<>();
        static{
            for(Size s: Size.values()){
                map.put(s.getVal(), s);
            }
        }

        Size(short val){
            this.val = val;
        }

        public short getVal(){ return val; }
        public static Size valueOf(short s){
            return map.get(s);
        }
    }

    public enum SortBy{
        RECENT((short)0),
        COLOR((short)1),
        ALPHA((short)2);

        short val;
        static Map<Short, SortBy> map = new HashMap<>();
        static{
            for(SortBy s: SortBy.values()){
                map.put(s.getVal(), s);
            }
        }

        SortBy(short val){
            this.val = val;
        }

        public short getVal(){ return val; }
        public static SortBy valueOf(short s){
            return map.get(s);
        }
    }

    public Settings(Context context){
        preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);

        Size iconSize = Size.valueOf((short)preferences.getInt("IconSize", getIconSize().getVal()));
        setIconSize(iconSize);

        SortBy sort = SortBy.valueOf((short)preferences.getInt("SortBy", getSortBy().getVal()));
        setSortBy(sort);

        Size textSize = Size.valueOf((short)preferences.getInt("TextSize", getTextSize().getVal()));
        setTextSize(textSize);

        NoteLookupTable.NoteColor dNoteColor = NoteLookupTable.getColorFromStr(preferences.getString("Color", null));
        if(dNoteColor != null) setDefaultColor(dNoteColor);

        NoteLookupTable.NoteFont dNoteFont = NoteLookupTable.getFontFromStr(preferences.getString("Font", null));
        if(dNoteFont != null) setDefaultFont(dNoteFont);

        setDefaultFontSize(preferences.getInt("FontSize", defaultFontSize));
    }

    // Icon size -------------------------

    public Size getIconSize(){ return iconSize; }
    public void setIconSize(Size iconSize){
        iconSize = iconSize;
        if(preferences != null) preferences.edit().putInt("IconSize", iconSize.getVal()).commit();
    }

    // -----------------------------------

    // Sort by ---------------------------

    public void setSortBy(SortBy sortBy){
        if(sortBy == sortBy) return;
        this.sortBy = sortBy;
        MainActivity.getDataManager().reSort();
        if(preferences != null) preferences.edit().putInt("SortBy", sortBy.getVal()).commit();
    }

    public SortBy getSortBy(){ return sortBy; }
    // -----------------------------------

    // Default Color ---------------------

    public void setDefaultColor(NoteLookupTable.NoteColor color){
        defaultColor = color;
        if(preferences != null) preferences.edit().putString("Color", NoteLookupTable.getColorStr(color)).commit();
    }
    public NoteLookupTable.NoteColor getDefaultColor(){ return defaultColor; }
    // -----------------------------------

    // Default Font ----------------------

    public void setDefaultFont(NoteLookupTable.NoteFont font){
        defaultFont = font;
        if(preferences != null) preferences.edit().putString("Font", NoteLookupTable.getFontString(font)).commit();
    }
    public NoteLookupTable.NoteFont getDefaultFont(){ return  defaultFont; }
    // -----------------------------------

    // Default Font Size

    public void setDefaultFontSize(int fontSize){
        defaultFontSize = fontSize;
        if(preferences != null) preferences.edit().putInt("FontSize", fontSize).commit();
    }
    public int getDefaultFontSize(){ return  defaultFontSize; }
    // -----------------------------------

    // Text Size -------------------------

    public Size getTextSize(){ return textSize; }
    public void setTextSize(Size textSize){
        this.textSize = textSize;
        if(preferences != null) preferences.edit().putInt("TextSize", textSize.getVal()).commit();
    }
    // -----------------------------------

    public void updateSettingsServerSide(){
        new UpdateSettingsTask(NoteLookupTable.getFontString(defaultFont),
                                defaultFontSize,
                                NoteLookupTable.getColorStr(defaultColor)){
            @Override
            protected void onPostExecute(String result) {
                if(result == null){
                    MainActivity.getDataManager().sessionExpired(null, "Session expired. Please log in again.");
                    return;
                }

                try{
                    // Convert response to a JSON object
                    JSONObject obj = new JSONObject(result);
                    // If successful flag received, save the session id on the phone for next time
                    if(!obj.getBoolean("successful")){
                        MainActivity.getDataManager().sessionExpired(null, "Session expired. Please log in again.");
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Failed to parse JSON");
                    MainActivity.getDataManager().sessionExpired(null, "Problem communicating with server. Please try again later.");
                }
            }
        }.execute();
    }
}
