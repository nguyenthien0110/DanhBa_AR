package com.qnu.danhba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditContactActivity extends AppCompatActivity {

    private EditText editFullnameEditText;
    private EditText editPhoneEditText;
    private Button editSaveButton;

    private DatabaseHelper databaseHelper;

    private int contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_contact_layout);

        databaseHelper = new DatabaseHelper(this);

        editFullnameEditText = findViewById(R.id.editFullnameEditText);
        editPhoneEditText = findViewById(R.id.editPhoneEditText);
        editSaveButton = findViewById(R.id.editSaveButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            contactId = extras.getInt("contactId");
            String fullname = extras.getString("fullname");
            String phoneNumber = extras.getString("phoneNumber");

            editFullnameEditText.setText(fullname);
            editPhoneEditText.setText(phoneNumber);
        }

        editSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateContact();
            }
        });
    }

    private void updateContact() {
        String newFullname = editFullnameEditText.getText().toString();
        String newPhoneNumber = editPhoneEditText.getText().toString();

        if (!newFullname.isEmpty() && isValidPhoneNumber(newPhoneNumber)) {
            if (!isPhoneNumberExists(newPhoneNumber)) {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_FULLNAME, newFullname);
                values.put(DatabaseHelper.COLUMN_PHONE, newPhoneNumber);
                db.update(DatabaseHelper.TABLE_CONTACTS, values, DatabaseHelper.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(contactId)});
                db.close();

                setResult(RESULT_OK);
                finish();
            } else {
                showToast("Phone number already exists.");
            }
        } else {
            showToast("Invalid input. Please provide a valid fullname and a 10-digit phone number.");
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}");
    }

    private boolean isPhoneNumberExists(String phoneNumber) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_PHONE + " = ? AND " + DatabaseHelper.COLUMN_ID + " != ?";
        String[] selectionArgs = {phoneNumber, String.valueOf(contactId)};
        Cursor cursor = db.query(DatabaseHelper.TABLE_CONTACTS, new String[]{DatabaseHelper.COLUMN_ID},
                selection, selectionArgs, null, null, null);

        boolean exists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return exists;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
