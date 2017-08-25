package fr.axelpetit.barcodescanner.core;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.util.List;

import fr.axelpetit.barcodescanner.camera.CameraPreview;
import fr.axelpetit.barcodescanner.camera.CameraPreview2;
import fr.axelpetit.barcodescanner.thread.CameraHandlerThread;
import fr.axelpetit.barcodescanner.thread.CameraProcessingHandlerThread;
import fr.axelpetit.barcodescanner.utils.CameraUtils;
import fr.axelpetit.barcodescanner.view.AutoFitTextureView;
import fr.axelpetit.barcodescanner.view.ViewFinder;

/**
 * Created by Axel on 08/08/2017.
 */

public class ScannerView extends FrameLayout {
    private CameraPreview mPreview;
    private CameraPreview2 mPreview2;
    private AutoFitTextureView mTextureView;
    private Rect framingRectInPreview;
    private ViewFinder viewFinder;
    private boolean useApi1 = false;
    private ResultHandler mResultHandler;
    private BarcodeDetector barcodeDetector;
    private int barcodeFormats = Barcode.ALL_FORMATS;
    private CameraHandlerThread cameraHandlerThread;
    private CameraProcessingHandlerThread cameraProcessingHandlerThread;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            if (mPreview2 != null)
                try {
                    mPreview2.openCamera(width, height);
                } catch (CameraPreview2.CameraApiNotSupport cameraApiNotSupport) {
                    cameraApiNotSupport.getMessage();
                }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            if (mPreview2 != null && !useApi1)
                mPreview2.configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            // Gets the task from the incoming Message object.
            CameraPreview preview = (CameraPreview) inputMessage.obj;
            switch (inputMessage.what) {
                // The decoding is done
                case CameraUtils.STATE_CAMERA_OPENED:
                            /*
                             * Moves the Bitmap from the task
                             * to the View
                             */
                    removeAllViews();
                    addView(preview);
                    addView(viewFinder);
                    break;
                default:
                            /*
                             * Pass along other messages from the UI
                             */
                    super.handleMessage(inputMessage);
            }
        }
    };
    public ScannerView(@NonNull Context context) {
        super(context);
        viewFinder = new ViewFinder(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextureView = new AutoFitTextureView(context);
            mPreview2 = new CameraPreview2(context, mTextureView);
        }
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
            Point screenResolution = viewFinder.getViewSize();
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
    public void startCamera() {
        BarcodeDetector.Builder builder = new BarcodeDetector.Builder(getContext());
        builder.setBarcodeFormats(barcodeFormats);
        barcodeDetector = builder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            try {
                startCameraApi2();
            } catch (CameraPreview2.CameraApiNotSupport cameraApiNotSupport) {
                startCameraApi1();
                useApi1 = true;
            }
        else
            startCameraApi1();
    }
    public void stopCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !useApi1)
            stopCameraApi2();
        else
            stopCameraApi1();

    }
    public void stopPreview() {
        if (mPreview != null) {
            stopCamera();
        }
    }
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (cameraProcessingHandlerThread == null) {
                cameraProcessingHandlerThread = new CameraProcessingHandlerThread(getContext(), ScannerView.this, barcodeDetector, mResultHandler);
            }
            synchronized (cameraProcessingHandlerThread) {
                cameraProcessingHandlerThread.startProcessing(data, camera);
            }
        }
    };
    public void setCameraApi1(Camera cameraApi1) {
        mPreview = new CameraPreview(getContext(), cameraApi1, this);
    }

    public void handleCameraState(int state) {
        switch (state) {
            case CameraUtils.STATE_CAMERA_OPENED:
                Message completeMessage = mHandler.obtainMessage(state, mPreview);
                completeMessage.sendToTarget();
                break;
        }
    }
    public void resumeCameraPreview() {
        mPreview.startPreview();
    }
    public Camera.PreviewCallback getPreviewCallback() {
        return mPreviewCallback;
    }

    public void setResultHandler(ResultHandler mResultHandler) {
        this.mResultHandler = mResultHandler;
    }
    public void setBarcodeFormats(List<Integer> barocodeFormats) {
        int res = barocodeFormats.get(0);
        for (int i = 1; i < barocodeFormats.size(); ++i){
            res |= barocodeFormats.get(i);
        }
        barcodeFormats = res;
    }

    private void startCameraApi1() {
        if (cameraHandlerThread == null) {
            cameraHandlerThread = new CameraHandlerThread(this, getContext());
        }
        synchronized (cameraHandlerThread) {
            cameraHandlerThread.openCamera();
        }
    }

    private void startCameraApi2() throws CameraPreview2.CameraApiNotSupport {
        if (mPreview2 != null ) {
            mPreview2.startBackgroundThread();
            if (mTextureView.isAvailable()) {
                mPreview2.openCamera(mTextureView.getWidth(), mTextureView.getHeight());
            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        }
    }
    private void stopCameraApi1() {
        if (cameraProcessingHandlerThread != null) {
            cameraProcessingHandlerThread.quit();
            cameraProcessingHandlerThread = null;
        }
        if (cameraHandlerThread != null) {
            cameraHandlerThread.quit();
            cameraHandlerThread = null;
        }
        if (mPreview != null) {
            mPreview.stopPreviewAndFreeCamera();
            mPreview = null;
        }
    }
    private void stopCameraApi2() {
        if (mPreview2 != null) {
            mPreview2.stopBackgroundThread();
            mPreview2.closeCamera();
            mPreview2 = null;
        }
    }
}
