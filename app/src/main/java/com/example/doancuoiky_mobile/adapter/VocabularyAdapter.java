package com.example.doancuoiky_mobile.adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.doancuoiky_mobile.R;
import com.example.doancuoiky_mobile.model.Vocabulary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    private List<Vocabulary> vocabularyList; // Danh sách từ vựng gốc
    private List<Vocabulary> favoriteList = new ArrayList<>(); // Danh sách các từ vựng yêu thích
    private TextToSpeech textToSpeech;

    public VocabularyAdapter(List<Vocabulary> vocabularyList, Context context) {
        this.vocabularyList = vocabularyList;

        // Khởi tạo TextToSpeech
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Set ngôn ngữ cho TextToSpeech
                int langResult = textToSpeech.setLanguage(Locale.US);
                if (langResult == TextToSpeech.LANG_MISSING_DATA
                        || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Xử lý khi ngôn ngữ không khả dụng
                }
            }
        });
    }

    @Override
    public VocabularyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate item_vocabulary.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VocabularyViewHolder holder, int position) {
        Vocabulary vocabulary = vocabularyList.get(position);

        holder.tvEnglishWord.setText(vocabulary.getEnglishWord());
        holder.tvVietnameseWord.setText(vocabulary.getVietnameseWord());

        // Set trạng thái icon yêu thích
        holder.btnFavorite.setImageResource(
                vocabulary.isStarred() ? R.drawable.ic_favourite_filled : R.drawable.ic_favorite
        );

        // Xử lý sự kiện phát âm
        holder.btnPlaySound.setOnClickListener(v -> {
            textToSpeech.speak(vocabulary.getEnglishWord(), TextToSpeech.QUEUE_FLUSH, null, null);
        });

        // Xử lý sự kiện đánh dấu yêu thích
        holder.btnFavorite.setOnClickListener(v -> {
            boolean isStarred = !vocabulary.isStarred();
            vocabulary.setStarred(isStarred);

            // Cập nhật lại icon
            holder.btnFavorite.setImageResource(
                    isStarred ? R.drawable.ic_favourite_filled : R.drawable.ic_favorite
            );

            // Quản lý danh sách favoriteList
            if (isStarred) {
                if (!favoriteList.contains(vocabulary)) {
                    favoriteList.add(vocabulary);
                    notifyDataSetChanged(); // Thêm vào danh sách yêu thích
                }
            } else {
                favoriteList.remove(vocabulary);
                notifyDataSetChanged(); // Xóa khỏi danh sách yêu thích
            }
        });
    }

    public void updateFavoriteList(List<Vocabulary> newFavoriteList) {
        this.favoriteList.clear();
        this.favoriteList.addAll(newFavoriteList);
        notifyDataSetChanged(); // Thông báo RecyclerView cập nhật dữ liệu
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

    /**
     * Phương thức trả về danh sách các từ vựng đã đánh dấu yêu thích
     */
    public List<Vocabulary> getFavoriteList() {
        return new ArrayList<>(favoriteList); // Trả về danh sách sao chép để tránh thay đổi gốc
    }

    public static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        TextView tvEnglishWord, tvVietnameseWord;
        ImageView ivImage;
        ImageButton btnPlaySound, btnFavorite;

        public VocabularyViewHolder(View itemView) {
            super(itemView);
            tvEnglishWord = itemView.findViewById(R.id.tvEnglishWord);
            tvVietnameseWord = itemView.findViewById(R.id.tvVietnameseWord);
            btnPlaySound = itemView.findViewById(R.id.btnPlaySound);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }

    /**
     * Giải phóng tài nguyên TextToSpeech
     */
    public void releaseResources() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
