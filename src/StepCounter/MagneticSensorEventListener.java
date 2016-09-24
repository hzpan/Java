package ca.uwaterloo.Lab4_202_26;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


/**
 * Class receives information from SensorManager when magnetic sensor values have changed
 * and stores maximum values on each axes during run.
 * 
 */

public class MagneticSensorEventListener implements SensorEventListener {
	
	// Results on the three axes from the sensor stored here, can be accessed from elsewhere
	public static float[] result = new float[3];
	
	// Class constructor specifying TextView on which to display data 
	public MagneticSensorEventListener (){
		//output = outputView;
	}
	
	@Override
	public void onAccuracyChanged(Sensor s, int i) {}

	@Override
	public void onSensorChanged(SensorEvent se) {
		
		// If the detected sensor event type is that of a magnetic sensor... 
		if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
			
			result[0] = se.values[0];
			result[1] = se.values[1];
			result[2] = se.values[2];
			
			
			result[0] = se.values[0];
			result[1] = se.values[1];
			result[2] = se.values[2];
			
		}
	}

	// Method to return the results of the magnetic sensor when called
	public float[] getResult() {
		return result;
	}
}
