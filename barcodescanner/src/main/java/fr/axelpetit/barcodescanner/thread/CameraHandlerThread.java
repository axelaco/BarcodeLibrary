package fr.axelpetit.barcodescanner.thread;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;

import fr.axelpetit.barcodescanner.camera.CameraWrapper;
import fr.axelpetit.barcodescanner.core.ScannerView;
import fr.axelpetit.barcodescanner.utils.CameraUtils;

/**
 * Created by Axel on 13/08/2017.
 */

public class CameraHandlerThread extends HandlerThread {
    private ScannerView scannerView;
    private Context context;
    public CameraHandlerThread(ScannerView scannerView, Context context) {
        super("CameraHandlerThread");
        this.scannerView = scannerView;
        this.context = context;
        start();
    }
    public void openCamera() {
        Handler handler = new Handler(getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                CameraWrapper cameraWrapper = new CameraWrapper(context);
                scannerView.setCameraApi1(cameraWrapper.getCamera(Camera.CameraInfo.CAMERA_FACING_BACK));
                scannerView.handleCameraState(CameraUtils.STATE_CAMERA_OPENED);
            }
        });
    }
}
