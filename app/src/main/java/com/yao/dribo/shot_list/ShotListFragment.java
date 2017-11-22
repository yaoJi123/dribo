package com.yao.dribo.shot_list;

//import android.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.yao.dribo.R;
import com.yao.dribo.Utils.ModelUtils;
import com.yao.dribo.auth.AuthException;
import com.yao.dribo.auth.AuthFunctions;
import com.yao.dribo.base.AuthTask;
import com.yao.dribo.base.InfiniteAdapter;
import com.yao.dribo.base.SpaceItemDecoration;
import com.yao.dribo.model.Shot;
import com.yao.dribo.shot_main.ShotFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Think on 2017/6/19.
 */

public class ShotListFragment extends Fragment{

    public static final int COUNT_PER_PAGE = 12;
    public static final int REQ_CODE_SHOT = 100;
    public static final String KEY_LIST_TYPE = "listType";
    public static final String KEY_BUCKET_ID = "bucketId";

    public static final int LIST_TYPE_POPULAR = 1;
    public static final int LIST_TYPE_LIKED = 2;
    public static final int LIST_TYPE_BUCKET = 3;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_container) SwipeRefreshLayout swipeRefreshLayout;
    private ShotListAdapter adapter;

    private int listType;

    private InfiniteAdapter.LoadMoreListener onLoadMore = new InfiniteAdapter.LoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (AuthFunctions.isLoggedIn()) {
                AsyncTaskCompat.executeParallel(new LoadShotsTask(false));
            }
        }
    };

    public static ShotListFragment newInstance(int listType) {
        Bundle args = new Bundle();
        args.putInt(KEY_LIST_TYPE, listType);

        ShotListFragment fragment= new ShotListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ShotListFragment newBucketListInstance (@NonNull String bucketId) {
        Bundle args = new Bundle();
        args.putInt(KEY_LIST_TYPE, LIST_TYPE_BUCKET);
        args.putString(KEY_BUCKET_ID, bucketId);

        ShotListFragment fragment = new ShotListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SHOT && resultCode == Activity.RESULT_OK) {
            Shot updateShot = ModelUtils.toObject(data.getStringExtra(ShotFragment.KEY_SHOT),
                    new TypeToken<Shot>(){});
            for (Shot shot : adapter.getData()) {
                if (TextUtils.equals(shot.id, updateShot.id)) {
                    shot.likes_count = updateShot.likes_count;
                    shot.buckets_count = updateShot.buckets_count;
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listType = getArguments().getInt(KEY_LIST_TYPE);

        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncTaskCompat.executeParallel(new LoadShotsTask(true));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        adapter = new ShotListAdapter(this, new ArrayList<Shot>(), onLoadMore);
        recyclerView.setAdapter(adapter);
    }




    private class LoadShotsTask extends AuthTask<Void, Void, List<Shot>> {

        private boolean refresh;

        private LoadShotsTask(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected List<Shot> doJob(Void... params) throws AuthException {
            int page = refresh ? 1 : adapter.getData().size() / AuthFunctions.COUNT_PER_PAGE + 1;
            switch (listType) {
                case LIST_TYPE_POPULAR:
                    return AuthFunctions.getShot(page);
                case LIST_TYPE_LIKED:
                    return AuthFunctions.getLikedShots(page);
                case LIST_TYPE_BUCKET:
                    String bucketId = getArguments().getString(KEY_BUCKET_ID);
                    return AuthFunctions.getBucketShots(bucketId, page);
                default:
                    return AuthFunctions.getShot(page);
            }
        }

        @Override
        protected void onSuccess(List<Shot> shots) {
            adapter.setShowLoading(shots.size() >= AuthFunctions.COUNT_PER_PAGE);

            if (refresh) {
                swipeRefreshLayout.setRefreshing(false);
                adapter.setData(shots);
            } else {
                swipeRefreshLayout.setEnabled(true);
                adapter.append(shots);
            }
        }

        @Override
        protected void onFailed(AuthException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }
}
