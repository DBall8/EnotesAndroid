package edudcball.wpi.users.enotesandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import AsyncTasks.LoginTask;

/**
 * Created by Owner on 1/7/2018.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private Button loginButton;
    private TextView messageText;

    private LoginActivity me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameField = (EditText) findViewById(R.id.usernameField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        loginButton = (Button) findViewById(R.id.loginButton);
        messageText = (TextView) findViewById(R.id.messageText);

        me = this;

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameAttempt = usernameField.getText().toString();
                String passwordAttempt = passwordField.getText().toString();
                messageText.setText("Logging in...");
                messageText.setVisibility(View.VISIBLE);

                new LoginTask(me){

                    @Override
                    protected void onPostExecute(String result) {
                        // this is executed on the main thread after the process is over
                        // update your UI here
                        try{
                            JSONObject obj = new JSONObject(result);
                            if(obj.getBoolean("successful")){
                                String sessionID = obj.getString("sessionID");
                                me.loginSuccess(sessionID);
                            }
                        }
                        catch(Exception e){
                            Log.d("MYAPP", "Failed to parse JSON");
                        }
                    }
                }.execute(usernameAttempt, passwordAttempt);

            }
        });
    }

    private void loginSuccess(String sessionID){
        messageText.setText("");
        messageText.setVisibility(View.GONE);
        NoteManager.sessionID = sessionID;
        startActivity(new Intent(this, MainActivity.class));
    }
}
