package com.example.walktheline.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.walktheline.R;

public class Player extends View {

    private int x;
    private int y;
    private int width;
    private int height;
    private Paint paint;


    public Player(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // set our instance variables based on the attrs from the XML
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Line, 0, 0);
        x = a.getInteger(R.styleable.Player_x, 300);
        y = a.getInteger(R.styleable.Player_y, 800);
        width = 80;
        height = 100;
        initPaint(); // get a basic paint
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onMeasure(int width, int height) {
        setMeasuredDimension(Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.adam); // get the player image from the resoruces
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true); // scale it down to the size we want
        canvas.drawBitmap(bitmap, x - width, screenHeight - y - height, paint); // draw it on the canvas, x and y is the bottom right corner
    }

    public int getPlayerWidth() {
        return width;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
