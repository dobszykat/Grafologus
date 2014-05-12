package com.grafologus.main;


import com.example.grafologus.R;
import com.grafologus.ui.ActivityLogin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ScrollView;

public class Partner extends Activity {

	// Deklarációk
	ProgressDialog pd;
	SocketClass socket;
	String longtext = "";
	String line = null;
	Intent intent;
	int width = 0;
	int height = 0;
	boolean chat = false;
	ScrollView sv;
	static int pBarMax = 60;
	boolean chatalertshow = false;
	int second = 0;
	Builder alert;
	boolean firstmessage = true;
	String participant;
	boolean connect = true;
	boolean graph_ok = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager w = getWindowManager();
		Display d = w.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		Log.v("partnr", "onCreate");
		alert = new AlertDialog.Builder(Partner.this);
	}

	// Figyelmeztetõ ablak beállításai
	public void SetAlertDialog(String title, String message,
			boolean cancelable, final boolean finish) {
		alert.setTitle(title).setMessage(message).setCancelable(cancelable);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (finish) {

					finish();
					System.exit(0);
				}
			}
		});
	}

	// Csatlakozás a szerverre
	public void Connect(final String participant, String fullname, String id, String ip) {
		this.participant = participant;
		socket = SocketClass.getInstance();
		socket.ConnectToPartner(participant, fullname, id, ip);
		pd = new ProgressDialog(this);
		pd.setCancelable(false);
		pd.setMessage("Várakozás a másik fél kapcsolódására!");
		pd.show();
		new Thread() {
			public void run() {
				if (participant.equals("grafologus\n")) {
					while (socket.Password() == 0 && socket.CanConnect()) {
					}
					if (socket.Password() == 2) {
						SetAlertDialog("Hiba",
								"Hibás felhasználónév vagy jelszó!", false,
								true);
						connect = false;
						runOnUiThread(new Runnable() {
							public void run() {
								alert.show();
							}
						});
					} else if (!socket.CanConnect()) {
						ServerNotAvailable();
						Thread.currentThread().interrupt();
					}
				}
				while (!socket.IsConnected() && second < pBarMax) {
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (second == pBarMax - 1) {
						SetAlertDialog(
								"Hiba",
								"Jelenleg nem elérhetõ másik fél. Kérem próbálkozzon késõbb!",
								false, true);
						runOnUiThread(new Runnable() {

							public void run() {
								alert.show();
							}
						});
					}
					if (!socket.CanConnect()) {
						ServerNotAvailable();
						connect = false;
						break;
					}
					second++;
				}
				pd.dismiss();
			}
		}.start();
		ChatAndConnectListener();
	}

	public void ServerNotAvailable() {
		SetAlertDialog("Hiba", "Jelenleg nem tud kapcsolódni a szerverhez!",
				false, true);
		runOnUiThread(new Runnable() {

			public void run() {
				alert.show();
			}
		});
	}

	// Chat üzeneteket figyeli, illetve a kapcsolatmegszakadást
	public void ChatAndConnectListener() {
		new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// Amikor nincs megnyitva a chat ablak, a másik fél
					// szövegét
					// összefûzi
					if (socket.IsChat() && !chat) {
						SetAlertDialog("Chat", "Üzenete érkezett", true, false);
						longtext += "Másik fél: " + socket.WhatIsLine() + "\n";
						if (firstmessage) {
							runOnUiThread(new Runnable() {
								public void run() {
									alert.show();
								}
							});
							firstmessage = false;
						}
					}
					if (socket.IsFinishedExam()) {
						SetAlertDialog("Vége",
								"Vége a vizsgálatnak! Viszontlátásra!", false,
								true);
						runOnUiThread(new Runnable() {
							public void run() {
								alert.show();
							}
						});
						break;
					} else if (socket.IsConnectionBreak()) {
						SetAlertDialog(
								"Hiba",
								"Megszakadt a kapcsolat!\n Kérem próbáljon újra kapcsolódni!",
								false, true);
						runOnUiThread(new Runnable() {
							public void run() {

								alert.show();

							}
						});
						break;
					}
				}
			}
		}.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (isFinishing()) {
			socket.Exit();
			finish();
		}
	}

	// Menü
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.chat:
			chat = true;
			intent = new Intent(this, Chat.class);
			intent.putExtra("messages", longtext);
			startActivityForResult(intent, 2);
			return true;
		case R.id.video:
			Intent intent = new Intent(this, ActivityLogin.class);
			intent.putExtra("participant", participant);
			startActivity(intent);
			return true;
		case android.R.id.home:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (2): {
			if (resultCode == Activity.RESULT_OK) {
				longtext = data.getStringExtra("longtext");
				chat = false;
				firstmessage = true;
			}
		}
		}

	}

	@Override
	public void onDestroy() {
		if (isFinishing()) {
			if (pd != null && pd.isShowing()) {
				pd.cancel();
				pd.dismiss();
			}
			socket.Exit();
		}
		System.gc();
		super.onDestroy();
	}
};
