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

import java.net.CookieManager;

import AsyncTasks.CreateUserTask;
import AsyncTasks.LoginTask;
import AsyncTasks.LogoutTask;

/**
 * Created by Owner on 1/7/2018.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private EditText passwordConfirm;
    private Button loginButton;
    private TextView messageText;
    private TextView otherLoginModeText;

    private Boolean newUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameField = (EditText) findViewById(R.id.usernameField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        passwordConfirm = (EditText) findViewById(R.id.passwordConfirmation);
        loginButton = (Button) findViewById(R.id.loginButton);
        messageText = (TextView) findViewById(R.id.messageText);
        otherLoginModeText = (TextView) findViewById(R.id.switchLoginType);

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

                if(newUser){
                    String passwordConfirmAttempt = passwordConfirm.getText().toString();
                    if(passwordAttempt.equals(passwordConfirmAttempt)){
                        createNewUser(usernameAttempt, passwordAttempt);
                    }
                    else{
                        messageText.setText("Passwords do not match.");
                        messageText.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    login(usernameAttempt, passwordAttempt);
                }



            }
        });

        otherLoginModeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchNewUser();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        messageText.setText("");
        messageText.setVisibility(View.GONE);
        if(newUser){
            switchNewUser();
        }
        loginButton.setEnabled(true);
    }


    private void login(final String usernameAttempt, final String passwordAttempt){

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        final AppCompatActivity me= this;

        new LoginTask(){

            @Override
            protected void onPostExecute(String result) {
                loginButton.setText("Login");
                loginButton.setEnabled(true);
                // this is executed on the main thread after the process is over
                // update your UI here
                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("successful")){
                        SharedPreferences sp = me.getSharedPreferences("Login", MODE_PRIVATE);
                        sp.edit().putString("username", usernameAttempt).putString("password", passwordAttempt).commit();
                        loginSuccess();
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

    private void createNewUser(final String usernameAttempt, final String passwordAttempt){

        loginButton.setEnabled(false);
        loginButton.setText("Creating account...");

        final AppCompatActivity me= this;

        new CreateUserTask(){

            @Override
            protected void onPostExecute(String result) {
                loginButton.setText("Create Account");
                loginButton.setEnabled(true);
                // this is executed on the main thread after the process is over
                // update your UI here
                try{
                    JSONObject obj = new JSONObject(result);
                    if(!obj.getBoolean("userAlreadyExists")){
                        SharedPreferences sp = me.getSharedPreferences("Login", MODE_PRIVATE);
                        sp.edit().putString("username", usernameAttempt).putString("password", passwordAttempt).commit();
                        loginSuccess();
                    }
                    else{
                        messageText.setText("User already exists, please choose a different username.");
                        messageText.setVisibility(View.VISIBLE);
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Failed to parse JSON");
                }
            }
        }.execute(usernameAttempt, passwordAttempt);
    }

    public static void logout(AppCompatActivity act){
        final AppCompatActivity activity = act;
        new LogoutTask(){

            @Override
            protected void onPostExecute(String result) {
                activity.startActivity(new Intent(activity, LoginActivity.class));
                NoteManager.cookies = new CookieManager();
                SharedPreferences sp = activity.getSharedPreferences("Login", MODE_PRIVATE);
                sp.edit().putString("username", null).putString("password", null).commit();
            }
        }.execute();

    }


    private void loginSuccess(){
        startActivity(new Intent(this, MainActivity.class));
    }

    private void switchNewUser(){
        if(newUser){
            passwordConfirm.setVisibility(View.GONE);
            loginButton.setText("Login");
            otherLoginModeText.setText(R.string.signUpSwitch);
            newUser = false;
        }
        else{
            passwordConfirm.setVisibility(View.VISIBLE);
            loginButton.setText("Create Account");
            otherLoginModeText.setText(R.string.loginSwitch);
            newUser = true;
        }
    }
}
