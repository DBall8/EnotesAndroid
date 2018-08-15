package edudcball.wpi.users.enotesandroid;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Owner on 1/5/2018.
 */

public class Note {
    private String tag;
    private String title;
    private String content;
    private JSONObject colors;
    private int x;
    private int y;
    private int width;
    private int height;
    private String font;
    private int fontSize;
    private int zindex;

    public Note(){
        this.tag = "note-" + System.currentTimeMillis();
        this.title = "";
        this.content = "";
        try {
            this.colors = new JSONObject("{head: \'#ddaf00\', body: \'#ffe062\'}");
        }
        catch(Exception e){
            this.colors = null;
        }
        this.x = 200;
        this.y = 200;
        this.width = 300;
        this.height = 400;
        this.font = "Arial";
        this.fontSize = 12;
        this.zindex = 9999;
    }


    public Note(JSONObject json) {
        try {
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
            Log.d("ERR", "FAILED TO BUILD NOTE");
        }
    }

    public void setContent(String c){
        this.content = c;
    }

    public void decrementZ(){
        if(this.zindex > 0){
            this.zindex--;
        }
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

    public int getX() { return this.x; }

    public int getY() { return this.y; }

    public int getWidth() { return this.width; }

    public int getHeight() { return this.height; }

    public String getFont(){ return this.font; }

    public int getFontSize(){ return this.fontSize; }

    public int getZ() { return this.zindex; }

    public JSONObject getColors() { return this.colors; }

    public void moveToTop(){
        this.zindex = 9999;
    }


}
