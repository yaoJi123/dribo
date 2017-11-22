package com.yao.dribo.bucket;

import android.support.v4.app.Fragment;

import com.yao.dribo.base.SingleFragmentActivity;
import com.yao.dribo.shot_list.ShotListFragment;

/**
 * Created by Think on 2017/7/5.
 */

public class BucketShotListActivity extends SingleFragmentActivity{
    public static final String KEY_BUCKET_NAME = "bucketName";


    @Override
    protected String getActivityTitle() {
        return getIntent().getStringExtra(KEY_BUCKET_NAME);
    }

    @Override
    protected Fragment newFragment() {
        String bucketId = getIntent().getStringExtra(ShotListFragment.KEY_BUCKET_ID);
        return bucketId == null
                ? ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_POPULAR)
                : ShotListFragment.newBucketListInstance(bucketId);
    }
}
