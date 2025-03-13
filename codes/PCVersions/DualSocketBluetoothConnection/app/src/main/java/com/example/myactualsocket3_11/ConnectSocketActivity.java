package com.example.myactualsocket3_11;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConnectSocketActivity extends AppCompatActivity {

    private String address;

    private String port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_socket);


        EditText B1ED = findViewById(R.id.b1ID);
        EditText b2ED = findViewById(R.id.b2ID);
        EditText addEdText = findViewById(R.id.addressET);
        EditText portEdText = findViewById(R.id.portET);


        Button confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v-> {
            Intent resultIntent = new Intent();
            port = portEdText.getText().toString();

            if(!port.equals("")){
                resultIntent.putExtra("port", port);

            }
            address = addEdText.getText().toString();

            if(!address.equals("")){
                resultIntent.putExtra("address", address);

            }
            String b1= B1ED.getText().toString();

            if(!b1.equals("")){
                resultIntent.putExtra("b1", b1);

            }

            String b2= b2ED.getText().toString();

            if(!b2.equals("")){
                resultIntent.putExtra("b2", b2);
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}