package axelpetit.fr.barcodelibraryexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import axelpetit.fr.barcodescanner.core.ScannerView;

public class MainActivity extends AppCompatActivity {
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
            mScannerView.startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScannerView != null) {
            mScannerView.stopPreview();
        }
    }
}
