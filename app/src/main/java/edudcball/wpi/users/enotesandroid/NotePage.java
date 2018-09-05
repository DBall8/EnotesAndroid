package edudcball.wpi.users.enotesandroid;

import android.util.Log;

import org.json.JSONObject;

public class NotePage {
    private String pageID;
    private String name;
    private int index;

    public NotePage(String name){
        this.pageID = "page-" + System.currentTimeMillis();
        this.name = name;
        this.index = 0; // TODO make this the length of current pages
    }

    public NotePage(JSONObject json){
        try {
            this.pageID = json.getString("pageid");
            this.name = json.getString("name");
            this.index = json.getInt("index");
        }
        catch (Exception e){
            Log.d("MYAPP", "Could not parse note page json: " + e.getMessage());
        }
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("pageID", pageID);
            json.put("name", name);
            json.put("index", index);
        }
        catch (Exception e){
            Log.d("MYAPP", "Could not form json for note page: " + e.getMessage());
        }

        return json;
    }

    public String getPageID(){ return pageID; }
    public String getName() { return  name; }
    public int getIndex(){ return index; }
}
