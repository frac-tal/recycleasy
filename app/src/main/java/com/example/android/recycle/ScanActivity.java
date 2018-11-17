package com.example.android.recycle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;

public class ScanActivity extends AppCompatActivity {

    static final String TAG = "ScanActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView mImageView;
    Bitmap mBitmap;
    List<FirebaseVisionBarcode> mBarcodes;
    TextView mBarcodeNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        dispatchTakePictureIntent();
        mImageView = findViewById(R.id.barcode_image);
        mBarcodeNumber = findViewById(R.id.barcode_number);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(mBitmap);
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);
            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                    .getVisionBarcodeDetector();
            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                            // Task completed successfully
                            Log.d(TAG, "SUCCESS!!!");
                            if (barcodes.size() > 0) {
                                mBarcodeNumber.setText(barcodes.get(0).getDisplayValue());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                            Log.d(TAG, "FAIL!!!");
                        }
                    });
        }
    }

}
