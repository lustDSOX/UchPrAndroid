package com.example.up;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    ListView listView;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView image = findViewById(R.id.image);
        recyclerView = findViewById(R.id.my_recycler_view);
        listView = findViewById(R.id.QuotesList);
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.background)));
        listView.setDividerHeight(48);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL); // Установка ориентации горизонтальной
        recyclerView.setLayoutManager(layoutManager);
        String imageUrl = User.avatar;
        Picasso.get()
                .load(imageUrl)
                .transform(new CircleTransform())
                .into(image);
        TextView w_name = findViewById(R.id.welcome_name);
        w_name.setText("С возвращением, " + User.nickName);

        getQuotes taskq = new getQuotes(listView, MainActivity.this);
        taskq.execute();
        getFeelings taskf = new getFeelings(recyclerView, MainActivity.this);
        taskf.execute();
    }

    class getQuotes extends AsyncTask<Void, Void, List<Quote>>{
        private ListView listView;
        private Context context;

        public getQuotes(ListView listView, Context context) {
            this.listView = listView;
            this.context = context;
        }

        @Override
        protected List<Quote> doInBackground(Void... voids) {
            List<Quote> quoteList = new ArrayList<Quote>();
            try {
                // создаем соединение
                URL url = new URL("http://mskko2021.mad.hakta.pro/api/quotes");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                int responseCode = conn.getResponseCode();
                // Чтение ответа сервера
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    JSONObject response = new JSONObject(sb.toString());
                    // Обработка ответа сервера

                    JSONArray json_array = new JSONArray(response.getString("data"));
                    for (int i = 0; i < json_array.length(); i++) {
                        JSONObject json_object = json_array.getJSONObject(i);
                        Quote quote = new Quote();
                        quote.image = json_object.getString("image");
                        quote.title = json_object.getString("title");
                        quote.description = json_object.getString("description");
                        quoteList.add(quote);
                    }
                    //Заполняем список
                    LVAdapter adapter = new LVAdapter(MainActivity.this,quoteList);
                    listView.setAdapter(adapter);

                } else {
                    Log.e("TAG", "Error: " + responseCode);
                }

                conn.disconnect();
            }
            catch (Exception e){
                Log.d("e",e.toString());
            }
            return quoteList;
        }
        @Override
        protected void onPostExecute(List<Quote> quoteList) {
            super.onPostExecute(quoteList);
            //Заполняем список
            LVAdapter adapter = new LVAdapter(MainActivity.this,quoteList);
            listView.setAdapter(adapter);
        }
    }

    class getFeelings extends AsyncTask<Void, Void, List<Feeling>>{
        private RecyclerView recyclerView;
        private Context context;

        public getFeelings(RecyclerView recyclerView, Context context) {
            this.recyclerView = recyclerView;
            this.context = context;
        }

        @Override
        protected List<Feeling> doInBackground(Void... voids) {
            List<Feeling> feelingList = new ArrayList<Feeling>();
            try {
                // создаем соединение
                URL url = new URL("http://mskko2021.mad.hakta.pro/api/feelings");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                int responseCode = conn.getResponseCode();
                // Чтение ответа сервера
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    JSONObject response = new JSONObject(sb.toString());
                    // Обработка ответа сервера
                    JSONArray json_array = new JSONArray(response.getString("data"));
                    for (int i = 0; i < json_array.length(); i++) {
                        JSONObject json_object = json_array.getJSONObject(i);
                        Feeling feeling = new Feeling();
                        feeling.image = json_object.getString("image");
                        feeling.title = json_object.getString("title");
                        feeling.position = json_object.getInt("position");
                        feelingList.add(feeling);
                    }
                    //Заполняем список
                    RVAdapter adapter = new RVAdapter(feelingList);
                    recyclerView.setAdapter(adapter);

                } else {
                    Log.e("TAG", "Error: " + responseCode);
                }

                conn.disconnect();
            }
            catch (Exception e){
                Log.d("e",e.toString());
            }
            return feelingList;
        }
        @Override
        protected void onPostExecute(List<Feeling> feelingList) {
            super.onPostExecute(feelingList);
            //Заполняем список
            RVAdapter adapter = new RVAdapter(feelingList);
            recyclerView.setAdapter(adapter);
        }
    }

    public void GoProfile(View v){
        Intent profile = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(profile);
    }

}