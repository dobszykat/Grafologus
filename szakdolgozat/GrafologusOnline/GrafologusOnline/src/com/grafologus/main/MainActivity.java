package com.grafologus.main;

import com.example.grafologus.R;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends Activity {

	EditText name;
	EditText id;
	Button login;
	RadioButton pat;
	RadioButton graph;
	String line = "";
	EditText ip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("MainActivity", "OnCreate");
		setContentView(R.layout.activity_main);
		initialUISetup();
	}

	public void initialUISetup() {
		graph = (RadioButton) findViewById(R.id.graphologist);
		pat = (RadioButton) findViewById(R.id.patient);
		name = (EditText) findViewById(R.id.name);
		id = (EditText) findViewById(R.id.id);
		login = (Button) findViewById(R.id.login);
		ip = (EditText) findViewById(R.id.serverip);
	}

	// Internet kapcsolat vizsgálat
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	// Belépés
	public void onClick_Login(View v) {
		if (!name.getText().toString().equals("")
				&& !id.getText().toString().equals("") && !ip.getText().toString().equals("")) {
			if (isNetworkAvailable()) {
				final String fname = name.getText() + "\n"; // Bejelentkezett
															// név
				String idtext = id.getText() + "\n";
				String serverip = ip.getText().toString();
				Log.i("id", idtext);
				if (graph.isChecked()) { // Grafológusként
					Intent intent = new Intent(this, Graphologist.class);
					intent.putExtra("id", idtext);
					intent.putExtra("Name", fname);
					intent.putExtra("ip", serverip);
					startActivity(intent);

				} else if (pat.isChecked()) { // Páciensként
					Intent intent = new Intent(this, Patient.class);
					intent.putExtra("id", idtext);
					intent.putExtra("Name", fname);
					intent.putExtra("ip", serverip);
					startActivity(intent);
				}

			} else {
				AlertDialog.Builder netNotAvailable = new AlertDialog.Builder(
						MainActivity.this);
				netNotAvailable.setMessage("Nincs internetkapcsolat.");
				netNotAvailable.setTitle("Hiba");
				netNotAvailable.show();
			}
		} else {
			AlertDialog.Builder emptytext = new AlertDialog.Builder(
					MainActivity.this);
			emptytext.setMessage("Kérem minden mezõt töltsön ki!");
			emptytext.setTitle("Hiba");
			emptytext.show();
		}
	}

}
