package com.grafologus.main;

import com.example.grafologus.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Chat extends Activity {
	EditText editText;
	TextView chattext;
	String longtext = "";
	SocketClass socket;
	ScrollView sv;
	String line = "";
	Bundle extras;
	boolean chat = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		Log.v("Chat", "onCreate");
		extras = getIntent().getExtras();
		longtext = extras.getString("messages");
		socket = SocketClass.getInstance();
		sv = (ScrollView) findViewById(R.id.chatscrollview);
		editText = (EditText) findViewById(R.id.text);
		chattext = (TextView) findViewById(R.id.chattext);
		chattext.setText(longtext);
		sv.post(new Runnable() {
			public void run() {
				sv.fullScroll(View.FOCUS_DOWN);
			}
		});
		ChatTextListener();

	}

	// Chat üzenet küldése
	public void OnClick_Send(View v) {
		if (!editText.getText().toString().equals("")) {
			socket.Printing("chat", true);
			socket.Printing(editText.getText().toString(), true);
			longtext += "Magam:" + editText.getText().toString() + "\n";
			editText.setText("");
			chattext.setText(longtext);
			sv.post(new Runnable() {
				public void run() {
					sv.fullScroll(View.FOCUS_DOWN);
				}
			});
		} else {
			Toast.makeText(getApplicationContext(), "Üres mezõ!",
					Toast.LENGTH_SHORT).show();
		}

	}

	public void ChatTextListener() {
		new Thread() {
			public void run() {
				while (true) {
					if (socket.IsChat() && chat) {
						line = socket.WhatIsLine();
						longtext += "Másik fél: " + line + "\n";
						runOnUiThread(new Runnable() {
							public void run() {
								chattext.setText(longtext);
								sv.post(new Runnable() {
									public void run() {
										sv.fullScroll(View.FOCUS_DOWN);
									}
								});

							}
						});
					}
				}
			}
		}.start();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("longtext", longtext);
		setResult(QuestionsList.RESULT_OK, intent);
		chat = false;
		finish();
	}
}
