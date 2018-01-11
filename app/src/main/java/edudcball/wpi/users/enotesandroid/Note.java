package edudcball.wpi.users.enotesandroid;

import org.json.JSONObject;

/**
 * Created by Owner on 1/5/2018.
 */

public class Note {
    private String tag;
    private String content;
    private String colors;
    private int x;
    private int y;
    private int width;
    private int height;
    private int zindex;

    public Note(String username){
        this.tag = username + '-' + System.currentTimeMillis();
        this.content = "";
        this.colors = "{}";
        this.x = 200;
        this.y = 200;
        this.width = 300;
        this.height = 400;
        this.zindex = 9999;
    }


    public Note(String tag, String content, int x, int y, int w, int h, int z, String colors) {
        this.tag = tag;
        this.content = content;
        this.colors = colors;
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.zindex = z;
    }

    public void setContent(String c){
        this.content = c;
    }

    public void decrementZ(){ this.zindex--; }

    public String getTag(){
        return this.tag;
    }

    public String getContent(){
        return this.content;
    }

    public int getX() { return this.x; };

    public int getY() { return this.y; };

    public int getW() { return this.width; }

    public int getH() { return this.height; };

    public int getZ() { return this.zindex; };

    public String getColors() { return this.colors; };


}
