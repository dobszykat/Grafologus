package com.grafologus.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class SocketClass {

	private PrintWriter printwriter;
	String line = null;
	Boolean connected = false;
	boolean chatischanged = false;
	boolean willgetchattext = false;
	boolean getdata = false;
	boolean getdrawing = false;
	boolean getdrawingcoords = false;
	boolean getallcoords = false;
	boolean finishexam = false;
	boolean connectionbreak = false;
	int passwordOk = 0; // 0: M�g nincs visszajelz�s, 1:J� 2:Rossz
	AsyncTask<String, Integer, String> socket;
	boolean canconnect = true;
	int id = 0;
	Coordinates c;
	int db = 0;
	boolean errordata = false;
	volatile boolean lineischanged = false;
	BufferedReader in;
	ArrayList<String> drawingname = new ArrayList<String>();
	ArrayList<String> drawingid = new ArrayList<String>();
	ArrayList<String> drawing = new ArrayList<String>();
	ArrayList<Coordinates> Drawings = new ArrayList<Coordinates>();
	List<Coordinates> listcoordinates = new ArrayList<Coordinates>();

	public static Socket client = null;
	private static SocketClass singleton = new SocketClass();

	private SocketClass() {
	};

	public static SocketClass getInstance() {
		return singleton;
	}

	public boolean IsFinishedExam() {
		return finishexam;
	}

	public boolean GetAllCoords() {
		if (getallcoords) {
			getallcoords = false;
			return true;
		}
		return false;
	}

	public String koords;

	public String WhatIsLine() {
		if (lineischanged) {
			lineischanged = false;
		}
		if (chatischanged) {
			chatischanged = false;
		}
		return line;

	}

	public boolean IfGetDrawing() {
		if (getdrawing) {
			getdrawing = false;
			return true;
		}
		return false;
	}

	public ArrayList<String> getDrawingsId() {
		return drawingid;
	}

	public ArrayList<String> DrawingsName() {
		return drawingname;
	}

	public ArrayList<Coordinates> Drawings() {
		return Drawings;
	}

	public boolean getCoords() {
		if (getdata) {
			getdata = false;
			return true;
		}
		return false;
	}

	public void SetPartnerId(String id) {

	}

	public void ConnectToPartner(final String resztvevo, final String name,
			String ownid, String ip) {
		socket = new PostTask().execute(resztvevo, name, ownid, ip);

	}

	private class PostTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected String doInBackground(String... params) {

			String resztvevo = params[0];
			String name = params[1];
			String ownid = params[2];
			String ip = params[3];
			InetAddress serverAddr;
			try {

				serverAddr = InetAddress.getByName(ip);
				client = new Socket(serverAddr, 7452);
			} catch (SocketException e) {
				canconnect = false;
				socket.cancel(true);
			} catch (UnknownHostException e) {
				canconnect = false;
				socket.cancel(true);
			} catch (SocketTimeoutException e) {
				canconnect = false;
				socket.cancel(true);
			} catch (NullPointerException e) {
				canconnect = false;
				socket.cancel(true);
			} catch (Exception e) {
				canconnect = false;
				socket.cancel(true);
			}
			if(client == null){ canconnect = false; socket.cancel(true);}
			try {
				printwriter = new PrintWriter(new OutputStreamWriter(
						client.getOutputStream(), "UTF-8"), true);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				canconnect = false; socket.cancel(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			printwriter.write(resztvevo);
			printwriter.write(name);
			printwriter.flush();
			SendPatientInfo(ownid);
			new Thread() {
				public void run() {
					try {
						in = new BufferedReader(new InputStreamReader(
								client.getInputStream()));
						while (true) {
							if ((line = in.readLine()) != null) {
								// A szerver foglalt (M�r kapcsol�dva vannak,
								// vagy m�r l�pett be ugyanolyan r�sztvev�)
								if (line.equals("No more partner")) {
									canconnect = false;
									socket.cancel(true);
								}
								// Grafol�gus jelsz� ellen�rz�s
								else if (line.equals("Password OK.")) {
									passwordOk = 1;
								} else if (line.equals("Wrong Password.")) {
									passwordOk = 2;
								}
								// Ha l�trej�tt a kapcsolat, a szerver jelzi
								else if (line.equals("Kapcsolat")) {
									connected = true;
								} else if (line
										.equals("Megszakadt a kapcsolat!")) {
									connectionbreak = true;
								}
								// Chat �zenet �rkezik
								else if (line.equals("chat")) {
									willgetchattext = true;
								}
								// Grafol�gus befejezi a vizsg�latot
								else if (line.equals("finishexam")) {
									finishexam = true;
								}
								// Grafol�gus megkapja a koordin�t�kat
								else if (line.equals("grafologus kap")) {
									StringBuilder tmp = new StringBuilder();
									String tmp2 = "";
									while (0 != (line = in.readLine())
											.compareTo("end")) {
										if (line != null) {
											tmp.append(line + "\n");
										}
									}
									tmp2 = tmp.toString();

									tmp2 = tmp2.substring(0, tmp.length() - 1);
									while (true) {
										if ((line = in.readLine()) != null
												&& !line.equals("")) {
											id = Integer.parseInt(line);
											break;
										}
									}
									ReadArray(tmp2);
								}
								// Kor�bbi rajzokb�l kiv�lasztott adatait kapja a grafol�gus
								else if (line.trim().equals(
										"Get selected drawing data")) {
									ReadCoordinates();
									// Kor�bbi rajzokn�l megkapja az eddig k�rdezettek list�j�t
								} else if (line.equals("GetQuestions")) {
									int num = 0;
									while (true) {
										if ((line = in.readLine()) != null) {
											num = Integer.parseInt(line);
											break;
										}
									}

									int k = 0;
									for (int i = 0; i < num * 2; i++) {
										while (true) {
											if ((line = in.readLine()) != null) {
												if ((k & 1) == 0) {
													drawingid.add(line);
													k++;
												} else {
													drawingname.add(line);
													k++;
												}
												break;
											}
										}
									}
									getdrawing = true;
								}
								//K�vetkez� �zenet a chat sz�vege lesz
								else if (willgetchattext) {
									willgetchattext = false;
									chatischanged = true;
								}
								//Kapott utas�t�st
								else {
									lineischanged = true;
								}
							}
						}
					}
						catch (SocketException e) {
						 connectionbreak = true;
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
			return "All Done!";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

		}
	}

	//Beolvassa a koordin�t�kat
	public void ReadCoordinates() throws IOException {
		StringBuilder tmp = new StringBuilder();
		String tmp2 = "";
		try {
			while (0 != (line = in.readLine()).compareTo("end")) {
				if (line != null) {
					tmp.append(line + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		tmp2 = tmp.toString();
		tmp2 = tmp2.substring(0, tmp2.length() - 1);
		ReadArray(tmp2);
	}

	//P�ciens elk�ldi a szervernek a koordin�t�kat
	public void SendArray(Coordinates c, String DrawingName) throws IOException {
		Printing("koord\n", false);
		Printing(DrawingName+"\n", true);
		koords = SerializeObject.objectToString(c);
		Log.i("koord", koords);
		Printing(koords, false);
		Printing("\nend\n", false);
		printwriter.flush();
	}

	//A beolvasott stringet konvert�lja Coordinates-�
	public Coordinates ReadArray(String x) throws StreamCorruptedException {
		Object obj = SerializeObject.stringToObject(x);
		if (obj instanceof Coordinates) {
			c = (Coordinates) obj;
		}
		
		getdata = true;

		return c;
	}

	//Adott rajz kit�rl�se (id alapj�n)
	public void RemoveRow() {
		Printing("Delete", true);
		Printing(String.valueOf(id), true);
	}

	//K�r�s a szervert�l, hogy adja meg a k�rd�sek list�j�t
	public void GetDrawingsList() {
		Printing("GetDrawingsList", true);
	}

	public int getId() {
		return id;
	}

	public Coordinates Coord() {
		return c;
	}

	//Kil�p�s
	public void Exit() {
		Printing("quit\n", true);
		try {
			in.close();
			printwriter.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
		}
	}

	//P�ciens elk�ldi az id-j�t, hogy ez alapj�n lehessen azonos�tani
	public void SendPatientInfo(String id) {
		printwriter.write("patientid\n");
		printwriter.write(id);
		printwriter.flush();
	}

	public boolean IsChat() {
		return chatischanged;
	}

	//�zenet k�ld�s
	public void Printing(String line, boolean flush) {
		printwriter.write(line + "\n");
		if (flush) {
			printwriter.flush();
		}
	}

	public int Password() {
		return passwordOk;
	}

	public boolean CanConnect() {
		return canconnect;
	}

	public Boolean IsConnected() {
		return connected;
	};

	public boolean IsConnectionBreak() {
		return connectionbreak;
	}

	public Boolean LineisChanged() {
		return lineischanged;
	}

};
