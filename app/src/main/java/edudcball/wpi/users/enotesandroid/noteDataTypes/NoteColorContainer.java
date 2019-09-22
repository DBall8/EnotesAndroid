package edudcball.wpi.users.enotesandroid.noteDataTypes;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.R;

class NoteColorContainer {
    JSONObject jsonVal;
    int intVal;
    int orderVal;
    String strVal;

    NoteColorContainer(Context context, NoteLookupTable.NoteColor color){
        switch(color){
            case YELLOW:
                jsonVal = NoteLookupTable.makeColorJSON("#ddaf00", "#ffe062");
                intVal = ContextCompat.getColor(context, R.color.noteYellow);
                strVal = context.getResources().getString(R.string.yellow);
                orderVal = 2;
                break;
            case ORANGE:
                jsonVal = NoteLookupTable.makeColorJSON("#e88a19", "#ffa63d");
                intVal = ContextCompat.getColor(context, R.color.noteOrange);
                strVal = context.getResources().getString(R.string.orange);
                orderVal = 1;
                break;
            case RED:
                jsonVal = NoteLookupTable.makeColorJSON("#f15656", "#f97e7e");
                intVal = ContextCompat.getColor(context, R.color.noteRed);
                strVal = context.getResources().getString(R.string.red);
                orderVal = 0;
                break;
            case GREEN:
                jsonVal = NoteLookupTable.makeColorJSON("#1ea723", "#53ce57");
                intVal = ContextCompat.getColor(context, R.color.noteGreen);
                strVal = context.getResources().getString(R.string.green);
                orderVal = 3;
                break;
            case BLUE:
                jsonVal = NoteLookupTable.makeColorJSON("#43a5ec", "#7fc8f5");
                intVal = ContextCompat.getColor(context, R.color.noteBlue);
                strVal = context.getResources().getString(R.string.blue);
                orderVal = 4;
                break;
            case PURPLE:
                jsonVal = NoteLookupTable.makeColorJSON("#b34ace", "#e083f7");
                intVal = ContextCompat.getColor(context, R.color.notePurple);
                strVal = context.getResources().getString(R.string.purple);
                orderVal = 5;
                break;
        }
    }
}
