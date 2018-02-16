package com.yao.dribo.shot_main;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * bind the image shot in ShotFragment
 */

public class ImageViewHolder extends RecyclerView.ViewHolder{

    SimpleDraweeView image;
    public ImageViewHolder(View itemView) {
        super(itemView);
        image = (SimpleDraweeView) itemView;
    }
}
