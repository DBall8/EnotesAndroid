package edudcball.wpi.users.enotesandroid.data.classes;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.observerPattern.Observable;

public class Page extends Observable {

    private String pageID;      // Unique identifier
    private String name;        // Page's name
    private int index;          // Index for ordering

    private HashMap<String, Note> notes;
    private String activeNoteId = "";

    public Page(String name, int index){
        this.pageID = "page-" + System.currentTimeMillis();
        this.name = name;
        this.index = index;

        notes = new HashMap<>();
    }

    public Page(JSONObject json){
        try {
            this.pageID = json.getString("pageid");
            this.name = json.getString("name");
            this.index = json.getInt("index");

            notes = new HashMap<>();
        }
        catch (Exception e){
            Log.d("MYAPP", "Could not parse note page json: " + e.getMessage());
        }
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("pageid", pageID);
            json.put("name", name);
            json.put("index", index);
        }
        catch (Exception e){
            Log.d("MYAPP", "Could not form json for note page: " + e.getMessage());
        }

        return json;
    }

    // PUBLIC METHODS ------------------------------------------------------------------------------

    public Note getNote(String noteId){
        return notes.get(noteId);
    }

    public boolean loadNote(Note newNote){
        if(notes.containsKey(newNote.getTag())){
            Log.d("MYAPP", "Duplicate note ID.");
            return false;
        }

        notes.put(newNote.getTag(), newNote);
        return true;
    }

    public boolean addNote(Note newNote){
        return loadNote(newNote);
    }

    public boolean deleteNote(String noteId){
        if(!notes.containsKey(noteId)){
            Log.d("MYAPP", "Attempted to delete note that does not exist.");
            return false;
        }

        notes.remove(noteId);
        return true;
    }

    public void setName(String name){ this.name = name; }
    public void setIndex(int index){ this.index = index; }

    public String getPageID(){ return pageID; }
    public String getName() { return  name; }
    public int getIndex(){ return index; }

    public void selectNote(String noteId){
        activeNoteId = noteId;
    }

    public List<String> getNoteTitleList(){
        return null;
    }


    public Note getActiveNote(){
        return notes.get(activeNoteId);
    }

    // DEBUG ---------------------------------------------------------------------------------------
    public void printNotes(){
        for (Map.Entry<String, Note> entry: notes.entrySet()){
            Log.d("MYAPP", "Tag: " + entry.getValue().getTag() + " Title: " + entry.getValue().getTitleForDisplay());
        }
    }
}

