package com.example.doancuoiky_mobile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doancuoiky_mobile.adapter.FlashCardAdapter;
import com.example.doancuoiky_mobile.helper.CloudinaryManager;
import com.example.doancuoiky_mobile.model.FlashCard;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


public class FlashcardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout dialogAddFlashCard;
    private ImageButton btnAddNewFlashCard;
    private Uri imageUri;
    private ImageButton btnUploadImage;
    private EditText englishWordEditText, vietnameseWordEditText;
    private TextView uploadedImageUrlTextView;
    public TextView topicNameTV;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_WRITE_PERMISSION = 101;

    private ActivityResultLauncher<Intent> imagePickerLauncherr;

    private TextToSpeech textToSpeech;
    private boolean isFrontVisible = true;
    public String folderName;
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            if (imageUri != null) {
                                handleImageUpload(imageUri);
                            } else {
                                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);// Liên kết với activity_flashcard.xml

        // Tìm nút Back và thiết lập sự kiện khi nhấn
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> onBackPressed());

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });
        // Đăng ký launcher
        imagePickerLauncherr = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleImageUpload(imageUri);
                        } else {
                            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        // Find views by ID
        dialogAddFlashCard = findViewById(R.id.dialog_addFlashCard);
        btnAddNewFlashCard = findViewById(R.id.btn_addNewFlashCard);
        btnUploadImage = findViewById(R.id.btn_uploadImage);

        englishWordEditText = findViewById(R.id.englishWord);
        vietnameseWordEditText = findViewById(R.id.vietnameseWord);
        uploadedImageUrlTextView = findViewById(R.id.uploadedImageUrl);
        topicNameTV = findViewById(R.id.topicTitle);


        db = FirebaseFirestore.getInstance();

        // Check if permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            }
        }


        // Set up Cloudinary image upload
        btnUploadImage.setOnClickListener(v ->  openImageChooser());

        // Set up Save button
        Button btnSave = findViewById(R.id.btn_saveFlashCard);
        btnSave.setOnClickListener(v -> {


            // Lấy giá trị từ EditText
            String englishWord = englishWordEditText.getText().toString().trim();
            String vietnameseWord = vietnameseWordEditText.getText().toString().trim();
            String imageUrl = uploadedImageUrlTextView.getText().toString().trim();

            // Kiểm tra xem người dùng đã nhập đủ thông tin chưa
            if (englishWord.isEmpty() || vietnameseWord.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(FlashcardActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Gọi phương thức saveFlashCard với các tham số
                saveFlashCard(englishWord, vietnameseWord, imageUrl, folderName);
            }
        });


        // Set up Cancel button
        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialogAddFlashCard.setVisibility(View.GONE));

        // Set OnClickListener for the button to show dialog
        btnAddNewFlashCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the dialog by changing visibility to VISIBLE
                dialogAddFlashCard.setVisibility(View.VISIBLE);
            }
        });


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get topic name from Intent
        String topicName = getIntent().getStringExtra("TOPIC_NAME");
        folderName = getIntent().getStringExtra("folderName");
        topicNameTV.setText(topicName);


        // Fetch flashcards for the given topic from Firestore
        loadFlashCards(folderName, topicName);
    }

    private void handleImage(Uri imageUri) {
        // Lấy tham chiếu đến ImageView trong layout để hiển thị hình ảnh
        ImageView imageView = findViewById(R.id.imageView);

        // Sử dụng thư viện Glide hoặc Picasso để tải hình ảnh từ URI vào ImageView
        Glide.with(this)
                .load(imageUri)
                .into(imageView);
    }

    private void handleImageUpload(Uri imageUri) {
        // Đảm bảo rằng bạn đã có URI của hình ảnh
        if (imageUri != null) {
            // Tải lên Cloudinary
            CloudinaryManager.uploadImage(imageUri, this, new CloudinaryManager.OnImageUploadedListener() {
                @Override
                public void onImageUploaded(String imageUrl) {
                    // Cập nhật URL của ảnh lên giao diện
                    uploadedImageUrlTextView.setText(imageUrl);
                    uploadedImageUrlTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(FlashcardActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onImageUploadFailed(Exception e) {
                    // Xử lý lỗi nếu tải ảnh thất bại
                    Toast.makeText(FlashcardActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // Phương thức phát âm từ tiếng Anh
    private void speakOut(String text) {
        if (textToSpeech != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Toast.makeText(this, "Text is empty or TTS not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            CloudinaryManager.uploadImage(imageUri, this, new CloudinaryManager.OnImageUploadedListener() {
                @Override
                public void onImageUploaded(String imageUrl) {

                    uploadedImageUrlTextView.setText(imageUrl);
                    uploadedImageUrlTextView.setVisibility(View.VISIBLE);
                }
                @Override
                public void onImageUploadFailed(Exception e) {
                    Toast.makeText(FlashcardActivity.this, " " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void saveFlashCard(String englishWord, String vietnameseWord, String imageUrl, String folderName) {
        String topicName = getIntent().getStringExtra("TOPIC_NAME");  // Lấy topicName từ Intent
        if (topicName != null && folderName != null) {
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

                                                // Tạo FlashCard
                                                FlashCard flashCard = new FlashCard(englishWord, vietnameseWord, imageUrl);

                                                // Thêm FlashCard vào collection "flashcards" của topic
                                                firestore.collection("folders")
                                                        .document(folderId) // Sử dụng folderId
                                                        .collection("topics")
                                                        .document(topicId) // Sử dụng topicId
                                                        .collection("flashcards")
                                                        .add(flashCard)
                                                        .addOnSuccessListener(documentReference -> {
                                                            Toast.makeText(FlashcardActivity.this, "FlashCard saved!", Toast.LENGTH_SHORT).show();
                                                            dialogAddFlashCard.setVisibility(View.GONE);  // Hide dialog
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(FlashcardActivity.this, "Error saving FlashCard", Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                // Nếu không tìm thấy topic, thông báo lỗi

                                            }
                                        });
                            } else {
                            }
                        } else {
                            // Lỗi khi truy vấn Firestore

                        }
                    })
                    .addOnFailureListener(e -> {

                    });
        } else {
        }
    }


//
//    public void pickImage() {
//        // Mở trình chọn ảnh
//        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
//                .setMediaType(PickVisualMediaRequest.TYPE_IMAGES) // Chọn loại media là hình ảnh
//                .build());
//    }




    // Function to fetch flashcards from Firestore based on folderName and topicName
    private void loadFlashCards(String folderName, String topicName) {
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

                                            // Truy vấn flashcards bên trong topic
                                            firestore.collection("folders")
                                                     .document(folderId) // Sử dụng folderId
                                                    .collection("topics")
                                                    .document(topicId) // Sử dụng topicId
                                                    .collection("flashcards")
                                                    .get()
                                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                                            List<FlashCard> flashCardList = new ArrayList<>();
                                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                                FlashCard flashCard = documentSnapshot.toObject(FlashCard.class);
                                                                if (flashCard != null) {
                                                                    flashCardList.add(flashCard);
                                                                }
                                                            }
                                                            // Hiển thị flashcards trong RecyclerView
                                                            displayFlashCards(flashCardList);
                                                        } else {
                                                            Toast.makeText(FlashcardActivity.this, "No flashcards found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(FlashcardActivity.this, "Failed to load flashcards", Toast.LENGTH_SHORT).show();
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


    private void displayFlashCards(List<FlashCard> flashCardList) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pass the list of flashcards and the context to the adapter
        FlashCardAdapter adapter = new FlashCardAdapter(flashCardList, this);
        recyclerView.setAdapter(adapter);
    }
    public void flipCard(View cardFront, View cardBack) {
        // Kiểm tra trạng thái của mặt thẻ
        if (isFrontVisible) {
            // Xoay từ mặt trước sang mặt sau
            cardFront.animate()
                    .rotationY(90)
                    .setDuration(300)
                    .withEndAction(() -> {
                        cardBack.setRotationY(-90); // Đặt mặt sau ở góc -90 độ
                        cardBack.animate().rotationY(0).setDuration(300).start(); // Xoay mặt sau từ -90 đến 0 độ
                    }).start();
        } else {
            // Xoay từ mặt sau sang mặt trước
            cardBack.animate()
                    .rotationY(90)
                    .setDuration(300)
                    .withEndAction(() -> {
                        cardFront.setRotationY(-90); // Đặt mặt trước ở góc -90 độ
                        cardFront.animate().rotationY(0).setDuration(300).start(); // Xoay mặt trước từ -90 đến 0 độ
                    }).start();
        }

        // Đổi trạng thái của biến isFrontVisible để theo dõi mặt hiện tại của thẻ
        isFrontVisible = !isFrontVisible;
    }

    // Đầu tiên, thay đổi phương thức mở trình chọn hình ảnh
    private void openImageChooser() {
        // Mở trình chọn hình ảnh từ thư viện
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"); // Sử dụng setDataAndType thay vì setType
        imagePickerLauncher.launch(intent); // Sử dụng ActivityResultLauncher thay vì startActivityForResult
    }





}
