package AsyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.LoginActivity;
import edudcball.wpi.users.enotesandroid.NetInfo;
import edudcball.wpi.users.enotesandroid.NoteManager;

/**
 * Created by Owner on 1/17/2018.
 */

public abstract class CreateUserTask extends AsyncTask<String, Integer, String> {

    static final String COOKIES_HEADER = "Set-Cookie";

    @Override
    protected String doInBackground(String... vals) {
        try{
            URL url = new URL(NetInfo.baseURL + "/newuser");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");

            JSONObject msg = new JSONObject();
            msg.put("username", vals[0]);
            msg.put("password", vals[1]);
            msg.put("stayLoggedIn", false);


            // write the message
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(msg.toString());
            out.close();

            Map<String, List<String>> headerFields = connection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    NoteManager.cookies.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }


            DataInputStream in = new DataInputStream(connection.getInputStream());
            String input;
            String response = "";
            while ((input = in.readLine()) != null) {
                //input = input.replace("null", "");
                response += input;
            }
            in.close();
            Log.d("MYAPP", response);

            connection.disconnect();

            return response;

        }catch(Exception e){
            Log.d("MYAPP", "UH OH " + e.toString());
            return null;
        }
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected abstract void onPostExecute(String result);
}
