package edudcball.wpi.users.enotesandroid.connection;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.data.classes.Note;
import edudcball.wpi.users.enotesandroid.data.classes.Page;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketConnection {

    private static final String URL = Settings.baseURL;

    private Socket socket;
    private String id = null;

    // ---------------------------------------------------

    public SocketConnection(final String username){

        try {
            socket = IO.socket(URL);

            socket.on("ready", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    id = (String)args[0];
                    if(username.equals("")){
                        Log.d("MYAPP", "SOCKET FAILED TO START: no username.");
                        return;
                    }

                    socket.emit("ready", username);
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

        }catch(Exception e){
            Log.d("MYAPP","FAILED TO PARSE SOCKET NOTE JSON: " + e.getMessage());
        }
    }

    private void updateNote(String msg){
        try{
            JSONObject json = new JSONObject(msg);
            Note note = new Note(json);

        } catch(Exception e){
            Log.d("MYAPP","FAILED TO PARSE SOCKET NOTE JSON: " + e.getMessage());
        }

    }

    private void deleteNote(String tag){

    }

    private void createPage(String msg){
        try{
            JSONObject json = new JSONObject(msg);
            Page page = new Page(json);
        }catch(Exception e){
            Log.d("MYAPP","FAILED TO PARSE SOCKET PAGE JSON: " + e.getMessage());
        }
    }

    private void updatePage(String msg){
        try{
            JSONObject json = new JSONObject(msg);
            Page page = new Page(json);
        }
        catch(Exception e){
            Log.d("MYAPP", "FAILED TO PARSE SOCKET PAGE UPDATE: " + e.getMessage());
        }
    }

    private void deletePage(String pageID){
        try{

        }catch(Exception e){
            Log.d("MYAPP","FAILED TO PARSE SOCKET PAGE JSON: " + e.getMessage());
        }
    }

    public void disconnect(){
        socket.disconnect();
    }

    public String getID(){ return id; }
}
