package axelpetit.fr.barcodescanner.thread;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.nio.ByteBuffer;

import axelpetit.fr.barcodescanner.utils.CameraUtils;

/**
 * Created by Axel on 13/08/2017.
 */

public class CameraProcessingHandlerThread extends HandlerThread {
    private Camera.PreviewCallback previewCallback;
    private BarcodeDetector detector;
    private Context context;
    public CameraProcessingHandlerThread(Context context, Camera.PreviewCallback previewCallback, BarcodeDetector barcodeDetector) {
        super("CameraProcessingHandlerThread");
        this.previewCallback = previewCallback;
        this.detector = barcodeDetector;
        this.context = context;
        start();
    }
    public void startProcessing(final byte[] bytes, final Camera camera) {
        Handler handler = new Handler(getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                System.out.println("Processing Image in BackgroundThread");
                camera.setOneShotPreviewCallback(previewCallback);
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                int width = size.width;
                int height = size.height;
                byte[] data = bytes;
                if (CameraUtils.getScreenOrientation(context) == Configuration.ORIENTATION_PORTRAIT) {
                    byte[] rotatedData = new byte[bytes.length];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++)
                            rotatedData[x * height + height - y - 1] = data[x + y * width];
                    }
                    int tmp = width;
                    width = height;
                    height = tmp;
                    data = rotatedData;
                }
                Frame frame = new Frame.Builder()
                        .setImageData(ByteBuffer.wrap(data), width, height, ImageFormat.NV21)
                        .build();
                final SparseArray<Barcode> barcodes = detector.detect(frame);
                if (barcodes.size() > 0) {
                    System.out.println("Barcode detected");
                }
            }
        });
    }
}
