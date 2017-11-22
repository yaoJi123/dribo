package com.yao.dribo.bucket;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.yao.dribo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Think on 2017/7/2.
 */

public class NewBucketFragment extends DialogFragment{
    public static final String KEY_BUCKET_NAME = "bucket_name";
    public static final String KEY_BUCKET_DESCRIPTION = "bucket_description";

    @BindView(R.id.new_bucket_name) EditText bucketName;
    @BindView(R.id.new_bucket_description) EditText bucketDescription;

    public static final String TAG = "NewBucketDialogFragment";

    public static NewBucketFragment newInstance() {
        return new NewBucketFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_bucket, null);
        ButterKnife.bind(this, view);
        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle(R.string.new_bucket_title)
                .setPositiveButton(R.string.new_bucket_create, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent result = new Intent();
                        result.putExtra(KEY_BUCKET_NAME, bucketName.getText().toString());
                        result.putExtra(KEY_BUCKET_DESCRIPTION, bucketDescription.getText().toString());
                        getTargetFragment().onActivityResult(BucketListFragment.REQ_CODE_NEW_BUCKET,
                                Activity.RESULT_OK,
                                result);
                        dismiss();
                    }
                }).setNegativeButton(R.string.new_bucket_cancel, null).show();
    }
}
