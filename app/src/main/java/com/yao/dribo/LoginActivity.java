package com.yao.dribo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.yao.dribo.auth.Auth;
import com.yao.dribo.auth.AuthActivity;
import com.yao.dribo.auth.AuthException;
import com.yao.dribo.auth.AuthFunctions;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Think on 2017/6/26.
 */

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.login_btn) TextView loginBtn;
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_login);
        ButterKnife.bind(this);

        AuthFunctions.init(this);

        if(!AuthFunctions.isLoggedIn()) {

            loginBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Auth.openAuthActivity(LoginActivity.this);
                }
            });
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Auth.REQ_CODE && resultCode == RESULT_OK) {
            final String authCode = data.getStringExtra(AuthActivity.KEY_CODE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String token = Auth.fetchAccessToken(authCode);
                        AuthFunctions.login(LoginActivity.this, token);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (IOException | AuthException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


}
