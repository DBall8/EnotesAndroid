package edudcball.wpi.users.enotesandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import AsyncTasks.DeleteTask;
import AsyncTasks.NewNoteTask;
import AsyncTasks.RetrieveNotesTask;
import AsyncTasks.UpdateNoteTask;

/**
 * Created by Owner on 1/5/2018.
 */

public class NoteManager {

    static private MainActivity parent;

    static private HashMap<String, Note> notes = new HashMap<String, Note>();
    static private ArrayList<String> noteTagLookup = new ArrayList<String>();
    static private ArrayList<String> noteTitles = new ArrayList<String>();

    static private ArrayAdapter<String> noteAdapter;
    public static String username = "";

    public static CookieManager cookies = new CookieManager();

    public static void init(final MainActivity parent, ListView lv, final Context context){

        NoteManager.parent = parent;

        NoteManager.noteAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, noteTitles){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(ContextCompat.getColor(context, R.color.black));
                Note n = getNote(noteTagLookup.get(position));
                JSONObject colors = n.getColors();
                if(colors != null){
                    try {
                        tv.setBackgroundColor(Color.parseColor(colors.getString("body")));
                    }catch(Exception e){
                        tv.setBackgroundColor(parent.getResources().getColor(R.color.defaultNote));
                    }
                }
                else{
                    tv.setBackgroundColor(parent.getResources().getColor(R.color.defaultNote));
                }


                return tv;
            }
        };
        lv.setAdapter(noteAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Note n = getNote(noteTagLookup.get(i));
                switchToNote(n);
            }
        });
    }

    public static void retrieveNotes(){
        new RetrieveNotesTask(){

            @Override
            protected void onPostExecute(String result) {
                loadNotes(result);
            }
        }.execute();
    }

    public static void sessionExpired(String message){
        Intent next = new Intent(parent, LoginActivity.class);
        next.putExtra("error", message);
        parent.startActivity(next);
    }

    public static void loadNotes(String res){
        try{
            JSONObject obj = new JSONObject(res);
            if(obj.getBoolean("sessionExpired")){
                Log.d("MYAPP", "Session expired");
                sessionExpired("Session expired. Please log in again.");
                return;
            }
            noteTitles.clear();
            noteTagLookup.clear();
            username = obj.getString("username");
            JSONArray arr = obj.getJSONArray("notes");
            for(int i=0; i<arr.length(); i++){
                JSONObject note = arr.getJSONObject(i);
                String tag = note.getString("tag");
                if(notes.get(tag) == null){
                    Note n = new Note(note.getString("tag"),
                            note.getString("content"),
                            note.getInt("x"),
                            note.getInt("y"),
                            note.getInt("width"),
                            note.getInt("height"),
                            note.getInt("zindex"),
                            note.getString("colors"));
                    notes.put(n.getTag(), n);
                }
                else{
                    int z = notes.get(tag).getZ();
                    notes.remove(tag);
                    Note n = new Note(note.getString("tag"),
                            note.getString("content"),
                            note.getInt("x"),
                            note.getInt("y"),
                            note.getInt("width"),
                            note.getInt("height"),
                            z,
                            note.getString("colors"));
                    notes.put(n.getTag(), n);
                }

            }
            createNoteList();
            Log.d("MYAPP", "NOTES LOADED");
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form response JSON for get notes");
            Log.d("MYAPP", e.getMessage().toString());
            sessionExpired("Error when contacting server. Please try again later.");
        }

    }

    public static void createNoteList(){
        Note n;
        ArrayList<String> workingList = new ArrayList<String>();
        for(Map.Entry<String, Note> cursor: notes.entrySet()){
            workingList.add(cursor.getKey());
        }

        int len = workingList.size();

        int z;
        for(int i=0; i<len; i++){
            String maxTag = null;
            int maxZ = 0;
            for(int j=0; j<workingList.size(); j++){
                if((z = getNote(workingList.get(j)).getZ()) > maxZ){
                    maxTag = workingList.get(j);
                    maxZ = z;
                }
            }
            if(maxTag != null){
                workingList.remove(maxTag);
                Note note = getNote(maxTag);
                noteTitles.add(getTitleFromNote(note));
                noteTagLookup.add(maxTag);
            }

        }

        noteAdapter.notifyDataSetChanged();
    }


    public static void addNote(Note n){
        notes.put(n.getTag(), n);
        noteTagLookup.add(n.getTag());
        noteTitles.add(getTitleFromNote(n));
        noteAdapter.notifyDataSetChanged();
    }

    public static void newNote(){
        if(username == null){
            Log.d("MYAPP", "Username is null");
        }
        Note n = new Note(username);

        new NewNoteTask(n){

            @Override
            protected void onPostExecute(String result) {
                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        sessionExpired("Session expired. Please log in again.");
                        return;
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    sessionExpired("Error when contacting server. Please try again later.");
                }
            }
        }.execute();

        addNote(n);
        decrementZ(n.getTag());
        switchToNote(n);
    }

    public static void updateNote(String tag){
        Note n = getNote(tag);
        new UpdateNoteTask(n){

            @Override
            protected void onPostExecute(String result) {
                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        sessionExpired("Session expired. Please log in again.");
                        return;
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    sessionExpired("Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    public static void deleteNote(String tag){
        notes.remove(tag);
        for(int i=0; i< noteTagLookup.size(); i++){
            if(noteTagLookup.get(i).equals(tag)){
                noteTagLookup.remove(i);
                noteTitles.remove(i);
                noteAdapter.notifyDataSetChanged();
                i--;
            }
        }
        new DeleteTask(tag){
            @Override
            protected void onPostExecute(String result) {
                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        NoteManager.sessionExpired("Session expired. Please log in again.");
                        return;
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for delete note");
                    sessionExpired("Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }



    public static Note getNote(String tag){
        return notes.get(tag);
    }

    private static String getTitleFromNote(Note n){
        String content = n.getContent();
        int end = content.length()<100? content.length(): 100;
        for(int i=0; i<end; i++){
            if(content.charAt(i) == '\n'){
                end = i;
                break;
            }
        }
        return n.getContent().substring(0, end);
    }

    public static void decrementZ(String tag){
        for(Map.Entry<String, Note> cursor: notes.entrySet()){
            if(cursor.getKey() != tag){
                cursor.getValue().decrementZ();
            }
        }
    }

    private static void switchToNote(Note n){
        n.moveToTop();
        decrementZ(n.getTag());
        Intent noteActivity = new Intent(parent, NoteActivity.class);
        noteActivity.putExtra("Tag", n.getTag());
        noteActivity.putExtra("Content", n.getContent());
        parent.startActivity(noteActivity);
    }

}
