package edudcball.wpi.users.enotesandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import AsyncTasks.UpdateNoteTask;

/**
 * Created by Owner on 1/6/2018.
 */

public class NoteActivity extends AppCompatActivity {

    private EditText contentView;
    private String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        tag = getIntent().getStringExtra("Tag");
        String content = getIntent().getStringExtra("Content");
        contentView = (EditText) findViewById(R.id.noteText);
        contentView.setText(content);

        contentView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                Note n = NoteManager.getNote(tag);
                n.setContent(editable.toString());
                NoteManager.updateNote(tag);
            }
        });

    }
}
