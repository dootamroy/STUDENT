package com.fusion.student;

import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//import hotchemi.android.rate.AppRate;
//import hotchemi.android.rate.StoreType;


public class MainActivity<manager> extends AppCompatActivity {

    //APP REVIEW.
    ReviewInfo reviewInfo;
    ReviewManager manager ; //= new FakeReviewManager(this);

    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
    int REQUEST_CODE_PERMISSIONS = 1001;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        AppRate.with(this)
                .setStoreType(StoreType.GOOGLEPLAY)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(2) // default 10
                .setRemindInterval(1) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false

                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
        */

        //REQUEST PERMISSION.
        if (!allPermissionsGranted()){
            //APP REQUEST.
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        //##################################################################################
        //Review API.
        manager = ReviewManagerFactory.create(this);
        Review();
        //##################################################################################



    }


    public void demo_pdf(View view){

        String folder_main = "MYSTUDENT_CV";
        String demo_text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";
        int demo_len = demo_text.length();

        //************* CHECK WHETHER FOLDER EXIST OR NOT. IF NOT THEN CREATE ONE!************
        File f = new File(Environment.getExternalStorageDirectory(),folder_main);
        if (!f.exists()){
            f.mkdirs();
        }
        //************************************************************************************



        PdfDocument mypdf = new PdfDocument();
        Paint mypaint = new Paint();

        PdfDocument.PageInfo myInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); //A4 portrait.
        PdfDocument.Page mypg = mypdf.startPage(myInfo);

        Canvas myCanvas = mypg.getCanvas();

        myCanvas.drawText(demo_text.substring(20,100),40,50, mypaint);
        mypdf.finishPage(mypg);


        File file = new File(Environment.getExternalStorageDirectory() + "/" + folder_main,"/demo.pdf");

        try {
            mypdf.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "saved in : " + Environment.getExternalStorageDirectory().getPath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        mypdf.close();



    }



    //review.
    private void Review()
    {
        manager.requestReviewFlow().addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(@NonNull Task<ReviewInfo> task) {
                if(task.isSuccessful()){
                    reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(MainActivity.this, "Rating Failed", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, "Reviewed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "In-App Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }



    //##################################################################################
    //permission request.

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(! allPermissionsGranted()){
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    //##################################################################################
}