package com.example.doancuoiky_mobile.adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doancuoiky_mobile.FlashcardActivity;
import com.example.doancuoiky_mobile.R;
import com.example.doancuoiky_mobile.model.FlashCard;

import java.util.List;
import java.util.Locale;

public class FlashCardAdapter extends RecyclerView.Adapter<FlashCardAdapter.FlashCardViewHolder> {

    private final List<FlashCard> flashCardList;
    private TextToSpeech textToSpeech = null;
    private Context context;

    public FlashCardAdapter(List<FlashCard> flashCardList, Context context) {
        this.flashCardList = flashCardList;
        this.context = context;

        // Khởi tạo TextToSpeech
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Xử lý khi ngôn ngữ không được hỗ trợ
                }
            }
        });
    }

    @Override
    public FlashCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new FlashCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlashCardViewHolder holder, int position) {
        FlashCard flashCard = flashCardList.get(position);

        // Mặc định hiển thị mặt trước
        holder.wordTitle.setText(flashCard.getEnglishWord());
        holder.wordDefinition.setText(flashCard.getVietnameseWord());

        // Hiển thị hình ảnh ở cả hai mặt
        if (flashCard.getImageUrl() != null && !flashCard.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(flashCard.getImageUrl())
                    .into(holder.wordImageFront); // Hình ảnh mặt trước

            Glide.with(holder.itemView.getContext())
                    .load(flashCard.getImageUrl())
                    .into(holder.wordImageBack); // Hình ảnh mặt sau
        } else {
            holder.wordImageFront.setImageResource(R.drawable.img_placeholder);
            holder.wordImageBack.setImageResource(R.drawable.img_placeholder);
        }

        // Xử lý sự kiện phát âm từ ở mặt trước
        holder.btnPlaySoundFront.setOnClickListener(v -> {
            textToSpeech.speak(flashCard.getEnglishWord(), TextToSpeech.QUEUE_FLUSH, null, null);
        });

        // Xử lý sự kiện phát âm từ ở mặt sau
        holder.btnPlaySoundBack.setOnClickListener(v -> {
            textToSpeech.speak(flashCard.getVietnameseWord(), TextToSpeech.QUEUE_FLUSH, null, null);
        });

        // Sự kiện khi click vào CardView để thay đổi mặt
        holder.flashCardContainer.setOnClickListener(v -> {
            // Thay đổi trạng thái mặt flashcard
            flashCard.setFlipped(!flashCard.isFlipped());
            notifyItemChanged(position);
            holder.wordDefinition.setText(flashCard.getVietnameseWord());

            // Cập nhật lại việc lật thẻ thông qua Activity
            ((FlashcardActivity) context).flipCard(holder.frontView, holder.backView);
        });

        // Kiểm tra mặt nào đang được hiển thị
        if (flashCard.isFlipped()) {
            holder.frontView.setVisibility(View.GONE);
            holder.backView.setVisibility(View.VISIBLE);
        } else {
            holder.frontView.setVisibility(View.VISIBLE);
            holder.backView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return flashCardList.size();
    }

    public static class FlashCardViewHolder extends RecyclerView.ViewHolder {
        // Mặt trước
        TextView wordTitle;
        ImageView wordImageFront;
        ImageButton btnPlaySoundFront;

        // Mặt sau
        TextView wordDefinition;
        ImageView wordImageBack;
        ImageButton btnPlaySoundBack;
        CardView flashCardContainer;

        // Các view cần thiết để lật thẻ
        LinearLayout frontView, backView;

        public FlashCardViewHolder(View itemView) {
            super(itemView);

            // Mặt trước
            wordTitle = itemView.findViewById(R.id.word_title);
            wordImageFront = itemView.findViewById(R.id.word_image);
            btnPlaySoundFront = itemView.findViewById(R.id.btnPlaySound);

            // Mặt sau
            wordDefinition = itemView.findViewById(R.id.word_definition);
            wordImageBack = itemView.findViewById(R.id.word_image2);
            btnPlaySoundBack = itemView.findViewById(R.id.btnPlaySound2);
            flashCardContainer = itemView.findViewById(R.id.flashcard_container);

            // Ánh xạ các view mặt trước và mặt sau
            frontView = itemView.findViewById(R.id.front_layout); // Thêm ánh xạ view mặt trước
            backView = itemView.findViewById(R.id.back_layout);   // Thêm ánh xạ view mặt sau
        }
    }

    public void releaseResources() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}

