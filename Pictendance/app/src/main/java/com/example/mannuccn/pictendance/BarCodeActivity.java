package com.example.mannuccn.pictendance;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.app.AlertDialog.Builder;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import org.w3c.dom.Text;

import java.util.Calendar;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler, View.OnClickListener {

    private ZXingScannerView mScannerView;

    private Button selectBtn, setDateBtn, setTimeBtn, doneBtn;
    private EditText selectClassET, dateET, timeET;

    private TextView classTV, dateTV, timeTV;

    private SQLiteDatabase db;

    // INFO THAT MUST BE CHECKED IN ORDER TO SCAN
    private String classSelected;
    private int hour, minute;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        selectBtn = (Button) findViewById(R.id.selectClassBtn2);
        selectBtn.setOnClickListener(this);

        setDateBtn = (Button) findViewById(R.id.setDateBtn);
        setDateBtn.setOnClickListener(this);

        setTimeBtn = (Button) findViewById(R.id.setTimeBtn);
        setTimeBtn.setOnClickListener(this);

        doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(this);

        selectClassET = (EditText) findViewById(R.id.selectClassNameET);
        dateET = (EditText) findViewById(R.id.enterDateET);
        timeET = (EditText) findViewById(R.id.enterTimeET);

        classTV = (TextView) findViewById(R.id.selectedClassTV);
        dateTV = (TextView) findViewById(R.id.selectedDateTV);
        timeTV = (TextView) findViewById(R.id.selectedTimeTV);

        db = openOrCreateDatabase("Pictendance", Context.MODE_PRIVATE, null);
        classSelected = "";
        hour = -1; minute = -1;
        date = "";
    }


    public void onClick(View view)
    {

        db = openOrCreateDatabase("Pictendance", Context.MODE_PRIVATE, null);

        if (view == doneBtn) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }

        if (view == selectBtn) {
            // when the user selects the class they want to take attendance for
            String pendingSelection = selectClassET.getText().toString().replaceAll("\\s+","");
            if (pendingSelection.equals("")) {
                showToast("Please enter a class name",0);
                return;
            }
            if (doesClassExist(pendingSelection)) {
                classSelected = pendingSelection;
                showToast(classSelected + " selected.",0);
                classTV.setText(classSelected);
            } else {
                showToast("Class doesn't exist",0);
            }
        }

        if (view == setDateBtn) {
            // when the user sets the current date
            if (classSelected.equals("")) {
                showToast("Please select a class",0);
                return;
            }
            if (dateET.getText().toString().equals("")) {
                showToast("Please enter a time",0);
                return;
            }
            if (dateET.getText().toString().replaceAll("\\s+","").length() > 20) {
                showToast("Please enter a shorter string",0);
                return;
            }

            String pendingSelection = dateET.getText().toString().replaceAll("\\s+","");
            if (pendingSelection.matches("[0-9]+")) {
                showToast("Date shouldn't just contain numbers",0);
                return;
            }
            this.date = pendingSelection;
            dateTV.setText(this.date);

            Cursor c = db.query(classSelected,null,null,null,null,null,null);
            String[] colNames = c.getColumnNames();

            for (String i: colNames) {
                if (i.equals(this.date)) {
                    showToast("Info: Date column already made",0);
                    return;
                }
            }
            // add the column
            try {
                db.execSQL("ALTER TABLE " + classSelected + " ADD " + this.date + " VARCHAR(10);");
            } catch (SQLiteException e) {
                e.printStackTrace();
                showToast("No SQL reserved words",0);
                return;
            }

            showToast("Successfully added date",0);
        }

        if (view == setTimeBtn) {
            // when the user sets the time at which every student should be in class
            String entered = timeET.getText().toString();
            int colonIndex = -1;
            for (int i = 0; i < entered.length(); i++) {
                if (entered.charAt(i) == ':') {
                    colonIndex = i;
                }
            }
            if (colonIndex == -1 || colonIndex == entered.length() - 1) {
                showToast("Error, Enter valid time",0);
                return;
            }
            String hours = entered.substring(0, colonIndex);
            String min = entered.substring(colonIndex + 1, entered.length());
            int hoursI = parseIntE(hours);
            int minI = parseIntE(min);
            if ((minI >= 0 && minI < 60) && (hoursI >= 0 && hoursI < 24)) {
                showToast("Time Entered",0);
                this.hour = hoursI;
                this.minute = minI;
                timeTV.setText(this.hour + ":" + this.minute);
            } else {
                showToast("Error, Enter valid time",0);
            }
        }

    }

    public void QrScanner(View view) {

        // when scan is pressed

        if (this.classSelected.equals("")) {
            showToast("Select a class",0);
            return;
        }
        if (this.hour == -1 || this.minute == -1) {
            showToast("Enter a valid time",0);
            return;
        }
        if (this.date.equals("")) {
            showToast("Enter a date",0);
            return;
        }

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();


    }

    private void parseOSIS(String osis) {
        if (parseIntE(osis) == -1) {
            showToast("Not an osis. Scan Again",0);

        } else {
            int curr_hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int curr_minutes = Calendar.getInstance().get(Calendar.MINUTE);

            // check if OSIS is there
            db = openOrCreateDatabase("Pictendance", Context.MODE_PRIVATE, null);
            Cursor c = db.rawQuery("SELECT * FROM " + classSelected + " WHERE osis='" + osis + "';", null);

            if (c.getCount() == 1) {
                // TODO: UPDATE TABLE
                String mark = "";
                if (curr_hours < hour) {
                    mark = "Pres";
                } else if (curr_hours > hour) {
                    mark = "Late";
                } else {
                    if (curr_minutes <= minute) {
                        mark = "Pres";
                    } else {
                        mark = "Late";
                    }
                }
                // UPDATE THE SQL
                db.execSQL("UPDATE " + classSelected + " SET " + this.date + "='" + mark + "' WHERE osis='" + osis + "';");
                showToast("Marked " + osis + " as " + mark, 0);

            } else {
                showToast("Duplicate OSIS's, please fix using manage class screen",0);
            }
        }


        mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mScannerView != null) {
            mScannerView.stopCamera();           // Stop camera on pause
        }

    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        String osis = null;

        Log.e("handler", rawResult.getText()); // Prints scan results
        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)

        // show the scanner result into dialog box.
        if (rawResult.getText().length() == 9) {
            osis = rawResult.getText();
			// call another method to update the table
            parseOSIS(osis);
        } else {
            osis = "Please scan an osis number";
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Scan Result");
            builder.setMessage(osis);
            AlertDialog alert1 = builder.create();
            alert1.show();
            mScannerView.resumeCameraPreview(this);
        }

        // If you would like to resume scanning, call this method below:
        // mScannerView.resumeCameraPreview(this);
    }
	//mScannerView.stopCamera to stop camera
    public void showMessage(String title,String message) {
        Builder builder=new Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public boolean doesClassExist(String classN) {
        db = openOrCreateDatabase("Pictendance", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table';", null);
        while (c.moveToNext()) {
            if (c.getString(0).equals(classN)) {
                return true;
            }
        }
        return false;
    }

    public void showToast(String message, int length) {
        Toast t;
        if (length == 0)
            t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        else
            t = Toast.makeText(this,message,Toast.LENGTH_LONG);
        t.show();
    }

    private int parseIntE(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


}