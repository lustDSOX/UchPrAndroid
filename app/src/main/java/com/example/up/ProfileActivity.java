package com.example.up;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.up.GalleryDirectory.MyAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    //ListView photoList1;
    GridView photoList;
    private static final int REQUEST_CODE_SELECT_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        photoList = findViewById(R.id.grid_view1);
        ImageView imageView = findViewById(R.id.image_profile);
        TextView textView = findViewById(R.id.tx_name_profile);
        String imageUrl = User.avatar;
        Picasso.get()
                .load(imageUrl)
                .transform(new CircleTransform())
                .into(imageView);
        textView.setText(User.nickName);
        SetAdapter();
    }


    public void UnLogin(View v){
        Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
        startActivity(intent);
        // Получаем объект SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        // Получаем объект Editor для редактирования SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Сохраняем данные о пользователе
        editor.putString("password", "");
        // Применяем изменения
        editor.apply();
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

    void PutImage(Photo photo){
        JSONArray jsonArray = GetImage();
        if(jsonArray == null)
            jsonArray = new JSONArray();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            photo.image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            JSONObject postData = new JSONObject();
            postData.put("image", encoded);
            postData.put("time", photo.timestamp);
            jsonArray.put(postData);

            String fileName = "data.json";
            String jsonString = jsonArray.toString();
            FileOutputStream fileOutputStream = openFileOutput(fileName, MODE_PRIVATE);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();
            SetAdapter();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    void SetAdapter(){
        JSONArray jsonArray = GetImage();
        List<Photo> photos = new ArrayList<>();
        try {
            if(jsonArray != null){
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_object = jsonArray.getJSONObject(i);
                    Photo photo = new Photo();
                    String encoded = json_object.getString("image");
                    byte[] decodedString = Base64.decode(encoded, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    photo.image = decodedBitmap;
                    photo.timestamp = json_object.getString("time");
                    photos.add(photo);
                }
            }
            MyAdapter adapter = new MyAdapter(this,ProfileActivity.this,photos);
            adapter.setShowButton(true);
            photoList.setAdapter(adapter);


        }
        catch (Exception e){}
    }

    public void chooseImage() {
        // открываем галерею для выбора изображения
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = radius;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try{
                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(imageUri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                Photo photo = new Photo();
                photo.image = image;
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                photo.timestamp = currentTime;
                PutImage(photo);
            }
            catch (Exception e){}
        }
    }
    public void GoMain(View v){
       finish();
    }


}