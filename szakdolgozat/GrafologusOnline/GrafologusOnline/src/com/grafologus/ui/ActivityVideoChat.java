package com.grafologus.ui;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.grafologus.R;
import com.quickblox.module.videochat.core.service.QBVideoChatService;
import com.quickblox.module.videochat.core.service.ServiceInteractor;
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.CallState;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat.model.objects.CameraQuality;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;
import com.quickblox.module.videochat.views.CameraView;
import com.quickblox.module.videochat.views.OpponentView;

import java.util.List;

public class ActivityVideoChat extends Activity {

    private CameraView cameraView;
    private OpponentView opponentSurfaceView;
    private ProgressBar opponentImageLoadingPb;
    private VideoChatConfig videoChatConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_chat_layout);

        initViews();
    }

    private void initViews() {

        opponentSurfaceView = (OpponentView) findViewById(R.id.opponentSurfaceView);
        cameraView = (CameraView) findViewById(R.id.camera_preview);
        opponentImageLoadingPb = (ProgressBar) findViewById(R.id.opponentImageLoading);

        // VideoChat settings
        videoChatConfig = (VideoChatConfig) getIntent().getParcelableExtra(
                VideoChatConfig.class.getCanonicalName());
        QBVideoChatService.getService().startVideoChat(videoChatConfig);
        
        
         cameraView.setCameraViewListener(new OnQBVideoChatListener() {
        	public void onCameraInit(List<Camera.Size> supportedPreviewSizes) {
                Camera.Size cameraPreviewSize = supportedPreviewSizes.get(supportedPreviewSizes.size() - 1);
                if (cameraPreviewSize.width > supportedPreviewSizes.get(0).width) {
                    cameraPreviewSize = supportedPreviewSizes.get(0);
                }

                cameraView.setCameraQuality(CameraQuality.LOW);
                //cameraView.setCameraPreviewSizeImageQuality(cameraPreviewSize, 10);
            }

			@Override
			public void onVideoChatStateChange(CallState arg0,
					VideoChatConfig arg1) {
				
			}
        });
        
        
    }

    @Override
    public void onResume() {
        QBVideoChatService.getService().setQbVideoChatListener(qbVideoChatListener);
        cameraView.setCameraViewListener(qbVideoChatListener);
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        QBVideoChatService.getService().finishVideoChat(videoChatConfig.getSessionId());
        super.onDestroy();
    }

    OnQBVideoChatListener qbVideoChatListener = new OnQBVideoChatListener() {
        @Override
        public void onCameraDataReceive(byte[] videoData) {
            if (videoChatConfig.getCallType() != CallType.VIDEO_AUDIO) {
                return;
            }
            ServiceInteractor.INSTANCE.sendVideoData(ActivityVideoChat.this, videoData);
        }

        @Override
        public void onMicrophoneDataReceive(byte[] audioData) {
            ServiceInteractor.INSTANCE.sendAudioData(ActivityVideoChat.this, audioData);
        }

        @Override
        public void onOpponentVideoDataReceive(byte[] videoData) {
            opponentSurfaceView.setData(videoData);
        }

        @Override
        public void onOpponentAudiDataReceive(byte[] audioData) {
            QBVideoChatService.getService().playAudio(audioData);
        }

        @Override
        public void onProgress(boolean progress) {
            opponentImageLoadingPb.setVisibility(progress ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onVideoChatStateChange(CallState callState, VideoChatConfig chat) {
            switch (callState) {
                case ON_CALL_START:
                    Toast.makeText(getBaseContext(), getString(R.string.call_start_txt), Toast.LENGTH_SHORT).show();
                    break;
                case ON_CANCELED_CALL:
                    Toast.makeText(getBaseContext(), getString(R.string.call_canceled_txt), Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ON_CALL_END:
                    finish();
                    break;
			default:
				break;
            }
        }
    };
}
