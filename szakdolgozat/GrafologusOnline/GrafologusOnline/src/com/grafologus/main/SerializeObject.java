package com.grafologus.main;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;

public class SerializeObject {
	private final static String TAG = "SerializeObject";

	@SuppressLint("NewApi")
	public static String objectToString(Serializable object) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(out).writeObject(object);
			byte[] data = out.toByteArray();
			out.close();

			out = new ByteArrayOutputStream();
			Base64OutputStream b64 = new Base64OutputStream(out, 0);
			b64.write(data);
			b64.close();
			out.close();
			return new String(out.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@TargetApi(8)
	public static Object stringToObject(String encodedObject) {
		try {
			return new ObjectInputStream(new Base64InputStream(
					new ByteArrayInputStream(encodedObject.getBytes()), 0))
					.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void WriteSettings(Context context, String data,
			String filename, Boolean isappend) {
		FileOutputStream fOut = null;
		OutputStreamWriter osw = null;

		try {
			int mode = 0;
			if (isappend)
				mode = Context.MODE_APPEND;
			else
				mode = Context.MODE_PRIVATE;
			fOut = context.openFileOutput(filename, mode);
			osw = new OutputStreamWriter(fOut);
			osw.write(data);
			osw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (osw != null)
					osw.close();
				if (fOut != null)
					fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String ReadSettings(Context context, String filename) {
		StringBuffer dataBuffer = new StringBuffer();
		try {
			InputStream instream = context.openFileInput(filename);
			if (instream != null) {
				InputStreamReader inputreader = new InputStreamReader(instream);
				BufferedReader buffreader = new BufferedReader(inputreader);

				String newLine;
				while ((newLine = buffreader.readLine()) != null) {
					dataBuffer.append(newLine);
				}
				instream.close();
			}

		} catch (java.io.FileNotFoundException f) {
			Log.e(TAG, "A fájl nem található: " + filename);
			try {
				context.openFileOutput(filename, Context.MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			Log.e(TAG, "IO Hiba :" + filename);
		}

		return dataBuffer.toString();
	}

	public static ArrayList<String> Read(Context context, String filename) {
		ArrayList<String> arr = new ArrayList<String>();
		StringBuffer dataBuffer = new StringBuffer();
		try {
			InputStream instream = context.openFileInput(filename);
			if (instream != null) {
				InputStreamReader inputreader = new InputStreamReader(instream);
				BufferedReader buffreader = new BufferedReader(inputreader);

				String newLine;
				while ((newLine = buffreader.readLine()) != null) {
					dataBuffer.append(newLine);
					arr.add(newLine);
				}
				instream.close();
			}

		} catch (java.io.FileNotFoundException f) {
			Log.i(TAG, "A fájl nem található : " + filename);
			try {
				context.openFileOutput(filename, Context.MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			Log.e(TAG, "IO Hiba :" + filename);
		}

		return arr;
	}
}
