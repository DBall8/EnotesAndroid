package edudcball.wpi.users.enotesandroid.NoteManager;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import edudcball.wpi.users.enotesandroid.activities.MainActivity;
import edudcball.wpi.users.enotesandroid.objects.Note;
import edudcball.wpi.users.enotesandroid.objects.NotePage;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketConnection {

    private static final String URL = "https://enotes.site";//"http://10.0.2.2:8080";//

    private Socket socket;
    private String id = null;


    // References to NoteManager's lists -----------------
    private HashMap<String, Note> notes;
    private ArrayList<String> noteTagLookup;
    private ArrayList<String> noteTitles;

    private ArrayList<String> pageTitles;
    // ---------------------------------------------------

    public SocketConnection(HashMap<String, Note> notes, ArrayList<String> noteTagLookup, ArrayList<String> noteTitles, ArrayList<String> pageTitles){

        this.notes = notes;
        this.noteTagLookup = noteTagLookup;
        this.noteTitles = noteTitles;
        this.pageTitles = pageTitles;

        try {
            socket = IO.socket(URL);

            socket.on("ready", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    id = (String)args[0];
                    socket.emit("ready", NoteManager.getUsername());
                    //Log.d("MYAPP", "SOCKET ID: " + id);
                }
            });

            socket.on("create", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    createNote((String)args[0]);
                }
            });

            socket.on("update", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    updateNote((String)args[0]);
                }
            });

            socket.on("delete", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    deleteNote((String)args[0]);
                }
            });

            socket.on("createpage", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    createPage((String)args[0]);
                }
            });

            socket.on("updatepage", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    updatePage((String)args[0]);
                }
            });

            socket.on("deletepage", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    deletePage((String)args[0]);
                }
            });

            socket.connect();

        } catch (Exception e) {
            Log.d("MYAPP", "Socket failed to connect. " + e.getMessage());
        }
    }

    private void createNote(String msg){
        try{
            JSONObject json = new JSONObject(msg);
            Note note = new Note(json);

            notes.put(note.getTag(), note);
            NoteManager.reSort();
        }catch(Exception e){
            Log.d("MYAPP","FAILED TO PARSE SOCKET NOTE JSON: " + e.getMessage());
        }
    }

    private void updateNote(String msg){
        try{
            JSONObject json = new JSONObject(msg);
            Note note = new Note(json);

            if(!notes.containsKey(note.getTag())) return;

            notes.remove(note.getTag());
            notes.put(note.getTag(), note);

            NoteManager.reSort();

        } catch(Exception e){
            Log.d("MYAPP","FAILED TO PARSE SOCKET NOTE JSON: " + e.getMessage());
        }

    }

    private void deleteNote(String tag){

        if(notes.containsKey(tag)) notes.remove(tag);
        NoteManager.reSort();
    }

    private void createPage(String msg){
        try{
            JSONObject json = new JSONObject(msg);
            NotePage page = new NotePage(json);

            NoteManager.addPageToArray(page);
            MainActivity.notifyAdatperChanged();
        }catch(Exception e){
            Log.d("MYAPP","FAILED TO PARSE SOCKET PAGE JSON: " + e.getMessage());
        }
    }

    private void updatePage(String msg){
        try{
            JSONObject json = new JSONObject(msg);
            NotePage page = new NotePage(json);
            NoteManager.updatePageArray(page);
        }
        catch(Exception e){
            Log.d("MYAPP", "FAILED TO PARSE SOCKET PAGE UPDATE: " + e.getMessage());
        }
    }

    private void deletePage(String pageID){
        try{
            NoteManager.removePageFromArray(null, pageID);
        }catch(Exception e){
            Log.d("MYAPP","FAILED TO PARSE SOCKET PAGE JSON: " + e.getMessage());
        }
    }

    public String getID(){ return id; }
}
