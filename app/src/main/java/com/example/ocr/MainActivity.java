package com.example.ocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
private EditText Display;
private Button Detecttext,speech,camera,sharetext,generatepdf;
private ImageView imageView;
private TextToSpeech toSpeech;
private static final int Request_Code=1;
Uri image_uri;
static final int REQUEST_IMAGE_Camera = 102;
Bitmap imageBitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Display=findViewById(R.id.display);
        imageView=findViewById(R.id.pic);
        sharetext=findViewById(R.id.share);
        Detecttext=findViewById(R.id.detectedtext);
        speech=findViewById(R.id.converter);
        generatepdf=findViewById(R.id.gpdf);
        camera=findViewById(R.id.capture);

        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)+ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=
                PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)||ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Grant Permission!")
                        .setMessage("Permisison Needed to take pictures")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA},Request_Code);

                            }
                        })
                        .setNegativeButton("Cancle", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
            else {
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},Request_Code);
            }
        }

        generatepdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str=Display.getText().toString();
                Intent intent= new Intent(MainActivity.this,Getpdf.class);
                intent.putExtra("text",str);
                startActivity(intent);

            }
        });
        toSpeech=new TextToSpeech(getApplicationContext(),new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
            if(i!=TextToSpeech.ERROR){
                toSpeech.setLanguage(Locale.ENGLISH);
            }
            }
        });
        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toSpeech.speak(Display.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
            }
        });
        sharetext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string=Display.getText().toString();
                Intent intent=new Intent();
                intent.setAction(intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,string);
                intent.setType("text/plain");
                startActivity(intent);
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("CHOOSE ANY ONE !")
                        .setPositiveButton("1)OPEN CAMERA.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dispatchTakePictureIntent();
                                Display.setText("");
                            }
                        }).setNeutralButton("2)OPEN GALLERY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Selectimage();
                        Display.setText("");
                    }
                }).create().show();
            }
        });
     Detecttext.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             detectextfromimage();


         }
     });
    }

    private void Selectimage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_Camera);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE );
        if(takePictureIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takePictureIntent,REQUEST_IMAGE_Camera);
        }


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("result", String.valueOf(resultCode));
        if (requestCode == REQUEST_IMAGE_Camera && resultCode == RESULT_OK) {
            Log.d("result1", String.valueOf(resultCode));
            Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                // update the preview image in the layout
                image_uri = selectedImageUri;
                imageView.setImageURI(selectedImageUri);
            } else {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                imageBitmap = thumbnail;
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(thumbnail);
            }

        }

    }

        private void detectextfromimage () {
            FirebaseVisionImage firebaseVisionImage = null;
            try {
                if(image_uri != null) {
                    firebaseVisionImage = FirebaseVisionImage.fromFilePath(getApplicationContext(), image_uri);
                }else if(imageBitmap != null){
                    firebaseVisionImage=FirebaseVisionImage.fromBitmap(imageBitmap);
                }
                if(firebaseVisionImage != null){
                FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
                firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        displaytextfromimage(firebaseVisionText);
                        Log.d("test1", String.valueOf(firebaseVisionText));
                        Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error occur" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("Error", e.getMessage());
                    }
                });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }



        }

        private void displaytextfromimage (FirebaseVisionText firebaseVisionText){
            List<FirebaseVisionText.Block> blocklist = firebaseVisionText.getBlocks();
            if (blocklist.size() == 0) {
                Toast.makeText(MainActivity.this, "no text found", Toast.LENGTH_SHORT).show();
            } else {
                for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                    String text = block.getText();
                    Display.setText(text);
                }
            }
        }
    }

