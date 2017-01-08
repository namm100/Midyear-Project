package com.example.mannuccn.pictendance;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText tableName,studentFirstName,studentLastName,studentOsis;
    Button btnCreate,btnAdd,btnDelete,btnView;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tableName=(EditText)findViewById(R.id.tableName);
        studentFirstName=(EditText)findViewById(R.id.studentFirstName);
        studentLastName=(EditText)findViewById(R.id.studentLastName);
        studentOsis=(EditText)findViewById(R.id.studentOsis);
        btnCreate = (Button)findViewById(R.id.btnCreate);
        btnAdd=(Button)findViewById(R.id.btnAdd);
        btnDelete=(Button)findViewById(R.id.btnDelete);
        btnView=(Button)findViewById(R.id.btnView);
        btnAdd.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
        btnView.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

    }
    public void onClick(View view)
    {
        if(view == btnAdd)
        {
            if(studentOsis.getText().toString().trim().length()==0||
                    studentFirstName.getText().toString().trim().length()==0||
                    studentLastName.getText().toString().trim().length()==0)
            {
                showMessage("Error", "Please enter all values");
                return;
            }
            db.execSQL("INSERT INTO '"+tableName.getText()+"' VALUES('"+studentOsis.getText()+"','"+studentLastName.getText()+"','"+studentFirstName.getText()+"');");
            showMessage("Success", "Student Added");
            clearText();
        }
        if(view==btnDelete) {
            if (studentOsis.getText().toString().trim().length() == 0) {
                showMessage("Error", "Please enter Osis");
                return;
            }
            Cursor c = db.rawQuery("SELECT * FROM '"+tableName.getText()+"' WHERE osis='" + studentOsis.getText() + "'", null);
            if(c.moveToFirst())
            {
                db.execSQL("DELETE FROM '"+tableName.getText()+"' WHERE osis='"+studentOsis.getText()+"'");
                showMessage("Success", "Record Deleted");
            }
            else
            {
                showMessage("Error", "Invalid Student");
            }
            clearText();
        }
        if(view==btnView)
        {
            Cursor c=db.rawQuery("SELECT * FROM '"+tableName.getText()+"'", null);
            if(c.getCount()==0)
            {
                showMessage("Error", "No records found");
                return;
            }
            StringBuffer buffer=new StringBuffer();
            while(c.moveToNext())
            {
                buffer.append("OSIS: "+c.getString(0)+"\n");
                buffer.append("Last Name: "+c.getString(0)+"\n");
                buffer.append("First Name: "+c.getString(0)+"\n\n");

            }
            showMessage("Student Details", buffer.toString());
        }
        if(view==btnCreate) {
            if (tableName.getText().toString().trim().length() == 0) {
                showMessage("Error", "Please enter a table Name");
                return;
            }
            db=openOrCreateDatabase('"tableName.getText()+"', Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS '"+tableName.getText()+"'(osis VARCHAR,last  VARCHAR,first VARCHAR)");
            clearText();
        }

    }
    public void showMessage(String title,String message)
    {
        Builder builder=new Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
    public void clearText()
    {
        tableName.setText("");
        studentOsis.setText("");
        studentFirstName.setText("");
        studentLastName.setText("");
    }
}

}
