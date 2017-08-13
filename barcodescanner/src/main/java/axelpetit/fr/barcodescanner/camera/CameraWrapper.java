package axelpetit.fr.barcodescanner.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import static axelpetit.fr.barcodescanner.utils.CameraUtils.getCameraDisplayOrientation;

/**
 * Created by Axel on 08/08/2017.
 */

public class CameraWrapper {
    private Camera camera1;
    private Context context;
    public CameraWrapper(Context context) {
        this.context = context;
    }

    public Camera getCamera(int orientation) {
        android.hardware.Camera c = null;
        android.hardware.Camera.Parameters params = null;
        try {
            c = android.hardware.Camera.open(orientation);
            int rotate = getCameraDisplayOrientation(context, orientation, camera1);
            c.setDisplayOrientation(rotate);
            params = c.getParameters();
            if (orientation == Camera.CameraInfo.CAMERA_FACING_BACK) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                params.setRotation(rotate);
            }
            if (orientation == Camera.CameraInfo.CAMERA_FACING_FRONT)
                params.setRotation(270);
            c.setParameters(params);
        }
        catch (Exception e){
            Log.d("CameraInstance", e.getMessage());
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable

    }
}
