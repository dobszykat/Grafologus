package com.grafologus.main;

import java.util.ArrayList;
import java.util.List;

import com.example.grafologus.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NewQuestion extends Activity {

	public List<EditText> TextList;
	ArrayList<String> NameList;
	EditText name;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newquestionnare);
		Log.v("OnCreate", "NewQuestion");
		TextList = new ArrayList<EditText>();
		name = (EditText) findViewById(R.id.nameofquestions);
		NameList = new ArrayList<String>();
		NewQuestionbtn();
	}

	public int count = 0;

	public void NewQuestionbtn() {
		TextView textview = new TextView(this);
		EditText edittext = new EditText(this);
		textview.setText("Ide írja a " + String.valueOf(count + 1)
				+ ". kérdését!");
		edittext.setSingleLine();
		edittext.setTextColor(getResources().getColor(R.color.black));
		edittext.setBackgroundResource(R.drawable.textstyle);
		textview.setTextColor(getResources().getColor(R.color.appyellow));
		LinearLayout ll = (LinearLayout) findViewById(R.id.NewQuestions);
		ll.addView(textview);
		ll.addView(edittext);
		TextList.add(edittext);
		count++;

	}

	boolean empty = false;
	public boolean save = false;

	public boolean CheckIfNameExists(String name) {
		NameList = SerializeObject.Read(NewQuestion.this, "namelist.dat");
		for (int i = 0; i < NameList.size(); i++) {
			if (NameList.get(i).equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void SaveQuestions(String getname) {
		if (!CheckIfNameExists(name.getText().toString())) {
			empty = false;
			if (!getname.equals("")) {
				ArrayList<String> QuestionsList = new ArrayList<String>();
				for (int i = 0; i < TextList.size(); i++) {
					if (!TextList.get(i).getText().toString().equals("")) {
						QuestionsList.add(TextList.get(i).getText().toString());
					} else {
						empty = true;
						break;
					}
				}
				if (!empty) {
					String ser = SerializeObject.objectToString(QuestionsList);
					if (ser != null && !ser.equalsIgnoreCase("")) {
						SerializeObject.WriteSettings(NewQuestion.this, ser,
								"myobject" + getname + ".dat", false);
						save = true;
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"Kitöltetlen mezõ!", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"Nem adta meg a kérdéssor nevét!", Toast.LENGTH_SHORT)
						.show();
			}

		} else {

			Toast.makeText(getApplicationContext(),
					"Ezzel a névvel már szerepel kérdéssor!",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.newbtn:
			NewQuestionbtn();
			return true;
		case R.id.done:
			SaveQuestions(name.getText().toString());
			if (save) {
				SerializeObject.WriteSettings(NewQuestion.this, name.getText()
						.toString() + "\n", "namelist.dat", true);
				finish();
			}
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.newquestionnare, menu);
		return super.onCreateOptionsMenu(menu);
	}

}
