package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_STUDENT = "extra_student";

    private static final String PREFS_NAME = "student_registration";

    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_GRADE = "grade";
    private static final String KEY_MAJOR = "major";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_TERMS = "terms";

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;

    private RadioGroup gradeRadioGroup;
    private Spinner majorSpinner;

    private CheckBox notificationsCheckBox;
    private CheckBox termsCheckBox;

    private TextView resultTextView;
    private TextView gradeErrorTextView;
    private TextView majorErrorTextView;
    private TextView termsErrorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);

        gradeRadioGroup = findViewById(R.id.gradeRadioGroup);
        majorSpinner = findViewById(R.id.majorSpinner);

        notificationsCheckBox = findViewById(R.id.notificationsCheckBox);
        termsCheckBox = findViewById(R.id.termsCheckBox);

        resultTextView = findViewById(R.id.resultTextView);
        gradeErrorTextView = findViewById(R.id.gradeErrorTextView);
        majorErrorTextView = findViewById(R.id.majorErrorTextView);
        termsErrorTextView = findViewById(R.id.termsErrorTextView);

        Button submitButton = findViewById(R.id.submitButton);

        setupLiveErrorClearing();

        Student savedStudent = loadStudent();
        if (savedStudent != null) {
            resultTextView.setText(formatStudent(savedStudent, true));
            resultTextView.setVisibility(View.VISIBLE);
        } else {
            resultTextView.setVisibility(View.GONE);
        }

        submitButton.setOnClickListener(v -> registerStudent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Student savedStudent = loadStudent();
        if (savedStudent != null) {
            resultTextView.setText(formatStudent(savedStudent, true));
            resultTextView.setVisibility(View.VISIBLE);
        }
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

    private void registerStudent() {
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

        Student student = new Student(
                firstName,
                lastName,
                email,
                grade,
                major,
                notificationsCheckBox.isChecked(),
                termsCheckBox.isChecked()
        );

        saveStudent(student);

        resultTextView.setText(formatStudent(student, false));
        resultTextView.setVisibility(View.VISIBLE);

        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra(EXTRA_STUDENT, student);
        startActivity(intent);
    }

    private void clearErrors() {
        firstNameEditText.setError(null);
        lastNameEditText.setError(null);
        emailEditText.setError(null);

        gradeErrorTextView.setVisibility(View.GONE);
        majorErrorTextView.setVisibility(View.GONE);
        termsErrorTextView.setVisibility(View.GONE);
    }

    public static void saveStudentStatic(AppCompatActivity activity, Student student) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_FIRST_NAME, student.getFirstName())
                .putString(KEY_LAST_NAME, student.getLastName())
                .putString(KEY_EMAIL, student.getEmail())
                .putString(KEY_GRADE, student.getGrade())
                .putString(KEY_MAJOR, student.getMajor())
                .putBoolean(KEY_NOTIFICATIONS, student.isNotificationsEnabled())
                .putBoolean(KEY_TERMS, student.isTermsAccepted())
                .apply();
    }

    private void saveStudent(Student student) {
        saveStudentStatic(this, student);
    }

    private Student loadStudent() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!prefs.contains(KEY_FIRST_NAME)) {
            return null;
        }

        return new Student(
                prefs.getString(KEY_FIRST_NAME, ""),
                prefs.getString(KEY_LAST_NAME, ""),
                prefs.getString(KEY_EMAIL, ""),
                prefs.getString(KEY_GRADE, ""),
                prefs.getString(KEY_MAJOR, ""),
                prefs.getBoolean(KEY_NOTIFICATIONS, false),
                prefs.getBoolean(KEY_TERMS, false)
        );
    }

    private String formatStudent(Student student, boolean saved) {
        String heading = saved ? getString(R.string.saved_title) : getString(R.string.result_title);

        String notifications = student.isNotificationsEnabled()
                ? getString(R.string.result_notifications_yes)
                : getString(R.string.result_notifications_no);

        String terms = student.isTermsAccepted()
                ? getString(R.string.result_terms_yes)
                : getString(R.string.result_terms_no);

        return heading + "\n\n"
                + getString(R.string.result_first_name, student.getFirstName()) + "\n"
                + getString(R.string.result_last_name, student.getLastName()) + "\n"
                + getString(R.string.result_email, student.getEmail()) + "\n"
                + getString(R.string.result_grade, student.getGrade()) + "\n"
                + getString(R.string.result_major, student.getMajor()) + "\n"
                + notifications + "\n"
                + terms;
    }
}