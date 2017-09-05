package fr.axelpetit.barcodescanner.thread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.nio.ByteBuffer;

import fr.axelpetit.barcodescanner.core.ResultHandler;
import fr.axelpetit.barcodescanner.core.ScannerView;
import fr.axelpetit.barcodescanner.utils.CameraUtils;
import fr.axelpetit.barcodescanner.utils.PlanarYUVLuminanceSource;

/**
 * Created by Axel on 27/08/2017.
 */

public class Camera2ProcessingHandlerThread extends HandlerThread {
    private BarcodeDetector detector;
    private Context context;
    private ScannerView scannerView;
    private boolean barcodeFinded = false;
    private Handler handler;
    private ResultHandler resultHandler;
    private int mPendingFrame = 0;
    private long mPendingTimeMillis;
    private long mStartTime = SystemClock.elapsedRealtime();
    private byte[] mPendingFrameData;
    private final Detector.Processor<Barcode> barcodeProcessor = new Detector.Processor<Barcode>() {
        @Override
        public void release() {

        }

        @Override
        public void receiveDetections(Detector.Detections<Barcode> detections) {
            if (detections.getDetectedItems().size() > 0) {
                resultHandler.handleResult(detections.getDetectedItems().valueAt(0));
                scannerView.stopPreview();
            }
        }
    };
    private int width;
    private int height;
    public Camera2ProcessingHandlerThread(BarcodeDetector detector, Context context, ScannerView scannerView, ResultHandler resultHandler) {
        super("Camera2ProcessingHandlerThread");
        this.detector = detector;
        this.context = context;
        this.scannerView = scannerView;
        this.resultHandler = resultHandler;
        detector.setProcessor(barcodeProcessor);

        start();
    }

    @SuppressLint("NewApi")
    public void startProcessing() {
        if (getLooper().getThread().isAlive() && !getLooper().getThread().isInterrupted()) {
            handler = new Handler(getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    System.gc();
                    if (mPendingFrameData != null) {
                        // System.out.println("Process Frame " + mPendingFrame);
                        if (CameraUtils.getScreenOrientation(context) == Configuration.ORIENTATION_PORTRAIT) {
                            mPendingFrameData = CameraUtils.rotateData(width, height, mPendingFrameData);
                            int tmp = width;
                            width = height;
                            height = tmp;
                        }
                        PlanarYUVLuminanceSource source = CameraUtils.buildLuminanceSource(mPendingFrameData, scannerView, width, height);
                        if (source != null) {
                            byte[] greyPixel = CameraUtils.decodeGreyscale(source.getMatrix(), source.getWidth(), source.getHeight());
                            Frame frame = new Frame.Builder()
                                    .setImageData(ByteBuffer.wrap(greyPixel), source.getWidth(), source.getHeight(), ImageFormat.NV21)
                                    .setId(mPendingFrame)
                                    .setTimestampMillis(mPendingTimeMillis)
                                    .build();
                            detector.receiveFrame(frame);
                            mPendingFrameData = null;
                        }
                    }
                }
            });
        }
    }


    public void setNextFrame(byte[] bytes, int height, int width) {
        this.mPendingFrameData = bytes;
        this.height = height;
        this.width = width;
        this.mPendingFrame++;
        this.mPendingTimeMillis = SystemClock.elapsedRealtime() - mStartTime;
    }

    public boolean isBarcodeFinded() {
        return barcodeFinded;
    }
    public void resetHandler() {
        this.handler = null;
    }
}
