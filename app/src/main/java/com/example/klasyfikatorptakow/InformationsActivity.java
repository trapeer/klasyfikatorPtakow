package com.example.klasyfikatorptakow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

public class InformationsActivity extends AppCompatActivity {

    private Button mButtonOK;
    private EditText mEditTextDeviceName;
    private EditText mEditTextPhoneNumber;
    SharedPreferences sharedPref;

    @Override
    public void onPause() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("deviceName", mEditTextDeviceName.getText().toString());
        editor.putString("phoneNumber", mEditTextPhoneNumber.getText().toString());
        editor.apply();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.informations);
        mButtonOK = findViewById(R.id.buttonOK);
        mEditTextDeviceName = findViewById(R.id.editText2);
        mEditTextPhoneNumber = findViewById(R.id.editText);

        sharedPref = getSharedPreferences("klasyfikator_ptakow_preferencje",Context.MODE_PRIVATE);
        mEditTextDeviceName.setText(sharedPref.getString("deviceName",""));
        mEditTextPhoneNumber.setText(sharedPref.getString("phoneNumber",""));

        mButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(intent);
            }
        });

    }
}