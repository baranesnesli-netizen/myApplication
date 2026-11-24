package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

public class EditActivity extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;

    private RadioGroup gradeRadioGroup;
    private RadioButton grade10RadioButton;
    private RadioButton grade11RadioButton;
    private RadioButton grade12RadioButton;

    private Spinner majorSpinner;

    private CheckBox notificationsCheckBox;
    private CheckBox termsCheckBox;

    private TextView gradeErrorTextView;
    private TextView majorErrorTextView;
    private TextView termsErrorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        firstNameEditText = findViewById(R.id.editFirstNameEditText);
        lastNameEditText = findViewById(R.id.editLastNameEditText);
        emailEditText = findViewById(R.id.editEmailEditText);

        gradeRadioGroup = findViewById(R.id.editGradeRadioGroup);
        grade10RadioButton = findViewById(R.id.editGrade10RadioButton);
        grade11RadioButton = findViewById(R.id.editGrade11RadioButton);
        grade12RadioButton = findViewById(R.id.editGrade12RadioButton);

        majorSpinner = findViewById(R.id.editMajorSpinner);

        notificationsCheckBox = findViewById(R.id.editNotificationsCheckBox);
        termsCheckBox = findViewById(R.id.editTermsCheckBox);

        gradeErrorTextView = findViewById(R.id.editGradeErrorTextView);
        majorErrorTextView = findViewById(R.id.editMajorErrorTextView);
        termsErrorTextView = findViewById(R.id.editTermsErrorTextView);

        Button saveAndReturnButton = findViewById(R.id.saveAndReturnButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        Serializable received = getIntent().getSerializableExtra(MainActivity.EXTRA_STUDENT);
        if (received instanceof Student) {
            populateFields((Student) received);
        }

        setupLiveErrorClearing();

        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        saveAndReturnButton.setOnClickListener(v -> saveAndReturn());
    }

    private void populateFields(Student student) {
        firstNameEditText.setText(student.getFirstName());
        lastNameEditText.setText(student.getLastName());
        emailEditText.setText(student.getEmail());

        String grade = student.getGrade();
        if (getString(R.string.grade_10).equals(grade)) {
            grade10RadioButton.setChecked(true);
        } else if (getString(R.string.grade_11).equals(grade)) {
            grade11RadioButton.setChecked(true);
        } else if (getString(R.string.grade_12).equals(grade)) {
            grade12RadioButton.setChecked(true);
        }

        String major = student.getMajor();
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) majorSpinner.getAdapter();
        if (adapter != null && major != null) {
            for (int i = 1; i < adapter.getCount(); i++) {
                Object item = adapter.getItem(i);
                if (item != null && major.equals(item.toString())) {
                    majorSpinner.setSelection(i);
                    break;
                }
            }
        }

        notificationsCheckBox.setChecked(student.isNotificationsEnabled());
        termsCheckBox.setChecked(student.isTermsAccepted());
    }

    private void setupLiveErrorClearing() {
        firstNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    firstNameEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        lastNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    lastNameEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String trimmed = s.toString().trim();
                if (!TextUtils.isEmpty(trimmed) && Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
                    emailEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        gradeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                gradeErrorTextView.setVisibility(View.GONE);
            }
        });

        majorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    majorErrorTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        termsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                termsErrorTextView.setVisibility(View.GONE);
            }
        });
    }

    private void saveAndReturn() {
        clearErrors();

        boolean valid = true;

        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            firstNameEditText.setError(getString(R.string.error_first_name));
            valid = false;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameEditText.setError(getString(R.string.error_last_name));
            valid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_email_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.error_email_invalid));
            valid = false;
        }

        int selectedGradeId = gradeRadioGroup.getCheckedRadioButtonId();
        String grade = "";

        if (selectedGradeId == -1) {
            gradeErrorTextView.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            RadioButton selectedGrade = findViewById(selectedGradeId);
            grade = selectedGrade.getText().toString();
        }

        int majorPosition = majorSpinner.getSelectedItemPosition();
        String major = "";

        if (majorPosition <= 0) {
            majorErrorTextView.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            major = majorSpinner.getSelectedItem().toString();
        }

        if (!termsCheckBox.isChecked()) {
            termsErrorTextView.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (!valid) {
            return;
        }

        Student updatedStudent = new Student(
                firstName,
                lastName,
                email,
                grade,
                major,
                notificationsCheckBox.isChecked(),
                termsCheckBox.isChecked()
        );

        MainActivity.saveStudentStatic(this, updatedStudent);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.EXTRA_STUDENT, updatedStudent);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void clearErrors() {
        firstNameEditText.setError(null);
        lastNameEditText.setError(null);
        emailEditText.setError(null);

        gradeErrorTextView.setVisibility(View.GONE);
        majorErrorTextView.setVisibility(View.GONE);
        termsErrorTextView.setVisibility(View.GONE);
    }
}