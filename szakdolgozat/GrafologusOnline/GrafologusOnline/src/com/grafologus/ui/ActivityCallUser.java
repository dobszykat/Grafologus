package com.grafologus.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.grafologus.model.DataHolder;
import com.quickblox.module.videochat.model.utils.Debugger;
import com.example.grafologus.R;
import com.grafologus.model.listener.OnCallDialogListener;
import com.grafologus.model.utils.DialogHelper;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat.core.service.QBVideoChatService;
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.CallState;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;


public class ActivityCallUser extends Activity {

    private ProgressDialog progressDialog;
    private Button videoCallBtn;
    private QBUser qbUser;
    private boolean isCanceledVideoCall;
    private VideoChatConfig videoChatConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_layout);
        initViews();
    }

    private void initViews() {
        int userId = getIntent().getIntExtra("userId", 0);
        qbUser = new QBUser(userId);
        isCanceledVideoCall = true;

        videoCallBtn = (Button) findViewById(R.id.videoCallBtn);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (isCanceledVideoCall) {
                    QBVideoChatService.getService().stopCalling(videoChatConfig);
                }
            }
        });

        videoCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                videoChatConfig = QBVideoChatService.getService().callUser(qbUser, CallType.VIDEO_AUDIO, null);
            }
        });

//        String userName = getIntent().getStringExtra("userName");
//        audioCallBtn.setText(audioCallBtn.getText().toString() + " " + userName);

        // Set VideoCHat listener
        //
        QBUser currentQbUser = DataHolder.getInstance().getCurrentQbUser();
        Debugger.logConnection("setQBVideoChatListener: " + (currentQbUser == null));
        QBVideoChatService.getService().setQBVideoChatListener(currentQbUser, qbVideoChatListener);
    }

    private OnQBVideoChatListener qbVideoChatListener = new OnQBVideoChatListener() {

        @Override
        public void onVideoChatStateChange(CallState state, VideoChatConfig receivedVideoChatConfig) {
            videoChatConfig = receivedVideoChatConfig;
            isCanceledVideoCall = false;
            switch (state) {
                case ON_CALLING:
                    showCallDialog();
                    break;
                case ON_ACCEPT_BY_USER:
                    progressDialog.dismiss();
                    startVideoChatActivity();
                    break;
                case ON_REJECTED_BY_USER:
                    progressDialog.dismiss();
                    break;
                case ON_DID_NOT_ANSWERED:
                    progressDialog.dismiss();
                    break;
                case ON_CANCELED_CALL:
                    isCanceledVideoCall = true;
                    videoChatConfig = null;
                    break;
                case ON_START_CONNECTING:
                    progressDialog.dismiss();
                    startVideoChatActivity();
                    break;
            }
        }
    };


    private void showCallDialog() {
        DialogHelper.showCallDialog(this, new OnCallDialogListener() {
            @Override
            public void onAcceptCallClick() {
                if (videoChatConfig == null) {
                    Toast.makeText(getBaseContext(), getString(R.string.call_canceled_txt), Toast.LENGTH_SHORT).show();
                    return;
                }
                QBVideoChatService.getService().acceptCall(videoChatConfig);
            }

            @Override
            public void onRejectCallClick() {
                if (videoChatConfig == null) {
                    Toast.makeText(getBaseContext(), getString(R.string.call_canceled_txt), Toast.LENGTH_SHORT).show();
                    return;
                }
                QBVideoChatService.getService().rejectCall(videoChatConfig);
            }
        });
    }

    @Override
    public void onResume() {
        try {
            QBVideoChatService.getService().setQbVideoChatListener(qbVideoChatListener);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        super.onResume();
    }


    private void startVideoChatActivity() {
        Intent intent = new Intent(getBaseContext(), ActivityVideoChat.class);
        intent.putExtra(VideoChatConfig.class.getCanonicalName(), videoChatConfig);
        startActivity(intent);
    }


    @Override
    public void onDestroy() {
        stopService(new Intent(getApplicationContext(), QBVideoChatService.class));
        super.onDestroy();
    }
}
