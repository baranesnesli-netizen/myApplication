package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

public class DetailsActivity extends AppCompatActivity {

    private TextView studentDetailsTextView;
    private Student currentStudent;

    private final ActivityResultLauncher<Intent> editStudentLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Serializable updated = result.getData().getSerializableExtra(MainActivity.EXTRA_STUDENT);
                            if (updated instanceof Student) {
                                currentStudent = (Student) updated;
                                displayStudentDetails(currentStudent);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        studentDetailsTextView = findViewById(R.id.studentDetailsTextView);
        Button editButton = findViewById(R.id.editButton);
        Button sendEmailButton = findViewById(R.id.sendEmailButton);
        Button shareButton = findViewById(R.id.shareButton);

        Serializable received = getIntent().getSerializableExtra(MainActivity.EXTRA_STUDENT);
        if (received instanceof Student) {
            currentStudent = (Student) received;
            displayStudentDetails(currentStudent);
        } else {
            studentDetailsTextView.setText(getString(R.string.error_missing_student_data));
            editButton.setEnabled(false);
            sendEmailButton.setEnabled(false);
            shareButton.setEnabled(false);
            return;
        }

        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(DetailsActivity.this, EditActivity.class);
            editIntent.putExtra(MainActivity.EXTRA_STUDENT, currentStudent);
            editStudentLauncher.launch(editIntent);
        });

        sendEmailButton.setOnClickListener(v -> sendStudentEmail());

        shareButton.setOnClickListener(v -> shareStudentDetails());
    }

    private void displayStudentDetails(Student student) {
        studentDetailsTextView.setText(buildStudentSummary(student));
    }

    private String buildStudentSummary(Student student) {
        String notifications = student.isNotificationsEnabled()
                ? getString(R.string.result_notifications_yes)
                : getString(R.string.result_notifications_no);

        String terms = student.isTermsAccepted()
                ? getString(R.string.result_terms_yes)
                : getString(R.string.result_terms_no);

        return getString(
                R.string.student_summary_template,
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getGrade(),
                student.getMajor(),
                notifications,
                terms
        );
    }

    private void sendStudentEmail() {
        if (currentStudent == null) {
            Toast.makeText(this, getString(R.string.error_missing_student_data), Toast.LENGTH_SHORT).show();
            return;
        }

        String summary = buildStudentSummary(currentStudent);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{currentStudent.getEmail()});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, summary);

        try {
            Intent chooser = Intent.createChooser(emailIntent, getString(R.string.email_chooser_title));
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.error_no_email_app), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareStudentDetails() {
        if (currentStudent == null) {
            Toast.makeText(this, getString(R.string.error_missing_student_data), Toast.LENGTH_SHORT).show();
            return;
        }

        String summary = buildStudentSummary(currentStudent);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, summary);

        try {
            Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share_chooser_title));
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.error_no_share_app), Toast.LENGTH_SHORT).show();
        }
    }
}