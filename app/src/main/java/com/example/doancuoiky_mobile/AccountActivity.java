package com.example.doancuoiky_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        TextView userinfo = findViewById(R.id.user_info);

        // Lấy instance của FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Lấy người dùng hiện tại
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            userinfo.setText(email);
        } else {
            // Người dùng chưa đăng nhập
            userinfo.setText("No user is logged in.");
        }

        // Tìm các nút theo ID
        ImageButton homeButton = findViewById(R.id.home_button);
        Button btnResetPass = findViewById(R.id.btnResetPass);
        Button logout = findViewById(R.id.btnLogout);
        ImageButton accountButton = findViewById(R.id.account_button);

        // Thiết lập sự kiện onClick cho nút homeButton
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển hướng tới Activity mới (FolderActivity)
                Intent intent = new Intent(AccountActivity.this, FolderActivity.class);
                startActivity(intent);
            }
        });

        // Thiết lập sự kiện onClick cho nút btnResetPass
        btnResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển hướng tới Activity mới (ChangePasswordPage)
                Intent intent = new Intent(AccountActivity.this, ChangePasswordPage.class);
                startActivity(intent);
            }
        });

        // Thiết lập sự kiện onClick cho nút btnLogout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đăng xuất người dùng
                FirebaseAuth.getInstance().signOut();

                // Chuyển hướng về trang đăng nhập
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Kết thúc Activity hiện tại để không quay lại trang AccountActivity
            }
        });
    }
}
