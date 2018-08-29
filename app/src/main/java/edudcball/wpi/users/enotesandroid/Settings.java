package edudcball.wpi.users.enotesandroid;

import org.json.JSONObject;

public class Settings {

    private String defaultFont;
    private JSONObject defaultColor;
    private int defaultFontSize;
    private SortBy sortBy = SortBy.RECENT;
    private Size iconSize = Size.MEDIUM;


    public enum Size{
        SMALL,
        MEDIUM,
        LARGE
    }

    public enum SortBy{
        RECENT,
        COLOR
    }

    public static Size getIconSize(){ return getInstance().iconSize; }
    public static void setIconSize(Size iconSize){
        getInstance().iconSize = iconSize;
        NoteManager.buildNoteAdapter();
    }

    public static void setSortBy(SortBy sortBy){
        if(getInstance().sortBy == sortBy) return;
        getInstance().sortBy = sortBy;
        NoteManager.reSort();
    }

    public static SortBy getSortBy(){ return getInstance().sortBy; }

    private Settings(){}

    private static class SingletonHelper{
        private static final Settings _instance = new Settings();
    }

    private static Settings getInstance(){ return SingletonHelper._instance; }
}
