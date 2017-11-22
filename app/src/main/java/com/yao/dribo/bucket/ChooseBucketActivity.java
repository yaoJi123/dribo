package com.yao.dribo.bucket;

import android.support.v4.app.Fragment;

import com.yao.dribo.R;
import com.yao.dribo.base.SingleFragmentActivity;

import java.util.ArrayList;

/**
 * Created by Think on 2017/7/3.
 */

public class ChooseBucketActivity extends SingleFragmentActivity {
    @Override
    protected Fragment newFragment() {
        boolean isChoosingMode = getIntent().getExtras().getBoolean(
                BucketListFragment.KEY_CHOOSING_MODE);
        ArrayList<String> chosenBucketIds = getIntent().getStringArrayListExtra(
                BucketListFragment.KEY_CHOSEN_BUCKET_IDS);
        return BucketListFragment.newInstance(null, isChoosingMode, chosenBucketIds);
    }

    @Override
    protected String getActivityTitle() {
        return getString(R.string.choose_bucket);
    }
}
