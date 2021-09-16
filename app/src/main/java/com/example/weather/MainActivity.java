
package com.example.weather;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class MainActivity extends AppCompatActivity {
    private EditText editTextCity;
    private TextView textViewWeather;
    private ImageView imageView;

    private String geoCity;
    private final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=bccdc8e3ead5089280397b80715a5cd7&lang=ru&units=metric";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();
        editTextCity = findViewById(R.id.editTextCity);
        textViewWeather = findViewById(R.id.textViewWeather);
        imageView = findViewById(R.id.imageView);


    }

    public void onClickShowWeather(View view) {
        String city = editTextCity.getText().toString().trim();
        if (!city.isEmpty()) {
            Toast.makeText(this, "geo", Toast.LENGTH_SHORT).show();
            DownloadWeatherTask task = new DownloadWeatherTask();
            String url = String.format(WEATHER_URL, city);
            task.execute(url);
        }
    }

    private class DownloadWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String city = jsonObject.getString("name");
                    String temp = jsonObject.getJSONObject("main").getString("temp");
                    String feelsLike = jsonObject.getJSONObject("main").getString("feels_like");
                    String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    String weather = String.format("%s\nТемпература: %s\nНа улице: %s\nОщущается как: %s", city, temp, description, feelsLike);

                    String icoID = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
                    String urlIco = String.format("http://openweathermap.org/img/wn/%s@2x.png", icoID);
                    Picasso.get().load(urlIco)
                            .placeholder(R.drawable.picasso)
                            .error(R.drawable.picasso)
                            .into(imageView);
                    imageView.setVisibility(View.VISIBLE);

                    textViewWeather.setText(weather);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.excepton_city, Toast.LENGTH_SHORT).show();
            }
        }
    }
}