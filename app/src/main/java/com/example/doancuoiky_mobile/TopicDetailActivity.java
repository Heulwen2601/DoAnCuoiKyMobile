package com.example.doancuoiky_mobile;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doancuoiky_mobile.model.FlashCard;
import com.example.doancuoiky_mobile.model.Vocabulary;
import java.util.ArrayList;
import java.util.List;

public class TopicDetailActivity extends AppCompatActivity {

    private TextView topicTitle, topicDescription;
    private List<Vocabulary> vocabularyItems;
    private List<FlashCard> flashCards;
    public String folderName;
    public ImageButton btnAccount, btnHome;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);


        folderName = getIntent().getStringExtra("folderName");

        String topicNamee = getIntent().getStringExtra("TOPIC_NAME");

        // Lấy các view trong layout
        topicTitle = findViewById(R.id.topicTitle);
        Button vocabularyButton = findViewById(R.id.vocabularyButton);
        ImageButton backButton = findViewById(R.id.back_button);
        Button flashcardButton = findViewById(R.id.flashcardButton);
        Button goQuiz = findViewById(R.id.quizButton);


        // Lấy thông tin từ Intent
        Intent intent = getIntent();
        String topicName = intent.getStringExtra("topicName");
//        String topicDesc = intent.getStringExtra("topicDesc");
        vocabularyItems = (List<Vocabulary>) intent.getSerializableExtra("vocabularyItems");
        flashCards = (List<FlashCard>) intent.getSerializableExtra("flashCardItems");
        // Hiển thị tên và mô tả chủ đề
        topicTitle.setText(topicNamee);
//        topicDescription.setText(topicDesc);
        // Sự kiện khi người dùng nhấn nút Vocabulary
        vocabularyButton.setOnClickListener(view -> {
            // Chuyển sang VocabularyActivity và truyền danh sách từ vựng
            Intent vocabIntent = new Intent(TopicDetailActivity.this, VocabularyActivity.class);
            vocabIntent.putExtra("folderName", folderName);
            vocabIntent.putExtra("TOPIC_NAME", topicNamee); // Truyền tên chủ đề (tuỳ chọn)
            vocabIntent.putExtra("vocabularyItems", (ArrayList<Vocabulary>) vocabularyItems); // Truyền danh sách từ vựng
            startActivity(vocabIntent);
        });

        // Xử lý sự kiện khi nhấn nút Flashcard
        flashcardButton.setOnClickListener(view -> {

            Intent flashcardIntent = new Intent(TopicDetailActivity.this, FlashcardActivity.class);
            flashcardIntent.putExtra("folderName", folderName);
            flashcardIntent.putExtra("TOPIC_NAME", topicNamee); // Truyền tên chủ đề
            flashcardIntent.putExtra("flashCardItems", (ArrayList<FlashCard>) flashCards); // Truyền danh sách từ vựng
            startActivity(flashcardIntent);
        });

        goQuiz.setOnClickListener(view -> {
            Intent quizIntent = new Intent(TopicDetailActivity.this, QuizActivity.class);
            quizIntent.putExtra("folderName", folderName);
            quizIntent.putExtra("TOPIC_NAME", topicNamee);
            quizIntent.putExtra("vocabularyItems", (ArrayList<Vocabulary>) vocabularyItems);
            startActivity(quizIntent);
        });

        // Sự kiện khi người dùng nhấn nút Back
        backButton.setOnClickListener(view -> onBackPressed());
    }
}
