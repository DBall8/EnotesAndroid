package edudcball.wpi.users.enotesandroid;

import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketConnection {

    private static final String URL = "http://10.0.2.2:8080";//"https://enotes.site";

    private Socket socket;
    private String id = null;

    public SocketConnection(){
        try {
            socket = IO.socket(URL);

            socket.on("ready", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    id = (String)args[0];
                    Log.d("MYAPP", "SOCKET ID: " + id);
                }
            });

            //socket.on("create");

            socket.connect();

        } catch (Exception e) {
            Log.d("MYAPP", "Socket failed to connect. " + e.getMessage());
        }
    }
}
