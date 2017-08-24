package axelpetit.fr.barcodescanner.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import axelpetit.fr.barcodescanner.R;

/**
 * Created by Axel on 08/08/2017.
 */

public class ViewFinder extends View {
    private Rect mFramingRect;
    private Paint mLaserPaint;
    private Paint mFinderMaskPaint;
    private Point viewSize;
    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private int scannerAlpha;
    private static final int POINT_SIZE = 10;
    private static final long ANIMATION_DELAY = 80l;
    private final int mDefaultLaserColor = getResources().getColor(R.color.viewfinder_laser);
    private final int mDefaultMaskColor = getResources().getColor(R.color.viewfinder_mask);
    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675;
    private Rect framingRectInPreview;

    public ViewFinder(Context context) {
        super(context);
        init();
    }

    public ViewFinder(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    private void init() {
        viewSize = new Point();
        //set up laser paint
        mLaserPaint = new Paint();
        mLaserPaint.setColor(mDefaultLaserColor);
        mLaserPaint.setStyle(Paint.Style.FILL);

        //finder mask paint
        mFinderMaskPaint = new Paint();
        mFinderMaskPaint.setColor(mDefaultMaskColor);
    }
    @Override
    public void onDraw(Canvas canvas) {
        drawViewFinderMask(canvas);
        drawLaser(canvas);
        viewSize.x = getWidth();
        viewSize.y = getHeight();

    }
    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Log.d("DrawViewFinderMask", "width: " + width + "-- height: " + height);
        Rect framingRect = getFramingRect();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(mDefaultMaskColor);
        canvas.drawRect(0, 0, width, framingRect.top, paint);
        canvas.drawRect(0, framingRect.top, framingRect.left, framingRect.bottom + 1, paint);
        canvas.drawRect(framingRect.right + 1, framingRect.top, width, framingRect.bottom + 1, paint);
        canvas.drawRect(0, framingRect.bottom + 1, width, height, paint);
        Log.d("FramingRect", "left: " + framingRect.left + "-- top: " + framingRect.top + "(" + framingRect.width() + ", " + framingRect.height());
    }

    public void drawLaser(Canvas canvas) {
        Rect framingRect = getFramingRect();

        // Draw a red "laser scanner" line through the middle to show decoding is active
        mLaserPaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = framingRect.height() / 2 + framingRect.top;
        canvas.drawRect(framingRect.left + 2, middle - 1, framingRect.right - 1, middle + 2, mLaserPaint);

        postInvalidateDelayed(ANIMATION_DELAY,
                framingRect.left - POINT_SIZE,
                framingRect.top - POINT_SIZE,
                framingRect.right + POINT_SIZE,
                framingRect.bottom + POINT_SIZE);
    }
    public synchronized Rect getFramingRect() {
        if (mFramingRect == null) {
            Point screenResolution = new Point(getWidth(), getHeight());
            if (screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
            int orientation = getResources().getConfiguration().orientation;
            int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
            int height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                int tmp = width;
                width = height;
                height = tmp;
            }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        }
        return mFramingRect;
    }
    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 5 * resolution / 8; // Target 5/8 of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    public Point getViewSize() {
        return viewSize;
    }
}
