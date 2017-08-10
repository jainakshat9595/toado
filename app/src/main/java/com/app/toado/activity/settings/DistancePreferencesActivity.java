package com.app.toado.activity.settings;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.app.toado.R;
import com.app.toado.activity.ToadoBaseActivity;
import com.app.toado.settings.UserSettingsSharedPref;

public class DistancePreferencesActivity extends ToadoBaseActivity {
    private Button submit;
    public float conversionFactor = 1.6F;
    public static String DistancePreference = "DistancePreference";

    private UserSettingsSharedPref userSettingsSharedPref;
    private SeekBar seekBar1;
    int val = 0;
    private TextView tvmax;
    private TextView tvmin;
    int progress = 0;
    private TextView tvdist;
    int yourStep = 10;
    ToggleButton tgb;
    Boolean status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_user_preferences);
        submit = (Button) findViewById(R.id.submit);

        Toast.makeText(DistancePreferencesActivity.this, "Minimum Distance must be greater than 10.", Toast.LENGTH_SHORT).show();

        userSettingsSharedPref = new UserSettingsSharedPref(DistancePreferencesActivity.this);
        tvmax = (TextView) findViewById(R.id.tvmax);
        tvmin = (TextView) findViewById(R.id.tvmin);
        tvdist = (TextView) findViewById(R.id.tvdistchosen);
        tvmin.setText(10 + "");

        tgb=(ToggleButton)findViewById(R.id.toggBtnGps);
        Boolean b =userSettingsSharedPref.getGpsBackSetting();
        System.out.println("distance pref act"+b);
        tgb.setChecked(b);

        tgb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                     userSettingsSharedPref.setGpsBackSetting(true);
                }
                else{
                     userSettingsSharedPref.setGpsBackSetting(false);
                }
            }
        });

        final Integer x = Integer.parseInt(userSettingsSharedPref.getValue().toString());
        seekBar1 = (SeekBar) findViewById(R.id.seekBar1);

        System.out.println("val of x" + x);

        if (x != null) {
            seekBar1.setProgress(x);
            tvdist.setText(x + " miles");
        } else {
            seekBar1.setProgress(10);
            tvdist.setText(10 + " miles");
        }

        seekBar1.setMax(50);

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                System.out.println("progress changed" + progress);
                if (progress < 10) {
                    seekBar1.setProgress(10);
                    val = 10;
                } else {
//                    val = progress;
                    val = ((int) Math.round(progress / yourStep)) * yourStep;
                }
                tvdist.setText(val + " miles");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String unit = "miles";
//                Intent in = new Intent(DistancePreferencesActivity.this, MainAct.class);

                if (val != 0) {
                    System.out.println(x + " val to send in distuserpref " + val);
                    if (val != x) {
                        int y = val;
//                        in.putExtra("distchanged", "yes");
                        userSettingsSharedPref.setDistancePref(unit, y, conversionFactor);
                        Toast.makeText(DistancePreferencesActivity.this, "Successfully saved your distance preference.", Toast.LENGTH_SHORT).show();
                    }

                }
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


}
