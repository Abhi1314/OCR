package com.example.ocr;



import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Getpdf extends AppCompatActivity {
EditText data,name;
Button PDF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getpdf);
        name=(EditText)findViewById(R.id.Documenttitle);
        PDF=(Button)findViewById(R.id.dpdf);
        data=(EditText)findViewById(R.id.Content);
        Intent intent=getIntent();
        String str=intent.getStringExtra("text");
        data.setText(str);
        ActivityCompat.requestPermissions(Getpdf.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        PDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createpdf(view);
            }
        });

    }
      public void createpdf(View view){
          PdfDocument pdfDocument= new PdfDocument();
          PdfDocument.PageInfo mypageinfo =new PdfDocument.PageInfo.Builder(300,500,1).create();
          PdfDocument.Page mypage=pdfDocument.startPage(mypageinfo);

          Paint paint= new Paint();
          String str=data.getText().toString();
          String str1=name.getText().toString();
          int x=10,y=10;
          mypage.getCanvas().drawText(str,x,y,paint);
          pdfDocument.finishPage(mypage);


          String myfilepath= Environment.getExternalStorageDirectory().getPath()+"/"+str1+".pdf";
          File file=new File(myfilepath);
          Toast.makeText(getApplicationContext(),"PDF CREATED",Toast.LENGTH_SHORT).show();

          try {
                pdfDocument.writeTo(new FileOutputStream(file));
          }catch (Exception e){
              e.printStackTrace();
              Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT
              ).show();

          }
          pdfDocument.close();
      }
}