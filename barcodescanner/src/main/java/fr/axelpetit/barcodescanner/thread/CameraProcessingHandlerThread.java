package fr.axelpetit.barcodescanner.thread;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.nio.ByteBuffer;

import fr.axelpetit.barcodescanner.core.ResultHandler;
import fr.axelpetit.barcodescanner.core.ScannerView;
import fr.axelpetit.barcodescanner.utils.CameraUtils;
import fr.axelpetit.barcodescanner.utils.PlanarYUVLuminanceSource;


/**
 * Created by Axel on 13/08/2017.
 */

public class CameraProcessingHandlerThread extends HandlerThread {
    private Camera.PreviewCallback previewCallback;
    private BarcodeDetector detector;
    private Context context;
    private ScannerView scannerView;
    private byte[] data;
    private ResultHandler mHandlerResult;
    public CameraProcessingHandlerThread(Context context, ScannerView scannerView, BarcodeDetector barcodeDetector, ResultHandler mHandlerResult) {
        super("CameraProcessingHandlerThread");
        this.scannerView = scannerView;
        this.previewCallback = scannerView.getPreviewCallback();
        this.detector = barcodeDetector;
        this.context = context;
        this.mHandlerResult = mHandlerResult;
        start();
    }
    public void startProcessing(final byte[] bytes, final Camera camera) {
        this.data = bytes;
        Handler handler = new Handler(getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = parameters.getPreviewSize();
                    int width = size.width;
                    int height = size.height;
                    if (CameraUtils.getScreenOrientation(context) == Configuration.ORIENTATION_PORTRAIT) {
                        data = CameraUtils.rotateData(width, height, bytes);
                        int tmp = width;
                        width = height;
                        height = tmp;
                    }
                    PlanarYUVLuminanceSource source = CameraUtils.buildLuminanceSource(data, scannerView, width, height);
                    byte[] greyPixel = CameraUtils.decodeGreyscale(source.getMatrix(), source.getWidth(), source.getHeight());
                    Frame frame = new Frame.Builder()
                            .setImageData(ByteBuffer.wrap(greyPixel), source.getWidth(), source.getHeight(), ImageFormat.NV21)
                            .build();
                    final SparseArray<Barcode> barcodes = detector.detect(frame);
                    if (barcodes.size() > 0) {
                        mHandlerResult.handleResult(barcodes.valueAt(0));
                        camera.setOneShotPreviewCallback(null);
                        camera.stopPreview();
                    } else
                        camera.setOneShotPreviewCallback(previewCallback);
                } catch (RuntimeException e){
                    Log.d("CameraProcessing", e.getMessage());
                    // Camera is not available (in use or does not exist)
                }
            }
        });
    }
}
