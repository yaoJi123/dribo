package com.yao.dribo.bucket;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.yao.dribo.R;
import com.yao.dribo.base.SingleFragmentActivity;

import java.util.ArrayList;

/**
 * the activity of the list of bucket in choosing mode or not choosing mode
 */

public class ChooseBucketActivity extends SingleFragmentActivity {
    @NonNull
    @Override
    protected Fragment newFragment() {
        boolean isChoosingMode = getIntent().getExtras().getBoolean(
                BucketListFragment.KEY_CHOOSING_MODE);
        ArrayList<String> chosenBucketIds = getIntent().getStringArrayListExtra(
                BucketListFragment.KEY_COLLECTED_BUCKET_IDS);
        return BucketListFragment.newInstance(null, isChoosingMode, chosenBucketIds);
    }

    @Override
    protected String getActivityTitle() {
        return getString(R.string.choose_bucket);
    }
}
