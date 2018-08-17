package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.NoteManager;


public abstract class HttpConnectionTask extends AsyncTask<String, Integer, String> {

    protected static final String baseURL = "https://enotes.site";//"http://10.0.2.2:8080";//
    protected static final String apiURL = "/api";
    protected static final String COOKIES_HEADER = "Set-Cookie";
    private static final int TIMEOUT = 5000; // ms

    protected HttpURLConnection connection;

    protected void connect(String urlStr, boolean doInput, boolean doOutput, String method){
        try {
            URL url = new URL(baseURL + urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setDoOutput(doOutput);
            connection.setDoInput(doInput);
            if (NoteManager.getCookies().getCookies().size() > 0) {
                connection.setRequestProperty("Cookie", TextUtils.join(";", NoteManager.getCookies().getCookies()));
            }
            connection.setRequestMethod(method);
        }
        catch(Exception e){
            NoteManager.sessionExpired("Lost connection to server.");
        }
    }

    protected void writeMessage(String msg){
        try {
            if (connection == null) {
                Log.d("ERROR", "No open connection");
                return;
            }

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(msg);
            out.close();
        }catch(IOException e){
            NoteManager.sessionExpired("Lost connection to server.");
        }
    }

    protected String readResponse(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            String response = "";
            while ((input = in.readLine()) != null) {
                response += input;
            }
            in.close();

            return response;
        }
        catch(IOException e){
            NoteManager.sessionExpired("Lost connection to server.");
            return null;
        }
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

    private void connectionLost(int responseCode){
        NoteManager.sessionExpired("Lost connection to server. (" + responseCode + ")");
    }
}
