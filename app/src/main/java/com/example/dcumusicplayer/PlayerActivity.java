package com.example.dcumusicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String mp3file = intent.getStringExtra("mp3");
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            ImageView ivCover = findViewById(R.id.ivCover);
            retriever.setDataSource(mp3file);
            byte[] b = retriever.getEmbeddedPicture();
            Bitmap cover = BitmapFactory.decodeByteArray(b, 0, b.length);
            ivCover.setImageBitmap(cover);

            TextView tvTitle = findViewById(R.id.tvTitle);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            tvTitle.setText(title);

            TextView tvDuration = findViewById(R.id.tvDuration);
            tvDuration.setText(ListViewMP3Adapter.getDuration(retriever));

            TextView tvArtist = findViewById(R.id.tvArtist);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            tvArtist.setText(artist);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

}