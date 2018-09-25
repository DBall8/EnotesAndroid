package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edudcball.wpi.users.enotesandroid.NoteManager.NoteManager;
import edudcball.wpi.users.enotesandroid.Settings;

public class ConnectionTestTask extends AsyncTask<Void, Integer, Boolean> {

    private static final int TIMEOUT = 5000; // Timeout time for connection in milliseconds

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            URL url = new URL(Settings.baseURL + "/connectiontest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setDoOutput(false);
            connection.setDoInput(true);

            // add any cookies that are saved
            if (NoteManager.getCookies().getCookies().size() > 0) {
                connection.setRequestProperty("Cookie", TextUtils.join(";", NoteManager.getCookies().getCookies()));
            }

            // set method
            connection.setRequestMethod("GET");

            // read until the message is finished
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            String response = "";
            while ((input = in.readLine()) != null) {
                response += input;
            }
            in.close();

            return response.equals("true");
        }
        catch(Exception e){
            Log.d("MYAPP", "Connection test failed: " + e.getMessage().toString());
            return false;
        }
    }
}
