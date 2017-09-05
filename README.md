Barcode Library
============

Android library projects

Pre-requisites
--------------
+ Android SDK 16+
+ Google Play services

Installation
------------

Add the following dependendy to your module build.gradle file.

`compile 'fr.axelpetit.barcodescanner:1.1.1'`

Add the following dependendy to your project build.gradle file.
```
repositories {
        jcenter()
        maven {
            url 'https://maven.google.com'
        }
    }
```

Simple Usage
------------

A basic Activity would look like this:
```java
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
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScannerView != null) {
            mScannerView.stopCamera();
        }
    }
    @Override
    public void handleResult(Barcode barcode) {
        Log.d("Result", barcode.displayValue);
        Log.d("BarcodeFormat", barcode.format);
    }
}
```
Please take look at [barcode-reference](https://developers.google.com/android/reference/com/google/android/gms/vision/barcode/Barcode)
for more information

Credits
-------
1. CameraPreview app from Android SDK APIDemos
2. The ZXing project: https://github.com/zxing/zxing
3. CameraPreview app with CameraApi2 from android-Camera2Basic: https://github.com/googlesamples/android-Camera2Basic
4. Google Mobile vision api: https://developers.google.com/vision/

Contributors
------------
https://github.com/axelaco/BarcodeLibrary/graphs/contributors

Licence
-------
Apache Licence, Version 2.0
