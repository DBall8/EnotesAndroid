package edudcball.wpi.users.enotesandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.AsyncTasks.userTasks.UpdateSettingsTask;
import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;

public class Settings {

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

    public static void init(Context context){
        SharedPreferences preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        getInstance().preferences = preferences;

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

        setDefaultFontSize(preferences.getInt("FontSize", getInstance().defaultFontSize));
    }

    // Icon size -------------------------

    public static Size getIconSize(){ return getInstance().iconSize; }
    public static void setIconSize(Size iconSize){
        getInstance().iconSize = iconSize;
        if(getInstance().preferences != null) getInstance().preferences.edit().putInt("IconSize", iconSize.getVal()).commit();
    }

    // -----------------------------------

    // Sort by ---------------------------

    public static void setSortBy(SortBy sortBy){
        if(getInstance().sortBy == sortBy) return;
        getInstance().sortBy = sortBy;
        NoteManager.reSort();
        if(getInstance().preferences != null) getInstance().preferences.edit().putInt("SortBy", sortBy.getVal()).commit();
    }

    public static SortBy getSortBy(){ return getInstance().sortBy; }
    // -----------------------------------

    // Default Color ---------------------

    public static void setDefaultColor(NoteLookupTable.NoteColor color){
        getInstance().defaultColor = color;
        if(getInstance().preferences != null) getInstance().preferences.edit().putString("Color", NoteLookupTable.getColorStr(color)).commit();
    }
    public static NoteLookupTable.NoteColor getDefaultColor(){ return getInstance().defaultColor; }
    // -----------------------------------

    // Default Font ----------------------

    public static void setDefaultFont(NoteLookupTable.NoteFont font){
        getInstance().defaultFont = font;
        if(getInstance().preferences != null) getInstance().preferences.edit().putString("Font", NoteLookupTable.getFontString(font)).commit();
    }
    public static NoteLookupTable.NoteFont getDefaultFont(){ return  getInstance().defaultFont; }
    // -----------------------------------

    // Default Font Size

    public static void setDefaultFontSize(int fontSize){
        getInstance().defaultFontSize = fontSize;
        if(getInstance().preferences != null) getInstance().preferences.edit().putInt("FontSize", fontSize).commit();
    }
    public static int getDefaultFontSize(){ return  getInstance().defaultFontSize; }
    // -----------------------------------

    // Text Size -------------------------

    public static Size getTextSize(){ return getInstance().textSize; }
    public static void setTextSize(Size textSize){
        getInstance().textSize = textSize;
        if(getInstance().preferences != null) getInstance().preferences.edit().putInt("TextSize", textSize.getVal()).commit();
    }
    // -----------------------------------

    public static void updateSettingsServerSide(){
        new UpdateSettingsTask(NoteLookupTable.getFontString(getInstance().defaultFont),
                                getInstance().defaultFontSize,
                                NoteLookupTable.getColorStr(getInstance().defaultColor)){
            @Override
            protected void onPostExecute(String result) {
                if(result == null){
                    NoteManager.sessionExpired(null, "Session expired. Please log in again.");
                    return;
                }

                try{
                    // Convert response to a JSON object
                    JSONObject obj = new JSONObject(result);
                    // If successful flag received, save the session id on the phone for next time
                    if(!obj.getBoolean("successful")){
                        NoteManager.sessionExpired(null, "Session expired. Please log in again.");
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Failed to parse JSON");
                    NoteManager.sessionExpired(null, "Problem communicating with server. Please try again later.");
                }
            }
        }.execute();
    }
    private Settings(){}

    private static class SingletonHelper{
        private static final Settings _instance = new Settings();
    }

    private static Settings getInstance(){ return SingletonHelper._instance; }
}
