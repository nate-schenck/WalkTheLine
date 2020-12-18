package com.example.walktheline.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.walktheline.R;

public class Platform extends View {

    private int width;
    private int color;
    private int distance;
    private int staticDist;
    private Paint paint;

    public Platform(Context c, int width, int distance, int color) {
        super(c);
        // initiate with our own values, used for the random generation
        this.width = width;
        this.distance = distance;
        this.color = color;

        initPaint();
    }

    public Platform(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // initiate values from an xml, we don't use this.
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Platform, 0, 0);
        width = a.getInteger(R.styleable.Platform_width, 0);
        color = a.getColor(R.styleable.Platform_plat_color, Color.BLACK);
        distance = a.getInteger(R.styleable.Platform_plat_distance, 20);

        initPaint();
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onMeasure(int width, int height) {
        //set the canvas to the whole screen. again, easier drawing.
        setMeasuredDimension(Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        Path p = new Path();
        p.addRect(distance, 800, distance + width, 0, Path.Direction.CW); // draw the platform on the path
        p.close();
        p.offset(0, c.getHeight() - 800); // put it on the bottom of the screen
        c.drawPath(p, paint); // draw the path on the canvas
    }

    public int getDistance() {
        return distance;
    }

    public int getPlatformWidth() { return width; }

    public void setStaticDistance(int staticDist) { this.staticDist = staticDist; }

    public void setTranslationX(int translation) {
        distance = staticDist - translation;
    }
}
