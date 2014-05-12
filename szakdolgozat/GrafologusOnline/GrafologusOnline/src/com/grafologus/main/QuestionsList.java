package com.grafologus.main;

import java.util.ArrayList;

import com.example.grafologus.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

@SuppressLint("NewApi")
public class QuestionsList extends Activity {
	ArrayList<String> NameList = new ArrayList<String>();
	ArrayList<String> arr = new ArrayList<String>();
	ArrayList<Button> buttonlist = new ArrayList<Button>();
	ArrayList<EditText> editList = new ArrayList<EditText>();
	ArrayList<String> text = new ArrayList<String>();
	LinearLayout ll;
	LinearLayout more;
	String name = null;
	Intent intent;
	ViewFlipper flipper;
	Menu menu;
	int count = 0;
	boolean morelist = false;
	int layoutnumber = 0;
	TextView noquestions;
	LayoutParams params;
	boolean cansend = true;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.v("OnCreate", "QuestionsList");
		setContentView(R.layout.list);
		initialUISetup();
		if (!NameList.isEmpty()) {
			for (int i = 0; i < NameList.size(); i++) {
				final Button butt = new Button(this);
				butt.setText(NameList.get(i));
				butt.setTextColor(getResources().getColor(R.color.appyellow));
				butt.setBackgroundResource(R.drawable.questionbutton);
				butt.setLayoutParams(params);
				butt.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						for (Button b : buttonlist) {
							if (b == butt) {
								b.setSelected(true);
							} else
								b.setSelected(false);
						}
						butt.setSelected(true);
						name = butt.getText().toString();
					}
				});
				buttonlist.add(butt);
				ll.addView(butt);
				count++;
			}
		} else {
			noquestions = new TextView(this);
			noquestions.setTextSize(22);
			noquestions.setText("Még nincsenek kérdéssorok mentve..");
			ll.addView(noquestions);
		}
	}

	public void initialUISetup() {
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		ll = (LinearLayout) findViewById(R.id.QuestionsList);
		more = (LinearLayout) findViewById(R.id.MoreList);
		Intent intent = getIntent();
		cansend = intent.getBooleanExtra("cansend", true);
		NameList = SerializeObject.Read(QuestionsList.this, "namelist.dat");
		params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 2, 0, 2);
	}

	@SuppressWarnings("unchecked")
	public void Show(String name) {
		editList.clear();
		more.removeAllViews();
		arr.clear();
		String ser = SerializeObject.ReadSettings(QuestionsList.this,
				"myobject" + name + ".dat");
		if (ser != null && !ser.equalsIgnoreCase("")) {
			Object obj = SerializeObject.stringToObject(ser);
			if (obj instanceof ArrayList) {
				arr = (ArrayList<String>) obj;
			}
		}
		flipper.showNext();

		for (int j = 0; j < arr.size(); j++) {
			EditText text = new EditText(this);
			text.setText(arr.get(j), EditText.BufferType.EDITABLE);
			text.setBackgroundResource(R.drawable.textstyle);
			text.setSingleLine();
			text.setTextColor(Color.BLACK);
			editList.add(text);
			more.addView(text);
		}
	}

	boolean empty = false;

	public void Save(String name) {
		empty = false;
		ArrayList<String> QuestionsList = new ArrayList<String>();
		for (int i = 0; i < editList.size(); i++) {
			if (!editList.get(i).getText().toString().equals("")) {
				QuestionsList.add(editList.get(i).getText().toString());
			} else {
				empty = true;
				break;
			}
		}
		if (!empty) {
			deleteFile("myobject" + name + ".dat");
			String ser = SerializeObject.objectToString(QuestionsList);
			if (ser != null && !ser.equalsIgnoreCase("")) {
				SerializeObject.WriteSettings(QuestionsList.this, ser,
						"myobject" + name + ".dat", false);
			}
			finish();
		} else {
			Toast.makeText(getApplicationContext(), "Kitöltetlen mezõ!",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void DeleteQuestionnare(String name) {
		if (name != null) {
			NameList.remove(name);
			deleteFile("namelist.dat");
			for (int i = 0; i < NameList.size(); i++) {
				SerializeObject.WriteSettings(QuestionsList.this,
						NameList.get(i) + "\n", "namelist.dat", true);
			}
			deleteFile("myobject" + name + ".dat");
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.questionslist, menu);
		if (!cansend) {
			menu.getItem(1).setVisible(false);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.editquestionnare:
			if (name != null) {
				Show(name);
				menu.setGroupVisible(R.id.questionmenu, false);
				menu.setGroupVisible(R.id.editmenu, true);
			}
			return true;
		case R.id.ok:
			if (name != null) {
				intent = new Intent();
				intent.putExtra("key", name);
				setResult(QuestionsList.RESULT_OK, intent);
				finish();
			}
			return true;
		case R.id.save:
			Save(name);
			return true;
		case R.id.delete:
			if (name != null) {
				DeleteQuestionnare(name);
			}
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onBackPressed() {
		layoutnumber = flipper.getDisplayedChild();
		if (layoutnumber != 0) {
			flipper.setDisplayedChild(layoutnumber - 1);
			menu.setGroupVisible(R.id.editmenu, false);
			menu.setGroupVisible(R.id.questionmenu, true);
		} else {
			finish();
		}

	}

}
