package fr.axelpetit.barcodescanner.core;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by Axel on 13/08/2017.
 */

public interface ResultHandler {
    void handleResult(Barcode barcode);
}
