package com.example.doancuoiky_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doancuoiky_mobile.adapter.TopicAdapter;
import com.example.doancuoiky_mobile.model.FlashCard;
import com.example.doancuoiky_mobile.model.Topic;
import com.example.doancuoiky_mobile.model.Vocabulary;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class Course1 extends AppCompatActivity {

    private RecyclerView topicListRecyclerView;
    private TopicAdapter topicAdapter;
    private RecyclerView topicRecyclerView;
    private List<Topic> topicList;
    private FirebaseFirestore db;
    public String folderName;
    public String folderNamee;
    public ImageButton btnAddNewTopic;
    public TextView header;
    public ImageButton btnAccount, btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course1);

        db = FirebaseFirestore.getInstance();
        topicRecyclerView = findViewById(R.id.topicList);
        topicList = new ArrayList<>();

        btnAddNewTopic = findViewById(R.id.btn_addNewTopic);
        btnAccount = findViewById(R.id.account_button);
        // Thiết lập sự kiện onClick
        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển hướng tới Activity mới
                Intent intent = new Intent(Course1.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        btnHome = findViewById(R.id.home_button);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển hướng tới Activity mới
                Intent intent = new Intent(Course1.this, FolderActivity.class);
                startActivity(intent);
            }
        });

        // Lấy folderName từ Intent
        folderName = getIntent().getStringExtra("folderName");
        header = findViewById(R.id.headerTitle);
        header.setText(folderName);
        folderNamee = folderName;

        if (folderName != null) {
            // Gọi Firebase để lấy các topic theo folderName
            getTopicsFromFirestore(folderName);
        } else {
            Toast.makeText(this, "Folder name is missing", Toast.LENGTH_SHORT).show();
        }

        topicListRecyclerView = findViewById(R.id.topicList);
        topicListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Gán onClickListener cho nút btnAddNewTopic
        btnAddNewTopic.setOnClickListener(v -> {
            Intent intent = new Intent(Course1.this, CreateTopicActivity.class);
            intent.putExtra("folderName", folderName); // Truyền folderName qua Activity tiếp theo
            startActivity(intent);
        });
    }



    private void getTopicsFromFirestore(String folderName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Tìm document có "name" = folderName trong collection "folders"
        db.collection("folders")
                .whereEqualTo("name", folderName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot folderDoc = task.getResult().getDocuments().get(0);
                        String folderId = folderDoc.getId(); // Lấy document id

                        // Lấy collection "topics" từ document tìm được
                        db.collection("folders")
                                .document(folderId)
                                .collection("topics")
                                .get()
                                .addOnCompleteListener(topicTask -> {
                                    if (topicTask.isSuccessful()) {
                                        List<Topic> topics = new ArrayList<>();
                                        for (DocumentSnapshot document : topicTask.getResult()) {
                                            String name = document.getString("name");
                                            String description = document.getString("description");
                                            String visibility = document.getString("privacy");
                                            List<FlashCard> flashCards = (List<FlashCard>) document.get("flashCards");

                                            // Truy vấn để lấy danh sách vocabulary của topic
                                            db.collection("folders")
                                                    .document(folderId)
                                                    .collection("topics")
                                                    .document(document.getId()) // Lấy topicId từ document hiện tại
                                                    .collection("vocabulary")
                                                    .get()
                                                    .addOnCompleteListener(vocabularyTask -> {
                                                        if (vocabularyTask.isSuccessful()) {
                                                            List<Vocabulary> vocabularies = new ArrayList<>();
                                                            for (DocumentSnapshot vocabularyDoc : vocabularyTask.getResult()) {
                                                                Vocabulary vocabulary = vocabularyDoc.toObject(Vocabulary.class);
                                                                if (vocabulary != null) {
                                                                    vocabularies.add(vocabulary);
                                                                }
                                                            }

                                                            // Tính số lượng vocabularies
                                                            int vocabularyCount = vocabularies.size();

                                                            // Chỉ cập nhật description nếu description không phải là null
                                                            String updatedDescription = description != null ? description : "";
                                                            if (vocabularyCount > 0) {
                                                                updatedDescription += " " + vocabularyCount + " từ vựng";
                                                            }

                                                            // Thêm topic vào danh sách
                                                            topics.add(new Topic(name, updatedDescription, visibility, vocabularies, flashCards));

                                                            // Sau khi tất cả topic đã được thêm, cập nhật RecyclerView
                                                            if (topics.size() == topicTask.getResult().size()) {
                                                                topicAdapter = new TopicAdapter(topics, new TopicAdapter.OnTopicClickListener() {
                                                                    @Override
                                                                    public void onTopicClick(Topic topic) {
                                                                        Intent intent = new Intent(Course1.this, TopicDetailActivity.class);
                                                                        intent.putExtra("folderName", folderName);
                                                                        intent.putExtra("TOPIC_NAME", topic.getName());
                                                                        intent.putExtra("TOPIC_DESCRIPTION", topic.getDescription());
                                                                        startActivity(intent);
                                                                    }
                                                                });
                                                                topicListRecyclerView.setAdapter(topicAdapter);
                                                            }
                                                        } else {
                                                            Toast.makeText(Course1.this, "Failed to load vocabulary", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    } else {
                                        Toast.makeText(Course1.this, "Failed to load topics", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(Course1.this, "Folder not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
