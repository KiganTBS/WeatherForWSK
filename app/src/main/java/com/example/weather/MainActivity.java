package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private EditText editTextCity;
    private TextView textViewWeather,textView;
    private ImageView imageView;
    FusedLocationProviderClient fusedLocationProviderClient;
    String innerCity;

    private final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=bccdc8e3ead5089280397b80715a5cd7&lang=ru&units=metric";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();
        editTextCity = findViewById(R.id.editTextCity);
        textViewWeather = findViewById(R.id.textViewWeather);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);
    }


    public void onClickShowWeather(View view) {
        String city = editTextCity.getText().toString().trim();
        if (!city.isEmpty()) {
            DownloadWeatherTask task = new DownloadWeatherTask();
            String url = String.format(WEATHER_URL, city);
            task.execute(url);
        }
    }

    public void CheckGeo(View view) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            innerCity = getLocation();
        } else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
        DownloadWeatherTask task = new DownloadWeatherTask();
        String url = String.format(WEATHER_URL, innerCity);
        task.execute(url);
    }

    @SuppressLint("MissingPermission")
    private String getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        innerCity = addresses.get(0).getLocality();
                        textView.setText(addresses.get(0).getLocality());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        });
        return innerCity;
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