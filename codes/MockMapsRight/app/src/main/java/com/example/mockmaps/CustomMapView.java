package com.example.mockmaps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.Timer;

public class CustomMapView extends View {

    private Paint roadPaint;
    private Paint circlePaint;

    private Paint pathway;

    private Paint pathwayDone;


    private Bitmap destPaint;

    private float circleX;
    private float circleY;

    private float destX;
    private float destY = -100;

    private float circleRadius;
    private double moveStep;
    private boolean isMoving = true;
    private Context context;

    private Bluetooth recievedBluetooth;


    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        roadPaint = new Paint();
        roadPaint.setColor(Color.GRAY);
        roadPaint.setStrokeWidth(dpToPx(10));
        roadPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        Singleton mySingleton = Singleton.getInstance((Activity) context);
        recievedBluetooth = mySingleton.getMyObject();


        pathway = new Paint();
        pathway.setColor(Color.RED);
        pathway.setStrokeWidth(dpToPx(6));
        pathway.setStyle(Paint.Style.FILL_AND_STROKE);


        pathwayDone = new Paint();
        pathwayDone.setColor(Color.RED);
        pathwayDone.setStrokeWidth(dpToPx(6));
        pathwayDone.setStyle(Paint.Style.FILL_AND_STROKE);
        pathwayDone.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0)); // 10:10 pattern for dots

        circlePaint = new Paint();
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.FILL);
        destPaint = BitmapFactory.decodeResource(getResources(), R.drawable.dest_icon);
        destPaint = Bitmap.createScaledBitmap(destPaint, (int)dpToPx(32), (int)dpToPx(32), false); // Resize the car image


        circleX = dpToPx(96);
        circleY =dpToPx(96);
        circleRadius = dpToPx(4);
        moveStep = 1;
    }

    private int lineHeight = -10;
    private int line2Height = -200;

    boolean firstLine = true;

    private int firstx = (int) dpToPx(96);

    private boolean secondLine = false;


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale(-1f, 1f, canvas.getWidth() / 2f, 0); // Scale by -1 in the x-axis
        super.onDraw(canvas);


        canvas.drawLine(firstx, 0,firstx, getHeight(), roadPaint);

        canvas.drawLine(firstx, lineHeight,firstx, circleY, pathway);
        canvas.drawLine(firstx, lineHeight+300,firstx, circleY, pathwayDone);


        canvas.drawLine(0, lineHeight,getWidth(), lineHeight, roadPaint);


        canvas.drawLine(line2Height, lineHeight,circleX, lineHeight, pathway);
        canvas.drawLine(circleY, lineHeight,firstx, lineHeight, pathwayDone);


        canvas.drawLine(line2Height, 0,line2Height, getWidth(), roadPaint);

        canvas.drawLine(line2Height, destY+destPaint.getHeight(),line2Height, circleY, pathway);
        canvas.drawLine(line2Height, circleY,line2Height, lineHeight, pathwayDone);


        canvas.drawCircle(circleX, circleY, circleRadius, circlePaint);

        canvas.drawBitmap(destPaint, circleX, destY, null);

        new CountDownTimer(15, 2) { // 3000 milliseconds, tick every 1000 milliseconds
            public void onTick(long millisUntilFinished) {

            }
            public void onFinish() {
                if (isMoving) {
                    if (firstLine) {
                        if (lineHeight>circleY-4&&lineHeight<circleY+4){
                            setRotation(getRotation()-2);
                            if (getRotation() == -90){
                                Log.d("ble", "doneRight");
                                recievedBluetooth.startAdd("doneRight");
                                firstLine = false;
                                secondLine = true;
                            }
                        }else{
                            if (lineHeight==circleY-100){
                                Log.d("ble", "right");
                                recievedBluetooth.startAdd("right");

                            }
                            lineHeight += moveStep;
                        }

                    }else if (secondLine){
                        if (line2Height>circleX-4&&line2Height<circleX+4){
                            setRotation(getRotation()+2);
                            if (getRotation() == 0){
                                Log.d("ble","doneLeft");
                                recievedBluetooth.startAdd("doneLeft");

                                secondLine = false;
                            }
                        }else{
                            if (line2Height==circleX-100) {
                                Log.d("ble", "left");
                                recievedBluetooth.startAdd("left");
                            }
                            line2Height += moveStep;
                            firstx += moveStep;

                        }
                    }else{
                        lineHeight += moveStep;
                        destY+=moveStep;
                        if (destY+destPaint.getHeight()>circleX-4&&destY+destPaint.getHeight()<circleX+4){
                            isMoving = false;

                            new CountDownTimer(500, 500) { // 3000 milliseconds, tick every 1000 milliseconds
                                public void onTick(long millisUntilFinished) {

                                }
                                public void onFinish() {
                                    Log.d("ble","destination");
                                    recievedBluetooth.startAdd("destination");

                                    Intent intent = new Intent(context, DoneActivity.class);
                                    context.startActivity(intent);
                                }
                            }.start();



                        }
                    }


                    invalidate();
                }
            }
        }.start();
        // If moving, update position and redraw (invalidate())
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    public void startMoving() {
        isMoving = true;
        invalidate();
    }

    public void stopMoving() {
        isMoving = false;
    }

    public boolean isMoving() {
        return isMoving;
    }
}
