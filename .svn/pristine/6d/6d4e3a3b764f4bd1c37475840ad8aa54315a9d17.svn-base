package ca.uwaterloo.Lab4_202_26;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Class receives information from SensorManager when accelerometer values have changed
 * and stores maximum values on each axes during run.
 * 
 * Outputs current axes values and maximum values as text, and graphs value on each axis.
 */

public class AccelerometerSensorEventListener implements SensorEventListener {

	// Results on the three axes from the sensor stored here, can be accessed from elsewhere
	public static float[] result = new float[3];
	
	public AccelerometerSensorEventListener (){
	}
	
	@Override
	public void onAccuracyChanged(Sensor s, int i) {}

	@Override
	public void onSensorChanged(SensorEvent se) {
		
		// If the detected sensor event type is that of an accelerometer... 
		if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			
			result[0] = se.values[0];
			result[1] = se.values[1];
			result[2] = se.values[2];

		}
	}
		
	public float[] getResult(){
		return result;
	}
}
