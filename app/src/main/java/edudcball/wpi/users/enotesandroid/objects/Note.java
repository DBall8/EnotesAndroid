package edudcball.wpi.users.enotesandroid.objects;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.activities.MainActivity;
import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;

/**
 * Class for containing all the data needed for a single Note
 */

public class Note {
    private String tag; // tag that uniquely identifies the note
    private String pageID; // id of the note page the note belongs to
    private String title; // the optional title displayed at the top of the note
    private String content; // the text content of the note
    private JSONObject colors; // a JSON containing the colors used for the note
    private int x; // the note's x coordinate
    private int y; // the note's y coordinate
    private int width; // the note's width
    private int height; // the note's height
    private String font; // the font of the note's text
    private int fontSize; // the size of the note's text
    private int zindex; // the depth of the note used for stacking and ordering

    /**
     * Constructor for a new empty note
     */
    public Note(){
        this.tag = "note-" + System.currentTimeMillis(); // create a tag from the current time
        this.pageID = MainActivity.getDataManager().getCurrentPageID();
        this.title = ""; // empty title
        this.content = ""; // empty content
        this.colors = NoteLookupTable.getColorJSON(MainActivity.getSettings().getDefaultColor()); // default color

        // default position of 200, 200
        this.x = 200;
        this.y = 200;
        // default size of 200 x 200
        this.width = 200;
        this.height = 200;
        this.font = NoteLookupTable.getFontString(MainActivity.getSettings().getDefaultFont()); // default font of Arial
        this.fontSize = MainActivity.getSettings().getDefaultFontSize(); //default size 12
        this.zindex = 9999; // starts on top (is quickly changed)
    }

    /**
     * Create the note from a JSON object
     * @param json the json object
     */
    public Note(JSONObject json) {
        try {
            // get each field from the JSON
            this.tag = json.getString("tag");
            this.pageID = json.getString("pageid");
            String title = json.getString("title");
            if (title == null || title.equals("null")) {
                this.title = "";
            } else {
                this.title = title;
            }
            this.content = json.getString("content");
            this.x = json.getInt("x");
            this.y = json.getInt("y");
            this.width = json.getInt("width");
            this.height = json.getInt("height");
            this.font = json.getString("font");
            this.fontSize = json.getInt("fontsize");
            this.zindex = json.getInt("zindex");

            try {
                this.colors = new JSONObject(json.getString("colors"));
            } catch (Exception e) {
                this.colors = null;
            }
        }
        catch(JSONException e){
            Log.d("MYAPP", "FAILED TO BUILD NOTE");
        }
    }

    /**
     * Create a json object representing the note
     * @return a json object representing the note
     */
    public JSONObject toJSON(){
        try {

            JSONObject json = new JSONObject();
            json.put("tag", tag);
            json.put("pageid", pageID);
            json.put("title", title);
            json.put("content", content);
            json.put("x", x);
            json.put("y", y);
            json.put("width", width);
            json.put("height", height);
            json.put("font", font);
            json.put("fontsize", fontSize);
            json.put("zindex", zindex);
            json.put("colors", colors);
            json.put("socketid", MainActivity.getDataManager().getSocketID());
            return json;

        } catch(JSONException e){
            Log.d("MYAPP", "UNABLE TO CONVERT NOTE TO JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a title to display on the note icon
     * @return a title to display on the note icon
     */
    public String getTitleForDisplay(){

        // If the note has a title, use that
        if(!title.equals("")){
            return title;
        }

        // Otherwise, create a dummy title from the first 100 characters in its content's first line
        int end = content.length()<100? content.length(): 100;
        for(int i=0; i<end; i++){
            if(content.charAt(i) == '\n'){
                end = i;
                break;
            }
        }
        return content.substring(0, end);
    }

    public void setColor(JSONObject colors){ this.colors = colors; }

    public void setFont(String font){
        this.font = font;
    }

    public void setFontSize(int fontSize){
        this.fontSize = fontSize;
    }

    public void setContent(String c){
        this.content = c;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setZIndex(int z){
        this.zindex = z;
    }

    public String getTag(){
        return this.tag;
    }

    public String getPageID(){ return this.pageID; }

    public String getTitle(){ return this.title; }

    public String getContent(){
        return this.content;
    }

    public String getFont(){ return this.font; }

    public int getFontSize(){ return this.fontSize; }

    public int getZ() { return this.zindex; }

    public JSONObject getColors() { return this.colors; }
}
