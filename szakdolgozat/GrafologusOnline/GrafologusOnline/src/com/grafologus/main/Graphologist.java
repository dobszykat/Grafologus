package com.grafologus.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.grafologus.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Graphologist extends Partner {

	// Deklarációk
	ProgressDialog pd;
	Graphologist_exam gv;
	Intent intent;
	Button button;
	Button show;
	Button replay;
	Button drawings;
	EditText tosend;
	int layoutnumber = 0;
	ViewFlipper flipper;
	SimpleDateFormat today;
	String line = "";
	String graphid = "";
	View view;
	Builder error;
	String datestring = "";
	boolean cansend = true;
	boolean isquestionnare = false;
	boolean getdrawingslist = false;
	FrameLayout frame;
	TextView waiting;
	ArrayList<String> drawingsnamelist = new ArrayList<String>();
	ArrayList<String> drawingidlist = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v("Graphologist", "onCreate");
		Intent myIntent = getIntent();
		graphid = myIntent.getStringExtra("id");
		String fullname = myIntent.getStringExtra("Name");
		String ip = myIntent.getStringExtra("ip");
		setContentView(R.layout.login_graf);
		Connect("grafologus\n", fullname, graphid,ip);
		initialUISetup();
	}

	@SuppressLint("SimpleDateFormat")
	public void initialUISetup() {
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		tosend = (EditText) findViewById(R.id.tosend);
		button = (Button) findViewById(R.id.sendquestion);
		replay = (Button) findViewById(R.id.replay);
		drawings = (Button) findViewById(R.id.drawings);
		show = (Button) findViewById(R.id.show);
		view = (View) findViewById(R.id.view);
		gv = new Graphologist_exam(getApplicationContext());
		frame = (FrameLayout) findViewById(R.id.viewframelayout2);
		waiting = (TextView) findViewById(R.id.waitingfordrawing);
		frame.addView(gv);
		pd = new ProgressDialog(this);
		show.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				flipper.showNext();
				gv.Drawing(socket.Coord(), height, width);
				if (!isquestionnare) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							button.setEnabled(true);
						}
					});
				}
			}
		});
		error = new AlertDialog.Builder(this)
				.setCancelable(false)
				.setTitle("Elnézést!")
				.setMessage(
						"Hibásan érkezett meg az adat!\nKérem adja meg újra az utasítást.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		today = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		datestring = today.format(date);

	}

	boolean pdshow = false;

	// Egyszerû utasítást kérünk
	public void OnClick_SendAsking(final View v) {
		if (!tosend.getText().toString().equals("")) {
			button.setEnabled(false);
			drawings.setEnabled(false);
			waiting.setVisibility(View.VISIBLE);
			show.setVisibility(View.INVISIBLE);
			line = tosend.getText() + "\n";
			SendLine(v);
		} else {
			new AlertDialog.Builder(this)
					.setCancelable(false)
					.setTitle("Hiba")
					.setMessage("Üresen elküldött utasítás!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		}
	}

	public void SendQuestionnare(final ArrayList<String> a, final View v) {
		isquestionnare = true;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				button.setEnabled(false);
				drawings.setEnabled(false);
				waiting.setVisibility(View.VISIBLE);
			}
		});
		new Thread() {
			public void run() {
				for (int i = 0; i < a.size(); i++) {
					if (i == a.size() - 1) {
						isquestionnare = false;
					}
					line = a.get(i);
					SendLine(v);
					while (true) {
						if (cansend) {
							break;
						}
					}

				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						button.setEnabled(true);
						drawings.setEnabled(true);
					}
				});
			}
		}.start();

	}

	public void ButtonSettings(final View v) {
		new Thread() {
			public void run() {
				while (true) {
					if (socket.getCoords()) {
						runOnUiThread(new Runnable() {

							public void run() {
								if (socket.Coord() != null) {
									waiting.setVisibility(View.INVISIBLE);
									show.setText("Rajz megtekintése - "
											+ line.trim());
									show.setVisibility(View.VISIBLE);
								} else {
									error.show();
									waiting.setVisibility(View.INVISIBLE);
									button.setEnabled(true);
									drawings.setEnabled(true);
								}
							}
						});

						break;
					}
				}
			}
		}.start();
	}

	public void SendLine(final View v) {
		cansend = false;
		socket.Printing(line, true);
		ButtonSettings(v);
	}

	public void OnClick_Replay(View v) {
		if (gv.FinishDrawing()) {
			gv.Replay();
		} else {
			Toast.makeText(getApplicationContext(),
					"Még nem fejezõdött be a rajz!", Toast.LENGTH_SHORT).show();
		}

	}

	// Koordináták mentése
	public void OnClick_SaveCoords(View v) {
		if (!isquestionnare) {
			button.setEnabled(true);
			drawings.setEnabled(true);
		}
		show.setVisibility(View.INVISIBLE);
		waiting.setVisibility(View.INVISIBLE);
		if (getdrawingslist) {
			drawingsnamelist.add(line.trim() + " - " + datestring);
			drawingidlist.add(String.valueOf(socket.getId()));
		}
		flipper.showPrevious();
		cansend = true;
	}

	// Utasítás újraküldése - nem megfelelõ rajz esetén
	public void OnClick_Again(View v) {
		button.setEnabled(false);
		show.setVisibility(View.INVISIBLE);
		waiting.setVisibility(View.VISIBLE);
		flipper.showPrevious();
		SendLine(v);
		socket.RemoveRow();
	}

	// Új kérdéssorok
	public void OnClick_NewQuestionnaire(View v) {
		intent = new Intent(this, NewQuestion.class);
		startActivity(intent);
	}

	// Meglévõ kérdéssorok
	public void OnClick_Questionnaire(View v) {
		intent = new Intent(this, QuestionsList.class);
		intent.putExtra("cansend", cansend);
		startActivityForResult(intent, 1);

	}

	// Korábbi rajzok megtekintése
	public void OnClick_Drawings(View v) {
		intent = new Intent(this, GetDrawings.class);
		if (!getdrawingslist) {
			socket.GetDrawingsList();
			pd.setMessage("Rajzok betöltése");
			pd.setCancelable(false);
			pd.show();
			new Thread() {
				public void run() {
					while (true) {
						if (socket.IfGetDrawing()) {
							drawingsnamelist = socket.DrawingsName();
							drawingidlist = socket.getDrawingsId();
							intent.putExtra("height", height);
							intent.putExtra("width", width);
							intent.putStringArrayListExtra("names",
									drawingsnamelist);
							intent.putStringArrayListExtra("id", drawingidlist);
							pd.dismiss();
							pd.cancel();
							getdrawingslist = true;
							startActivity(intent);
							break;
						}
					}
				}
			}.start();
		} else {
			intent.putExtra("height", height);
			intent.putExtra("width", width);
			intent.putStringArrayListExtra("names", drawingsnamelist);
			intent.putStringArrayListExtra("id", drawingidlist);
			startActivity(intent);
		}

	}

	// Vizsgálat befejezése
	public void OnClick_FinishExam(View v) {
		socket.Printing("finishexam\n", true);
		finish();
		System.exit(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (1): {
			if (resultCode == Activity.RESULT_OK) {
				String questionname = data.getStringExtra("key");
				ArrayList<String> a = new ArrayList<String>();

				String ser = SerializeObject.ReadSettings(Graphologist.this,
						"myobject" + questionname + ".dat");
				if (ser != null && !ser.equalsIgnoreCase("")) {
					Object obj = SerializeObject.stringToObject(ser);
					if (obj instanceof ArrayList) {
						a = (ArrayList<String>) obj;
					}
				}
				View view = this.getCurrentFocus();
				SendQuestionnare(a, view);
			}
		}
		}

	}

	@Override
	public void onBackPressed() {
		if (flipper.getDisplayedChild() == 1) {
			Toast.makeText(
					getApplicationContext(),
					"Addig nem tud visszalépni amíg nem kéri újra, vagy nem menti az ábrát!",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onWindowFocusChanged(boolean focus) {
		super.onWindowFocusChanged(focus);
		height = frame.getMeasuredHeight();
		width = frame.getMeasuredWidth();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!cansend) {
			menu.getItem(0).setEnabled(false);
		} else {
			menu.getItem(0).setEnabled(true);
		}

		return true;
	}

}
