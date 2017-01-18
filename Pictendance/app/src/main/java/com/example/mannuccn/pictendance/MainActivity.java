package com.example.mannuccn.pictendance;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private SQLiteDatabase db;

    private Button createBtn, selectBtn, addBtn, deleteBtn, viewFileBtn, takeAttendanceBtn, viewClassListBtn;
    private EditText createClassET, selectClassET, lastNameET, firstNameET, osisET, enterClassET;
    private TextView classSelectedTV;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA= 1;
    private static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 2;

    private String classSelected;
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setTitle("Manage Classes");

        this.createBtn = (Button) findViewById(R.id.createClassBtn);
        this.createBtn.setOnClickListener(this);

        this.selectBtn = (Button) findViewById(R.id.selectClassBtn);
        this.selectBtn.setOnClickListener(this);

        this.addBtn = (Button) findViewById(R.id.addBtn);
        this.addBtn.setOnClickListener(this);

        this.deleteBtn = (Button) findViewById(R.id.deleteBtn);
        this.deleteBtn.setOnClickListener(this);

        this.viewFileBtn = (Button) findViewById(R.id.viewFileBtn);
        this.viewFileBtn.setOnClickListener(this);

        this.takeAttendanceBtn = (Button) findViewById(R.id.takeAttendanceBtn);
        this.takeAttendanceBtn.setOnClickListener(this);

        this.viewClassListBtn = (Button) findViewById(R.id.classListBtn);
        this.viewClassListBtn.setOnClickListener(this);

        this.createClassET = (EditText) findViewById(R.id.createClassNameET);
        this.selectClassET = (EditText) findViewById(R.id.classNameSelectET);
        this.lastNameET = (EditText) findViewById(R.id.lastNameET);
        this.firstNameET = (EditText) findViewById(R.id.firstNameET);
        this.osisET = (EditText) findViewById(R.id.osisET);
        this.enterClassET = (EditText) findViewById(R.id.enterClassET);

        this.classSelectedTV = (TextView) findViewById(R.id.selectedClassTV);
        this.classSelectedTV.setText("Class Selected: ");

        createClassET.setText("");
        selectClassET.setText("");
        lastNameET.setText("");
        firstNameET.setText("");
        osisET.setText("");
        enterClassET.setText("");


        //request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
            }
        }

        db = openOrCreateDatabase("Pictendance", Context.MODE_PRIVATE, null);
        classSelected = "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {}
                return;
            }
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {}
                return;
            }
        }
    }

    public void onClick(View view) {

        db = openOrCreateDatabase("Pictendance", Context.MODE_PRIVATE, null);

        if (view == takeAttendanceBtn) {
            Intent i = new Intent(this, BarCodeActivity.class);
            startActivity(i);
        }

        if (view == createBtn) {
            // when a new class is being created
            String newClassName = createClassET.getText().toString().replaceAll("\\s+","");
            if (newClassName.equals("")) {
                showToast("Please enter a name", 0);
                return;
            }
            if (doesClassExist(newClassName)) {
                showToast("Name already taken",0);
                System.out.print("ALREADY TAKEN");
                return;
            }

            db.execSQL("CREATE TABLE IF NOT EXISTS " + newClassName +
                "(osis VARCHAR(11), lastName VARCHAR(50), firstName VARCHAR(50));");
            showToast("Successfully created " + newClassName + "!",0);
            classSelected = newClassName;
            classSelectedTV.setText("Class Selected: " + classSelected);
            createClassET.setText("");
        }

        if (view == selectBtn) {
            // when a class is being selected
            String pendingSelection = selectClassET.getText().toString().replaceAll("\\s+","");
            if (pendingSelection.equals("")) {
                showToast("Please enter a class name",0);
                return;
            }
            if (doesClassExist(pendingSelection)) {
                classSelected = pendingSelection;
                classSelectedTV.setText("Class Selected: " + classSelected);
                selectClassET.setText("");
                showToast(classSelected + " selected.",0);
            } else {
                showToast("Class doesn't exist",0);
            }

        }

        if (view == addBtn) {
            if (classSelected.equals("")) {
                showToast("Please select a class",0);
                return;
            }
            // when a student is added
            // check first name, last name, osis
            String enteredOsis = osisET.getText().toString();
            String enteredLastName = lastNameET.getText().toString();
            String enteredFirstName = firstNameET.getText().toString();
            if (enteredOsis.equals("") || enteredFirstName.equals("") || enteredLastName.equals("")) {
                showToast("Plese enter all fields",0);
                return;
            }
            if (enteredFirstName.length() > 45 || enteredLastName.length() > 45) {
                showToast("Fields are too long. Please shorten",0);
                return;
            }
            if (enteredOsis.length() != 9) {
                showToast("OSIS must be 9 digits long",0);
                return;
            }
            int osis = parseIntE(enteredOsis);
            if (osis == -1) {
                showToast("Please enter a valid osis",0);
                return;
            }
            db.execSQL("INSERT INTO " + classSelected + " (osis, lastName, firstName) values('" + osis + "','" + enteredLastName + "','" + enteredFirstName + "');");
            showToast("Succsess, " + enteredFirstName + " " + enteredLastName + " " + osis + " has been entered",0);
            clearText();
        }

        if (view == deleteBtn) {
            if (classSelected.equals("")) {
                showToast("Please select a class",0);
                return;
            }
            // when a student is being deleted
            String enteredOsis = osisET.getText().toString();
            String enteredLastName = lastNameET.getText().toString();
            String enteredFirstName = firstNameET.getText().toString();
            if (enteredOsis.equals("") || enteredFirstName.equals("") || enteredLastName.equals("")) {
                showToast("Plese enter all fields",0);
                return;
            }
            if (enteredFirstName.length() > 45 || enteredLastName.length() > 45) {
                showToast("Fields are too long. Please shorten",0);
                return;
            }
            if (enteredOsis.length() != 9) {
                showToast("OSIS must be 9 digits long",0);
                return;
            }
            int osis = parseIntE(enteredOsis);
            if (osis == -1) {
                showToast("Please enter a valid osis",0);
                return;
            }
            // find if student exists
            // if exists, delete it
            Cursor c = db.rawQuery("SELECT * FROM " + classSelected + " WHERE osis='" + osis+"';", null); // TODO RESUME EHRE
            if (c.getCount() == 1) {
                db.execSQL("DELETE FROM " + classSelected + " WHERE osis='" + osis + "';");
                showToast("Success. student deleted",0);
                clearText();
                return;
            }
            while (c.moveToNext()) {
                if (enteredLastName.equals(c.getString(c.getColumnIndex("lastName")))) {
                    db.execSQL("DELETE FROM " + classSelected + " WHERE osis='" + osis + "' AND lastName='" + enteredLastName + "';");
                    showToast("Success. student deleted",0);
                    clearText();
                    return;
                }
            }
            showToast("Error: Correct fields or Student not found",0);
        }

        if (view == viewClassListBtn) {
            // when the user requests to see a list of classes
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table';", null);
            if (c.getCount() == 0 || c.getCount() == 1) {
                showToast("No Classes Found",0);
                return;
            }
            StringBuffer buffer = new StringBuffer();
            c.moveToNext();
            while (c.moveToNext()) {
                buffer.append(c.getString(0) + "\n");
                buffer.append("\n");
            }
            showMessage("Class List", buffer.toString());

        }

        if (view == viewFileBtn) {
            // when the user requests to see a classes files
            if (enterClassET.getText().toString().equals("")) {
                showToast("Please enter a class",0);
                return;
            }
            String classEntered = enterClassET.getText().toString().replaceAll("\\s+","");
            if (!doesClassExist(classEntered)) {
                showToast("Class entered doesn't exist",0);
                enterClassET.setText("");
                return;
            }
            // class exists
            Cursor c = db.query(classEntered,null,null,null,null,null,null);
            String[] colNames = c.getColumnNames();

            //TODO:TEMPORARY START
            StringBuffer buffer = new StringBuffer();
            for (String i: colNames) {
                buffer.append(i + "\n");
                buffer.append("\n");
            }
            showMessage(classEntered + "'s Columns", buffer.toString());
            //TODO:TEMPORARY END

        }

    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }
    private int parseIntE(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    public void showMessage(String title, String message)
    {
        Builder builder=new Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void showToast(String message, int length) {
        Toast t;
        if (length == 0)
            t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        else
            t = Toast.makeText(this,message,Toast.LENGTH_LONG);
        t.show();
    }

    private boolean doesClassExist(String classN) {
        db = openOrCreateDatabase("Pictendance", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table';", null);
        while (c.moveToNext()) {
            if (c.getString(0).equals(classN)) {
                return true;
            }
        }
        return false;
    }

    public void clearText()
    {
        osisET.setText("");
        lastNameET.setText("");
        firstNameET.setText("");
    }
}