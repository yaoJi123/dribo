package com.yao.dribo.bucket;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yao.dribo.R;
import com.yao.dribo.base.BaseViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Bind the views in BucketListAdapter
 */

public class BucketViewHolder extends BaseViewHolder{

    @BindView(R.id.bucket_layout) View bucketLayout;
    @BindView(R.id.bucket_name) TextView bucketName;
    @BindView(R.id.bucket_shot_count) TextView bucketShotCount;
    @BindView(R.id.bucket_shot_chosen) ImageView bucketShotChosen;

    public BucketViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
