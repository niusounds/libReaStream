package com.eje_c.reastream;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.net.UnknownHostException;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {
    private ReaStream reaStream;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reaStream = new ReaStream();

        prefs = getSharedPreferences("app_status", MODE_PRIVATE);

        EditText identifier = (EditText) findViewById(R.id.input_identifier);
        identifier.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String newVal = s.toString();
                prefs.edit().putString("identifier", newVal).apply();
                reaStream.setIdentifier(newVal);
            }
        });

        final String defaultIdentifier = prefs.getString("identifier", null);
        if (defaultIdentifier != null) {
            identifier.setText(defaultIdentifier);
        }

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup_mode);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_mode_receive:
                        if (reaStream.isSending()) {
                            reaStream.stopSending();
                        }
                        if (!reaStream.isReceiving()) {
                            reaStream.startReveiving();
                        }
                        break;
                    case R.id.radio_mode_send:
                        if (reaStream.isReceiving()) {
                            reaStream.stopReceiving();
                        }
                        if (!reaStream.isSending()) {
                            reaStream.startSending();
                        }
                        break;
                }
            }
        });

        CheckBox enabled = (CheckBox) findViewById(R.id.check_enabled);
        enabled.setChecked(reaStream.isEnabled());
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reaStream.setEnabled(isChecked);
            }
        });

        reaStream.startReveiving();

        EditText remoteAddress = (EditText) findViewById(R.id.input_remoteAddress);
        remoteAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String newVal = s.toString();

                // Check IP address format
                if (newVal.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                    prefs.edit().putString("remoteAddress", newVal).apply();
                    try {
                        reaStream.setRemoteAddress(newVal);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        final String defaultRemoteAddress = prefs.getString("remoteAddress", null);
        if (defaultRemoteAddress != null) {
            remoteAddress.setText(defaultRemoteAddress);
        }

        try {
            reaStream.setRemoteAddress(remoteAddress.getText().toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        reaStream.close();
        super.onDestroy();
    }
}
