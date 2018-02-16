package com.yao.dribo.bucket;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yao.dribo.R;
import com.yao.dribo.base.BaseViewHolder;
import com.yao.dribo.base.InfiniteAdapter;
import com.yao.dribo.model.Bucket;
import com.yao.dribo.shot_list.ShotListFragment;

import java.util.List;

/**
 * apply the data from BucketListFragment to layout list_item_bucket
 */

public class BucketListAdapter extends InfiniteAdapter<Bucket> {

    private boolean isChoosingMode;

    public BucketListAdapter(@NonNull Context context,
                             @NonNull List<Bucket> data,
                             @NonNull LoadMoreListener loadMoreListener,
                             boolean isChoosingMode) {
        super(context, data, loadMoreListener);
        this.isChoosingMode = isChoosingMode;
    }


    @Override
    protected BaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_bucket, parent, false);
        return new BucketViewHolder(view);
    }

    @Override
    protected void onBindItemViewHolder(BaseViewHolder holder, final int position) {
        final Bucket bucket = getData().get(position);
        final BucketViewHolder bucketViewHolder = (BucketViewHolder) holder;

        bucketViewHolder.bucketName.setText(bucket.name);
        bucketViewHolder.bucketShotCount.setText(formatShotCount(bucket.shots_count));

        if (isChoosingMode) {
            bucketViewHolder.bucketShotChosen.setVisibility(View.VISIBLE);
            bucketViewHolder.bucketShotChosen.setImageDrawable(
                    bucket.isChoosing
                            ? ContextCompat.getDrawable(getContext(), R.drawable.ic_check_box_black_24dp)
                            : ContextCompat.getDrawable(getContext(), R.drawable.ic_check_box_outline_blank_black_24dp));
            bucketViewHolder.bucketLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bucket.isChoosing = !bucket.isChoosing;
                    notifyDataSetChanged();
                }
            });
        } else {
            bucketViewHolder.bucketShotChosen.setVisibility(View.GONE);
            bucketViewHolder.bucketLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), BucketShotListActivity.class);
                    intent.putExtra(ShotListFragment.KEY_BUCKET_ID, bucket.id);
                    intent.putExtra(BucketShotListActivity.KEY_BUCKET_NAME, bucket.name);
                    getContext().startActivity(intent);
                }
            });
        }

    }

    private String formatShotCount(int shotsCount) {
        return shotsCount == 0
                ? getContext().getString(R.string.shot_count_single, shotsCount)
                : getContext().getString(R.string.shot_count_plural, shotsCount);
    }



}
