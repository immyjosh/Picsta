package com.ijp.app.picsta.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.ijp.app.picsta.Interface.ItemClickListener;
import com.ijp.app.picsta.R;

public class ListWallpaperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    ItemClickListener itemClickListener;
    public ImageView wallpaper;

    public void setItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener=itemClickListener;
    }

    public ListWallpaperViewHolder(View itemView) {
        super(itemView);
        wallpaper=itemView.findViewById(R.id.image_wallpaper);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition());
    }
}
