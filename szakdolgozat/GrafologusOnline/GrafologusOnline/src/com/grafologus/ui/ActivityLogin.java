package com.grafologus.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.videochat.core.service.QBVideoChatService;
import com.example.grafologus.R;
import com.grafologus.model.DataHolder;


public class ActivityLogin extends Activity {

    private final String FIRST_USER_PASSWORD = "grafologus"; //fromme2900@dayrep.com
    private final String FIRST_USER_LOGIN = "grafologus";
    private final String SECOND_USER_PASSWORD = "pacienss";
    private final String SECOND_USER_LOGIN = "paciens";  //anist1983@rhyta.com

    private final int firstUserId = 1031907;
    private final String firstUserName = "grafologus";
    private final String secondUserName = "paciens";
    private final int secondUserId = 1031906;

    private ProgressBar loading;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, QBVideoChatService.class));
        Intent intent = getIntent();
        String participant = intent.getStringExtra("participant");
        Log.i("intent", participant);
        
        setContentView(R.layout.login_layout);
        loading = (ProgressBar) findViewById(R.id.Loading);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);

        
        progressDialog.show();
        if(participant.contains("grafologus")) {
        	createSession(FIRST_USER_LOGIN, FIRST_USER_PASSWORD);
        	Log.i("ezzzz","sd");
        } else {
        	createSession(SECOND_USER_LOGIN, SECOND_USER_PASSWORD);
        }
        
        QBSettings.getInstance().fastConfigInit("9813", "AwqM9ckyEPMOd3A", "t5Qn9wxp92JqTCH");
    }

    @Override
    public void onResume() {
        progressDialog.dismiss();
        super.onResume();
    }

    private void createSession(String login, final String password) {

        QBAuth.createSession(login, password, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    // save current user
                    DataHolder.getInstance().setCurrentQbUser(((QBSessionResult) result).getSession().getUserId(), password);

                    // show next activity
                    showCallUserActivity();
                }
            }
        });
    }
    
    public void onProgress(boolean progress) {
        loading.setVisibility(progress ? View.VISIBLE : View.GONE);
    }

    private void showCallUserActivity() {
        Intent intent = new Intent(this, ActivityCallUser.class);
        intent.putExtra("userId", DataHolder.getInstance().getCurrentQbUser().getId() == firstUserId ? secondUserId : firstUserId);
        intent.putExtra("userName", DataHolder.getInstance().getCurrentQbUser().getId() == firstUserId ? secondUserName : firstUserName);
        startActivity(intent);
        finish();
    }
    
    
}