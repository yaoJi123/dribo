package com.yao.dribo.bucket;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yao.dribo.R;
import com.yao.dribo.auth.AuthException;
import com.yao.dribo.auth.AuthFunctions;
import com.yao.dribo.base.AuthTask;
import com.yao.dribo.base.InfiniteAdapter;
import com.yao.dribo.base.SpaceItemDecoration;
import com.yao.dribo.model.Bucket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Think on 2017/6/20.
 */

/**
 * what this class for
 * what this function for
 * get data for bucket fragment.
 */
public class BucketListFragment extends Fragment {

    public static final int REQ_CODE_NEW_BUCKET = 100;
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_CHOOSING_MODE = "choose_mode";
    public static final String KEY_CHOSEN_BUCKET_IDS = "chosen_bucket_ids";
    public static final String KEY_COLLECTED_BUCKET_IDS = "collected_bucket_ids";

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_container) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fab) FloatingActionButton fab;

    private BucketListAdapter adapter;

    private String userId;
    private boolean isChoosingMode;
    private Set<String> collectedBucketIdSet;

    private InfiniteAdapter.LoadMoreListener onLoadMore = new InfiniteAdapter.LoadMoreListener() {
        @Override
        public void onLoadMore() {
            AsyncTaskCompat.executeParallel(new LoadBucketTask(false));
        }
    };

    public static BucketListFragment newInstance(@Nullable String userId,
                                                 boolean isChoosingMode,
                                                 @Nullable ArrayList<String> chosenBucketIds) {
        Bundle args = new Bundle();
        args.putString(KEY_USER_ID, userId);
        args.putBoolean(KEY_CHOOSING_MODE, isChoosingMode);
        args.putStringArrayList(KEY_CHOSEN_BUCKET_IDS, chosenBucketIds);

        BucketListFragment fragment = new BucketListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView (LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();
        userId = args.getString(KEY_USER_ID);
        isChoosingMode = args.getBoolean(KEY_CHOOSING_MODE);

        if (isChoosingMode) {
            List<String> chosenBucketIdList = args.getStringArrayList(KEY_CHOSEN_BUCKET_IDS);
            if (chosenBucketIdList != null) {
                collectedBucketIdSet = new HashSet<>(chosenBucketIdList);
            } else {
                collectedBucketIdSet = new HashSet<>();
            }
        } else {
            collectedBucketIdSet = new HashSet<>();
        }

        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncTaskCompat.executeParallel(new LoadBucketTask(true));
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        adapter = new BucketListAdapter(getContext(), new ArrayList<Bucket>(), onLoadMore, isChoosingMode);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewBucketFragment dialogFragment = NewBucketFragment.newInstance();
                dialogFragment.setTargetFragment(BucketListFragment.this, REQ_CODE_NEW_BUCKET);
                dialogFragment.show(getFragmentManager(), NewBucketFragment.TAG);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isChoosingMode) {
            inflater.inflate(R.menu.bucket_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            ArrayList<String > chosenBucketIds = new ArrayList<>();
            for (Bucket bucket : adapter.getData()) {
                if (bucket.isChoosing) {
                    chosenBucketIds.add(bucket.id);
                }
            }

            Intent result = new Intent();
            result.putStringArrayListExtra(KEY_CHOSEN_BUCKET_IDS, chosenBucketIds);
            getActivity().setResult(Activity.RESULT_OK, result);
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQ_CODE_NEW_BUCKET && resultCode == Activity.RESULT_OK) {
            String bucketName = data.getStringExtra(NewBucketFragment.KEY_BUCKET_NAME);
            String bucketDescription = data.getStringExtra(NewBucketFragment.KEY_BUCKET_DESCRIPTION);
            if (!TextUtils.isEmpty(bucketName)) {
                AsyncTaskCompat.executeParallel(new NewBucketTask(bucketName, bucketDescription));
            }
        }
    }

    private class LoadBucketTask extends AuthTask<Void, Void, List<Bucket>> {
        private boolean refresh;

        public LoadBucketTask(boolean refresh) {this.refresh = refresh;}


        @Override
        protected List<Bucket> doJob(Void... params) throws AuthException{
            final int page = refresh ? 1 : adapter.getData().size() / AuthFunctions.COUNT_PER_PAGE + 1;
            return userId == null
                    ? AuthFunctions.getUserBuckets(page)
                    : AuthFunctions.getUserBuckets(userId, page);
        }

        @Override
        protected void onSuccess(List<Bucket> buckets) {
            adapter.setShowLoading(buckets.size() >= AuthFunctions.COUNT_PER_PAGE);

            for(Bucket bucket : buckets) {
                if (collectedBucketIdSet.contains(bucket.id)){
                    bucket.isChoosing = true;
                }
            }

            if (refresh) {
                adapter.setData(buckets);
                swipeRefreshLayout.setRefreshing(false);
            } else {
                adapter.append(buckets);
            }

            swipeRefreshLayout.setEnabled(true);

        }

        @Override
        protected void onFailed(AuthException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }


    private class NewBucketTask extends AuthTask<Void, Void, Bucket> {
        private String name;
        private String description;

        private NewBucketTask(String bucketName, String bucketDescription) {
            this.name = bucketName;
            this.description = bucketDescription;
        }

        @Override
        protected Bucket doJob(Void... params) throws AuthException {
            return AuthFunctions.newBucket(name, description);
        }

        @Override
        protected void onSuccess(Bucket bucket) {
            bucket.isChoosing = true;
            adapter.prepend(Collections.singletonList(bucket));

        }

        @Override
        protected void onFailed(AuthException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();

        }
    }
}
