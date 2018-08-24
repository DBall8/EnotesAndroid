package edudcball.wpi.users.enotesandroid;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class for containing all the data needed for a single Note
 */

public class Note {
    private String tag; // tag that uniquely identifies the note
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
        this.title = ""; // empty title
        this.content = ""; // empty content
        try { // default color of yellow
            this.colors = new JSONObject("{head: \'#ddaf00\', body: \'#ffe062\'}");
        }
        catch(Exception e){
            this.colors = null;
        }

        // default position of 200, 200
        this.x = 200;
        this.y = 200;
        // default size of 300 x 400
        this.width = 300;
        this.height = 400;
        this.font = "Arial"; // default font of Arial
        this.fontSize = 12; //default size 12
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
            json.put("title", title);
            json.put("content", content);
            json.put("x", x);
            json.put("y", y);
            json.put("width", width);
            json.put("height", height);
            json.put("font", font);
            json.put("fontSize", fontSize);
            json.put("zindex", zindex);
            json.put("colors", colors);
                /*
                json.put("tag", tag);
                json.put("newtitle", title);
                json.put("newcontent", content);
                json.put("newx", x);
                json.put("newy", y);
                json.put("newW", width);
                json.put("newH", height);
                json.put("newFont", font);
                json.put("newFontSize", fontSize);
                json.put("newZ", zindex);
                json.put("newColors", colors);
                */
            return json;

        } catch(JSONException e){
            Log.d("MYAPP", "UNABLE TO CONVERT NOTE TO JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Set the color of the note
     * @param color the number ID of the color to set to
     */
    public void setColor(int color){
        // Get the JSON version of the color
        JSONObject colorJSON = ColorConversions.getJSONFromColor(color);
        if(colorJSON != null)
            this.colors = colorJSON;
    }

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

    public String getTitle(){ return this.title; }

    public String getContent(){
        return this.content;
    }

    public String getFont(){ return this.font; }

    public int getFontSize(){ return this.fontSize; }

    public int getZ() { return this.zindex; }

    public JSONObject getColors() { return this.colors; }
}
