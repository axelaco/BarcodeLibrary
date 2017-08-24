package axelpetit.fr.barcodelibraryexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import axelpetit.fr.barcodescanner.core.ResultHandler;
import axelpetit.fr.barcodescanner.core.ScannerView;

public class MainActivity extends AppCompatActivity implements ResultHandler {
    private ScannerView mScannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ScannerView(getApplicationContext());
        setContentView(mScannerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mScannerView != null) {
            mScannerView.startCamera();
            mScannerView.setResultHandler(this);
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
        Toast.makeText(this, barcode.displayValue, Toast.LENGTH_SHORT).show();
    }
}
