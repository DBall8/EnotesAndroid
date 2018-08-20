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

import java.net.CookieManager;
import java.net.HttpCookie;

import edudcball.wpi.users.enotesandroid.AsyncTasks.CreateUserTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.LoginTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.LogoutTask;

/**
 * Class for running the Login screen of the application
 */

public class LoginActivity extends AppCompatActivity {

    // Screen fields and buttons
    private EditText usernameField;
    private EditText passwordField;
    private EditText passwordConfirm;
    private Button loginButton;
    private TextView messageText;
    private TextView otherLoginModeText;

    // True when in new user mode, false when in log in mode
    private Boolean newUser = false;

    /**
     * Runs when the login activity is first opened
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load layout
        setContentView(R.layout.activity_login);

        // find all the views
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        passwordConfirm = findViewById(R.id.passwordConfirmation);
        loginButton = findViewById(R.id.loginButton);
        messageText = findViewById(R.id.messageText);
        otherLoginModeText = findViewById(R.id.switchLoginType);

        // Set up button actions

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLoginClick();
            }
        });

        // this makes clicking on the text below the login fields switch the activity between
        // new user mode and login mode, where new user mode is for creating a new account
        // and login mode is for logging in to an existing account
        otherLoginModeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchNewUser();
            }
        });
    }

    /**
     * Gets called each time the user returns to the login screen
     */
    @Override
    protected void onResume(){
        super.onResume();

        // Wipe any saved session info
        NoteManager.resetCookies();
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        sp.edit().putString("session", null).commit();

        // Get any error message text that might have been provided if an error caused te
        String error = getIntent().getStringExtra("error");
        if(error != null){
            messageText.setText(error);
            messageText.setVisibility(View.VISIBLE);
        }
        else{
            messageText.setText("");
            messageText.setVisibility(View.GONE);
        }

        if(newUser){
            switchNewUser();
        }
        loginButton.setEnabled(true);
    }

    private void handleLoginClick(){
        // get username and password field values
        String usernameAttempt = usernameField.getText().toString();
        String passwordAttempt = passwordField.getText().toString();

        if(usernameAttempt.equals("")){
            displayError("Please enter a username.");
            return;
        }
        if(passwordAttempt.equals("")){
            displayError("Please enter a password.");
            return;
        }


        // if in new user mode, check that the confirm password matches the given password
        if(newUser){
            String passwordConfirmAttempt = passwordConfirm.getText().toString();
            if(passwordConfirmAttempt.equals("")){
                displayError("Please enter confirm your password.");
                return;
            }
            if(passwordAttempt.equals(passwordConfirmAttempt)){
                createNewUser(usernameAttempt, passwordAttempt);
            }
            else{
                displayError("Error: passwords do not match.");
            }
        }
        else{
            login(usernameAttempt, passwordAttempt);
        }
    }


    private void login(final String usernameAttempt, final String passwordAttempt){

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        final AppCompatActivity me= this;

        new LoginTask(usernameAttempt, passwordAttempt){

            @Override
            protected void onPostExecute(String result) {

                loginButton.setText("Login");
                loginButton.setEnabled(true);

                if(result == null) return;

                // this is executed on the main thread after the process is over
                // update your UI here
                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("successful")){
                        SharedPreferences sp = me.getSharedPreferences("Login", MODE_PRIVATE);
                        sp.edit().putString("session", NoteManager.getCookies().getCookies().get(0).toString()).commit();
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
        }.execute();
    }

    private void createNewUser(final String usernameAttempt, final String passwordAttempt){

        loginButton.setEnabled(false);
        loginButton.setText("Creating account...");

        final AppCompatActivity me= this;

        new CreateUserTask(usernameAttempt, passwordAttempt){

            @Override
            protected void onPostExecute(String result) {

                loginButton.setText("Create Account");
                loginButton.setEnabled(true);

                if(result == null) return;

                // this is executed on the main thread after the process is over
                // update your UI here
                try{
                    JSONObject obj = new JSONObject(result);
                    if(!obj.getBoolean("userAlreadyExists")){

                        SharedPreferences sp = me.getSharedPreferences("Login", MODE_PRIVATE);
                        sp.edit().putString("session", NoteManager.getCookies().getCookies().get(0).toString()).commit();
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

    private void displayError(String errorText){
        messageText.setText(errorText);
        messageText.setVisibility(View.VISIBLE);
    }

    private void hideError(){
        messageText.setVisibility(View.GONE);
    }
}
