package fr.axelpetit.barcodescanner.thread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.nio.ByteBuffer;

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
    private byte[] mPendingFrameData;
    private int width;
    private int height;
    public Camera2ProcessingHandlerThread(BarcodeDetector detector, Context context, ScannerView scannerView) {
        super("Camera2ProcessingHandlerThread");
        this.detector = detector;
        this.context = context;
        this.scannerView = scannerView;
        start();
    }

    @SuppressLint("NewApi")
    public void startProcessing() {
        Handler handler = new Handler(getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
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
                            .build();
                    final SparseArray<Barcode> barcodes = detector.detect(frame);
                    if (barcodes.size() > 0) {
                        System.out.println("Result: " + barcodes.valueAt(0).displayValue);
                    }
                }
            }
        });

    }


    public void setNextFrame(byte[] bytes, int height, int width) {
        this.mPendingFrameData = bytes;
        this.height = height;
        this.width = width;
    }
}
