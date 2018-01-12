package com.example.saugatjonchhen.image_filter_test;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import net.alhazmy13.imagefilter.ImageFilter;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.example.saugatjonchhen.image_filter_test.BitmapFilters.getBitmapOnFilterValue;

public class FilterIntent extends AppCompatActivity {
    public final String LOG_TAG = "this class";
    protected ImageView imageFromCamera;
    protected Bitmap image;
    private boolean mIsFlingFired;
    private GestureDetector mGesture;
    private GeneralUtils.ScrollDirection mCurrentScrollDirection;
    private Bitmap mCurrentBitmap;
    private Bitmap mPreviousBitmap;
    private Bitmap mNextBitmap = null;
    private Bitmap mTempBitmap;
    private Bitmap mResultBitmap;
    private Canvas mImageCanvas;
    private BitmapFilters.Filters[] value = BitmapFilters.Filters.values();
    private BitmapFilters.Filters mCurrentFilter;// = value[0];
    private BitmapFilters.Filters mPreviousFilter;// = value[1];
    private BitmapFilters.Filters mNextFilter;// = value[2];
    //    private BitmapFilters.Filters[] mTempFilter = BitmapFilters.Filters.values();
    private BitmapCache mBitmapCache;
    int mTouchX;
    private static final int SWIPE_DISTANCE_THRESHOLD = 125;
    private static final int SWIPE_VELOCITY_THRESHOLD = 75;


    int x = 0;   // The x coordinate of the filter. This variable will be manipulated in either onFling or onScroll.
    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_intent);
        mGesture = new GestureDetector(this, mOnGesture);
        mCurrentScrollDirection = GeneralUtils.ScrollDirection.NONE;
        mImageCanvas = new Canvas();
        imageFromCamera = findViewById(R.id.imageFromCamera);
        String filename = getIntent().getStringExtra("image");

        try {
            FileInputStream is = this.openFileInput(filename);
            Log.i("img", is.toString());
            bmp = BitmapFactory.decodeStream(is);
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            byte[] data = stream.toByteArray();
//            Camera.CameraInfo info = new Camera.CameraInfo();
//            Camera.getCameraInfo(mCurrentCameraId, info);
//            Camera.Parameters parameters = mCamera.getParameters();
//            int imageFormat = parameters.getPreviewFormat();
//            if (ImageFormat.NV21 == imageFormat) {
//                Rect rect = new Rect(0, 0, parameters.getPreviewSize().width,
//                        parameters.getPreviewSize().height);
//                ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
//                YuvImage img = new YuvImage(data, ImageFormat.NV21,
//                        parameters.getPreviewSize().width, parameters.getPreviewSize().height, null);
//                img.compressToJpeg(rect, 80, BAOS);
//                byte[] bytes = BAOS.toByteArray();
//                mCurrentBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                mCurrentFilter = BitmapFilters.Filters.NORMAL;
//                mBitmapCache.addBitmapCache(mCurrentFilter.name(), mCurrentBitmap.copy(Bitmap.Config.ARGB_8888, true));
//            }
            imageFromCamera.setImageBitmap(bmp);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        mTempBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.message);
        mTempBitmap = bmp;
        if (mTempBitmap != null)
            Log.i("check2", "not null");
        else
            Log.i("check2", "null");
        mCurrentScrollDirection = GeneralUtils.ScrollDirection.NONE;
        mCurrentFilter = BitmapFilters.Filters.NORMAL;
        imageFromCamera.setOnTouchListener(imageTouchListener);
        if (bmp == null) {
            mCurrentBitmap = BitmapFactory.decodeResource(FilterIntent.this.getResources(), R.drawable.message);
        } else {
            mCurrentBitmap = bmp;
        }

//        mCurrentFilter = BitmapFilters.Filters.NORMAL;
        mBitmapCache = BitmapCache.getInstance();
        mBitmapCache.initializeCache();
        mBitmapCache.addBitmapCache(mCurrentFilter.name(), mCurrentBitmap.copy(Bitmap.Config.ARGB_8888, true));
        new LoadOtherBitmaps().
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    View.OnTouchListener imageTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGesture.onTouchEvent(event);
            Log.i("check1", "touch true");
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP: {
                    Log.i("check1", "action up true");
                    if (!mIsFlingFired) {
                        Log.i("check1", "misflingfired true");
                        resetToCurrentFilter();
                    }
                    break;
                }
            }
            return true;
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled = mGesture.onTouchEvent(ev);
        return handled;
    }


    private GestureDetector.OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            if (mIsFlingFired) {
                mIsFlingFired = false;
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            switch (mCurrentScrollDirection) {
                case LEFT: {
                    overlayNextBitmap(mCurrentBitmap.getWidth() - 1);
                    imageFromCamera.setImageDrawable(new BitmapDrawable(getResources(), mNextBitmap));
                    shuffleBitmap(true);
                    break;
                }
                case RIGHT: {

                    overlayPreviousBitmap(1);
                    imageFromCamera.setImageDrawable(new BitmapDrawable(getResources(), mPreviousBitmap));
                    shuffleBitmap(false);
                    break;
                }
            }
            mIsFlingFired = true;
            mCurrentScrollDirection = GeneralUtils.ScrollDirection.NONE;
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // checks if we're touching for more than 2f. I like to have this implemented, to prevent
            // jerky image motion, when not really moving my finger, but still touching. Optional.
            if (mCurrentScrollDirection.ordinal() == GeneralUtils.ScrollDirection.NONE.ordinal()) {
                if (distanceX > 0) {
                    mCurrentScrollDirection = GeneralUtils.ScrollDirection.LEFT;
                } else {
                    mCurrentScrollDirection = GeneralUtils.ScrollDirection.RIGHT;
                }
            }
            mTouchX = (int) e2.getX();
            overlayBitmaps(mTouchX);
            return false;
        }
    };

    private void overlayBitmaps(int coordinateX) {

        switch (mCurrentScrollDirection) {
            case NONE: {
                //do nothing here
                break;
            }
            case LEFT: {
                overlayNextBitmap(coordinateX);
                break;
            }
            case RIGHT: {
                overlayPreviousBitmap(coordinateX);
                break;
            }
        }
    }

    private void overlayPreviousBitmap(int coordinateX) {
        mImageCanvas.save();
        Log.i("Imagewidth", String.valueOf(mCurrentBitmap.getWidth()));
        Bitmap OSBitmap = Bitmap.createBitmap(mCurrentBitmap, coordinateX, 0, mCurrentBitmap.getWidth() - coordinateX, mCurrentBitmap.getHeight());
        mImageCanvas.drawBitmap(OSBitmap, coordinateX, 0, null);

        Bitmap FSBitmap = Bitmap.createBitmap(mPreviousBitmap, 0, 0, coordinateX, mCurrentBitmap.getHeight());
        mImageCanvas.drawBitmap(FSBitmap, 0, 0, null);

        mImageCanvas.restore();

        imageFromCamera.setImageDrawable(new BitmapDrawable(getResources(), mResultBitmap));
    }

    private void overlayNextBitmap(int coordinateX) {
        mImageCanvas.save();

//        Bitmap OSBitmap = Bitmap.createBitmap(mCurrentBitmap, 0, 0, coordinateX, mCurrentBitmap.getHeight());
        Bitmap OSBitmap = Bitmap.createBitmap(mCurrentBitmap, 0, 0, coordinateX, mCurrentBitmap.getHeight());
        mImageCanvas.drawBitmap(OSBitmap, 0, 0, null);

        Bitmap FSBitmap = Bitmap.createBitmap(mNextBitmap, coordinateX, 0, mCurrentBitmap.getWidth() - coordinateX, mCurrentBitmap.getHeight());
        mImageCanvas.drawBitmap(FSBitmap, coordinateX, 0, null);

        mImageCanvas.restore();

        imageFromCamera.setImageDrawable(new BitmapDrawable(getResources(), mResultBitmap));
    }

    private void shuffleBitmap(boolean isSwipeLeft) {
        if (isSwipeLeft) {
            mPreviousBitmap = mCurrentBitmap;
            mPreviousFilter = mCurrentFilter;
            mCurrentBitmap = mNextBitmap;
            mCurrentFilter = mNextFilter;
            mNextFilter = mCurrentFilter.next();
            //note next bitmap can be null if not found in cache
            mNextBitmap = mBitmapCache.getBitmapCache(mNextFilter.name());

            if (mNextBitmap != null) {
                Log.i("check1", "next filter index " + mNextFilter.ordinal() + " " + mNextFilter.name());
            } else {
                Log.i("check1", "next bitmap IS NULL");
                mNextBitmap = mTempBitmap;
            }

        } else {
            mNextBitmap = mCurrentBitmap;
            mNextFilter = mCurrentFilter;
            mCurrentFilter = mPreviousFilter;
            mCurrentBitmap = mPreviousBitmap;
            mPreviousFilter = mCurrentFilter.previous();
            //note previous bitmap can be null if not found in cache
            mPreviousBitmap = mBitmapCache.getBitmapCache(mPreviousFilter.name());

            if (mPreviousBitmap != null) {
                Log.i("check1", "previous filter index " + mPreviousFilter.ordinal() + " " + mPreviousFilter.name());
            } else {
                Log.i("check1", "previous bitmap IS NULL");
                mPreviousBitmap = mTempBitmap;
            }
        }
    }

    private void resetToCurrentFilter() {
        imageFromCamera.setImageDrawable(new BitmapDrawable(getResources(), mCurrentBitmap));
        mIsFlingFired = false;
        mCurrentScrollDirection = GeneralUtils.ScrollDirection.NONE;
    }


    private class LoadOtherBitmaps extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            mNextBitmap = mCurrentBitmap.copy(Bitmap.Config.ARGB_8888, true);

            mNextFilter = mCurrentFilter.next();
            mNextBitmap = getBitmapOnFilterValue(mNextFilter, mNextBitmap);
            mBitmapCache.addBitmapCache(mNextFilter.name(), mNextBitmap);
            mPreviousBitmap = mCurrentBitmap.copy(Bitmap.Config.ARGB_8888, true);
            mPreviousFilter = mCurrentFilter.previous();
            mPreviousBitmap = getBitmapOnFilterValue(mPreviousFilter, mPreviousBitmap);
            mBitmapCache.addBitmapCache(mPreviousFilter.name(), mPreviousBitmap);
            mResultBitmap = Bitmap.createBitmap(mCurrentBitmap.getWidth(), mCurrentBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            mImageCanvas = new Canvas(mResultBitmap);

            Bitmap currentBitmap = mCurrentBitmap;
            BitmapFilters.Filters currentFilter = mCurrentFilter;
            BitmapFilters.Filters looperFilter = currentFilter.next().next();

            Log.d(LOG_TAG, "current filter " + currentFilter.name());
            Log.i("current filter ", currentFilter.name());
            while (looperFilter.ordinal() != currentFilter.ordinal()) {
                Log.d(LOG_TAG, "looper filter " + looperFilter.name());
                Log.i("looper filter ", looperFilter.name());
                Bitmap bitmap = mBitmapCache.getBitmapCache(looperFilter.name());
                if (bitmap == null) {
                    Log.d(LOG_TAG, "looper filter was NULL");
                    Log.i("looper filter was NULL", looperFilter.name());
                    bitmap = getBitmapOnFilterValue(looperFilter, currentBitmap.copy(Bitmap.Config.ARGB_8888, true));
                    mBitmapCache.addBitmapCache(looperFilter.name(), bitmap);
                } else {
                    Log.d(LOG_TAG, "looper filter was NOT NULL");
                    Log.i("looper filter", "looper filter was NOT NULL");
                }
                looperFilter = looperFilter.next();
            }

            return false;
        }
    }


}




