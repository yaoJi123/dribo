package com.yao.dribo.shot_main;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.yao.dribo.R;
import com.yao.dribo.Utils.ModelUtils;
import com.yao.dribo.auth.AuthException;
import com.yao.dribo.auth.AuthFunctions;
import com.yao.dribo.base.AuthTask;
import com.yao.dribo.bucket.BucketListFragment;
import com.yao.dribo.bucket.ChooseBucketActivity;
import com.yao.dribo.model.Bucket;
import com.yao.dribo.model.Shot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Think on 2017/6/28.
 */

public class ShotFragment extends Fragment{

    public static final int REQ_CODE_BUCKET = 100;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    public static final String KEY_SHOT = "shot";

    private ShotAdapter adapter;
    private Shot shot;
    private ArrayList<String> collectedBucketIds;
    private boolean isLiking;

    public static ShotFragment newInstance (@NonNull Bundle args) {
        ShotFragment fragment = new ShotFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        shot = ModelUtils.toObject(getArguments().getString(KEY_SHOT), new TypeToken<Shot>(){});

        adapter = new ShotAdapter(shot, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        isLiking = true;
        AsyncTaskCompat.executeParallel(new CheckLikeTask());
        AsyncTaskCompat.executeParallel(new LoadBucketsTask());

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_BUCKET && resultCode == Activity.RESULT_OK) {
            List<String> chosenBucketIds = data.getStringArrayListExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS);
            List<String> addedBucketIds = new ArrayList<>();
            List<String> removeBuckedIds = new ArrayList<>();


            for (String chosenBucketId: chosenBucketIds) {
                if (!collectedBucketIds.contains(chosenBucketId)) {
                    addedBucketIds.add(chosenBucketId);
                }
            }

            for (String collectedBucketId : collectedBucketIds) {
                if (!chosenBucketIds.contains(collectedBucketId)) {
                    removeBuckedIds.add(collectedBucketId);
                }
            }

            AsyncTaskCompat.executeParallel(new UpdateBucketTask(addedBucketIds, removeBuckedIds));
        }
    }

    private void setResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_SHOT, ModelUtils.toString(shot, new TypeToken<Shot>(){}));
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
    }

    public void like(String shotId, boolean like) {
        if (!isLiking) {
            isLiking = true;
            AsyncTaskCompat.executeParallel(new LikeTask(shotId, like));
        }
    }

    public void bucket() {
        if (collectedBucketIds == null) {
            Snackbar.make(getView(), R.string.shot_detail_loading_buckets, Snackbar.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(getContext(), ChooseBucketActivity.class);
            intent.putExtra(BucketListFragment.KEY_CHOOSING_MODE, true);
            intent.putStringArrayListExtra(BucketListFragment.KEY_COLLECTED_BUCKET_IDS,
                    collectedBucketIds);
            startActivityForResult(intent, REQ_CODE_BUCKET);
        }
    }

    public void share() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shot.title + " " + shot.html_url);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_shot)));
    }


    private class UpdateBucketTask extends AuthTask<Void, Void, Void> {

        private List<String> added;
        private List<String> removed;

        private UpdateBucketTask(@NonNull List<String> added,
                                 @NonNull List<String> removed) {
            this.added = added;
            this.removed = removed;
        }

        @Override
        protected Void doJob(Void... params) throws AuthException {
            for (String addedId : added) {
                AuthFunctions.addBucketShot(addedId, shot.id);
            }

            for (String removedId : removed) {
                AuthFunctions.removeBucketShot(removedId, shot.id);
            }
            return null;
        }

        @Override
        protected void onSuccess(Void aVoid) {
            collectedBucketIds.addAll(added);
            collectedBucketIds.removeAll(removed);

            shot.bucketed = !collectedBucketIds.isEmpty();
            shot.buckets_count += added.size() - removed.size();

            recyclerView.getAdapter().notifyDataSetChanged();

            setResult();
        }

        @Override
        protected void onFailed(AuthException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private class LikeTask  extends AuthTask<Void, Void, Void> {
        private String id;
        private boolean like;

        public LikeTask(String id, boolean like) {
            this.id = id;
            this.like = like;
        }

        @Override
        protected Void doJob(Void... params) throws AuthException {
            if (like) {
                AuthFunctions.likeShot(id);
            } else {
                AuthFunctions.unlikeShot(id);
            }
            return null;
        }

        @Override
        protected void onSuccess(Void s) {
            isLiking = false;

            shot.liked = like;
            shot.likes_count += like ? 1 : -1;
            recyclerView.getAdapter().notifyDataSetChanged();

            setResult();
        }

        @Override
        protected void onFailed(AuthException e) {
            isLiking = false;
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private class CheckLikeTask extends AuthTask<Void, Void, Boolean> {

        @Override
        protected Boolean doJob(Void... params) throws AuthException {
            return AuthFunctions.isLikingShot(shot.id);
        }

        @Override
        protected void onSuccess(Boolean result) {
            isLiking = false;
            shot.liked = result;
            recyclerView.getAdapter().notifyDataSetChanged();
        }

        @Override
        protected void onFailed(AuthException e) {
            isLiking = false;
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private class LoadBucketsTask extends AuthTask<Void, Void, List<String>> {

        @Override
        protected List<String> doJob(Void... params) throws AuthException {
            List<Bucket> shotBuckets = AuthFunctions.getShotBuckets(shot.id);
            List<Bucket> userBuckets = AuthFunctions.getUserBuckets();

            Set<String> userBucketIds = new HashSet<>();
            for (Bucket userBucket : userBuckets) {
                userBucketIds.add(userBucket.id);
            }

            List<String> collectedBucketIds = new ArrayList<>();
            for (Bucket shotBucket : shotBuckets) {
                if (userBucketIds.contains(shotBucket.id)) {
                    collectedBucketIds.add(shotBucket.id);
                }
            }

            return collectedBucketIds;
        }

        @Override
        protected void onSuccess(List<String> result) {
            collectedBucketIds = new ArrayList<>(result);

            if (result.size() > 0) {
                shot.bucketed = true;
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }

        @Override
        protected void onFailed(AuthException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }
}
