package com.example.doancuoiky_mobile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doancuoiky_mobile.adapter.VocabularyAdapter;
import com.example.doancuoiky_mobile.model.FlashCard;
import com.example.doancuoiky_mobile.model.Topic;
import com.example.doancuoiky_mobile.model.Vocabulary;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VocabularyActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private RecyclerView recyclerView;
    private RecyclerView recyclerViewFav;
    private VocabularyAdapter adapter;
    private VocabularyAdapter adapterFav;
    private List<Vocabulary> vocabularyList;
    private List<Vocabulary> vocabularyListFav;
    private LinearLayout vocabularyInputForm;
    private EditText englishWordEditText, vietnameseWordEditText;
    private Button saveVocabularyButton;
    private FirebaseFirestore firestore;
    private TextToSpeech textToSpeech;
    private Spinner spinnerViewVocab;
    public TextView header;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        textToSpeech = new TextToSpeech(this, this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);

        String folderNamee = getIntent().getStringExtra("folderName");

        String topicName = getIntent().getStringExtra("TOPIC_NAME");// Nhận tên topic

        header = findViewById(R.id.topicTitle);
        header.setText(topicName);

        // Firebase Firestore instance
        firestore = FirebaseFirestore.getInstance();

        // Tìm view RecyclerView và thiết lập LayoutManager
        recyclerView = findViewById(R.id.vocabularyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewFav = findViewById(R.id.vocabularyListFavourite);
        recyclerViewFav.setLayoutManager(new LinearLayoutManager(this));

        // Dữ liệu mẫu với cấu trúc đầy đủ
        vocabularyList = new ArrayList<>();
        adapter = new VocabularyAdapter(vocabularyList, this);
        recyclerView.setAdapter(adapter);

        vocabularyListFav = adapter.getFavoriteList();
        adapterFav = new VocabularyAdapter(vocabularyListFav,this);



        // Tìm nút Back và thiết lập sự kiện khi nhấn
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> onBackPressed());

        // Tìm các view nhập từ vựng và nút lưu
        vocabularyInputForm = findViewById(R.id.vocabularyInputForm);
        englishWordEditText = findViewById(R.id.englishWord);
        vietnameseWordEditText = findViewById(R.id.vietnameseWord);
        saveVocabularyButton = findViewById(R.id.saveVocabularyButton);


        spinnerViewVocab = findViewById(R.id.spinnerViewVocab);
        // Cấu hình Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Mặc định", "Yêu thích"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerViewVocab.setAdapter(spinnerAdapter);

        // Xử lý sự kiện khi chọn Spinner
        spinnerViewVocab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Hiển thị danh sách mặc định
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerViewFav.setVisibility(View.GONE);
                } else {
                    // Hiển thị danh sách yêu thích
                    recyclerView.setVisibility(View.GONE);
                    recyclerViewFav.setVisibility(View.VISIBLE);

                    // Lấy danh sách yêu thích từ Adapter mặc định
                    adapterFav.updateFavoriteList(adapter.getFavoriteList());
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì
            }
        });

        recyclerViewFav.setAdapter(adapterFav);



    // Hiển thị form nhập từ vựng khi nhấn nút Thêm từ vựng
        ImageButton addNewVocabularyButton = findViewById(R.id.btn_addNewVocabulary);
        addNewVocabularyButton.setOnClickListener(view -> vocabularyInputForm.setVisibility(View.VISIBLE));

        // Lưu từ vựng mới vào Firestore khi nhấn nút Save
        saveVocabularyButton.setOnClickListener(view -> {
            String englishWord = englishWordEditText.getText().toString().trim();
            String vietnameseWord = vietnameseWordEditText.getText().toString().trim();

            if (!englishWord.isEmpty() && !vietnameseWord.isEmpty()) {
                Vocabulary newVocabulary = new Vocabulary(englishWord, vietnameseWord, "", "", "not_learned", false, "");

                // Kiểm tra nếu folderName là null
                if (folderNamee == null || folderNamee.isEmpty()) {
                    // Nếu folderName null, lưu vào topic mặc định
                    firestore.collection("topics")
                            .whereEqualTo("name", topicName) // Lọc theo tên topic mặc định là "topic 3"
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    // Lấy document đầu tiên (giả định chỉ có 1 topic với name là "topic 3")
                                    DocumentSnapshot topicDoc = task.getResult().getDocuments().get(0);
                                    String topicId = topicDoc.getId(); // Lấy topicId

                                    // Thêm từ vựng vào topic trong Firestore
                                    firestore.collection("topics")
                                            .document(topicId) // Sử dụng topicId đã lấy được
                                            .collection("vocabulary")
                                            .add(newVocabulary)
                                            .addOnSuccessListener(documentReference -> {
                                                Toast.makeText(VocabularyActivity.this, "Thêm từ vựng mới thành công", Toast.LENGTH_SHORT).show();
                                                vocabularyList.add(newVocabulary); // Thêm từ vựng vào danh sách
                                                adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                                                vocabularyInputForm.setVisibility(View.GONE); // Ẩn form nhập liệu

                                                // Reset lại form nhập
                                                englishWordEditText.setText(""); // Làm trống trường tiếng Anh
                                                vietnameseWordEditText.setText(""); // Làm trống trường tiếng Việt
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(VocabularyActivity.this, "Failed to add vocabulary", Toast.LENGTH_SHORT).show());
                                } else {

                                }
                            });
                } else {
                    // Nếu folderName không phải null, thực hiện truy vấn tìm folder và topic
                    firestore.collection("folders")
                            .whereEqualTo("name", folderNamee) // Lọc folder theo name
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        // Kiểm tra nếu có kết quả
                                        DocumentSnapshot folderDoc = task.getResult().getDocuments().get(0);
                                        String folderId = folderDoc.getId(); // Lấy folderId

                                        // Log để kiểm tra folderId và folderName
                                        Log.d("Folder Found", "Folder ID: " + folderId + " | Folder Name: " + folderNamee);

                                        // Truy vấn topic bên trong folder theo topicName
                                        firestore.collection("folders")
                                                .document(folderId) // Sử dụng folderId lấy được từ bước trước
                                                .collection("topics")
                                                .whereEqualTo("name", topicName) // Lọc topic theo name
                                                .get()
                                                .addOnCompleteListener(topicTask -> {
                                                    if (topicTask.isSuccessful() && !topicTask.getResult().isEmpty()) {
                                                        // Lấy document đầu tiên (giả định topicName là duy nhất)
                                                        DocumentSnapshot topicDoc = topicTask.getResult().getDocuments().get(0);
                                                        String topicId = topicDoc.getId(); // Lấy topicId

                                                        // Thêm từ vựng vào topic trong folder
                                                        firestore.collection("folders")
                                                                .document(folderId) // Sử dụng folderId
                                                                .collection("topics")
                                                                .document(topicId) // Sử dụng topicId
                                                                .collection("vocabulary")
                                                                .add(newVocabulary)
                                                                .addOnSuccessListener(documentReference -> {
                                                                    Toast.makeText(VocabularyActivity.this, "Thêm từ vựng mới thành công", Toast.LENGTH_SHORT).show();
                                                                    vocabularyList.add(newVocabulary); // Thêm từ vựng vào danh sách
                                                                    adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                                                                    vocabularyInputForm.setVisibility(View.GONE); // Ẩn form nhập liệu

                                                                    // Reset lại form nhập
                                                                    englishWordEditText.setText(""); // Làm trống trường tiếng Anh
                                                                    vietnameseWordEditText.setText(""); // Làm trống trường tiếng Việt
                                                                })
                                                                .addOnFailureListener(e -> Toast.makeText(VocabularyActivity.this, "Failed to add vocabulary", Toast.LENGTH_SHORT).show());
                                                    } else {

                                                    }
                                                });
                                    } else {
                                        // Nếu không tìm thấy folderName
                                        Toast.makeText(VocabularyActivity.this, "Folder not found", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Lỗi khi truy vấn Firestore
                                    Toast.makeText(VocabularyActivity.this, "Failed to load folders", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Lỗi khi truy vấn Firestore
                                Toast.makeText(VocabularyActivity.this, "Failed to load folders", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(VocabularyActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });



        // Lấy danh sách từ vựng từ Firestore
        getVocabularyListFromFirestore(folderNamee, topicName);
        getVocabularyListFromFirestore(topicName);
    }

    private void getVocabularyListFromFirestore(String folderName, String topicName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Truy vấn để tìm folder có "name" khớp với folderName
        firestore.collection("folders")
                .whereEqualTo("name", folderName) // Lọc folder theo name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Kiểm tra nếu có kết quả
                            DocumentSnapshot folderDoc = task.getResult().getDocuments().get(0);
                            String folderId = folderDoc.getId(); // Lấy folderId

                            // Log để kiểm tra folderId và folderName
                            Log.d("Folder Found", "Folder ID: " + folderId + " | Folder Name: " + folderName);

                            // Truy vấn topic bên trong folder theo topicName
                            firestore.collection("folders")
                                    .document(folderId) // Sử dụng folderId lấy được từ bước trước
                                    .collection("topics")
                                    .whereEqualTo("name", topicName) // Lọc topic theo name
                                    .get()
                                    .addOnCompleteListener(topicTask -> {
                                        if (topicTask.isSuccessful() && !topicTask.getResult().isEmpty()) {
                                            // Lấy document đầu tiên (giả định topicName là duy nhất)
                                            DocumentSnapshot topicDoc = topicTask.getResult().getDocuments().get(0);
                                            String topicId = topicDoc.getId(); // Lấy topicId

                                            // Truy vấn danh sách từ vựng bên trong topic
                                            firestore.collection("folders")
                                                    .document(folderId) // Sử dụng folderId
                                                    .collection("topics")
                                                    .document(topicId) // Sử dụng topicId
                                                    .collection("vocabulary")
                                                    .get()
                                                    .addOnCompleteListener(vocabularyTask -> {
                                                        if (vocabularyTask.isSuccessful()) {
                                                            if (!vocabularyTask.getResult().isEmpty()) {
                                                                vocabularyList.clear(); // Xóa danh sách trước khi thêm mới
                                                                for (DocumentSnapshot vocabularyDoc : vocabularyTask.getResult()) {
                                                                    Vocabulary vocabulary = vocabularyDoc.toObject(Vocabulary.class);
                                                                    if (vocabulary != null) {
                                                                        vocabularyList.add(vocabulary);
                                                                    }
                                                                }
                                                                adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                                                            } else {
                                                                Toast.makeText(VocabularyActivity.this, "No vocabulary found", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(VocabularyActivity.this, "Failed to load vocabulary", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                        }
                                    });
                        } else {
                            // Nếu không tìm thấy folderName
                            Toast.makeText(VocabularyActivity.this, "Folder not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Lỗi khi truy vấn Firestore
                        Toast.makeText(VocabularyActivity.this, "Failed to load folders", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi truy vấn Firestore
                    Toast.makeText(VocabularyActivity.this, "Failed to load folders", Toast.LENGTH_SHORT).show();
                });
    }
    private void getVocabularyListFromFirestore(String topicName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Truy vấn để tìm topic có "name" khớp với topicName
        firestore.collection("topics")
                .whereEqualTo("name", topicName) // Lọc topic theo name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Kiểm tra nếu có kết quả
                            DocumentSnapshot topicDoc = task.getResult().getDocuments().get(0);
                            String topicId = topicDoc.getId(); // Lấy topicId

                            // Log để kiểm tra topicId và topicName
                            Log.d("Topic Found", "Topic ID: " + topicId + " | Topic Name: " + topicName);

                            // Truy vấn danh sách từ vựng bên trong topic
                            firestore.collection("topics")
                                    .document(topicId) // Sử dụng topicId lấy được từ bước trước
                                    .collection("vocabulary")
                                    .get()
                                    .addOnCompleteListener(vocabularyTask -> {
                                        if (vocabularyTask.isSuccessful()) {
                                            if (!vocabularyTask.getResult().isEmpty()) {
                                                vocabularyList.clear(); // Xóa danh sách trước khi thêm mới
                                                for (DocumentSnapshot vocabularyDoc : vocabularyTask.getResult()) {
                                                    Vocabulary vocabulary = vocabularyDoc.toObject(Vocabulary.class);
                                                    if (vocabulary != null) {
                                                        vocabularyList.add(vocabulary);
                                                    }
                                                }
                                                adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                                            } else {
                                                Toast.makeText(VocabularyActivity.this, "No vocabulary found", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(VocabularyActivity.this, "Failed to load vocabulary", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                        }
                    } else {
                        // Lỗi khi truy vấn Firestore
                        Toast.makeText(VocabularyActivity.this, "Failed to load topics", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi truy vấn Firestore
                    Toast.makeText(VocabularyActivity.this, "Failed to load topics", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set ngôn ngữ mặc định là tiếng Anh
            int langResult = textToSpeech.setLanguage(Locale.ENGLISH);
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show();
        }
    }
}
