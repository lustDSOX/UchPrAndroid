package com.example.up;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PhotoActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private GestureDetector gestureDetector;
    int image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        image = getIntent().getIntExtra("image_id",-1);
        JSONArray jsonArray = GetImage();
        if(jsonArray != null){
            try{
            JSONObject json_object = jsonArray.getJSONObject(image);
            String encoded = json_object.getString("image");
            byte[] decodedString = Base64.decode(encoded, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            SubsamplingScaleImageView imageView = findViewById(R.id.imageView);
            imageView.setImage(ImageSource.bitmap(decodedBitmap));
            imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
            imageView.setMinScale(1.0f);
            imageView.setMaxScale(2.0f);
            }
            catch (Exception e){}
        }
        gestureDetector = new GestureDetector(this, this);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Обрабатываем событие касания
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Определяем направление жеста по скорости по оси X
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX > 0) {
                // Слева направо - закрытие окна
                Intent intent = new Intent(PhotoActivity.this,ProfileActivity.class);
                startActivity(intent);

            } else {

                // Справа налево - удаление фотографии
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Удалить изображение");
                builder.setMessage("Вы уверены, что хотите удалить это изображение?");
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteImage(null);
                    }
                });
                builder.setNegativeButton("Нет", null);
                builder.show();
            }
        }
        return true;
    }


    JSONArray GetImage(){
        String fileName = "data.json";
        try {
            FileInputStream fileInputStream = openFileInput(fileName);
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            fileInputStream.read(buffer);
            fileInputStream.close();
            String jsonString = new String(buffer, "UTF-8");// в этой строке jsonString будет содержать JSON-массив
            JSONArray json_array = new JSONArray(jsonString);
            return json_array;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void DeleteImage(View v){
        JSONArray jsonArray = GetImage();
        if(jsonArray == null)
            return;
        try {
            jsonArray.remove(image);
            String fileName = "data.json";
            String jsonString = jsonArray.toString();
            FileOutputStream fileOutputStream = openFileOutput(fileName, MODE_PRIVATE);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();
            Intent intent = new Intent(PhotoActivity.this,ProfileActivity.class);
            startActivity(intent);
        }
        catch (Exception e){}
    }

    public void GoBack(View v){
        finish();
    }
}