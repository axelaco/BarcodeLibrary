package axelpetit.fr.barcodescanner.thread;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import axelpetit.fr.barcodescanner.core.ResultHandler;
import axelpetit.fr.barcodescanner.core.ScannerView;
import axelpetit.fr.barcodescanner.utils.CameraUtils;
import axelpetit.fr.barcodescanner.utils.PlanarYUVLuminanceSource;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


/**
 * Created by Axel on 13/08/2017.
 */

public class CameraProcessingHandlerThread extends HandlerThread {
    private Camera.PreviewCallback previewCallback;
    private BarcodeDetector detector;
    private Context context;
    private ScannerView scannerView;
    private boolean barcodeFinded = false;
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
                if (!barcodeFinded) {
                    camera.setOneShotPreviewCallback(previewCallback);
                    Camera.Parameters parameters = camera.getParameters(); // TODO this method is called after camera.release()
                    Camera.Size size = parameters.getPreviewSize();
                    int width = size.width;
                    int height = size.height;
                   if (CameraUtils.getScreenOrientation(context) == Configuration.ORIENTATION_PORTRAIT) {
                        data = CameraUtils.rotateData(width, height, bytes);
                        int tmp = width;
                        width = height;
                        height  = tmp;
                    }
                    PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height);
                    byte[] greyPixel = CameraUtils.decodeGreyscale(source.getMatrix(), source.getWidth(), source.getHeight());
                 /*   ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Bitmap editedBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(),
                            android.graphics.Bitmap.Config.ARGB_8888);
                    editedBitmap.setPixels(greyPixel, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());
                    editedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    try {
                        File mediaFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        FileOutputStream outputStream = new FileOutputStream(mediaFile);
                        outputStream.write(baos.toByteArray());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                   */
                    Frame frame = new Frame.Builder()
                            .setImageData(ByteBuffer.wrap(greyPixel), source.getWidth(), source.getHeight(), ImageFormat.NV21)
                            .build();
                    final SparseArray<Barcode> barcodes = detector.detect(frame);
                    if (barcodes.size() > 0) {
                        mHandlerResult.handleResult(barcodes.valueAt(0));
                        camera.setOneShotPreviewCallback(null);
                        camera.stopPreview();
                    }
                }
            }
        });
    }

    public boolean isBarcodeFinded() {
        return barcodeFinded;
    }
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = scannerView.getFramingRectInPreview(new Point(width, height));
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        PlanarYUVLuminanceSource source = null;

        try {
            source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                    rect.width() , rect.height(), false);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return source;
    }
    private static File getOutputMediaFile(int type){

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "BarcodeLibraryExample");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}
