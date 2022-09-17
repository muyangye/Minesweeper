package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String resultMessage = intent.getStringExtra("resultMessage");
        TextView result = (TextView) findViewById(R.id.result);
        result.setText(resultMessage);
    }

    public void playAgain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}