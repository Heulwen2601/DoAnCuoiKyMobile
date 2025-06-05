package com.example.doancuoiky_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEdit, oldPasswordEdit, newPasswordEdit, refillPasswordEdit;
    private Button saveButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_page);

        // Firebase Auth instance
        auth = FirebaseAuth.getInstance();

        // References to UI components
        emailEdit = findViewById(R.id.email_edit);
        oldPasswordEdit = findViewById(R.id.old_password_edit);
        newPasswordEdit = findViewById(R.id.new_password_edit);
        refillPasswordEdit = findViewById(R.id.refill_password_edit);
        saveButton = findViewById(R.id.btnSaveNewPass);

        // Handle Save Button click
        saveButton.setOnClickListener(view -> handleChangePassword());
    }

    private void handleChangePassword() {
        String email = emailEdit.getText().toString().trim();
        String oldPassword = oldPasswordEdit.getText().toString().trim();
        String newPassword = newPasswordEdit.getText().toString().trim();
        String confirmPassword = refillPasswordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(oldPassword) ||
                TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase user re-authentication
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null && user.getEmail().equals(email)) {
            auth.signInWithEmailAndPassword(email, oldPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Update password
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                            // Chuyển về MainActivity sau khi cập nhật thành công
                            goToMainActivity();
                        } else {
                            Toast.makeText(this, "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated or email mismatch!", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm chuyển về MainActivity
    private void goToMainActivity() {
        Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Đảm bảo thoát tất cả Activity cũ
        startActivity(intent);
        finish(); // Đóng ChangePasswordPage
    }

}
