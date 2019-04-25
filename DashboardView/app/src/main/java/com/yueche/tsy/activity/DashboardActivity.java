package com.yueche.tsy.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.yueche.tsy.view.DashboardView;
import com.yueche.tsy.view.R;

/**
 * 仪表盘界面
 */
public class DashboardActivity extends AppCompatActivity {

    DashboardView dashboardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dashboardView = findViewById(R.id.dash_board_view);
        dashboardView.setOilValue(0.7f);
        dashboardView.setRmpValue(7.4f);
        dashboardView.setTemperature(50);
        dashboardView.setSpeedValue(140);
    }
}
