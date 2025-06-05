package com.example.doancuoiky_mobile;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doancuoiky_mobile.adapter.FolderAdapter;
import com.example.doancuoiky_mobile.adapter.TopicAdapter;
import com.example.doancuoiky_mobile.model.Folder;
import com.example.doancuoiky_mobile.model.Topic;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity {

    private RecyclerView folderRecyclerView, topicRecyclerView;
    private FolderAdapter folderAdapter;
    private TopicAdapter topicAdapter;
    private List<Folder> folderList = new ArrayList<>();
    private List<Topic> topicList = new ArrayList<>();
    private FirebaseFirestore db;
    private Spinner filterSpinner;
    private LinearLayout addFolderForm;

    private ImageButton btnAccount;
    private ImageButton btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Khởi tạo Views
        addFolderForm = findViewById(R.id.addFolderForm);
        EditText edtFolderName = findViewById(R.id.englishWord); // ID cho EditText tên fol     der
        EditText edtFolderDescription = findViewById(R.id.vietnameseWord); // ID cho EditText mô tả folder

        // Nút Save
        findViewById(R.id.saveVocabularyButton).setOnClickListener(v -> {
            String name = edtFolderName.getText().toString().trim();
            String description = edtFolderDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(FolderActivity.this, "Tên Folder không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo Folder mới
            Folder newFolder = new Folder(name, description, null);

            // Lưu vào Firestore
            db.collection("folders")
                    .add(newFolder)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(FolderActivity.this, "Thêm Folder thành công", Toast.LENGTH_SHORT).show();
                        addFolderForm.setVisibility(View.GONE);
                        edtFolderName.setText("");
                        edtFolderDescription.setText("");
                        loadFoldersFromFirestore(); // Refresh danh sách folders
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FolderActivity.this, "Lỗi khi thêm Folder", Toast.LENGTH_SHORT).show();
                    });
        });

        // Nút Cancel
        findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            addFolderForm.setVisibility(View.GONE);
            edtFolderName.setText(""); // Reset dữ liệu trong form
            edtFolderDescription.setText("");
        });

        btnAccount = findViewById(R.id.account_button);
        // Thiết lập sự kiện onClick
        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển hướng tới Activity mới
                Intent intent = new Intent(FolderActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        btnHome = findViewById(R.id.home_button);
        // Thiết lập sự kiện onClick

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        folderRecyclerView = findViewById(R.id.folderList);
        topicRecyclerView = findViewById(R.id.topicList);
        filterSpinner = findViewById(R.id.spinnerView);
        addFolderForm = findViewById(R.id.addFolderForm);

        // Setup RecyclerViews
        setupFolderRecyclerView();
        setupTopicRecyclerView();

        // Setup Spinner
        setupFilterSpinner();

        // Show Bottom Sheet Dialog on Add Button Click
        findViewById(R.id.btn_addNewTopic).setOnClickListener(v -> showCustomBottomSheet());

        // Load folders initially
        loadFoldersFromFirestore();
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                if ("Danh sách topic".equals(selectedOption)) {
                    folderRecyclerView.setVisibility(View.GONE);
                    topicRecyclerView.setVisibility(View.VISIBLE);
                    if (topicList.isEmpty()) {
                        loadTopicsFromFirestore();
                    }
                } else if ("Danh sách folder".equals(selectedOption)) {
                    topicRecyclerView.setVisibility(View.GONE);
                    folderRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    folderRecyclerView.setVisibility(View.GONE);
                    topicRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void setupFolderRecyclerView() {
        folderAdapter = new FolderAdapter(this, folderList, folder -> {
            Intent intent = new Intent(FolderActivity.this, Course1.class);
            intent.putExtra("folderName", folder.getName());
            startActivity(intent);
        });
        folderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        folderRecyclerView.setAdapter(folderAdapter);
    }

    private void setupTopicRecyclerView() {
        topicAdapter = new TopicAdapter(topicList, topic -> {
            Intent intent = new Intent(FolderActivity.this, TopicDetailActivity.class);
            intent.putExtra("TOPIC_NAME", topic.getName());
            intent.putExtra("TOPIC_DESCRIPTION", topic.getDescription());
            startActivity(intent);
        });
        topicRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        topicRecyclerView.setAdapter(topicAdapter);
    }

    private void loadFoldersFromFirestore() {
        db.collection("folders")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        folderList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Folder folder = document.toObject(Folder.class);
                            folderList.add(folder);
                        }
                        folderAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(FolderActivity.this, "Failed to load folders", Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    private void loadTopicsFromFirestore() {
//        db.collection("topics")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        topicList.clear();
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            String name = document.getString("name");
//                            String description = document.getString("description");
//                            String visibility = document.getString("privacy");
//                            topicList.add(new Topic(name, description, visibility, null, null));
//                        }
//                        topicAdapter.notifyDataSetChanged();
//                    } else {
//                        Toast.makeText(FolderActivity.this, "Failed to load topics", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    private void loadTopicsFromFirestore() {
        db.collection("topics")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        topicList.clear(); // Xóa danh sách cũ trước khi thêm dữ liệu mới
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Lấy thông tin cơ bản của topic
                            String name = document.getString("name");
                            String description = document.getString("description");
                            String visibility = document.getString("privacy");

                            // Truy vấn collection "vocabulary" để đếm số lượng từ vựng
                            db.collection("topics")
                                    .document(document.getId())
                                    .collection("vocabulary")
                                    .get()
                                    .addOnCompleteListener(vocabularyTask -> {
                                        if (vocabularyTask.isSuccessful()) {
                                            int vocabularyCount = vocabularyTask.getResult().size(); // Đếm số lượng từ vựng

                                            // Cập nhật mô tả kèm số lượng từ vựng
                                            String updatedDescription = (description != null ? description : "") +
                                                    (vocabularyCount > 0 ? " " + vocabularyCount + " từ vựng" : "");

                                            // Thêm topic vào danh sách
                                            topicList.add(new Topic(name, updatedDescription, visibility, null, null));

                                            // Cập nhật RecyclerView sau khi xử lý xong tất cả topics
                                            if (topicList.size() == task.getResult().size()) {
                                                topicAdapter.notifyDataSetChanged();
                                            }
                                        } else {
                                            Toast.makeText(FolderActivity.this, "Failed to load vocabulary", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(FolderActivity.this, "Failed to load topics", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showCustomBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheetlayout, null);
        bottomSheetDialog.setContentView(sheetView);

        sheetView.findViewById(R.id.btn_addFolder).setOnClickListener(v -> {
            addFolderForm.setVisibility(View.VISIBLE);
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.btn_addTopic).setOnClickListener(v -> {
            Intent intent = new Intent(FolderActivity.this, CreateTopicActivity.class);
            startActivity(intent);
        });

        bottomSheetDialog.show();
    }
}
