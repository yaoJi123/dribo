package com.yao.dribo.shot_list;

import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.yao.dribo.R;
import com.yao.dribo.base.BaseViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;




public class ShotViewHolder extends BaseViewHolder{
    @BindView(R.id.shot_clickable_cover) public View cover;
    @BindView(R.id.shot_like_count) public TextView likeCount;
    @BindView(R.id.shot_bucket_count) public TextView bucketCount;
    @BindView(R.id.shot_view_count) public TextView viewCount;
    @BindView(R.id.shot_image) public SimpleDraweeView image;

    public ShotViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
