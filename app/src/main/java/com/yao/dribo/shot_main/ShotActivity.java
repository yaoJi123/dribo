package com.yao.dribo.shot_main;

import android.support.v4.app.Fragment;

import com.yao.dribo.base.SingleFragmentActivity;

/**
 * The activity for the single shot details
 */

public class ShotActivity extends SingleFragmentActivity {
    public static final String KEY_SHOT_TITLE = "shot_title";

    @Override
    protected String getActivityTitle() {
        return getIntent().getStringExtra(KEY_SHOT_TITLE);
    }

    @Override
    protected Fragment newFragment() {
        return ShotFragment.newInstance(getIntent().getExtras());
    }
}
