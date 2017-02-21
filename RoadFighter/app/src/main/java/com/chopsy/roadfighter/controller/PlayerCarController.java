package com.chopsy.roadfighter.controller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.chopsy.roadfighter.view.PlayerCarView;

public class PlayerCarController implements SensorEventListener {

    private PlayerCarView mPlayerCarView;
    private SensorManager mSensorManager;
    private ScoreboardController scoreboardController;


    public PlayerCarController(PlayerCarView playerCarView) {
        mPlayerCarView = playerCarView;
        GameContext.registerPlayerCarController(this);
        mSensorManager = (SensorManager) GameContext.getGameController().getSystemService(Context
                .SENSOR_SERVICE);
        scoreboardController = GameContext.getScoreboardController();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (Math.abs(event.values[0]) < 1) {
                return;
            }
            boolean turnLeft = event.values[0] > 0;
            mPlayerCarView.updateView(turnLeft);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void startSensorManager() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor
                .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    public void stopSensorManager() {
        mSensorManager.unregisterListener(this);
    }

    public void updateRoadView() {
        GameContext.getRoadController().updateRoadView();
    }

    public void updateScoreboardSpeed(int speed) {

        scoreboardController.setSpeed(speed);
    }

    public void updateScoreboardDistance(int distance) {
        scoreboardController.setDistance(distance);
        scoreboardController.refresh();
    }
}
