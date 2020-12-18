package com.example.walktheline.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.walktheline.R;

public class Line extends View {

    private double height;
    private int width;
    private double angle;
    private int color;
    private int distance;
    private int staticDist;
    private Paint paint;

    public Line(Context c) {
        super(c);
        //default values
        height = 0;
        width = 10;
        angle = 90;
        color = Color.BLACK;
        distance = 300;
        staticDist = 300;

        initPaint();
    }

    public Line(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //assign values from the XML file
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Line, 0, 0);
        height = a.getInteger(R.styleable.Line_line_height, 0);
        width = a.getInteger(R.styleable.Line_line_width, 0);
        angle = a.getFloat(R.styleable.Line_angle_from_top, 0);
        color = a.getColor(R.styleable.Line_fill_color, Color.BLACK);

        distance = 300;
        staticDist = 300;
        initPaint();
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onMeasure(int width, int height) {
        // let the canvas fill the screen. this will make the falling animation easier to draw
        setMeasuredDimension(Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels);
    }


    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        // x and y values for each corner of the triangle based on the angle and size using trig
        float brX = (float)this.width;
        float brY = 0;
        float blX = (float) (this.width * Math.cos(Math.toRadians(90 + (int)this.angle))) + this.width;
        float blY = (float) (this.width * Math.sin(Math.toRadians(90 + (int)this.angle)));
        float trX = (float) (this.height * Math.cos(Math.toRadians((int)this.angle))) + this.width;
        float trY = (float) (this.height * Math.sin(Math.toRadians((int)this.angle)));
        float tlX = trX + blX - (float)this.width;
        float tlY = trY + blY;
        // draw a path that will be the bridge
        Path p = new Path();
        p.moveTo(brX, -brY);
        p.lineTo(blX, -blY);
        p.lineTo(tlX, -tlY);
        p.lineTo(trX, -trY);
        p.lineTo(brX, -brY);
        p.close();
        p.offset(distance - this.width, Resources.getSystem().getDisplayMetrics().heightPixels - 800);
        c.drawPath(p, paint); // draw it on the canvas.
    }

    public void setHeight(double height){
        this.height = height;
    }

    public void setWidth(int width){
        this.width = width;
    }

    public double getLineHeight(){
        return height;
    }

    public int getLineWidth(){
        return width;
    }

    public void setAngle(double angle) {this.angle = angle;}

    public double getAngle(){return angle;}

    public void setTranslationX(int translation) {
        distance = staticDist - translation;
        invalidate();
        requestLayout();
    }

}
