package com.example.doancuoiky_mobile.helper;

import com.example.doancuoiky_mobile.model.Topic;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {

    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Thêm Topic vào Firestore từ đối tượng Topic
    public void addTopic(Topic topic, FirestoreCallback callback) {
        // Tạo một document mới trong collection "topics"
        DocumentReference topicRef = db.collection("topics").document();  // Tạo document mới với auto-generated ID

        // Tạo dữ liệu của Topic dưới dạng Map
        Map<String, Object> topicData = new HashMap<>();
        topicData.put("name", topic.getName());
        topicData.put("description", topic.getDescription());
        topicData.put("privacy", topic.getPrivacy());
        topicData.put("createdAt", Timestamp.now());
        topicData.put("vocabularyCount", 0);  // Bắt đầu với 0 từ vựng

        // Thêm document vào Firestore
        topicRef.set(topicData)
                .addOnSuccessListener(aVoid -> {
                    // Nếu thành công, thông báo đã thêm topic
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    // Nếu thất bại, thông báo lỗi
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    // Thêm Vocabulary vào Subcollection của Topic
    public void addVocabularyToTopic(String topicId, String word, String meaning, String example) {
        // Lấy reference đến document của Topic
        DocumentReference topicRef = db.collection("topics").document(topicId);

        // Tạo một document mới trong subcollection "vocabularies"
        CollectionReference vocabRef = topicRef.collection("vocabularies");

        // Dữ liệu vocabulary
        Map<String, Object> vocabularyData = new HashMap<>();
        vocabularyData.put("word", word);
        vocabularyData.put("meaning", meaning);
        vocabularyData.put("example", example);
        vocabularyData.put("createdAt", Timestamp.now());

        // Thêm vocabulary vào subcollection "vocabularies"
        vocabRef.add(vocabularyData)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Thêm từ vựng mới thành công");

                    // Cập nhật vocabularyCount khi thêm từ vựng
                    updateVocabularyCount(topicId);
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error adding vocabulary: " + e.getMessage());
                });
    }

    // Cập nhật số lượng từ vựng trong Topic
    private void updateVocabularyCount(String topicId) {
        // Lấy reference đến document của Topic
        DocumentReference topicRef = db.collection("topics").document(topicId);

        // Lấy số lượng từ vựng hiện tại
        topicRef.collection("vocabularies").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int newCount = queryDocumentSnapshots.size();  // Số lượng từ vựng hiện tại
                    topicRef.update("vocabularyCount", newCount)  // Cập nhật "vocabularyCount"
                            .addOnSuccessListener(aVoid -> {
                                System.out.println("Vocabulary count updated successfully!");
                            })
                            .addOnFailureListener(e -> {
                                System.err.println("Error updating vocabulary count: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error getting vocabularies: " + e.getMessage());
                });
    }
}
