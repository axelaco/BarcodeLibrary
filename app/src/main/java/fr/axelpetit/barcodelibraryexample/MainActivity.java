package fr.axelpetit.barcodelibraryexample;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;

import fr.axelpetit.barcodescanner.core.ResultHandler;
import fr.axelpetit.barcodescanner.core.ScannerView;

public class MainActivity extends AppCompatActivity implements ResultHandler {
    private ScannerView mScannerView;
    private AlertDialog dialog;
    private FrameLayout frameLayout;
    private boolean toggle = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScannerView = new ScannerView(getApplicationContext());
        frameLayout = findViewById(R.id.frame_cam);
        Button btn  = findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle = !toggle;
                if (mScannerView != null) {
                    mScannerView.setFlash(toggle);
                }
            }
        });
        frameLayout.addView(mScannerView);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Barcode Result");
        dialog = builder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO Camera doesn't restart after onPause method with CameraApi1 for new devices
        if (mScannerView != null) {
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScannerView != null) {
            dialog.dismiss();
            mScannerView.stopPreview();
        }
    }

    @Override
    public void handleResult(Barcode barcode) {
        showPopUp(barcode.displayValue);
      // mScannerView.resumeCameraPreview(); // TODO Add Method to resumeCameraPreview
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
