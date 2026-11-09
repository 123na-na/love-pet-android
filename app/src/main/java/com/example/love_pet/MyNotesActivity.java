package com.example.love_pet;

import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class MyNotesActivity extends AppCompatActivity {

    private PetDataManager dataManager;
    private GridLayout gridNotes;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notes);

        dataManager = PetDataManager.getInstance(this);
        gridNotes = findViewById(R.id.grid_notes);
        layoutEmpty = findViewById(R.id.layout_empty);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        android.widget.ScrollView scrollView = findViewById(R.id.scroll_notes);
        if (scrollView != null) {
            scrollView.setPadding(0, 0, 0, dpToPx(140));
        }

        loadNotes();
    }

    private void loadNotes() {
        gridNotes.removeAllViews();

        String currentNickname = UserManager.getInstance().getCurrentNickname();
        List<PetDataManager.PetNoteEntity> notes = dataManager.getNotesByAuthor(currentNickname);

        if (notes.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            gridNotes.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        gridNotes.setVisibility(View.VISIBLE);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cardWidth = (screenWidth - dpToPx(24) - dpToPx(8)) / 2;
        int imageHeight = (int) (cardWidth * 4.0 / 3.0);

        for (int index = 0; index < notes.size(); index++) {
            PetDataManager.PetNoteEntity note = notes.get(index);
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_pet_card, gridNotes, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cardWidth;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMarginStart(index % 2 == 0 ? 0 : dpToPx(8));
            params.setMarginEnd(index % 2 == 0 ? dpToPx(8) : 0);
            params.topMargin = dpToPx(8);

            cardView.setLayoutParams(params);

            ImageView ivImage = cardView.findViewById(R.id.iv_pet_image);
            ivImage.getLayoutParams().height = imageHeight;

            String imageRes = note.getImageRes();
            if (imageRes != null && imageRes.startsWith("/")) {
                Glide.with(this).load(new File(imageRes)).into(ivImage);
            } else {
                int resId = dataManager.getResourceIdByName(imageRes);
                ivImage.setImageResource(resId);
            }

            ivImage.setClipToOutline(true);
            ivImage.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dpToPx(12));
                }
            });

            TextView tvTitle = cardView.findViewById(R.id.tv_title);
            tvTitle.setText(note.getTitle());

            ImageView ivAvatar = cardView.findViewById(R.id.iv_avatar);
            String avatarRes = note.getAvatarRes();
            if (avatarRes != null && avatarRes.startsWith("/")) {
                File avatarFile = new File(avatarRes);
                if (avatarFile.exists()) {
                    Glide.with(this).load(avatarFile).into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.nav_profile_normal);
                }
            } else {
                int resId = dataManager.getResourceIdByName(avatarRes);
                if (resId != 0) {
                    ivAvatar.setImageResource(resId);
                } else {
                    ivAvatar.setImageResource(R.drawable.nav_profile_normal);
                }
            }
            ivAvatar.setClipToOutline(true);
            ivAvatar.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });

            TextView tvUsername = cardView.findViewById(R.id.tv_username);
            tvUsername.setText(note.getAuthorName());

            ImageView ivLikeSmall = cardView.findViewById(R.id.iv_like_small);
            if (note.isLiked()) {
                ivLikeSmall.setImageResource(R.drawable.ic_heart_red);
            } else {
                ivLikeSmall.setImageResource(R.drawable.ic_like_normal);
            }

            TextView tvLikeCount = cardView.findViewById(R.id.tv_like_count);
            tvLikeCount.setText(String.valueOf(note.getLikeCount()));

            long noteId = note.getId();
            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(MyNotesActivity.this, DetailActivity.class);
                intent.putExtra("noteId", noteId);
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                intent.putExtra("username", note.getAuthorName());
                intent.putExtra("location", note.getLocation());
                intent.putExtra("publishTime", note.getPublishTime());
                intent.putExtra("likeCount", note.getLikeCount());
                intent.putExtra("collectCount", note.getCollectCount());
                intent.putExtra("commentCount", note.getCommentCount());
                intent.putExtra("imageRes", note.getImageRes());
                intent.putExtra("avatarRes", note.getAvatarRes());
                startActivity(intent);
            });

            cardView.setOnLongClickListener(v -> {
                showDeleteDialog(noteId);
                return true;
            });

            gridNotes.addView(cardView);
        }
    }

    private void showDeleteDialog(long noteId) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除这条笔记吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    boolean success = dataManager.deleteNote(noteId);
                    if (success) {
                        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                        loadNotes();
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}