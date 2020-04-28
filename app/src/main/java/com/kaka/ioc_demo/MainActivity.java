package com.kaka.ioc_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.kaka.annotation.TestBindView;
public class MainActivity extends AppCompatActivity {

    @TestBindView(R.id.button)
    Button button;

    @TestBindView(R.id.textView)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewBindUtil.bind(this);
        button.setText("按钮");
        textView.setText("文本");
    }
}
