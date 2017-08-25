package fr.axelpetit.barcodescanner.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import fr.axelpetit.barcodescanner.core.ScannerView;
import fr.axelpetit.barcodescanner.utils.CameraUtils;


/**
 * Created by Axel on 08/08/2017.
 */

public class CameraPreview extends SurfaceView  implements SurfaceHolder.Callback {
    private final String Tag = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera.Size mPreviewSize;
    private Camera mCamera;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.PreviewCallback previewCallback;
    private boolean cameraClosed;

    public CameraPreview(Context context, Camera camera, ScannerView scannerView) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        this.mCamera = camera;
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.previewCallback = scannerView.getPreviewCallback();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(mHolder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        if (mCamera != null) {
           // stopPreviewAndFreeCamera();
            stopCameraPreview();
            mPreviewSize = CameraUtils.chooseOptimalSize(mSupportedPreviewSizes, w,  h);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture.
            startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
        }
    }

    public void stopCameraPreview() {
        if (mCamera != null) {
            getHolder().removeCallback(this);
            mCamera.setOneShotPreviewCallback(null);
            mCamera.stopPreview();
        }
    }
    public void stopPreviewAndFreeCamera() {
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();
            mCamera = null;
            cameraClosed = true;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = CameraUtils.chooseOptimalSize(mSupportedPreviewSizes, width, height);
        }
    }

    public void startPreview() {
        if (mCamera != null) {
            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture.
            mHolder.addCallback(this);
            mCamera.startPreview();
            mCamera.setOneShotPreviewCallback(previewCallback);
            cameraClosed = false;
        }
    }
}
