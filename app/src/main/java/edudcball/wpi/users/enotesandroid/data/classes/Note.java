package edudcball.wpi.users.enotesandroid.data.classes;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.noteTasks.UpdateNoteTask;
import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;
import edudcball.wpi.users.enotesandroid.observerPattern.Observable;

/**
 * Class for containing all the data needed for a single Note
 */

public class Note extends Sortable {
    private String id;         // id that uniquely identifies the note
    private String pageID;      // id of the note page the note belongs to
    private String title;       // the optional title displayed at the top of the note
    private String content;     // the text content of the note
    private JSONObject colors;  // a JSON containing the colors used for the note
    private int x;              // the note's x coordinate
    private int y;              // the note's y coordinate
    private int width;          // the note's width
    private int height;         // the note's height
    private String font;        // the font of the note's text
    private int fontSize;       // the size of the note's text
    private int zindex;         // the depth of the note used for stacking and ordering

    private boolean hasChanged; // True when the note has changed but the server has not been updated

    /**
     * Constructor for a new empty note
     */
    public Note(String ownerPageId, JSONObject colors, String font, int fontSize){
        this.id = "note-" + System.currentTimeMillis(); // create a id from the current time
        this.pageID = ownerPageId;
        this.title = "";        // empty title
        this.content = "";      // empty content

        // default position of 200, 200
        this.x = 200;
        this.y = 200;

        // default size of 200 x 200
        this.width = 200;
        this.height = 200;

        // Attributes
        this.font = font;
        this.fontSize = fontSize;
        this.colors = colors;
        this.zindex = 9999; // starts on top (is quickly changed)

        this.hasChanged = false;
    }

    /**
     * Create the note from a JSON object
     * @param json the json object
     */
    public Note(JSONObject json) {
        try {
            // get each field from the JSON
            this.id = json.getString("tag");
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

            this.hasChanged = false;

            try {
                this.colors = new JSONObject(json.getString("colors"));
            } catch (Exception e) {
                this.colors = NoteLookupTable.getColorJSON(Settings.getDefaultColor());
            }

            if (font == null ||
                font.equals("null") ||
                font.length() <=0){

                font = NoteLookupTable.getFontString(Settings.getDefaultFont());
            }

            if(Settings.isDebug()){
                Log.d("MYAPP", "Created Note " + id + " in page " + pageID);
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
            json.put("tag", id);
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
    public String getDisplayTitle(){

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

    public void update(final Callback<Boolean> callback){
        if (hasChanged) {
            new UpdateNoteTask(this, new Callback<String>() {
                @Override
                public void run(String param) {
                    if (param == null) {
                        Log.d("MYAPP", "Update note returned null");
                        callback.run(false);
                    }

                    try {
                        JSONObject json = new JSONObject(param);
                        if (json.getBoolean("successful")) {
                            hasChanged = false;
                            callback.run(true);
                        } else {
                            callback.run(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.run(false);
                    }
                }
            }).execute();
        }
    }

    public void consumeSocketUpdate(Note note){
        this.title = note.title;
        this.content = note.content;
        if (note.colors != null){
            this.colors = note.colors;
        }
        this.x = note.x;
        this.y = note.y;
        this.width = note.width;
        this.height = note.height;
        this.font = note.font;
        this.fontSize = note.fontSize;
        this.zindex = note.zindex;

        if (font == null ||
            font.equals("null") ||
            font.length() <=0){

            font = NoteLookupTable.getFontString(Settings.getDefaultFont());
        }

        notifyObservers(id);
    }

    public void setColor(JSONObject colors){
        this.colors = colors;
        this.hasChanged = true;
    }

    public void setFont(String font){
        this.font = font;
        this.hasChanged = true;
    }

    public void setFontSize(int fontSize){
        this.fontSize = fontSize;
        this.hasChanged = true;
    }

    public void setContent(String c){
        this.content = c;
        this.hasChanged = true;
    }

    public void setTitle(String title){
        this.title = title;
        this.hasChanged = true;
        notifyObservers(id);
    }

    public void setZIndex(int z){
        this.zindex = z;
        this.hasChanged = true;
        notifyObservers(id);
    }

    public String getId(){
        return this.id;
    }

    public String getPageID(){ return this.pageID; }

    public String getTitle(){ return this.title; }

    public String getContent(){
        return this.content;
    }

    public String getFont(){
        return this.font;
    }

    public int getFontSize(){ return this.fontSize; }

    public int getZ() { return this.zindex; }

    public int getIndex() { return getZ(); }

    public JSONObject getColors() { return this.colors; }
}

