package com.example.doancuoiky_mobile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doancuoiky_mobile.model.Vocabulary;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class QuizActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView questionCard, progressText;
    private Button nextButton, backButton;
    private ImageButton speakButton;// Thêm speakButton
    private RadioGroup answerOptions;
//    private FirebaseFirestore firestore;
    private TextToSpeech textToSpeech;

    public List<Vocabulary> vocabularyList;
    public List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    public String topicNamee;
    public String folderNamee;
    private boolean isAnswered = false;
    public int score = 0;
    public TextView topicNameTV;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, this);

        // Get topic name from Intent
        folderNamee = getIntent().getStringExtra("folderName");
        topicNamee = getIntent().getStringExtra("TOPIC_NAME");
//        firestore = FirebaseFirestore.getInstance();

        // Bind views
        questionCard = findViewById(R.id.questionCard);
        progressText = findViewById(R.id.progressText);
        answerOptions = findViewById(R.id.answerOptions);
        nextButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.backButton);
        speakButton = findViewById(R.id.btnSound); // Nút để đọc câu hỏi
        topicNameTV = findViewById(R.id.topicName);
        topicNameTV.setText(topicNamee);

        // Load vocabulary from Firestore
        getVocabularyListFromFirestore(folderNamee, topicNamee);
        getVocabularyListFromFirestore(topicNamee);

        // Set up TTS button listener
        speakButton.setOnClickListener(v -> {
            Question currentQuestion = questionList.get(currentQuestionIndex);
            speakText(currentQuestion.getQuestionText());
        });
    }

    private void getVocabularyListFromFirestore(String folderName, String topicName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Khởi tạo danh sách nếu chưa được khởi tạo
        if (vocabularyList == null) {
            vocabularyList = new ArrayList<>();
        }

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
                                                                        Log.d("Vocabulary", "English: " + vocabulary.getEnglishWord() + ", Vietnamese: " + vocabulary.getVietnameseWord());
                                                                        vocabularyList.add(vocabulary);
                                                                    }
                                                                }
                                                                // Chuyển vocabularyList thành questionList
                                                                questionList = generateQuestions(vocabularyList);
                                                                // Cập nhật UI với câu hỏi đầu tiên
                                                                updateQuestion();

                                                                setupButtonListeners();
//                                                            adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                                                            } else {
                                                                Toast.makeText(QuizActivity.this, "No vocabulary found", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(QuizActivity.this, "Failed to load vocabulary", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {

                                        }
                                    });
                        } else {
                            // Nếu không tìm thấy folderName

                        }
                    } else {
                        // Lỗi khi truy vấn Firestore

                    }
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi truy vấn Firestore

                });
    }

    private void getVocabularyListFromFirestore(String topicName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Khởi tạo danh sách nếu chưa được khởi tạo
        if (vocabularyList == null) {
            vocabularyList = new ArrayList<>();
        }

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
                                                        Log.d("Vocabulary", "English: " + vocabulary.getEnglishWord() + ", Vietnamese: " + vocabulary.getVietnameseWord());
                                                        vocabularyList.add(vocabulary);
                                                    }
                                                }
                                                // Chuyển vocabularyList thành questionList
                                                questionList = generateQuestions(vocabularyList);
                                                // Cập nhật UI với câu hỏi đầu tiên
                                                updateQuestion();

                                                setupButtonListeners();
                                            } else {

                                            }
                                        } else {
                                            Toast.makeText(QuizActivity.this, "Failed to load vocabulary", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {

                        }
                    } else {
                        // Lỗi khi truy vấn Firestore

                    }
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi truy vấn Firestore

                });
    }



    private void updateQuestion() {
        if (questionList.isEmpty()) return;

        // Lấy câu hỏi hiện tại
        Question currentQuestion = questionList.get(currentQuestionIndex);
        questionCard.setText(currentQuestion.getQuestionText());

        // Cập nhật các lựa chọn đáp án
        RadioButton option1 = findViewById(R.id.option1);
        RadioButton option2 = findViewById(R.id.option2);
        RadioButton option3 = findViewById(R.id.option3);
        RadioButton option4 = findViewById(R.id.option4);

        option1.setText(currentQuestion.getOption1());
        option2.setText(currentQuestion.getOption2());
        option3.setText(currentQuestion.getOption3());
        option4.setText(currentQuestion.getOption4());

        // Cập nhật tiến độ
        progressText.setText((currentQuestionIndex + 1) + "/" + questionList.size());
        answerOptions.clearCheck();

        // Reset màu sắc của các đáp án về màu mặc định
        option1.setBackgroundColor(getResources().getColor(R.color.default_option));
        option2.setBackgroundColor(getResources().getColor(R.color.default_option));
        option3.setBackgroundColor(getResources().getColor(R.color.default_option));
        option4.setBackgroundColor(getResources().getColor(R.color.default_option));

        // Thiết lập sự kiện khi người dùng chọn đáp án
        answerOptions.setOnCheckedChangeListener((group, checkedId) -> {
            // Lấy đáp án đúng từ câu hỏi hiện tại
            String correctAnswer = currentQuestion.getCorrectAnswer();
            String selectedAnswer = "";

            // Kiểm tra ID của đáp án được chọn
            if (checkedId == R.id.option1) {
                selectedAnswer = option1.getText().toString();
            } else if (checkedId == R.id.option2) {
                selectedAnswer = option2.getText().toString();
            } else if (checkedId == R.id.option3) {
                selectedAnswer = option3.getText().toString();
            } else if (checkedId == R.id.option4) {
                selectedAnswer = option4.getText().toString();
            }

            // Cập nhật màu sắc các đáp án
            updateAnswerColors(option1, option2, option3, option4, selectedAnswer, correctAnswer);
        });
    }




    private void updateAnswerColors(RadioButton option1, RadioButton option2, RadioButton option3, RadioButton option4, String selectedAnswer, String correctAnswer) {
        // Đặt màu sắc mặc định cho các đáp án
        option1.setBackgroundColor(getResources().getColor(R.color.default_option));
        option2.setBackgroundColor(getResources().getColor(R.color.default_option));
        option3.setBackgroundColor(getResources().getColor(R.color.default_option));
        option4.setBackgroundColor(getResources().getColor(R.color.default_option));

        // Kiểm tra đáp án và cập nhật màu sắc
        if (selectedAnswer.equals(correctAnswer)) {
            // Nếu đáp án đúng, đổi màu xanh cho đáp án đúng
            score++;
            if (selectedAnswer.equals(option1.getText().toString())) {
                option1.setBackgroundColor(getResources().getColor(R.color.correct_background));
            } else if (selectedAnswer.equals(option2.getText().toString())) {
                option2.setBackgroundColor(getResources().getColor(R.color.correct_background));
            } else if (selectedAnswer.equals(option3.getText().toString())) {
                option3.setBackgroundColor(getResources().getColor(R.color.correct_background));
            } else if (selectedAnswer.equals(option4.getText().toString())) {
                option4.setBackgroundColor(getResources().getColor(R.color.correct_background));
            }
        } else {
            // Nếu đáp án sai, đổi màu đỏ cho đáp án sai và xanh cho đáp án đúng
            if (selectedAnswer.equals(option1.getText().toString())) {
                option1.setBackgroundColor(getResources().getColor(R.color.wrong_background));
            } else if (selectedAnswer.equals(option2.getText().toString())) {
                option2.setBackgroundColor(getResources().getColor(R.color.wrong_background));
            } else if (selectedAnswer.equals(option3.getText().toString())) {
                option3.setBackgroundColor(getResources().getColor(R.color.wrong_background));
            } else if (selectedAnswer.equals(option4.getText().toString())) {
                option4.setBackgroundColor(getResources().getColor(R.color.wrong_background));
            }

            // Đánh dấu đáp án đúng
            if (correctAnswer.equals(option1.getText().toString())) {
                option1.setBackgroundColor(getResources().getColor(R.color.correct_background));
            } else if (correctAnswer.equals(option2.getText().toString())) {
                option2.setBackgroundColor(getResources().getColor(R.color.correct_background));
            } else if (correctAnswer.equals(option3.getText().toString())) {
                option3.setBackgroundColor(getResources().getColor(R.color.correct_background));
            } else if (correctAnswer.equals(option4.getText().toString())) {
                option4.setBackgroundColor(getResources().getColor(R.color.correct_background));
            }
        }
    }



    private void setupButtonListeners() {
        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                updateQuestion();
            } else {
                showResultDialog(score, vocabularyList.size());            }
        });

        backButton.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                updateQuestion();
            } else {
                Toast.makeText(this, "This is the first question.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speakText(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private List<Question> generateQuestions(List<Vocabulary> vocabularies) {
        List<Question> questions = new ArrayList<>();
        Random random = new Random();

        for (Vocabulary vocabulary : vocabularies) {
            if (vocabulary == null || vocabulary.getEnglishWord() == null || vocabulary.getVietnameseWord() == null) {
                Log.w("generateQuestions", "Skipped null or incomplete vocabulary: " + vocabulary);
                continue; // Bỏ qua các mục không hợp lệ
            }

            String correctAnswer = vocabulary.getVietnameseWord();

            // Tạo danh sách câu trả lời sai
            List<String> wrongAnswers = new ArrayList<>();
            for (Vocabulary otherVocab : vocabularies) {
                if (otherVocab == null || otherVocab.getVietnameseWord() == null || otherVocab.getEnglishWord().equals(vocabulary.getEnglishWord())) {
                    continue; // Bỏ qua mục null, không có nghĩa hoặc trùng với vocabulary đang xét
                }
                wrongAnswers.add(otherVocab.getVietnameseWord());
            }

            // Trộn danh sách đáp án sai và chọn ngẫu nhiên 3 đáp án sai
            Collections.shuffle(wrongAnswers);
            List<String> options = new ArrayList<>();
            options.add(correctAnswer);
            options.addAll(wrongAnswers.subList(0, Math.min(3, wrongAnswers.size())));

            // Trộn lại các đáp án để đảm bảo câu hỏi không bị lặp lại
            Collections.shuffle(options);
            // Tạo câu hỏi mới và thiết lập đáp án đúng
            Question question = new Question(vocabulary.getEnglishWord(), options.get(0), options.get(1), options.get(2), options.get(3), null);
            question.setCorrectAnswer(correctAnswer);  // Lưu đáp án đúng vào câu hỏi
            questions.add(question);
        }

        return questions;
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
            int langResult = textToSpeech.setLanguage(Locale.ENGLISH);
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    public static class Question {
        private final String questionText;
        private final String option1, option2, option3, option4;
        private String correctAnswer;

        public Question(String questionText, String option1, String option2, String option3, String option4, String correctAnswer) {
            this.questionText = questionText;
            this.option1 = option1;
            this.option2 = option2;
            this.option3 = option3;
            this.option4 = option4;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getOption1() {
            return option1;
        }

        public String getOption2() {
            return option2;
        }

        public String getOption3() {
            return option3;
        }

        public String getOption4() {
            return option4;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }
        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }
    }

    public int calculateScore(List<Question> questions, List<String> userAnswers) {
        int score = 0;

        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getCorrectAnswer().equals(userAnswers.get(i))) {
                score++;
            }
        }

        return score;
    }

    public void showResultDialog(int score, int totalQuestions) {
        // Khởi tạo AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Cài đặt thông điệp
        String message = "Bạn đã hoàn thành bài thi.\nĐiểm của bạn: " + score + "/" + totalQuestions;

        // Tạo lời khuyên dựa trên điểm
        String advice;
        if (score == totalQuestions) {
            advice = "Chúc mừng! Bạn đã trả lời đúng tất cả các câu hỏi!";
        } else if (score >= totalQuestions / 2) {
            advice = "Tốt rồi! Bạn đã làm rất tốt, nhưng vẫn có thể cải thiện.";
        } else {
            advice = "Cố gắng hơn nữa! Hãy ôn lại những câu trả lời sai.";
        }

        // Cài đặt nội dung thông báo cho dialog
        builder.setMessage(message + "\n\n" + advice)
                .setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Đóng dialog khi nhấn nút "Đóng"
                        dialog.dismiss();
                        finish();
                    }
                });

        // Tạo và hiển thị AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }




}

