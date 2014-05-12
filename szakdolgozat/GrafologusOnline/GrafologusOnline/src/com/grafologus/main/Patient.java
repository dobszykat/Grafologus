package com.grafologus.main;

import java.io.IOException;

import com.example.grafologus.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Patient extends Partner {

	MyTouchEventView tv;
	FrameLayout frame;
	ProgressDialog pd;
	EditText editText;
	TextView chattext;
	Button finish;
	ViewFlipper flipper;
	Button button;
	TextView wait;
	String line = null;
	String patientid = "";
	String longtext = "";
	int height = 0;
	int width = 0;
	boolean finish_drawing = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_pac);
		Log.v("Patient", "onCreate");
		Intent myIntent = getIntent();
		patientid = myIntent.getStringExtra("id");
		String fullname = myIntent.getStringExtra("Name");
		String ip = myIntent.getStringExtra("ip");
		InitialUISetup();
		Connect("paciens\n", fullname, patientid,ip);
		Examination();

	}

	public void InitialUISetup() {
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		tv = new MyTouchEventView(getApplicationContext());
		frame = (FrameLayout) findViewById(R.id.viewframelayout);
		frame.addView(tv);
		finish = (Button) findViewById(R.id.finish);
		button = (Button) findViewById(R.id.letsdraw);
		wait = (TextView) findViewById(R.id.pleasewait);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

	}

	public void OnClick_Finish(View v) {
		finish_drawing = true;
		Coordinates c = new Coordinates();
		c.koordtombx = tv.Xkoords();
		c.koordtomby = tv.Ykoords();
		c.speedarray = tv.Speedkoords();
		c.height = height;
		c.width = width;

		try {
			socket.SendArray(c, line.trim());
		} catch (IOException e) {
			e.printStackTrace();
		}
		tv.Reset();
		flipper.showPrevious();

	}

	public void Examination() {
		new Thread() {
			public void run() {
				while (true) {
					if (socket.LineisChanged() && !socket.IsChat()) {
						line = socket.WhatIsLine();
						runOnUiThread(new Runnable() {

							public void run() {
								if (finish_drawing) {

									new AlertDialog.Builder(Patient.this)
											.setCancelable(false)
											.setTitle("Grafológus üzenete")
											.setMessage(
													"Kérem rajzolja be a következõt!: "
															+ line)
											.setPositiveButton(
													"OK",
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int which) {
															wait.setVisibility(View.INVISIBLE);
															button.setText("Ralzolja le: "
																	+ line);
															button.setVisibility(View.VISIBLE);
														}
													}).show();
									finish_drawing = false;
								}
							}

						});

						finish.setClickable(true);
					}
				}
			}
		}.start();
	}

	public void OnClick_Drawing(View v) {
		flipper.showNext();
		button.setVisibility(View.INVISIBLE);
		wait.setVisibility(View.VISIBLE);
	}

	@Override
	public void onBackPressed() {
		if (!finish_drawing) {
			Toast.makeText(
					getApplicationContext(),
					"Amíg nem fejezte be a rajzolást, addig nem tud visszalépni!",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!finish_drawing) {
			menu.getItem(0).setEnabled(false);
			menu.getItem(0).setCheckable(false);
		} else {
			menu.getItem(0).setEnabled(true);
			menu.getItem(0).setCheckable(true);
		}

		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean focus) {
		super.onWindowFocusChanged(focus);
		height = frame.getMeasuredHeight();
		width = frame.getMeasuredWidth();
	}

}
