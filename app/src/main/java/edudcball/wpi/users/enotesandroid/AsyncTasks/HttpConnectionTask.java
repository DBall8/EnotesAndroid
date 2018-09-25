package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.NoteManager.NoteManager;
import edudcball.wpi.users.enotesandroid.Settings;


/**
 * An abstract class for managing communication with the enotes server
 * Overrides AsyncTask
 * Each subclass from this class should override onPostExecute to call the callback function
 */
public abstract class HttpConnectionTask extends AsyncTask<String, Integer, String> {

    // URL of the server
    protected static final String baseURL = Settings.baseURL;
    protected static final String apiURL = "/api"; // path that all note requests are sent to
    protected static final String COOKIES_HEADER = "Set-Cookie"; // header to look for new cookies to save
    private static final int TIMEOUT = 5000; // Timeout time for connection in milliseconds

    protected HttpURLConnection connection; // active http connection

    /**
     * Creates a connection with the server
     * @param urlStr the path to connect to
     * @param doInput true if the connection will expect a response
     * @param doOutput true if the app will send a message to the server
     * @param method HTTP method that will be used (GET, POST, PUT, or DELETE)
     */
    protected void connect(String urlStr, boolean doInput, boolean doOutput, String method) throws Exception{
        try {
            // connect
            URL url = new URL(baseURL + urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setDoOutput(doOutput);
            connection.setDoInput(doInput);

            // add any cookies that are saved
            if (NoteManager.getCookies().getCookies().size() > 0) {
                connection.setRequestProperty("Cookie", TextUtils.join(";", NoteManager.getCookies().getCookies()));
            }

            // set method
            connection.setRequestMethod(method);
        }
        catch(Exception e){
            throw e;
        }
    }

    /**
     * Sends a message to the server
     * @param msg the message to send
     */
    protected void writeMessage(String msg) throws Exception{
        try {
            // fail if connection isnt open
            if (connection == null) {
                Log.d("ERROR", "No open connection");
                throw new Exception("Lost connection to server.");
            }

            // Write message
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(msg);
            out.close();

        }catch(IOException e){
            throw e;
        }
    }

    /**
     * Reads a response from the server
     * @return the response body as a string
     */
    protected String readResponse() throws Exception{
        try {
            // read until the message is finished
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
            throw e;
        }
    }

    /**
     * Save cookies set by the server
     */
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
