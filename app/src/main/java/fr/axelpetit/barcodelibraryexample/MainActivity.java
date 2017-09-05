package fr.axelpetit.barcodelibraryexample;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import fr.axelpetit.barcodescanner.core.ResultHandler;
import fr.axelpetit.barcodescanner.core.ScannerView;

public class MainActivity extends AppCompatActivity implements ResultHandler {
    private ScannerView mScannerView;
    private AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ScannerView(getApplicationContext());
        setContentView(mScannerView);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Barcode Result");
        dialog = builder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO Camera doesn't restart after onPause method with CameraApi1 for new devices
        if (mScannerView != null) {
            /*List<Integer> barcodeFormats = new ArrayList<>();
            barcodeFormats.add(Barcode.QR_CODE);
            barcodeFormats.add(Barcode.EAN_13);

            mScannerView.setBarcodeFormats(barcodeFormats);
            */
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScannerView != null) {
            mScannerView.stopPreview();
        }
    }

    @Override
    public void handleResult(Barcode barcode) {
      /*  AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Hello")
                .setMessage(barcode.displayValue)
                .setPositiveButton("Yes", null)
                .setNegativeButton("No", null)
                .setNeutralButton("Maybe", null);
        AlertDialog dialog = builder.create(); */
       // dialog.show();
        showPopUp(barcode.displayValue);
        System.out.println("Res: " + barcode.displayValue);
     //   mScannerView.resumeCameraPreview(); // TODO Add Method to resumeCameraPreview
    }
    private void showPopUp(final String text) {
        final Activity activity = this;
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setMessage(text);
                    dialog.show();
                }
            });
        }
    }
}
