package edudcball.wpi.users.enotesandroid.objects;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.NoteManager;

public class NotePage {
    private String pageID;
    private String name;
    private int index;

    public NotePage(String name){
        this.pageID = "page-" + System.currentTimeMillis();
        this.name = name;
        this.index = NoteManager.getNumPages();
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

    public void setName(String name){ this.name = name; }
    public void setIndex(int index){ this.index = index; }

    public String getPageID(){ return pageID; }
    public String getName() { return  name; }
    public int getIndex(){ return index; }
}
