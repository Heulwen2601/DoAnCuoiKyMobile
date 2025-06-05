package com.example.doancuoiky_mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doancuoiky_mobile.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, edtConfirmPassword;
    private Button signUpButton, btnLogin;
    private FirebaseAuth mAuth;
    // Sửa mDatabase thành DatabaseReference
    private DatabaseReference mDatabase;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://doancuoiky-0127-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        // Reference to Firebase Database

        // Link views
        emailEditText = findViewById(R.id.email_edit);
        passwordEditText = findViewById(R.id.password_edit);
        signUpButton = findViewById(R.id.signup_button);
        edtConfirmPassword = findViewById(R.id.confirm_password_edit);
        btnLogin = findViewById(R.id.login_button);

        // Sign-up button listener
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = edtConfirmPassword.getText().toString().trim();

                if (validateInput(email, password, confirmPassword)) {
                    checkIfEmailExistsAndRegister(email, password, confirmPassword); // Kiểm tra email trước khi tạo tài khoản
                }
            }
        });

        // Login button listener (optional)
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    // Validate input fields
    private boolean validateInput(String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match.");
            return false;
        }
        return true;
    }

    // Check if the email already exists in Firebase Auth
    private void checkIfEmailExistsAndRegister(String email, String password, String confirmPassword) {

        // Kiểm tra email đã đăng ký chưa
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                        if (task.isSuccessful()) {
                            // Kiểm tra xem email đã được đăng ký bằng password hay chưa
                            if (task.getResult().getSignInMethods().contains("password")) {
                                // Email đã có tài khoản
                                Toast.makeText(SignUpActivity.this, "Email này đã được đăng ký bởi tài khoản khác.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Tiến hành đăng ký tài khoản mới
                                createAccount(email, password); // Proceed with creating the account
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Kiểm tra email thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Create a new user in Firebase Auth and save user data to Firebase Database
    private void createAccount(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Thêm dữ liệu người dùng vào Firebase Database
                            if (user != null) {
                                User newUser = new User(email, "defaultName"); // Dữ liệu mẫu cho người dùng
                                mDatabase.child("users").child(user.getUid()).setValue(newUser)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Hiển thị thông báo thành công
                                                    Toast.makeText(SignUpActivity.this, "Tạo tài khoản thành công.", Toast.LENGTH_SHORT).show();

                                                    // Chuyển hướng đến màn hình đăng nhập
                                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class); // Giả sử màn hình đăng nhập là MainActivity
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Đảm bảo rằng màn hình đăng ký sẽ bị xóa
                                                    startActivity(intent);
                                                    finish(); // Đóng màn hình đăng ký
                                                } else {
                                                    Toast.makeText(SignUpActivity.this, "Lưu dữ liệu người dùng thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
