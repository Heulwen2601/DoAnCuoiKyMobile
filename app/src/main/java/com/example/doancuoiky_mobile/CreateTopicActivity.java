package com.example.doancuoiky_mobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doancuoiky_mobile.model.Topic;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CreateTopicActivity extends AppCompatActivity {

    private EditText topicName, topicDescription;
    private RadioGroup privacyRadioGroup;
    private RadioButton radioPublic, radioPrivate;
    private Button saveTopicButton, cancelTopicButton;
    private String folderName; // Nhận từ Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_topic);

        // Ánh xạ view
        topicName = findViewById(R.id.topicName);
        topicDescription = findViewById(R.id.topicDescription);
        privacyRadioGroup = findViewById(R.id.privacyRadioGroup);
        saveTopicButton = findViewById(R.id.saveTopicButton);
        cancelTopicButton = findViewById(R.id.cancelTopicButton);

        // Ánh xạ đúng các RadioButton
        radioPublic = findViewById(R.id.radioPublic);
        radioPrivate = findViewById(R.id.radioPrivate);

        // Kiểm tra xem các RadioButton đã được ánh xạ thành công chưa
        if (radioPublic == null || radioPrivate == null) {
            Toast.makeText(this, "Lỗi ánh xạ các RadioButton", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xử lý sự kiện cho radioPublic
        radioPublic.setOnClickListener(v -> {
            if (radioPublic.isChecked()) {
                radioPrivate.setChecked(false);
            }
        });

        // Xử lý sự kiện cho radioPrivate
        radioPrivate.setOnClickListener(v -> {
            if (radioPrivate.isChecked()) {
                radioPublic.setChecked(false);
            }
        });

        // Lấy folderName từ Intent
        Intent intent = getIntent();
        folderName = intent.getStringExtra("folderName");



        // Nút Lưu
        saveTopicButton.setOnClickListener(v -> saveTopicToFirestore());

        // Nút Hủy
        cancelTopicButton.setOnClickListener(v -> finish()); // Quay lại Activity trước đó
    }


    private void saveTopicToFirestore() {
        String name = topicName.getText().toString().trim();
        String description = "Chưa có từ vựng.";
        String privacy = null;

        // Lấy giá trị từ RadioButton
        if (radioPublic.isChecked()) {
            privacy = radioPublic.getText().toString();
        } else if (radioPrivate.isChecked()) {
            privacy = radioPrivate.getText().toString();
        }

        // Kiểm tra thông tin
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(privacy)) {
            showDialog("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // Lấy Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (TextUtils.isEmpty(folderName)) {
            // Trường hợp không có folderName
            Topic newTopic = new Topic(name, description, privacy, null, null);
            db.collection("topics")
                    .add(newTopic)
                    .addOnSuccessListener(documentReference -> {
                        showDialog("Đã tạo topic mới thành công.", true);
                    })
                    .addOnFailureListener(e -> {
                        showDialog("Có lỗi xảy ra: " + e.getMessage());
                    });
        } else {
            // Trường hợp có folderName, tìm folderId để lưu vào đúng folder
            String finalPrivacy = privacy;
            db.collection("folders")
                    .whereEqualTo("name", folderName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot folderDoc = task.getResult().getDocuments().get(0);
                            String folderId = folderDoc.getId();

                            // Tạo topic mới
                            Topic newTopic = new Topic(name, description, finalPrivacy, null, null);

                            // Lưu topic vào collection "topics" trong folder này
                            db.collection("folders")
                                    .document(folderId)
                                    .collection("topics")
                                    .add(newTopic)
                                    .addOnSuccessListener(documentReference -> {
                                        showDialog("Đã tạo mới thành công.", true);
                                    })
                                    .addOnFailureListener(e -> {
                                        showDialog("Có lỗi xảy ra: " + e.getMessage());
                                    });

                        } else {
                            showDialog("Không tìm thấy folder phù hợp.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        showDialog("Có lỗi xảy ra khi tìm folder: " + e.getMessage());
                    });
        }
    }





    private void showDialog(String message) {
        showDialog(message, false);
    }

    private void showDialog(String message, boolean isSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (isSuccess) {
                        finish(); // Quay lại Activity trước nếu lưu thành công
                    }
                });
        builder.create().show();
    }
}
