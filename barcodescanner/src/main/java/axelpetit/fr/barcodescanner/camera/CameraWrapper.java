package axelpetit.fr.barcodescanner.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static axelpetit.fr.barcodescanner.utils.CameraUtils.getCameraDisplayOrientation;

/**
 * Created by Axel on 08/08/2017.
 */

public class CameraWrapper {
    private Camera camera1;
    private Context context;
    private String mCameraId;
    private CameraDevice.StateCallback mStateCallback;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Handler mBackgroundHandler;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    public CameraWrapper(Context context) {
        this.context = context;
    }

    public CameraWrapper(Context context, String mCameraId, CameraDevice.StateCallback mStateCallback, Handler mBackgroundHandler) {
        this.context = context;
        this.mCameraId = mCameraId;
        this.mStateCallback = mStateCallback;
        this.mBackgroundHandler = mBackgroundHandler;
    }

    public Camera getCamera(int cameraId) {
        Camera c = null;
        Camera.Parameters params = null;
        try {
            c = Camera.open(cameraId);
            int rotate = getCameraDisplayOrientation(context, cameraId, c);
            c.setDisplayOrientation(rotate);
            params = c.getParameters();
            if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                params.setRotation(rotate);
            }
            if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                params.setRotation(270);
          /*  if (params.getMaxNumMeteringAreas() > 0){ // check that metering areas are supported
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                Rect areaRect1 = new Rect(-100, -100, 100, 100);    // specify an area in center of image
                meteringAreas.add(new Camera.Area(areaRect1, 600)); // set weight to 60%
                params.setMeteringAreas(meteringAreas);
            }
                */
            c.setParameters(params);
        }
        catch (Exception e){
            Log.d("CameraInstance", e.getMessage());
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
