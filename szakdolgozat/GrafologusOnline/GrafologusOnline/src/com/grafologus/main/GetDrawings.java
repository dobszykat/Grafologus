package com.grafologus.main;

import java.util.ArrayList;

import com.example.grafologus.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class GetDrawings extends Activity {
	ListView drawinglist;
	ArrayAdapter<String> adapter;
	Intent intent;
	SocketClass socket;
	Graphologist_exam gv;
	int height = 0;
	int width = 0;
	ViewFlipper flipper;
	ArrayList<String> namelist;
	ArrayList<String> idlist;
	ProgressDialog pd;
	Builder error;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.getdrawing);
		initialUISetup();
		Log.v("GetDrawings", "OnCreate");
		Intent intent = getIntent();
		Bundle extras = getIntent().getExtras();
		height = extras.getInt("height");
		width = extras.getInt("width");
		namelist = (ArrayList<String>) intent.getStringArrayListExtra("names");
		idlist = (ArrayList<String>) intent.getStringArrayListExtra("id");
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, namelist);
		drawinglist.setAdapter(adapter);
		drawinglist.setOnItemClickListener(clickListener);
		socket = SocketClass.getInstance();
		pd = new ProgressDialog(this);
		error = new AlertDialog.Builder(this)
				.setCancelable(false)
				.setTitle("Elnézést!")
				.setMessage(
						"Hibásan érkezett meg az adat, vagy rosszul lett elmentve az adatbázisban!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
	}

	public void initialUISetup() {
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		gv = new Graphologist_exam(getApplicationContext());
		drawinglist = (ListView) findViewById(R.id.list);
		FrameLayout frame = (FrameLayout) findViewById(R.id.viewdrawing);
		frame.addView(gv);
	}

	private OnItemClickListener clickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, final View view,
				int position, long id) {
			socket.Printing("GetDrawing", true);
			socket.Printing(idlist.get(position), true);
			Drawing();
		}
	};

	public void Drawing() {
		pd.setMessage("Rajz betöltése");
		pd.setCancelable(false);
		pd.show();
		new Thread() {
			public void run() {
				while (true) {
					if (socket.getCoords()) {
						pd.dismiss();
						runOnUiThread(new Runnable() {

							public void run() {
								if (socket.Coord() != null) {
									flipper.showNext();
									gv.Drawing(socket.Coord(), height, width);
								} else {
									error.show();
								}
							}
						});
						break;
					}
				}
			}
		}.start();

	}

	public void OnClick_Replay(View v) {
		if (gv.FinishDrawing()) {
			gv.Replay();
		} else {
			Toast.makeText(getApplicationContext(),
					"Még nem fejezõdött be a rajz!", Toast.LENGTH_SHORT).show();
		}

	}

	public void OnClick_Back(View v) {
		if (gv.FinishDrawing()) {
			gv.Reset();
			Thread.currentThread().interrupt();
			flipper.showPrevious();
		} else {
			Toast.makeText(getApplicationContext(),
					"Még nem fejezõdött be a rajz!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
