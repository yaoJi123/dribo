package com.yao.dribo.base;

import android.os.AsyncTask;

import com.yao.dribo.auth.AuthException;

/**
 * Created by Think on 2017/7/5.
 */

public abstract class AuthTask<Params, Progress, Result> extends AsyncTask <Params, Progress, Result>{

    private AuthException exception;

    protected abstract Result doJob(Params... params) throws AuthException;

    protected abstract void onSuccess(Result result);

    protected abstract void onFailed(AuthException e);

    @Override
    protected Result doInBackground(Params... params) {
        try {
            return doJob(params);
        } catch (AuthException e) {
            e.printStackTrace();
            exception = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (exception != null) {
            onFailed(exception);
        } else {
            onSuccess(result);
        }
    }
}
