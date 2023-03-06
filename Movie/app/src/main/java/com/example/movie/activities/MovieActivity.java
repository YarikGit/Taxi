package com.example.movie.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.movie.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieActivity extends AppCompatActivity {

    private ImageView poster;
    private TextView title, genre, year, language, country,
            rated, runtime, released, director,
            writer, plot, actors, awards ;
    private String movieId;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        linkViews();
        getIdIntent();
        requestQueue = Volley.newRequestQueue(this);

        getMovieData();
    }

    private void linkViews(){
        poster = findViewById(R.id.posterView);
        title = findViewById(R.id.dataTitle);
        genre = findViewById(R.id.dataGenre);
        year = findViewById(R.id.dataYear);
        language = findViewById(R.id.dataLanguage);
        country= findViewById(R.id.dataCountry);
        rated = findViewById(R.id.dataRated);
        runtime = findViewById(R.id.dataRuntime);
        released = findViewById(R.id.dataReleased);
        director = findViewById(R.id.dataDirector);
        writer = findViewById(R.id.dataWriter);
        plot = findViewById(R.id.dataPlot);
        actors = findViewById(R.id.dataActors);
        awards = findViewById(R.id.dataAward);
    }

    private void getIdIntent(){
        Intent intent = getIntent();
        movieId = intent.getStringExtra("dataUrl");

        Log.d("URLvali", "Get Intent");
    }

    private void getMovieData(){

        if(movieId == null)
            return;

        String url = "https://www.omdbapi.com/?apikey=b8235670&i=" + movieId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    String titleJ = response.getString("Title");
                    String yearJ = response.getString("Year");
                    String ratedJ = response.getString("Rated");
                    String releasedJ = response.getString("Released");
                    String runtimeJ = response.getString("Runtime");
                    String genreJ = response.getString("Genre");
                    String directorJ = response.getString("Director");
                    String writerJ = response.getString("Writer");
                    String actorsJ = response.getString("Actors");
                    String plotJ = response.getString("Plot");
                    String languageJ = response.getString("Language");
                    String countryJ = response.getString("Country");
                    String awardsJ = response.getString("Awards");
                    String posterJ = response.getString("Poster");

                    setText(title, titleJ, null);
                    setText(genre, "Genre", genreJ);
                    setText(year,"Year", yearJ);
                    setText(language, "Language", languageJ);
                    setText(country, "Country", countryJ);
                    setText(rated, "Rated", ratedJ);
                    setText(runtime,"Runtime", runtimeJ);
                    setText(released, "Released", releasedJ);
                    setText(director, "Director", directorJ);
                    setText(writer,"Writer", writerJ);
                    setText(actors,"Actors", actorsJ);
                    setText(awards, "Awards", awardsJ);
                    setText(plot, "Plot",plotJ);

                    if(posterJ.length() < 2){
                        poster.setImageResource(R.drawable.baseline_hide_image_24);
                    }else {
                        Picasso.get().load(posterJ).into(poster);
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    private void setText(TextView view, String title, String body){
        Spannable spans;
        int titleLength;
        if(body != null) {
            titleLength = (title + ": ").length();
            spans = new SpannableString(title +": " + body);
        }else {
            titleLength = title.length();
            spans = new SpannableString(title);
        }

        spans.setSpan(new ForegroundColorSpan(getColor(R.color.green)), 0, titleLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        view.setText(spans);

    }


}
