package axelpetit.fr.barcodescanner.core;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.util.List;

import axelpetit.fr.barcodescanner.camera.CameraPreview;
import axelpetit.fr.barcodescanner.camera.CameraWrapper;
import axelpetit.fr.barcodescanner.thread.CameraHandlerThread;
import axelpetit.fr.barcodescanner.thread.CameraProcessingHandlerThread;
import axelpetit.fr.barcodescanner.utils.CameraUtils;
import axelpetit.fr.barcodescanner.utils.ViewFinder;

/**
 * Created by Axel on 08/08/2017.
 */

public class ScannerView extends FrameLayout {
    private  CameraWrapper cameraWrapper;
    private CameraPreview mPreview;
    private Rect framingRectInPreview;
    private ViewFinder viewFinder;
    private Camera camera1;
    private ResultHandler mResultHandler;
    private BarcodeDetector barcodeDetector;
    private int barcodeFormats = Barcode.ALL_FORMATS;
    private CameraHandlerThread cameraHandlerThread;
    private CameraProcessingHandlerThread cameraProcessingHandlerThread;
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
        cameraWrapper = new CameraWrapper(context);
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
        if (cameraHandlerThread == null) {
            cameraHandlerThread = new CameraHandlerThread(this, getContext());
        }
        synchronized (cameraHandlerThread) {
            cameraHandlerThread.openCamera();
        }
    }
    public void stopCamera() {
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
        this.camera1 = cameraApi1;
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
}
