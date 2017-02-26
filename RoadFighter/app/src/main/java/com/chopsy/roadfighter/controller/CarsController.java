package com.chopsy.roadfighter.controller;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.chopsy.roadfighter.model.RaceStatus;
import com.chopsy.roadfighter.view.CarsView;

public class CarsController implements SensorEventListener, View.OnTouchListener {

    private CarsView mCarsView;
    private SensorManager mSensorManager;

    private ScoreboardController mScoreboardController;
    private GameController mGameController;
    private CollisionDetector mCollisionDetector;
    private Handler mPlayerCarSpeedHandler;
    private Handler mBotCarSpeedHandler;
    private long timeInterval = 500;
    private int mSpeed = 0;
    private int distance = 0;

    private int mBotCurrentSpeed = 2;
    private final static int minSpeed = 2;

    private RaceStatus currentRaceStatus = RaceStatus.PLAYING;


    public CarsController(CarsView carsView) {
        mCarsView = carsView;
        GameContext.registerPlayerCarController(this);
        mGameController = GameContext.getGameController();
        mScoreboardController = GameContext.getScoreboardController();
        mCollisionDetector = new CollisionDetector();
        mSensorManager = (SensorManager) mGameController.getSystemService(Context
                .SENSOR_SERVICE);
        mCarsView.setOnTouchListener(this);

        mBotCurrentSpeed = minSpeed;
        mBotCarSpeedHandler = new Handler();
        mBotCarSpeedHandler.postDelayed(botCarSpeedControlAction, 1000);


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (Math.abs(event.values[0]) < 1) {
                return;
            }
            boolean turnLeft = event.values[0] > 0;
            mCarsView.updatePlayerCarView(turnLeft);
            mCarsView.reDraw();
            detectCollision();
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
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    public void updateRoadView() {
        GameContext.getRoadController().updateRoadView();
    }

    public void updateScoreboardSpeed(int speed) {

        mScoreboardController.setSpeed(speed);
    }

    public void updateBackground(int speed) {
        mGameController.updateBackground(speed);
    }

    public void updateScoreboardDistance(int distance) {
        mScoreboardController.setDistance(distance);
        mScoreboardController.refresh();
    }

    public void performActionUp() {
        if (mPlayerCarSpeedHandler == null) return;
        mPlayerCarSpeedHandler.removeCallbacks(increasePlayerCarSpeedAction);
        mPlayerCarSpeedHandler.postDelayed(decreasePlayerCarSpeedAction, timeInterval);
//        return false;
    }

    public void performActionDown() {
        if (mPlayerCarSpeedHandler != null) {
            mPlayerCarSpeedHandler.removeCallbacks(decreasePlayerCarSpeedAction);
            mPlayerCarSpeedHandler.postDelayed(increasePlayerCarSpeedAction, timeInterval);
            return;
        }
        mPlayerCarSpeedHandler = new Handler();
        mPlayerCarSpeedHandler.postDelayed(increasePlayerCarSpeedAction, timeInterval);
//        return;
    }

    Runnable increasePlayerCarSpeedAction = new Runnable() {
        @Override
        public void run() {

            if (currentRaceStatus == RaceStatus.PLAYING) {
                if (timeInterval <= 1) {
                    timeInterval = 1;
                } else {
                    timeInterval -= 5;
                    mSpeed++;
                    updateBotCarSpeed(mSpeed / 5 * 10);
                    updateScoreboardSpeed(mSpeed);

                }
                distance += mSpeed * 5;
                updateBotCarSpeed(mSpeed / 5 * 10);
                updateBackground(mSpeed);
                updateScoreboardDistance(distance);
                mPlayerCarSpeedHandler.postDelayed(this, timeInterval);
                updateRoadView();
            }

        }
    };

    Runnable decreasePlayerCarSpeedAction = new Runnable() {
        @Override
        public void run() {

            if (currentRaceStatus == RaceStatus.PLAYING) {
                if (timeInterval >= 500) {
                    timeInterval = 500;
                    mPlayerCarSpeedHandler.removeCallbacks(decreasePlayerCarSpeedAction);
                    mPlayerCarSpeedHandler.removeCallbacks(increasePlayerCarSpeedAction);
                    timeInterval = 500;
                    mSpeed = 0;
                    updateRoadView();
                    updateScoreboardSpeed(mSpeed);
                    updateBotCarSpeed(mSpeed / 500 * 10);
                    mPlayerCarSpeedHandler = null;
                } else {
                    timeInterval += 20;
                    distance += mSpeed * 20;
                    mSpeed -= 4;
                    if (mSpeed < 1) {
                        mSpeed = 1;
                    }
                    updateBotCarSpeed(mSpeed / 5 * 10);
                    updateBackground(mSpeed);
                    updateScoreboardDistance(distance);
                    updateRoadView();
                    updateScoreboardSpeed(mSpeed);
                    mPlayerCarSpeedHandler.postDelayed(this, timeInterval);
                }
            }
        }
    };

    Runnable botCarSpeedControlAction = new Runnable() {
        @Override
        public void run() {
            updateBotCarPosition();
            mBotCarSpeedHandler.postDelayed(this, 10);
        }
    };

    private void updateBotCarSpeed(int playerCarSpeed) {
        mBotCurrentSpeed = minSpeed + playerCarSpeed;
    }

    public void updateBotCarPosition() {
        int distanceCovered = mBotCurrentSpeed * mCarsView.getHeight() / (102 * 10);
        int carPos = mCarsView.getBotCarTopEnd();
        carPos += distanceCovered;
        if (carPos > mCarsView.getHeight()) {
            carPos = 0;
        }
        mCarsView.setBotCarTopEnd(carPos);
        mCarsView.reDraw();
        detectCollision();
    }

    private void detectCollision() {
        if (isCollisionHappens()) {
            resetGame();
            mCarsView.reDraw();
        }
    }

    private void resetGame() {
        resetEverythingExceptPlayerCar();
        stopListenersAndSensors();
        refreshPlayerCar();
        startGame();
    }

    private void startGame() {

    }

    private void refreshPlayerCar() {

    }

    private void stopListenersAndSensors() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        currentRaceStatus = RaceStatus.PAUSE;

    }

    private void resetEverythingExceptPlayerCar() {
        mCarsView.setBotCarTopEnd(0);
        updateBotCarSpeed(0);
        updateScoreboardSpeed(0);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (currentRaceStatus == RaceStatus.PLAYING) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    performActionDown();
                    break;
                case MotionEvent.ACTION_UP:
                    performActionUp();
                    break;
            }
            return true;
        }
        return false;
    }

    private boolean isCollisionHappens() {
        Rect playerCarBounds = mCarsView.getPlayerCarBounds();
        Rect botCarBounds = mCarsView.getBotCarBounds();
        return mCollisionDetector.areIntersected(playerCarBounds, botCarBounds);
    }
}
