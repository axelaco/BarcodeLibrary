package axelpetit.fr.barcodescanner.core;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import axelpetit.fr.barcodescanner.camera.CameraPreview;
import axelpetit.fr.barcodescanner.camera.CameraWrapper;
import axelpetit.fr.barcodescanner.utils.ViewFinder;

/**
 * Created by Axel on 08/08/2017.
 */

public class ScannerView extends FrameLayout {
    private CameraPreview mPreview;
    private Rect framingRectInPreview;
    private ViewFinder viewFinder;
    private Camera camera1;
    private CameraWrapper cameraWrapper;
    private boolean inPreview;

    public ScannerView(@NonNull Context context) {
        super(context);
        cameraWrapper = new CameraWrapper(context);
        mPreview = new CameraPreview(context, cameraWrapper.getCamera(Camera.CameraInfo.CAMERA_FACING_BACK));
        viewFinder = new ViewFinder(context);
    }

    public ScannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public synchronized Rect getFramingRectInPreview(Point cameraResolution) {
        if (framingRectInPreview == null) {
            Rect framingRect = viewFinder.getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Point screenResolution = new Point();
            windowManager.getDefaultDisplay().getSize(screenResolution);
            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    public void stopPreview() {
        if (mPreview != null) {
            mPreview.stopPreviewAndFreeCamera();
        }
    }
    public void startPreview() {
        removeAllViews();
        if (mPreview != null) {
            if (mPreview.cameraClose()) {
                camera1 = cameraWrapper.getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                mPreview.setCamera(camera1);
            }
            addView(mPreview);
            addView(viewFinder);
            mPreview.startPreview();
        }
    }
}
