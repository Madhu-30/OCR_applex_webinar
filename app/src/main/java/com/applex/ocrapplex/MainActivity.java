package com.applex.ocrapplex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity
{
    ImageView preview;
    EditText result;
    Button select_image;
    ImageButton share,search;
    ConnectivityManager cm;

    String[] storagePermission;
    private static final int IMAGE_PICK_GALLERY_CODE = 7;
    private static final int STORAGE_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.preview);
        result = findViewById(R.id.result);
        select_image = findViewById(R.id.select_image);
        share = findViewById(R.id.share);
        search = findViewById(R.id.search);

        storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        cm = (ConnectivityManager) MainActivity.this.getSystemService(CONNECTIVITY_SERVICE);

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkStoragePermission()) {
                    requestStoragePermission();
                }
                else {
                    pickGallery();
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = result.getText().toString();
                if(text.length()!=0){
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_SEND);
                    i.putExtra(Intent.EXTRA_TEXT,text);
                    i.setType("text/plain");
                    startActivity(Intent.createChooser(i,"Share with"));

                }
                else{
                    Toast.makeText(MainActivity.this,"Field Empty",Toast.LENGTH_LONG).show();
                }
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = result.getText().toString();
                if(text.length()!=0){
                    Search();
                }
                else{
                    Toast.makeText(MainActivity.this,"Field Empty",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void Search() {
        if(cm.getActiveNetworkInfo() != null){
            String text = result.getText().toString();
            if(text.length()!=0){
                Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
                i.putExtra(SearchManager.QUERY,text);
                startActivity(i);

            }
            else{
                Toast.makeText(MainActivity.this,"Field Empty",Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(MainActivity.this,"Please check your internet connection and try again...",Toast.LENGTH_LONG).show();
        }

    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this, storagePermission, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == STORAGE_REQUEST_CODE) {
            if(grantResults.length > 0) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickGallery();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void pickGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            CropImage.activity(uri)
                    .setActivityTitle("OCR Crop")
                    .setCropMenuCropButtonTitle("Crop")
                    .setMultiTouchEnabled(true)
                    .setAllowRotation(true)
                    .setAllowCounterRotation(true)
                    .setAllowFlipping(true)
                    .setAutoZoomEnabled(true)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(MainActivity.this);
        }
        else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult uri = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                preview.setImageURI(uri.getUri());

                BitmapDrawable bitmapDrawable = (BitmapDrawable) preview.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer textRecognizer = new TextRecognizer.Builder(MainActivity.this).build();

                if(textRecognizer.isOperational()){
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);

                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i=0;i<items.size();i++){
                        TextBlock myItem = items.valueAt(i);
                        stringBuilder.append(myItem.getValue());
                        if(i != items.size()-1){
                            stringBuilder.append("\n");
                        }
                    }
                    result.setText(stringBuilder.toString());



                }
                else{
                    Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_LONG).show();
                }


            }
        }
    }
}