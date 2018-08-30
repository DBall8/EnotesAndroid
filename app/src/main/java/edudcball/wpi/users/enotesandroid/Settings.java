package edudcball.wpi.users.enotesandroid;

import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;

public class Settings {

    private NoteLookupTable.NoteFont defaultFont = NoteLookupTable.NoteFont.ARIAL;
    private NoteLookupTable.NoteColor defaultColor = NoteLookupTable.NoteColor.YELLOW;
    private int defaultFontSize = 12;
    private SortBy sortBy = SortBy.RECENT;
    private Size iconSize = Size.MEDIUM;
    private Size textSize = Size.MEDIUM;


    public enum Size{
        SMALL,
        MEDIUM,
        LARGE
    }

    public enum SortBy{
        RECENT,
        COLOR
    }

    // Icon size -------------------------

    public static Size getIconSize(){ return getInstance().iconSize; }
    public static void setIconSize(Size iconSize){ getInstance().iconSize = iconSize; }

    // -----------------------------------

    // Sort by ---------------------------

    public static void setSortBy(SortBy sortBy){
        if(getInstance().sortBy == sortBy) return;
        getInstance().sortBy = sortBy;
        NoteManager.reSort();
    }

    public static SortBy getSortBy(){ return getInstance().sortBy; }
    // -----------------------------------

    // Default Color ---------------------

    public static void setDefaultColor(NoteLookupTable.NoteColor color){ getInstance().defaultColor = color; }
    public static NoteLookupTable.NoteColor getDefaultColor(){ return getInstance().defaultColor; }
    // -----------------------------------

    // Default Font ----------------------

    public static void setDefaultFont(NoteLookupTable.NoteFont font){ getInstance().defaultFont = font; }
    public static NoteLookupTable.NoteFont getDefaultFont(){ return  getInstance().defaultFont; }
    // -----------------------------------

    // Default Font Size

    public static void setDefaultFontSize(int fontSize){ getInstance().defaultFontSize = fontSize; }
    public static int getDefaultFontSize(){ return  getInstance().defaultFontSize; }
    // -----------------------------------

    // Text Size -------------------------

    public static Size getTextSize(){ return getInstance().textSize; }
    public static void setTextSize(Size textSize){ getInstance().textSize = textSize; }
    // -----------------------------------

    private Settings(){}

    private static class SingletonHelper{
        private static final Settings _instance = new Settings();
    }

    private static Settings getInstance(){ return SingletonHelper._instance; }
}
