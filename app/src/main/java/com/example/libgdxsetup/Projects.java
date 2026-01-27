package com.example.libgdxsetup;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.libgdxsetup.databinding.LayoutProjectsBinding;

public class Projects extends AppCompatActivity {
    LayoutProjectsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutProjectsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
