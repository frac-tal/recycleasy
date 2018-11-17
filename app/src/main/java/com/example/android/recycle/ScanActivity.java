package com.example.android.recycle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class ScanActivity extends AppCompatActivity {

    static final String TAG = "ScanActivity";
    ImageView mImageView;
    Bitmap mBitmap;
    List<FirebaseVisionBarcode> mBarcodes;
    TextView mBarcodeNumber;
    public static final int PICK_IMAGE = 1;
    TextView productNameTextView;
    TextView subNameTextView;
    TextView brandTextView;
    TextView materialsTextView;
    TextView answerTextView;
    LinearLayout answerColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mImageView = findViewById(R.id.barcode_image);
        mBarcodeNumber = findViewById(R.id.product_name);
        productNameTextView = findViewById(R.id.product_name);
        subNameTextView = findViewById(R.id.sub_name);
        brandTextView = findViewById(R.id.brand);
        materialsTextView = findViewById(R.id.packaging_materials);
        answerTextView = findViewById(R.id.answer);
        answerColor = findViewById(R.id.answer_color);


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            //TODO: action
            Uri selectedImageUri = data.getData();
            mImageView.setImageURI(selectedImageUri);
            FirebaseVisionBarcodeDetectorOptions options =
                    new FirebaseVisionBarcodeDetectorOptions.Builder()
                            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS).build();
            FirebaseVisionImage image;
            try {
                image = FirebaseVisionImage.fromFilePath(getBaseContext(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                    .getVisionBarcodeDetector(options);
            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                            // Task completed successfully
                            Log.d(TAG, "SUCCESS!!!");
                            Log.d(TAG, "but 0 barcodes recognized :(");
                            if (barcodes.size() > 0) {
                                Log.d(TAG, "num of barcodes" + barcodes.size());
                                String url = makeUrlString(barcodes.get(0).getDisplayValue());
                                fetchJSON(url);
                            } else {
                                mBarcodeNumber.setText("no barcodes recognized :(");
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

    private void fetchJSON(String url){
        RequestQueue queue = Volley.newRequestQueue(ScanActivity.this);

        Log.d(TAG, "preparing request");
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG, "got response: " + response.substring(0, 50));
                        try {
                            JSONObject json = new JSONObject(response);
                            Log.d(TAG, "parsed JSON");
                            populateUI(json);
                        } catch (JSONException e){
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Got Error response: " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void populateUI(JSONObject jsonObject){
        try {
            JSONObject productJSON = jsonObject.getJSONObject("product");
            String name;
            String subName;
            String brand;
            String packaging;
            try {
                name = productJSON.getString("product_name");
            } catch (org.json.JSONException e){
                try {
                    name = productJSON.getString("product_name_de");
                } catch (org.json.JSONException e_de){
                    name = "";
                }

            }
            Log.d(TAG, name);
            try {
                subName = productJSON.getString("generic_name_de");
            } catch (JSONException e){
                subName = "";
            }
            try {
                brand = productJSON.getString("brands");
            } catch (JSONException e){
                brand = " ";
            }
            try {
                packaging = productJSON.getString("packaging");
            } catch (JSONException e){
                packaging = "no packaging information";
            }

            productNameTextView.setText(name);
            subNameTextView.setText(subName);
            brandTextView.setText(brand);
            materialsTextView.setText(packaging);
        } catch (org.json.JSONException e){
            Log.d(TAG, "json error when searching name: " + e.getMessage());
        }


    }

    private static String makeUrlString(String barcode){
        return String.format("https://world.openfoodfacts.org/api/v0/product/%s.json", barcode);

    }
}
