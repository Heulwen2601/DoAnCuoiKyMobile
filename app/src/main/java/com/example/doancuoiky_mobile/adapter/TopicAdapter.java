package com.example.doancuoiky_mobile.adapter;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.example.doancuoiky_mobile.R;
import com.example.doancuoiky_mobile.model.Topic;
import com.example.doancuoiky_mobile.model.Vocabulary;

import java.util.ArrayList;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private List<Topic> topicList;
    private OnTopicClickListener listener;

    // Constructor nhận vào danh sách Topic và listener để xử lý click
    public TopicAdapter(List<Topic> topicList, OnTopicClickListener listener) {
        this.topicList = topicList;
        this.listener = listener;
    }

    @Override
    public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate layout item_course (item trong danh sách chủ đề)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TopicViewHolder holder, int position) {
        // Lấy đối tượng topic tại vị trí hiện tại
        Topic topic = topicList.get(position);
        holder.topicName.setText(topic.getName()); // Set tên chủ đề
        holder.topDescription.setText(topic.getDescription()); // Set mô tả chủ đề

        // Cập nhật số lượng từ vựng trong TextView
        int vocabularyCount = topic.getVocabularies() != null ? topic.getVocabularies().size() : 0;
        String vocabularyText = vocabularyCount + " từ vựng";
        holder.topDescription.setText(vocabularyText); // Cập nhật số lượng từ vựng

        // Lắng nghe sự kiện click và gọi phương thức onTopicClick khi người dùng nhấn vào item
        holder.itemView.setOnClickListener(v -> listener.onTopicClick(topic));

        // Hiển thị PopupMenu khi nhấn vào topicOptions
        holder.topicOptions.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.privacy_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> handleMenuClick(item, topic, view));
            popupMenu.show();
        });
    }

    // Xử lý các lựa chọn trong PopupMenu
    private boolean handleMenuClick(MenuItem item, Topic topic, View view) {
        int itemId = item.getItemId();
        if (itemId == R.id.option_private) {
            topic.setPrivacy("Private");
            Toast.makeText(view.getContext(), "Đã thiết lập chế độ Private cho topic " + topic.getName(), Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.option_public) {
            topic.setPrivacy("Public");
            Toast.makeText(view.getContext(), "Đã thiết lập chế độ Public cho topic " + topic.getName(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    // Phương thức cập nhật số lượng từ vựng khi thêm từ vựng mới
    public void addVocabulary(Vocabulary vocabulary, int position) {
        Topic topic = topicList.get(position);
        if (topic.getVocabularies() == null) {
            topic.setVocabularies(new ArrayList<>());
        }
        topic.getVocabularies().add(vocabulary);
        notifyItemChanged(position); // Cập nhật lại item ở vị trí đó
    }

    @Override
    public int getItemCount() {
        return topicList.size();  // Trả về số lượng topic trong danh sách
    }

    // Interface để xử lý sự kiện click
    public interface OnTopicClickListener {
        void onTopicClick(Topic topic);
    }
    // Thêm Interface mới cho hành động khác
    public interface OnTopicActionListener {
        void onDeleteTopic(Topic topic);
    }

    // ViewHolder chứa các view trong mỗi item
    public static class TopicViewHolder extends RecyclerView.ViewHolder {

        TextView topicName;
        TextView topDescription; // Sửa lại tên biến cho đúng với TextView hiển thị số lượng từ vựng
        ImageView topicOptions;

        public TopicViewHolder(View itemView) {
            super(itemView);
            topicName = itemView.findViewById(R.id.nameTopic);
            topDescription = itemView.findViewById(R.id.topDescription); // Sửa lại tên biến
            topicOptions = itemView.findViewById(R.id.topicOptions);
        }
    }
}