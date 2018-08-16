package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.NoteManager;


public abstract class ENotesTask extends AsyncTask<String, Integer, String> {

    protected static final String baseURL = "https://enotes.site";//"http://10.0.2.2:8080";//
    protected static final String apiURL = "/api";
    protected static final String COOKIES_HEADER = "Set-Cookie";

    protected HttpURLConnection connection;

    protected void connect(String urlStr, boolean doInput, boolean doOutput, String method) throws Exception{
        URL url = new URL(baseURL + urlStr);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(doOutput);
        connection.setDoInput(doInput);
        if(NoteManager.getCookies().getCookies().size() > 0){
            connection.setRequestProperty("Cookie", TextUtils.join(";", NoteManager.getCookies().getCookies()));
        }
        connection.setRequestMethod(method);
    }

    protected void writeMessage(String msg) throws IOException{
        if(connection == null){
            Log.d("ERROR","No open connection");
            return;
        }

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeBytes(msg);
        out.close();
    }

    protected String readResponse() throws IOException{
        DataInputStream in = new DataInputStream(connection.getInputStream());
        String input;
        String response = "";
        while ((input = in.readLine()) != null) {
            response += input;
        }
        in.close();

        return response;
    }

    protected void saveCookies(){
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                NoteManager.getCookies().add(null, HttpCookie.parse(cookie).get(0));
            }
        }
    }
}
