package com.example.doancuoiky_mobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doancuoiky_mobile.Course1;
import com.example.doancuoiky_mobile.R;
import com.example.doancuoiky_mobile.model.Folder;

import java.util.List;

// Trong FolderAdapter.java
public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private Context context;
    private List<Folder> folderList;
    private OnFolderClickListener listener;

    public FolderAdapter(Context context, List<Folder> folderList, OnFolderClickListener listener) {
        this.context = context;
        this.folderList = folderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folderList.get(position);
        holder.folderNameTextView.setText(folder.getName());

        // Lắng nghe sự kiện click vào item folder
        holder.itemView.setOnClickListener(v -> listener.onFolderClick(folder));
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public interface OnFolderClickListener {
        void onFolderClick(Folder folder);
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {

        TextView folderNameTextView;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.itemName);
        }
    }
}

