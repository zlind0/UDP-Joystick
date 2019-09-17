package com.example.hzl.udpjoystick;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

/**
 * Created by HZL on 2015/3/7.
 */
public class AxisSensor {
	private float x, y, z;
	SensorManager sensorMgr_acc;
	Sensor sensor_acc;

	AxisSensor(Context context) {
		sensorMgr_acc = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		sensor_acc = sensorMgr_acc.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorMgr_acc.registerListener(new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				x = event.values[SensorManager.DATA_X];
				y = event.values[SensorManager.DATA_Y];
				z = event.values[SensorManager.DATA_Z];
			}
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		}, sensor_acc, SensorManager.SENSOR_DELAY_GAME);

	}
}
