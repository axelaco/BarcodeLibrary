package axelpetit.fr.barcodescanner.utils;

import android.annotation.SuppressLint;
import android.util.Size;

import java.util.Comparator;

/**
 * Created by Axel on 25/08/2017.
 */

public class CompareSizesByArea implements Comparator<Size> {
    @SuppressLint("NewApi")
    @Override
    public int compare(Size lhs, Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    }
}

