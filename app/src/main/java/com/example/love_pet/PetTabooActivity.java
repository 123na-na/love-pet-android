package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class PetTabooActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_TITLE = "title";

    public static final String CATEGORY_FOOD = "food";
    public static final String CATEGORY_HOME = "home";
    public static final String CATEGORY_PLANT = "plant";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_taboo);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        LinearLayout categoryFood = findViewById(R.id.category_food);
        LinearLayout categoryHome = findViewById(R.id.category_home);
        LinearLayout categoryPlant = findViewById(R.id.category_plant);

        categoryFood.setOnClickListener(v -> openList(CATEGORY_FOOD, "食物类"));
        categoryHome.setOnClickListener(v -> openList(CATEGORY_HOME, "居家用品"));
        categoryPlant.setOnClickListener(v -> openList(CATEGORY_PLANT, "植物类"));
    }

    private void openList(String category, String title) {
        Intent intent = new Intent(this, PetTabooListActivity.class);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.putExtra(EXTRA_TITLE, title);
        startActivity(intent);
    }
}
