package com.yao.dribo.shot_main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.yao.dribo.R;
import com.yao.dribo.model.Shot;

/**
 * apply the data from ShotFragment to layout shot_item_image and layout shot_item_info
 */

public class ShotAdapter extends RecyclerView.Adapter{
    private static final int VIEW_TYPE_SHOT_IMAGE = 0;
    private static final int VIEW_TYPE_SHOT_INFO = 1;

    private final ShotFragment shotFragment;
    private final Shot shot;


    public ShotAdapter(@NonNull Shot shot,
                       @NonNull ShotFragment shotFragment) {
        this.shot = shot;
        this.shotFragment = shotFragment;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_SHOT_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shot_item_image, parent, false);
                return new ImageViewHolder(view);
            case VIEW_TYPE_SHOT_INFO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shot_item_info, parent, false);
                return new InfoViewHolder(view);
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_SHOT_IMAGE:
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.parse(shot.getImageUrl()))
                        .setAutoPlayAnimations(true)
                        .build();
                ((ImageViewHolder) holder).image.setController(controller);
                break;
            case VIEW_TYPE_SHOT_INFO:
                final InfoViewHolder shotDetailViewHolder = (InfoViewHolder) holder;
                shotDetailViewHolder.title.setText(shot.title);
                shotDetailViewHolder.authorName.setText(shot.user.name);
                shotDetailViewHolder.description.setText(
                        Html.fromHtml(shot.description == null ? "" : shot.description));
                shotDetailViewHolder.description.setMovementMethod(LinkMovementMethod.getInstance());
                shotDetailViewHolder.authorPicture.setImageURI(Uri.parse(shot.user.avatar_url));

                shotDetailViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
                shotDetailViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
                shotDetailViewHolder.viewCount.setText(String.valueOf(shot.views_count));
                shotDetailViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shotFragment.share();
                    }
                });

                shotDetailViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shotFragment.like(shot.id, !shot.liked);
                    }
                });
                shotDetailViewHolder.bucketButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shotFragment.bucket();
                    }
                });

                Drawable likeDrawable = shot.liked
                        ? ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_dribbble_18dp)
                        : ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_border_black_18dp);
                shotDetailViewHolder.likeButton.setImageDrawable(likeDrawable);

                Drawable bucketDrawable = shot.bucketed
                        ? ContextCompat.getDrawable(getContext(), R.drawable.ic_inbox_dribbble_18dp)
                        : ContextCompat.getDrawable(getContext(), R.drawable.ic_inbox_black_18dp);
                shotDetailViewHolder.bucketButton.setImageDrawable(bucketDrawable);
                break;
        }

    }

    @NonNull
    private Context getContext() {
        return shotFragment.getContext();
    }

//    private void bucket(Context context) {
//        if (collectedBucketIds != null) {
//            Intent intent = new Intent(context, ChooseBucketActivity.class);
//            intent.putStringArrayListExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS,
//                    collectedBucketIds);
//            shotFragment.startActivityForResult(intent, shotFragment.REQ_CODE_BUCKET);
//        }
//    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_SHOT_IMAGE;
        } else {
            return VIEW_TYPE_SHOT_INFO;
        }
    }

//    private void share (Context context) {
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.putExtra(Intent.EXTRA_TEXT, shot.title + " " + shot.html_url);
//        shareIntent.setType("text/plain");
//        context.startActivity(Intent.createChooser(shareIntent,context.getString(R.string.share_shot)));
//    }

//    public void updateCollectedBucketIds(List<String> bucketIds) {
//        if (collectedBucketIds == null) {
//            collectedBucketIds = new ArrayList<>();
//        }
//
//        collectedBucketIds.clear();
//        collectedBucketIds.addAll(bucketIds);
//
//
//        shot.bucketed = !bucketIds.isEmpty();
//        notifyDataSetChanged();
//    }

//    public void updateCollectedBucketIds (@NonNull List<String> addedIds,
//                                          @NonNull List<String> removedIds) {
//        if (collectedBucketIds == null) {
//            collectedBucketIds = new ArrayList<>();
//        }
//
//        collectedBucketIds.addAll(addedIds);
//        collectedBucketIds.removeAll(removedIds);
//
//        shot.bucketed = !collectedBucketIds.isEmpty();
//        shot.buckets_count += addedIds.size() - removedIds.size();
//        notifyDataSetChanged();
//    }
//
//    public List<String> getReadOnlyCollectedBucketIds() {
//        return Collections.unmodifiableList(collectedBucketIds);
//    }
}
