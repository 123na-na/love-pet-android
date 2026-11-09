package com.example.love_pet;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 已选图片列表适配器
 */
public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_ADD = 1;

    private List<Uri> imageList;
    private Context context;
    private OnImageRemoveListener removeListener;
    private OnAddImageListener addListener;

    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }

    public interface OnAddImageListener {
        void onAddImage();
    }

    public ImageAdapter(List<Uri> imageList, Context context) {
        this.imageList = imageList;
        this.context = context;
    }

    public void setOnImageRemoveListener(OnImageRemoveListener listener) {
        this.removeListener = listener;
    }

    public void setOnAddImageListener(OnAddImageListener listener) {
        this.addListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position < imageList.size() ? TYPE_IMAGE : TYPE_ADD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_add_image, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_publish_image, parent, false);
            return new ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            ImageViewHolder imageHolder = (ImageViewHolder) holder;
            Uri uri = imageList.get(position);
            
            imageHolder.ivImage.setImageURI(uri);
            
            imageHolder.ivDelete.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onImageRemove(position);
                }
            });
        } else if (holder instanceof AddViewHolder) {
            AddViewHolder addHolder = (AddViewHolder) holder;
            addHolder.ivAdd.setOnClickListener(v -> {
                if (addListener != null) {
                    addListener.onAddImage();
                }
            });
        }
    }

    private static final int MAX_IMAGES = 9;

    @Override
    public int getItemCount() {
        return imageList.size() >= MAX_IMAGES ? imageList.size() : imageList.size() + 1;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivDelete;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAdd;

        AddViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAdd = itemView.findViewById(R.id.iv_add_image);
        }
    }
}
