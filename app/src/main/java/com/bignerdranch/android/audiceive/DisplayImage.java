package com.bignerdranch.android.audiceive;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DisplayImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_full_image);
        ImageView ivImage = (ImageView) findViewById(R.id.ivResult);
        TextView tvImageName = (TextView) findViewById(R.id.tvImageName);

        SearchResult searchResult = (SearchResult) getIntent().getSerializableExtra("result");
        String url = searchResult.getFullUrl();
        Picasso.with(this).load(url).into(ivImage);
        tvImageName.setText(searchResult.getTitle());
        // getActionBar().hide();
        getSupportActionBar().hide();
    }
}
