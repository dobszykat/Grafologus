package com.grafologus.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class Graphologist_exam extends View {

	final String LOG_TAG = "debugger";
	private Paint paint = new Paint();
	private Path path = new Path();
	private Paint circlePaint = new Paint();
	private Path circlePath = new Path();
	Coordinates c = null;
	int height = 0;
	int width = 0;
	boolean finishdrawing = true;
	float rateheight = 0;
	float ratewidth = 0;
	boolean stopdrawing = false;

	public boolean FinishDrawing() {
		if (finishdrawing) {
			finishdrawing = false;
			return true;
		}
		return false;
	}

	public Graphologist_exam(Context context) {
		super(context);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(2f);

		circlePaint.setAntiAlias(true);
		circlePaint.setColor(Color.RED);
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setStrokeJoin(Paint.Join.MITER);
		circlePaint.setStrokeWidth(4f);

	}

	public void Replay() {
		stopdrawing = true;
		Reset();
		Drawing(c, height, width);
	}

	public void Reset() {
		path.reset();
		postInvalidate();
	}

	public void Drawing(Coordinates co, final int height, final int width) {
		finishdrawing = false;
		this.height = height;
		this.width = width;
		c = co;
		stopdrawing = false;
		Reset();
		circlePath.reset();
		rateheight = (float) height / c.height;
		ratewidth = (float) width / c.width;
		new Thread() {
			@Override
			public void run() {
				long speed = 0;
				path.moveTo(c.koordtombx.get(0) * rateheight,
						c.koordtomby.get(0) * ratewidth);
				int k;
				for (k = 1; k < c.koordtombx.size(); k++) {
					if (!stopdrawing) {
						speed = c.speedarray.get(k) - c.speedarray.get(k - 1);
						if (c.koordtombx.get(k - 1) == -8
								&& c.koordtombx.get(k) != -8) {
							path.moveTo(c.koordtombx.get(k) * rateheight,
									c.koordtomby.get(k) * ratewidth);
							circlePath.reset();
						} else if (c.koordtombx.get(k - 1) != -8
								&& c.koordtombx.get(k) != -8) {
							path.lineTo(c.koordtombx.get(k) * rateheight,
									c.koordtomby.get(k) * ratewidth);
							circlePath.reset();
							circlePath.addCircle(c.koordtombx.get(k)
									* rateheight, c.koordtomby.get(k)
									* ratewidth, 30, Path.Direction.CW);
						}
						try {

							long tmp = speed;
							if (tmp < 0) {
								tmp = 0;
							}
							if (tmp > 1000) {
								tmp = 1000;
							}
							Thread.sleep(tmp);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						postInvalidate();
					} else {
						Reset();
						break;
					}
				}
				circlePath.reset();
				postInvalidate();
				finishdrawing = true;

			}
		}.start();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawPath(path, paint);
		canvas.drawPath(circlePath, circlePaint);
	}
}
