package com.technology.lpjxlove.clockdemo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.Calendar;

/**
 * Created by LPJXLOVE on 2017/5/13.
 */

public class ClockView extends ImageView {
    //View默认最小宽度
    private static final int DEFAULT_MIN_WIDTH = 200;
    //秒针长度
    private float secondPointerLength;
    //分针长度
    private float minutePointerLength;
    //时针长度
    private float hourPointerLength;
    //外圆边框宽度
    private static final float DEFAULT_BORDER_WIDTH = 6f;
    //指针反向超过圆点的长度
    private static final float DEFAULT_POINT_BACK_LENGTH = 0f;
    //长刻度线
    private static final float DEFAULT_LONG_DEGREE_LENGTH = 40f;
    //短刻度线
    private static final float DEFAULT_SHORT_DEGREE_LENGTH = 20f;

    private ValueAnimator mSecondAnimator= ValueAnimator.ofFloat(0,1);

    private float circlePadding=45f;

    private float second;

    private Bitmap mBitmap;
    private Bitmap mBitmapBackground;
    private Bitmap Background;
    private Bitmap minutesBitmap;
    private Bitmap hoursBitmap;

    private Thread timeThread = new Thread() {
        @Override
        public void run() {
            try {
                while(true){
                    updateHandler.
                            sendEmptyMessage(0);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            invalidate();
        }
    };

    public ClockView(Context context) {
        super(context);
        init();
    }

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //启动线程让指针动起来
    private void init(){
        mBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.second);
        mBitmapBackground=BitmapFactory.decodeResource(getResources(), R.drawable.am);
        Background=BitmapFactory.decodeResource(getResources(), R.drawable.background);
        minutesBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.minutes);
        hoursBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.hours);

        Calendar now=Calendar.getInstance();
        second=now.get(Calendar.SECOND);
      //  timeThread.start();
        mSecondAnimator.setDuration(1000);
        mSecondAnimator.setInterpolator(new LinearInterpolator());
        mSecondAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mSecondAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Calendar now=Calendar.getInstance();
                second=now.get(Calendar.SECOND);
                Log.i("test", "onAnimation: "+second);
            }
        });
        mSecondAnimator.start();
    }

    /**
     * 计算时针、分针、秒针的长度
     */
    private void reset(){
        float r = (Math.min(getHeight() / 2, getWidth() / 2) - DEFAULT_BORDER_WIDTH / 2);
        secondPointerLength = r * 0.8f;
        minutePointerLength = r * 0.6f;
        hourPointerLength = r * 0.4f;
    }

    /**
     * 根据角度和长度计算线段的起点和终点的坐标
     * @param angle
     * @param length
     * @return
     */
    private float[] calculatePoint(float angle, float length){
        float[] points = new float[4];
        if(angle <= 90f){
            points[0] = -(float) Math.sin(angle*Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[1] = (float) Math.cos(angle*Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[2] = (float) Math.sin(angle*Math.PI/180) * length;
            points[3] = -(float) Math.cos(angle*Math.PI/180) * length;
        }else if(angle <= 180f){
            points[0] = -(float) Math.cos((angle-90)*Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[1] = -(float) Math.sin((angle-90)*Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[2] = (float) Math.cos((angle-90)*Math.PI/180) * length;
            points[3] = (float) Math.sin((angle-90)*Math.PI/180) * length;
        }else if(angle <= 270f){
            points[0] = (float) Math.sin((angle-180)*Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[1] = -(float) Math.cos((angle-180)*Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[2] = -(float) Math.sin((angle-180)*Math.PI/180) * length;
            points[3] = (float) Math.cos((angle-180)*Math.PI/180) * length;
        }else if(angle <= 360f){
            points[0] = (float) Math.cos((angle-270)*Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[1] = (float) Math.sin((angle-270)*Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[2] = -(float) Math.cos((angle-270)*Math.PI/180) * length;
            points[3] = -(float) Math.sin((angle-270)*Math.PI/180) * length;
        }
        return points;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float p = mSecondAnimator.getAnimatedFraction();
        reset();
        //画外圆
        canvas.save();
        float borderWidth = DEFAULT_BORDER_WIDTH;
        float r = Math.min(getHeight() / 2, getWidth() / 2) - borderWidth / 2;
        Paint paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setAntiAlias(true);
        paintCircle.setColor(Color.TRANSPARENT);
        paintCircle.setAlpha(80);
      //  canvas.drawCircle(getWidth() / 2, getHeight() / 2, r, paintCircle);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setColor(Color.WHITE);
     //   canvas.drawCircle(getWidth()/2,getHeight()/2 ,r/2, paintCircle);

        //画刻度线
        float degreeLength = 0f;
        Paint paintDegree = new Paint();
        paintDegree.setAntiAlias(true);
        paintDegree.setStrokeCap(Paint.Cap.ROUND);
        paintDegree.setColor(Color.GRAY);
        for(int i=0;i<120;i++){
            paintDegree.setStrokeWidth(4);
            degreeLength = DEFAULT_SHORT_DEGREE_LENGTH-5f;
            canvas.drawLine(getWidth()/2, Math.abs(getHeight()/2 - r-circlePadding), getWidth()/2,Math.abs(getHeight()/2 - r-circlePadding) + degreeLength, paintDegree);
            canvas.rotate(360/120, getWidth()/2, getHeight()/2);
        }
      /*  for (int i=0;i<12;i++){

                paintDegree.setStrokeWidth(5);
                degreeLength = DEFAULT_SHORT_DEGREE_LENGTH-5f;
                canvas.drawLine(getWidth()/2, Math.abs(getHeight()/2 - r-2*circlePadding), getWidth()/2,Math.abs(getHeight()/2 - r-2*circlePadding) + degreeLength, paintDegree);
                canvas.rotate(30, getWidth()/2, getHeight()/2);

        }*/


        canvas.translate(getWidth() / 2, getHeight() / 2);
     /*   //刻度数字
        int degressNumberSize = 30;
        Paint paintDegreeNumber = new Paint();
        paintDegreeNumber.setTextAlign(Paint.Align.CENTER);
        paintDegreeNumber.setTextSize(degressNumberSize);
        paintDegreeNumber.setFakeBoldText(true);
        for(int i=0;i<12;i++){
            float[] temp = calculatePoint((i+1)*30, r - DEFAULT_LONG_DEGREE_LENGTH - degressNumberSize/2 - 15);
           // canvas.drawText((i+1)+"", temp[2], temp[3] + degressNumberSize/2-6, paintDegreeNumber);
        }*/

        //画指针
        Paint paintHour = new Paint();
        paintHour.setAntiAlias(true);
        paintHour.setStrokeWidth(5);
        paintHour.setStrokeCap(Paint.Cap.ROUND);
        paintHour.setColor(Color.WHITE);
        Paint paintMinute = new Paint();
        paintMinute.setAntiAlias(true);
        paintMinute.setStrokeWidth(5);
        paintMinute.setColor(Color.WHITE);
        paintMinute.setStrokeCap(Paint.Cap.ROUND);
        Paint paintSecond = new Paint();
        paintSecond.setAntiAlias(true);
        paintSecond.setStrokeWidth(5);
        paintSecond.setColor(Color.WHITE);
        paintSecond.setStrokeCap(Paint.Cap.ROUND);
        Calendar now = Calendar.getInstance();
        float[] hourPoints = calculatePoint(now.get(Calendar.HOUR_OF_DAY)%12/12f*360, hourPointerLength);
       // canvas.save();
        canvas.rotate(now.get(Calendar.HOUR_OF_DAY)%12/12f*360);
        canvas.drawBitmap(hoursBitmap,-hoursBitmap.getWidth()/2,-hoursBitmap.getHeight()/2,paintMinute);
       // canvas.restore();
       // canvas.drawLine(hourPoints[0], hourPoints[1], hourPoints[2], hourPoints[3], paintHour);
        float[] minutePoints = calculatePoint(now.get(Calendar.MINUTE)/60f*360, minutePointerLength);
     //   canvas.save();
       // canvas.translate(getWidth()/2,getHeight()/2);
        canvas.rotate(-60);
        canvas.rotate(now.get(Calendar.MINUTE)/60f*360);
        canvas.drawBitmap(minutesBitmap,-minutesBitmap.getWidth()/2,-minutesBitmap.getHeight()/2,paintMinute);
      //  invalidate();
      //  canvas.restore();
        //canvas.drawLine(minutePoints[0], minutePoints[1], minutePoints[2], minutePoints[3], paintMinute);



        float[] secondPoints = calculatePoint(now.get(Calendar.SECOND)/60f*360, secondPointerLength);
        //canvas.rotate(now.get(Calendar.SECOND)/60f*360);
        Log.i("test", "angle: "+now.get(Calendar.SECOND)/60f*360+1*p*60);
      //  canvas.drawLine(secondPoints[0], secondPoints[1], secondPoints[2], secondPoints[3], paintSecond);



        //画圆心
        Paint paintCenter = new Paint();
        paintCenter.setStyle(Paint.Style.STROKE);
        paintCenter.setColor(Color.WHITE);
        canvas.drawCircle(0, 0, 10, paintCenter);
        canvas.restore();

        canvas.save();
        int count= (int) (now.get(Calendar.SECOND)/60f*360)/6;
        //画有色刻度
        for (int i=0 ;i<=count*2;i++){
            paintDegree.setStrokeWidth(5);
            paintDegree.setColor(getResources().getColor(R.color.lightYellow));
            degreeLength = DEFAULT_SHORT_DEGREE_LENGTH-7f;
            canvas.drawLine(getWidth()/2, Math.abs(getHeight()/2 - r-circlePadding), getWidth()/2,Math.abs(getHeight()/2 - r-circlePadding) + degreeLength, paintDegree);
            canvas.rotate(3, getWidth()/2, getHeight()/2);
        }
        canvas.restore();
        //画光
        canvas.save();
        canvas.translate(getWidth()/2 ,getHeight() /2);
        canvas.drawBitmap(mBitmapBackground,-mBitmapBackground.getHeight()/2,-mBitmapBackground.getWidth()/2 ,paintCenter);
        canvas.drawBitmap(Background,-Background.getHeight()/2,-Background.getWidth()/2 ,paintCenter);
        canvas.rotate(now.get(Calendar.SECOND)/60f*360);
        canvas.drawBitmap(mBitmap,-mBitmap.getHeight()/2,-mBitmap.getWidth()/2 ,paintCenter);
        invalidate();
        canvas.restore();





    }

    /**
     * 当布局为wrap_content时设置默认长宽
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int origin){
        int result = DEFAULT_MIN_WIDTH;
        int specMode = MeasureSpec.getMode(origin);
        int specSize = MeasureSpec.getSize(origin);
        if(specMode == MeasureSpec.EXACTLY){
            result = specSize;
        }else{
            if(specMode == MeasureSpec.AT_MOST){
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
}
