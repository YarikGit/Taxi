package com.example.jsontest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ImageView button;
    private TextView textView;
    private EditText editText;
    private RequestQueue requestQueue;
    private String searchData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.searchButton);
        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        requestQueue = Volley.newRequestQueue(this);

        getSearchData();

    }


    private void getSearchData(){

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                searchData = editText.getText().toString();
                if(searchData != null){
                    getWeather();
                }
                else{
                    Toast.makeText(MainActivity.this, "Write Your City!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getWeather(){

        String url = "https://api.weatherbit.io/v2.0/current?key=efe4689854f5436585edd7b423e8ecc9&country=Ukraine&city=" + searchData;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            try {
                JSONArray array = response.getJSONArray("data");

                for(int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);

                    String city = jsonObject.getString("city_name");
                    String temp = jsonObject.getString("temp");
                    String clouds = jsonObject.getString("clouds");
                    String dewpt = jsonObject.getString("dewpt");
                    String sunrise = jsonObject.getString("sunrise");
                    String sunset = jsonObject.getString("sunset");
                    String snow = jsonObject.getString("snow");

                    JSONObject ob = jsonObject.getJSONObject("weather");
                    String precipitation = ob.getString("description");

                    addDataText("City", city);
                    addDataText("Temperature", temp);
                    addDataText("Sky", precipitation);
                    addDataText("Clouds %", clouds);
                    addDataText("Snow %", snow);
                    addDataText("Dewpt %", dewpt);
                    addDataText("Sunrises", sunrise);
                    addDataText("Sunset", sunset);
                }

            } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("Nothing finding!");
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

    private void addDataText(String title, String body){
        int length = title.length() +1;

        Spannable spans = new SpannableString(title + ": " + body);
        spans.setSpan(new ForegroundColorSpan(getColor(R.color.teal_200)),0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.append(spans);
        textView.append("\n");
    }


}