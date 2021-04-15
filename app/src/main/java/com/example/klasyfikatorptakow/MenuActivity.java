package com.example.klasyfikatorptakow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import pub.devrel.easypermissions.EasyPermissions;

public class MenuActivity extends AppCompatActivity {

    private Button mButtonphoto_trap;
    private Button mButtonphoto;
    private Button mButtoninformations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        mButtonphoto_trap = findViewById(R.id.buttonphoto_trap);
        mButtonphoto = findViewById(R.id.buttonphoto);
        mButtoninformations = findViewById(R.id.buttoninformations);
        EasyPermissions.requestPermissions(
                this,
                "",
                CameraSelector.LENS_FACING_BACK,
                Manifest.permission.CAMERA );
        EasyPermissions.requestPermissions(
                this,
                "",
                10224,
                Manifest.permission.SEND_SMS );
        mButtonphoto_trap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Photo_trapActivity.class);
                startActivity(intent);
            }
        });

        mButtonphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
                startActivity(intent);
            }
        });

        mButtoninformations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InformationsActivity.class);
                startActivity(intent);
            }
        });
    }
}