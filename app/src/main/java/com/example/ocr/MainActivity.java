package com.example.ocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
private TextView Display;
private Button Detecttext,speech,camera,sharetext,generatepdf;
private ImageView imageView;
private Bitmap imageBitmap;
private TextToSpeech toSpeech;
static final int REQUEST_IMAGE_CAPTURE = 1;

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
        generatepdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str=Display.getText().toString();
                Intent intent= new Intent(MainActivity.this,Getpdf.class);
                intent.putExtra("text",str);
                startActivity(intent);

            }
        });
        toSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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
              dispatchTakePictureIntent();
              Display.setText("");
            }
        });
     Detecttext.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             detectextfromimage();
         }
     });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
        }


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }
    private void detectextfromimage() {
        FirebaseVisionImage firebaseVisionImage=FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector= FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displaytextfromimage(firebaseVisionText);
                Toast.makeText(MainActivity.this,"success",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Error occur"+e.getMessage(),Toast.LENGTH_SHORT).show();
                Log.d("Error",e.getMessage());
            }
        });
    }

    private void displaytextfromimage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block>blocklist=firebaseVisionText.getBlocks();
        if(blocklist.size()== 0){
            Toast.makeText(MainActivity.this,"no text found",Toast.LENGTH_SHORT).show();
        }
        else {
            for(FirebaseVisionText.Block block: firebaseVisionText.getBlocks())
            {
                String text=block.getText();
                Display.setText(text);
            }
        }
    }
}
