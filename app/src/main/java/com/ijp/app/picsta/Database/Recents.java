package com.ijp.app.picsta.Database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "recents",primaryKeys = {"imageUrl","categoryId"})
public class Recents {

    @ColumnInfo(name = "imageUrl")
    @NonNull
    private String imageUrl;

    @ColumnInfo(name = "categoryId")
    @NonNull
    private String categoryId;

    @ColumnInfo(name = "saveTime")
    private String saveTime;

    public Recents(@NonNull String imageUrl, @NonNull String categoryId, String saveTime) {
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.saveTime = saveTime;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@NonNull String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @NonNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(String saveTime) {
        this.saveTime = saveTime;
    }
}
