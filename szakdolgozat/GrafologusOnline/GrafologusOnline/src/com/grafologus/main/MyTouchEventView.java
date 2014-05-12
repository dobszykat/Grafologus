package com.grafologus.main;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class MyTouchEventView extends View {

	public boolean befejezte = false;
	private boolean finishdrawing = false;
	private Paint paint = new Paint();
	private Path path = new Path();
	private Paint circlePaint = new Paint();
	private Path circlePath = new Path();

	public Button vege;
	public LayoutParams params;
	ArrayList<Float> koordtombx = new ArrayList<Float>();
	ArrayList<Float> koordtomby = new ArrayList<Float>();
	ArrayList<Long> speedarray = new ArrayList<Long>();
	ArrayList<Long> speedstop = new ArrayList<Long>();

	public ArrayList<Float> Xkoords() {
		return koordtombx;
	}

	public ArrayList<Float> Ykoords() {
		return koordtomby;
	}

	public ArrayList<Long> Speedkoords() {
		return speedarray;
	}

	public MyTouchEventView(Context context) {
		super(context);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(2f);

		circlePaint.setAntiAlias(true);
		circlePaint.setColor(Color.BLUE);
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setStrokeJoin(Paint.Join.MITER);
		circlePaint.setStrokeWidth(4f);
		
		vege = new Button(context);
		vege.setText("Befejeztem!");

		params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
	}

	public Boolean IsFinished() {
		if (befejezte) {
			befejezte = false;
			return true;
		}

		return befejezte;
	}

	boolean szunet = false;

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawPath(path, paint);
		canvas.drawPath(circlePath, circlePaint);
	}

	public void Reset(){
		path.reset();
		postInvalidate();
		koordtombx.clear();
		koordtomby.clear();
		speedarray.clear();
	}
	
	//Valamit rajzolnak
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!finishdrawing) {
			float Pointx = event.getX();
			float Pointy = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				szunet = false;
				path.moveTo(Pointx, Pointy);
				return true;
			case MotionEvent.ACTION_MOVE:
				path.lineTo(Pointx, Pointy);
				circlePath.reset();
				circlePath.addCircle(Pointx, Pointy, 30, Path.Direction.CW);
				break;

			case MotionEvent.ACTION_UP:
				circlePath.reset();
				szunet = true;
				break;
			default:
				return false;
			}
			
			if (!szunet) {
				koordtombx.add(Pointx);
				koordtomby.add(Pointy);
			} else {
				koordtombx.add((float) -8.0);
				koordtomby.add((float) -8.0);
			}
			speedarray.add(event.getEventTime());
			postInvalidate();
		}
		return true;
	}
}
