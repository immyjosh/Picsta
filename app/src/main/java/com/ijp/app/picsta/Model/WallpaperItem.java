package com.ijp.app.picsta.Model;

public class WallpaperItem {
    public String imageUrl;
    public String categoryId;

    public WallpaperItem() {
    }

    public WallpaperItem(String imageUrl, String categoryId) {
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }

    public String getImagelink() {
        return imageUrl;
    }

    public void setImagelink(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
