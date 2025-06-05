package com.example.doancuoiky_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyPage extends AppCompatActivity {

    private EditText editTextNumber1, editTextNumber2, editTextNumber3, editTextNumber4;
    private Button btnVerify;
    private String generatedOTP; // Mã OTP được truyền từ Intent
    private String email;        // Email được truyền từ Intent
    private String newPassword;  // Mật khẩu mới được truyền từ Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_page);

        // Lấy giá trị từ Intent
        Intent intent = getIntent();
        generatedOTP = intent.getStringExtra("generatedOTP");
        email = intent.getStringExtra("email");
        newPassword = intent.getStringExtra("newPassword");

        // Tham chiếu đến các thành phần UI
        editTextNumber1 = findViewById(R.id.editTextNumber);
        editTextNumber2 = findViewById(R.id.editTextNumber2);
        editTextNumber3 = findViewById(R.id.editTextNumber3);
        editTextNumber4 = findViewById(R.id.editTextNumber4);
        btnVerify = findViewById(R.id.btnVerify);

        // Gắn sự kiện click cho nút Verify
        btnVerify.setOnClickListener(view -> handleVerifyOTP());
    }

    private void handleVerifyOTP() {
        // Lấy OTP từ input người dùng
        String inputOTP = editTextNumber1.getText().toString() +
                editTextNumber2.getText().toString() +
                editTextNumber3.getText().toString() +
                editTextNumber4.getText().toString();

        // Kiểm tra OTP hợp lệ
        if (TextUtils.isEmpty(inputOTP) || inputOTP.length() < 4) {
            Toast.makeText(this, "Please enter the full OTP.", Toast.LENGTH_SHORT).show();
            return;
        }

        // So sánh OTP nhập vào với OTP đã gửi
        if (inputOTP.equals(generatedOTP)) {
            Toast.makeText(this, "OTP verified successfully!", Toast.LENGTH_SHORT).show();
            // Thay đổi mật khẩu
            updatePassword();
        } else {
            Toast.makeText(this, "Incorrect OTP, please try again.", Toast.LENGTH_SHORT).show();
            // Reset các trường nhập OTP
            resetOTPFields();
        }
    }

    private void resetOTPFields() {
        editTextNumber1.setText("");
        editTextNumber2.setText("");
        editTextNumber3.setText("");
        editTextNumber4.setText("");
    }

    private void updatePassword() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Kiểm tra nếu user hiện tại không null và email khớp
        if (user != null && user.getEmail().equals(email)) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                            // Chuyển về màn hình chính
                            navigateToMainActivity();
                        } else {
                            Toast.makeText(this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not authenticated or email mismatch!", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(VerifyPage.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
