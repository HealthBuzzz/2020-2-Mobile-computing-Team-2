package com.healthbuzz.healthbuzz.data;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.healthbuzz.healthbuzz.R;
import com.healthbuzz.healthbuzz.RealtimeModel;
import com.healthbuzz.healthbuzz.data.model.UserCount;

public class CommunityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RealtimeModel.INSTANCE.getCommunity().observe(this, community -> {
            TextView stretch = findViewById(R.id.stretching);
            TextView water = findViewById(R.id.water);
            TextView stretchCount = findViewById(R.id.stretching2);
            TextView waterCount = findViewById(R.id.water2);

            String textStretching = "";
            String textStretching2 = "";
            if (community == null)
                return;
            if (community.getStretching() != null) {
                int i = 1;
                for (UserCount nameWithCount : community.getStretching()) {
                    textStretching += String.format("%d. %-10s\n", i, nameWithCount.getUsername());
                    textStretching2 += "" + nameWithCount.getCount() + "\n";
                    i++;
                }
                stretch.setText(textStretching);
                stretchCount.setText(textStretching2);
            }
            String textWater = "";
            String textWater2 = "";
            if (community.getWater() != null) {
                int i = 1;
                for (UserCount nameWithCount : community.getWater()) {
                    textWater += String.format("%d. %-10s\n", i, nameWithCount.getUsername());
                    textWater2 += "" + nameWithCount.getCount() + "\n";
                    i++;
                }
                water.setText(textWater);
                waterCount.setText(textWater2);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LoginDataSource.getCommunity();
    }
}