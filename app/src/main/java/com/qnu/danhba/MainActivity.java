package com.qnu.danhba;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText fullnameEditText;
    private EditText phoneEditText;
    private ImageButton saveButton;
    private ListView dataListView;

    private ArrayList<Contact> contactList;
    private ContactAdapter adapter;
    private DatabaseHelper databaseHelper;

    private int editingContactId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        fullnameEditText = findViewById(R.id.fullnameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        saveButton = findViewById(R.id.saveButton);
        dataListView = findViewById(R.id.dataListView);

        contactList = new ArrayList<>();
        adapter = new ContactAdapter(this, contactList);

        adapter.setOnDeleteClickListener(new ContactAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                showDeleteConfirmationDialog(position);
            }
        });

        adapter.setOnEditClickListener(new ContactAdapter.OnEditClickListener() {
            @Override
            public void onEditClick(int position) {
                Contact contact = contactList.get(position);
                editingContactId = contact.getId();
                openEditContactActivity(contact);
            }
        });

        dataListView.setAdapter(adapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullname = fullnameEditText.getText().toString();
                String phoneNumber = phoneEditText.getText().toString();

                if (!fullname.isEmpty() && isValidPhoneNumber(phoneNumber)) {
                    if (!isPhoneNumberExists(phoneNumber)) {
                        addContact(fullname, phoneNumber);
                    } else {
                        showToast("Phone number already exists.");
                    }

                    clearInputs();
                } else {
                    showToast("Invalid input. Please provide a valid fullname and a 10-digit phone number.");
                }
            }
        });

        loadContacts();
    }

    @SuppressLint("Range")
    private void loadContacts() {
        contactList.clear();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CONTACTS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                String fullname = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FULLNAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE));
                contactList.add(new Contact(id, fullname, phoneNumber));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        adapter.notifyDataSetChanged();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}");
    }

    private boolean isPhoneNumberExists(String phoneNumber) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CONTACTS, new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_PHONE + " = ?", new String[]{phoneNumber}, null, null, null);

        boolean exists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return exists;
    }

    private void addContact(String fullname, String phoneNumber) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_FULLNAME, fullname);
        values.put(DatabaseHelper.COLUMN_PHONE, phoneNumber);
        long newRowId = db.insert(DatabaseHelper.TABLE_CONTACTS, null, values);
        db.close();

        Contact newContact = new Contact((int) newRowId, fullname, phoneNumber);
        contactList.add(newContact);

        adapter.notifyDataSetChanged();
    }

    private void clearInputs() {
        fullnameEditText.setText("");
        phoneEditText.setText("");
    }

    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this contact?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteContact(position);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void deleteContact(int position) {
        Contact contact = contactList.get(position);
        int contactId = contact.getId();

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CONTACTS, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(contactId)});
        db.close();

        contactList.remove(position);
        adapter.notifyDataSetChanged();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openEditContactActivity(Contact contact) {
        Intent intent = new Intent(this, EditContactActivity.class);
        intent.putExtra("contactId", contact.getId());
        intent.putExtra("fullname", contact.getFullname());
        intent.putExtra("phoneNumber", contact.getPhoneNumber());
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadContacts();
        }
    }
}
