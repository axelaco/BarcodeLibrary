package fr.axelpetit.barcodescanner.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.media.Image;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.axelpetit.barcodescanner.core.ScannerView;

import static android.content.ContentValues.TAG;

/**
 * Created by Axel on 08/08/2017.
 */

public class CameraUtils {
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    public static final int MAX_PREVIEW_WIDTH = 1920;
    public static final int STATE_PREVIEW = 1;
    public static final int STATE_WAITING_LOCK = 2;
    public static final int STATE_WAITING_PRECAPTURE = 3;
    public static final int STATE_WAITING_NON_PRECAPTURE = 4;
    public static final int STATE_PICTURE_TAKEN = 5;
    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    public static final int MAX_PREVIEW_HEIGHT = 1080;
    public static final int STATE_CAMERA_OPENED = 1;
    public static final int STATE_TASK_COMPLETE = 2;
    private int mSensorOrientation;

    public static int getCameraDisplayOrientation(Context context, int cameraId, Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size chooseOptimalSize2(Size[] choices, int textureViewWidth,
                                           int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static Camera.Size chooseOptimalSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    static class CompareSizesByArea implements Comparator<Size> {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean allowCamera2Support(Context context, String cameraId) {
        CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            int support = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            return support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED || support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL || support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3;
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static int getScreenOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    public static byte[] rotateData(int width, int height, byte[] data) {
        byte[] yuv = new byte[width * height];
        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int y = height - 1; y >= 0; y--) {
                yuv[i] = data[y * width + x];
                i++;
            }
        }
        return yuv;
    }

    public static byte[] decodeGreyscale(byte[] nv21, int width, int height) {
        int pixelCount = width * height;
        byte[] out = new byte[pixelCount];
        for (int i = 0; i < pixelCount; ++i) {
            int luminance = nv21[i] & 0xFF;
            out[i] = (byte) Color.argb(0xFF, luminance, luminance, luminance);
            //out[i] = (0xff000000 | luminance <<16 | luminance <<8 | luminance);//No need to create Color object for each.
        }
        return out;
    }
    @SuppressLint("NewApi")
    public static byte[] convertYUV_TO_NV21(Image image) {
        Image.Plane Y = image.getPlanes()[0];
        Image.Plane U = image.getPlanes()[1];
        Image.Plane V = image.getPlanes()[2];

        int Yb = Y.getBuffer().remaining();
        int Ub = U.getBuffer().remaining();
        int Vb = V.getBuffer().remaining();

        byte[] data = new byte[Yb + Ub + Vb];

        Y.getBuffer().get(data, 0, Yb);
        U.getBuffer().get(data, Yb, Ub);
        V.getBuffer().get(data, Yb + Ub, Vb);
        return data;
    }

    public  static PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, ScannerView scannerView, int width, int height) {
        Rect rect = scannerView.getFramingRectInPreview(new Point(width, height));
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        PlanarYUVLuminanceSource source = null;

        try {
            source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                    rect.width(), rect.height(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return source;
    }

    public static boolean isFlashModeSupported(Camera camera) {
        if (camera == null) {
            return false;
        }

        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getFlashMode() == null) {
            return false;
        }
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        return true;
    }
}
