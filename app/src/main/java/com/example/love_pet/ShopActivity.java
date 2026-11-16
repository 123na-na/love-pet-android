package com.example.love_pet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 商城页面Activity
 */
public class ShopActivity extends BaseActivity {

    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_shop;
    }

    @Override
    protected void initViews() {
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 搜索框
        findViewById(R.id.et_search).setOnClickListener(v -> {
            // TODO: 跳转到搜索页面
            showToast("搜索功能开发中");
        });

        // 扫码按钮
        findViewById(R.id.btn_scan).setOnClickListener(v -> {
            showToast("扫码功能开发中");
        });

        // 购物车按钮
        findViewById(R.id.btn_cart).setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // 分类点击事件
        setupCategoryClickListeners();

        // 商品列表
        rvProducts = findViewById(R.id.rv_products);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(this, productList);
        rvProducts.setAdapter(productAdapter);

        // 加载商品数据
        loadProducts();
    }

    /**
     * 设置分类点击监听器
     */
    private void setupCategoryClickListeners() {
        int[] categoryIds = {
                R.id.category_villa,
                R.id.category_milk,
                R.id.category_bowl,
                R.id.category_snack,
                R.id.category_clothes,
                R.id.category_medicine,
                R.id.category_care,
                R.id.category_towel
        };

        String[] categoryNames = { "别墅", "牛奶", "饭盆", "零食", "衣服", "药物", "洗护", "毛巾" };

        for (int i = 0; i < categoryIds.length; i++) {
            final int index = i;
            final String name = categoryNames[i];
            findViewById(categoryIds[i]).setOnClickListener(v -> {
                showToast("进入" + name + "分类");
                // TODO: 跳转到对应分类的商品列表
            });
        }
    }

    /**
     * 加载商品数据
     */
    private void loadProducts() {
        // 使用数据导入器
        ProductDataImporter importer = new ProductDataImporter(this);

        // 如果数据库为空，从 JSON 文件导入
        if (importer.needImport()) {
            int count = importer.importFromJson();
            if (count > 0) {
                showToast("成功导入 " + count + " 件商品");
            }
        }

        // 从数据库加载商品
        productList.addAll(importer.getAllProducts());
        productAdapter.notifyDataSetChanged();
    }

    // ==================== 商品数据模型 ====================

    public static class Product {
        public int id;
        public String title;
        public String spec;
        public String price;
        public String location;
        public String tags;
        public String shopName;
        public String brand;
        public int imageRes;
        public int shopAvatar;

        public Product(int id, String title, String spec, String price, String location,
                       String tags, String shopName, String brand, int imageRes) {
            this.id = id;
            this.title = title;
            this.spec = spec;
            this.price = price;
            this.location = location;
            this.tags = tags;
            this.shopName = shopName;
            this.brand = brand;
            this.imageRes = imageRes;
            this.shopAvatar = getShopAvatarRes(shopName);
        }

        public static int getShopAvatarRes(String shopName) {
            if (shopName == null) return R.drawable.ic_paw_black;
            if ("官方旗舰店".equals(shopName)) return R.drawable.shop3;
            if ("木屋旺旗舰店".equals(shopName)) return R.drawable.shop4;
            if ("帕特旗舰店".equals(shopName)) return R.drawable.shop2;
            if ("超宠爱旗舰店".equals(shopName)) return R.drawable.shop1;
            return R.drawable.ic_paw_black;
        }
    }

    // ==================== 商品适配器 ====================

    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

        private Context context;
        private List<Product> products;

        public ProductAdapter(Context context, List<Product> products) {
            this.context = context;
            this.products = products;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_product_card, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Product product = products.get(position);

            // 设置商品图片
            holder.ivImage.setImageResource(product.imageRes);

            // 设置品牌标签（如果有）
            if (product.brand != null && !product.brand.isEmpty()) {
                holder.tvBrand.setText(product.brand);
                holder.tvBrand.setVisibility(View.VISIBLE);
            } else {
                holder.tvBrand.setVisibility(View.GONE);
            }

            // 设置商品标题
            holder.tvTitle.setText(product.title);

            // 设置规格（如果有）
            if (product.spec != null && !product.spec.isEmpty()) {
                holder.tvSpec.setText(product.spec);
                holder.tvSpec.setVisibility(View.VISIBLE);
            } else {
                holder.tvSpec.setVisibility(View.GONE);
            }

            // 设置价格
            holder.tvPrice.setText(product.price);

            // 设置位置
            holder.tvLocation.setText(product.location);

            // 设置标签
            setupTags(holder.layoutTags, holder.tag1, holder.tag2, product.tags);

            // 设置店铺名称和头像
            holder.tvShopName.setText(product.shopName);
            holder.ivShopAvatar.setImageResource(product.shopAvatar);

            // 点击事件
            holder.itemView.setOnClickListener(v -> {
                // 跳转到商品详情页
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productId", product.id); // 使用真实数据库ID
                context.startActivity(intent);
            });
        }

        /**
         * 设置标签显示
         */
        private void setupTags(LinearLayout layoutTags, TextView tag1, TextView tag2, String tagsText) {
            if (tagsText == null || tagsText.isEmpty()) {
                layoutTags.setVisibility(View.GONE);
                return;
            }

            layoutTags.setVisibility(View.VISIBLE);

            // 解析标签（用换行符或空格分隔）
            String[] tagArray = tagsText.split("[\n ]+");

            if (tagArray.length > 0 && !tagArray[0].isEmpty()) {
                tag1.setText(tagArray[0]);
                tag1.setVisibility(View.VISIBLE);
            } else {
                tag1.setVisibility(View.GONE);
            }

            if (tagArray.length > 1 && !tagArray[1].isEmpty()) {
                tag2.setText(tagArray[1]);
                tag2.setVisibility(View.VISIBLE);
            } else {
                tag2.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        static class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvBrand;
            TextView tvTitle;
            TextView tvSpec;
            TextView tvPrice;
            TextView tvLocation;
            LinearLayout layoutTags;
            TextView tag1;
            TextView tag2;
            ImageView ivShopAvatar;
            TextView tvShopName;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.iv_product_image);
                tvBrand = itemView.findViewById(R.id.tv_brand);
                tvTitle = itemView.findViewById(R.id.tv_product_title);
                tvSpec = itemView.findViewById(R.id.tv_product_spec);
                tvPrice = itemView.findViewById(R.id.tv_price);
                tvLocation = itemView.findViewById(R.id.tv_location);
                layoutTags = itemView.findViewById(R.id.layout_tags);
                tag1 = itemView.findViewById(R.id.tag_1);
                tag2 = itemView.findViewById(R.id.tag_2);
                ivShopAvatar = itemView.findViewById(R.id.iv_shop_avatar);
                tvShopName = itemView.findViewById(R.id.tv_shop_name);
            }
        }
    }
}
