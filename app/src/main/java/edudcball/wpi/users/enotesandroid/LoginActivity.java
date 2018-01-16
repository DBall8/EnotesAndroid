package edudcball.wpi.users.enotesandroid;

import android.content.Intent;
import android.content.SharedPreferences;
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

        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        String username = sp.getString("username", null);
        String password = sp.getString("password", null);
        if(username != null && password != null){
            login(username, password);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameAttempt = usernameField.getText().toString();
                String passwordAttempt = passwordField.getText().toString();
                messageText.setText("Logging in...");
                messageText.setVisibility(View.VISIBLE);

                login(usernameAttempt, passwordAttempt);


            }
        });
    }

    private void login(final String usernameAttempt, final String passwordAttempt){
        new LoginTask(me){

            @Override
            protected void onPostExecute(String result) {
                messageText.setText("");
                messageText.setVisibility(View.GONE);
                // this is executed on the main thread after the process is over
                // update your UI here
                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("successful")){
                        SharedPreferences sp = me.getSharedPreferences("Login", MODE_PRIVATE);
                        sp.edit().putString("username", usernameAttempt).putString("password", passwordAttempt).commit();
                        me.loginSuccess();
                    }
                    else{
                        messageText.setText("Login failed. Incorrect username or password.");
                        messageText.setVisibility(View.VISIBLE);
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Failed to parse JSON");
                }
            }
        }.execute(usernameAttempt, passwordAttempt);
    }


    private void loginSuccess(){
        startActivity(new Intent(this, MainActivity.class));
    }
}
