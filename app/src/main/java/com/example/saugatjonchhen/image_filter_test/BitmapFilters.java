package com.example.saugatjonchhen.image_filter_test;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

/**
 * Created by Saugat Jonchhen on 1/8/2018.
 */

public class BitmapFilters {

    private static final String LOG_TAG = BitmapFilters.class.getSimpleName();

    public enum Filters {
        SEPIA, GREY, NORMAL, BRIGHT, BINARY;

        private static Filters[] value = values();

        public Filters next() {
            return value[(this.ordinal() + 1) % value.length];
        }

        public Filters previous() {
            if (this.ordinal() == 0) {
                return value[value.length - 1];
            } else {
                return value[((this.ordinal() - 1) % value.length)];
            }
        }
    }

    public static Bitmap getBitmapOnFilterValue(BitmapFilters.Filters bitmapFilter, Bitmap source) {
        switch (bitmapFilter) {
            case SEPIA: {
                return getSepiaScaledBitmap(source);
            }

            case GREY: {
                return getGreyScaledBitmap(source);
            }

            case BINARY: {
                return getBinaryScaledBitmap(source);
            }

            case BRIGHT: {
                return getBrightScaledBitmap(source);
            }

        }
        return null;
    }

    public static Bitmap getGreyScaledBitmap(Bitmap source) {
        float[] GrayArray = {
                0.213f, 0.715f, 0.072f, 0.0f, 0.0f,
                0.213f, 0.715f, 0.072f, 0.0f, 0.0f,
                0.213f, 0.715f, 0.072f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
        };
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix(GrayArray);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setAntiAlias(true);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(source);
        canvas.drawBitmap(source, 0, 0, paint);
        return source;
    }


//    public static Bitmap getInvertedColorBitmap(Bitmap source) {
//        float[] InvertColorArray = {
//                1.4f,-0.062f,-0.062f,0,0,
//                -0.122f,1.378f,-0.122f,0,0,
//                -0.016f,-0.016f,1.483f,0,0,
//                -0.03f,0.05f,-0.02f,0,1};
//        Paint paint = new Paint();
//        ColorMatrix colorMatrix = new ColorMatrix(InvertColorArray);
//        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
//        paint.setAntiAlias(true);
//        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
//        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
//        paint.setColorFilter(filter);
//        Canvas canvas = new Canvas(source);
//        canvas.drawBitmap(source, 0, 0, paint);
//        return source;
//    }

    private static Bitmap getSepiaScaledBitmap(Bitmap source) {
        float[] sepiaArray = {1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 0.8f, 0, 0,
                0, 0, 0, 1, 0};
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix(sepiaArray);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setAntiAlias(true);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(source);
        canvas.drawBitmap(source, 0, 0, paint);
        return source;
    }

    private static Bitmap getBinaryScaledBitmap(Bitmap source) {
        float m = 255f;
        float t = -255 * 128f;
        float[] binaryArray = {
                m, 0, 0, 1, t,
                0, m, 0, 1, t,
                0, 0, m, 1, t,
                0, 0, 0, 1, 0
        };
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix(binaryArray);
        ColorMatrix binaryColorMatrix = new ColorMatrix();
        binaryColorMatrix.setSaturation(0);
        binaryColorMatrix.postConcat(colorMatrix);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(binaryColorMatrix);
        paint.setAntiAlias(true);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(source);
        canvas.drawBitmap(source, 0, 0, paint);
        return source;
    }

    /**
     * @param source input bitmap
     * @return new bitmap
     */
    private static Bitmap getBrightScaledBitmap(Bitmap source) {
        //contrast 0..10 1 default
        //brightness -255..255 0 is default
        int contrast = 2;
        int brightness = 20;
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Canvas canvas = new Canvas(source);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(source, 0, 0, paint);

        return source;
    }
}