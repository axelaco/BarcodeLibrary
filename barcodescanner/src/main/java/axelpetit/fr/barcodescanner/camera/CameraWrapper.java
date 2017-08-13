package axelpetit.fr.barcodescanner.camera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
