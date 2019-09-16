package edudcball.wpi.users.enotesandroid.data.classes;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.noteTasks.DeleteNoteTask;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.noteTasks.NewNoteTask;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.pageTasks.UpdatePageTask;
import edudcball.wpi.users.enotesandroid.data.SortedList;
import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;
import edudcball.wpi.users.enotesandroid.observerPattern.IObserver;

public class Page implements Sortable {

    private String pageID;      // Unique identifier
    private String name;        // Page's name
    private int index;          // Index for ordering

    private SortedList<Note> notes;
    private int activeNoteIndex = 0;
    private boolean hasChanged;

    public Page(){
        this.pageID = "page-" + System.currentTimeMillis();
        this.name = "";
        this.index = 0;

        this.hasChanged = false;

        notes = new SortedList();
    }

    public Page(JSONObject json){
        try {
            this.pageID = json.getString("pageid");
            this.name = json.getString("name");
            this.index = json.getInt("index");

            this.hasChanged = false;

            notes = new SortedList();

            if(Settings.isDebug()){
                Log.d("MYAPP", "Created Page " + pageID);
            }
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
        return notes.getItem(noteId);
    }
    public Note getNote(int index){
        return notes.getItem(index);
    }

    public void addNote(Note newNote){
        if(notes.containsItemWithId(newNote.getId())){
            Log.d("MYAPP", "Duplicate note ID.");
            return;
        }

        notes.add(newNote);
    }

    public void createNote(final Callback<Note> callback){

        NoteLookupTable.NoteColor color = Settings.getDefaultColor();
        NoteLookupTable.NoteFont font = Settings.getDefaultFont();
        final Note newNote = new Note(pageID, NoteLookupTable.getColorJSON(color), NoteLookupTable.getFontString(font), Settings.getDefaultFontSize());

        new NewNoteTask(newNote, new Callback<String>() {
            @Override
            public void run(String param) {
                if (param == null){
                    // Todo error handle
                    Log.d("MYAPP", "Response is null");
                    callback.run(null);
                    return;
                }

                try{
                    JSONObject json = new JSONObject(param);
                    if(json.getBoolean("successful")){
                        addNote(newNote);
                        callback.run(newNote);
                    }
                }
                catch (JSONException e){
                    // TODO error handle
                    e.printStackTrace();
                    callback.run(null);
                }
            }
        }).execute();
    }

    public void deleteNote(final String noteId, final Callback<Boolean> callback){
        if(!notes.containsItemWithId(noteId)){
            Log.d("MYAPP", "Attempted to delete note that does not exist.");
            callback.run(false);
            return;
        }

        new DeleteNoteTask(noteId, new Callback<String>() {
            @Override
            public void run(String param) {
                if (param == null){
                    Log.d("MYAPP", "Delete note returned null");
                    callback.run(false);
                }

                try{
                    JSONObject json = new JSONObject(param);
                    if(json.getBoolean("successful")){
                        notes.remove(noteId);
                        callback.run(true);
                    }
                }
                catch(JSONException e){
                    e.printStackTrace();
                    callback.run(false);
                }
            }
        }).execute();
    }

    public void update(final Callback<Boolean> callback){

        for (int i=0; i<notes.size(); i++){
            notes.getItem(i).update(callback);
        }

        if (hasChanged) {
            new UpdatePageTask(this, new Callback<String>() {
                @Override
                public void run(String param) {
                    if (param == null) {
                        Log.d("MYAPP", "Update page returned null");
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

    public void setName(String name){
        this.name = name;
        this.hasChanged = true;
    }
    public void setIndex(int index){ this.index = index;
        this.hasChanged = true; }

    public String getId(){ return pageID; }
    public String getName() { return  name; }
    public int getIndex(){ return index; }

    public void selectNote(int index){
        activeNoteIndex = index;
    }

    public void selectNote(String id){
        activeNoteIndex = notes.getItemIndex(id);
    }

    public void subscribe(IObserver observer){
        notes.clearObservers();
        notes.subscribe(observer);
    }

    public List<String> getNoteTitleList(){
        return notes.getTitleList();
    }

    public Note getActiveNote(){
        return notes.getItem(activeNoteIndex);
    }

    public String getDisplayTitle(){ return name; }

    // DEBUG ---------------------------------------------------------------------------------------
    public void printNotes(){
        for (int i=0; i<notes.size(); i++){
            Note note = notes.getItem(i);
            Log.d("MYAPP", "Tag: " + note.getId() + " Title: " + note.getDisplayTitle());
        }
    }
}

