package com.example.nasaimageoftheday;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView titleTextView, dateTextView, descriptionTextView;
    private NasaImageViewModel viewModel;

    private static final String PREF_FILE_NAME = "NASA_IMAGE_PREF";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        titleTextView = findViewById(R.id.titleTextView);
        dateTextView = findViewById(R.id.dateTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);

        String apiKey = "OZ4E94kBiX8exC0bhOos0ac3LJPhEmT37yQFKppI";

        viewModel = new ViewModelProvider(this).get(NasaImageViewModel.class);
        viewModel.getImageData().observe(this, nasaImageData -> {
            if (nasaImageData != null) {
                titleTextView.setText(nasaImageData.getTitle());
                dateTextView.setText(nasaImageData.getDate());
                descriptionTextView.setText(nasaImageData.getExplanation());


                // Load image using your preferred method (e.g., Picasso, Glide)
                Picasso.get().load(nasaImageData.getUrl()).into(imageView);
            } else {
                showError("Failed to fetch NASA image data. Please try again later.");
            }
        });

        viewModel.init(apiKey, getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE));
    }

    private void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
