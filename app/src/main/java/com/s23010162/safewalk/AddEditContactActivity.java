package com.s23010162.safewalk;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditContactActivity extends AppCompatActivity {

    private EditText etContactName, etContactPhone, etContactRelation;
    private Button btnSaveContact;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);

        db = AppDatabase.getDatabase(this);
        etContactName = findViewById(R.id.etContactName);
        etContactPhone = findViewById(R.id.etContactPhone);
        etContactRelation = findViewById(R.id.etContactRelation);
        btnSaveContact = findViewById(R.id.btnSaveContact);

        btnSaveContact.setOnClickListener(v -> saveContact());
    }

    private void saveContact() {
        String name = etContactName.getText().toString().trim();
        String phone = etContactPhone.getText().toString().trim();
        String relation = etContactRelation.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        EmergencyContact contact = new EmergencyContact(name, phone, relation);

        new Thread(() -> {
            db.emergencyContactDao().insert(contact);
            runOnUiThread(() -> {
                Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
} 