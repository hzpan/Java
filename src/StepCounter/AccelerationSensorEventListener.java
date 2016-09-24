package ca.uwaterloo.Lab4_202_26;

import java.net.NoRouteToHostException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.w3c.dom.UserDataHandler;

import android.R.string;
import android.animation.FloatArrayEvaluator;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.cardemulation.OffHostApduService;
import android.util.Log;
import android.widget.TextView;
import mapper.FloatHelper;
import mapper.InterceptPoint;
import mapper.Mapper;

/**
 * Class receives information from SensorManager when accelerometer values have changed
 * Calculates the steps the user has taken, as well as the north-south and east-west displacement of the user
 * 
 * Outputs values, and graphs acceleration on each axis.
 */

public class AccelerationSensorEventListener implements SensorEventListener {

	private static TextView output;
	private static TextView instructionView;
	private static Mapper mapView;
	private static List <InterceptPoint> interceptPoints = new ArrayList<InterceptPoint>();
	private static List <InterceptPoint> ghostList = new ArrayList<InterceptPoint>();
	private static List<PointF> path = new ArrayList<PointF>();
	private static PointF startPoint = new PointF();
	private static PointF endPoint = new PointF();
	private static  PointF userPoint = new PointF();
	private static PointF selectedPoint = new PointF();
	private static PointF ghostPoint1 = new PointF();
	private static PointF ghostPoint2 = new PointF();
	private static PointF firstInterceptPoint = new PointF();
	private static float angle = 0;
	private static float referenceAngle = 0;
	private static float returnAngle = 0;
	private static float distance = 0;
	private static float tolerance = 1.1f;
	private static boolean isThereAWall = false;
	private static String userInstruction = "";
	private static String hitWall="";
	
//	static PathPlanner pathPlanner = new PathPlanner(1f);
//	PathPlanner pathPlanner;
//	LineGraphView graphView;
	
	public static int stepCounter = 0; 
	public static int stateCounter = 0;
	
	public static float sum = 0;
	public static int avgCount = 0;
	public static double avgOrientation = 0;
	public static float northDisplace = 0;
	private static float takeNorthStep = 0;
	private static float takeEastStep = 0;
	public static float eastDisplace = 0;
	public static boolean negative = false;
	
	public static float [] list = new float[3];
	public static float [] R = new float [9];
	public static float [] I = new float [9];
	public static float[] result = new float [3];
	
	private static float xMax = 0;
	private static float yMax = 0;
	private static float zMax = 0;
	private static float zMin = 0;
	
	private static float[] xArray = new float [2];
	private static float[] yArray = new float [2];
	private static float[] zArray = new float [2];

	
	public static void stepCounter (float[] inputArray){
			
		if (inputArray[0] > 3.4 || inputArray[0] < -3.4 ){
			stateCounter = 0;
		}
		else {
			
			switch(stateCounter){
//			State 0 where we need the acceleration to be positive and below 9.0 (the max acceleration we have limited the step counter to)
				case 0:
					negative = false;
					sum = 0;
					avgCount = 0;
					if (9.0 > inputArray[2] && inputArray[2] > 1.2){
						stateCounter ++;
						
//						Log.d("Switch", "case 0: " +  Integer.toString(stateCounter));
						sum += Math.abs(list[0]);
						avgCount++;
					}
					else {
						stateCounter = 0;
					}
					break;
					
//			State 1 where we need the acceleration to decrease back to 0 
				case 1: 
					if (inputArray[2] <1.2){
						if (list[0]<0){
							negative = true;
						}
						sum += Math.abs(list[0]);
						avgCount++;
						stateCounter ++; 
//						Log.d("Switch", "case 1: " +  Integer.toString(stateCounter));
					}
					break;
					
//			State 2 where we need the acceleration go to its minimum (falling), but still be above our set limit of -13  	
				case 2:
//					if ((-13.0 < inputArray[2] && inputArray[2] < 0.0) && Math.abs(inputArray[1]) > 0.7f * Math.abs(inputArray[2])){
					if ((-13.0 < inputArray[2] && inputArray[2] < 0.0)){
						if (list[0]<0){
							negative = true;
						}
						
						sum += Math.abs(list[0]);
						avgCount++;
						stateCounter ++;
					}
					else {
						stateCounter = 0;
					}
					break;
//			State 3: if the acceleration then returns to 0, this counts as one step
//					we also finish calculating the north-south and east-west orientation components of the step
//					and add those components to our running count of the displacement in each axis
				case 3: 
					if (inputArray[2] > 0.0){
						if (list[0]<0){
							negative = true;
						}
						
						sum += Math.abs(list[0]);
						avgCount++;
						avgOrientation = sum/avgCount;
//						avgOrientation = (double) list[0]*Math.PI/180;
						if (negative){
							avgOrientation = -1*avgOrientation;
						}
						//stateCounter ++;
						stateCounter = 0;
						stepCounter++;
						takeNorthStep = (float) Math.cos(avgOrientation);
						takeEastStep = (float) Math.sin(avgOrientation);
						northDisplace -= takeNorthStep;
						eastDisplace += takeEastStep;
						//set user point as the first point
						PointF thisPint = new PointF(mapView.getUserPoint().x, mapView.getUserPoint().y);
						//increment first and set to second point
					    PointF nextPoint = new PointF(thisPint.x + takeEastStep, thisPint.y - takeNorthStep);
					    //if the difference between user point and selected point is less than 0.5, then we get to the point
					    if (Math.abs(mapView.getUserPoint().x - mapView.getSelectedPoint().x) < 0.5f &&
								Math.abs(mapView.getUserPoint().y - mapView.getSelectedPoint().y) < 0.5f	){
							
							userInstruction = "you got there";
							hitWall = "";
							
						}
					    else{
							if (mapView.calculateIntersections(nextPoint, 
											new PointF(nextPoint.x +0.6f, nextPoint.y)).isEmpty()
									&& mapView.calculateIntersections(nextPoint, 
											new PointF(nextPoint.x -0.6f, nextPoint.y)).isEmpty()
									&& mapView.calculateIntersections(nextPoint, 
											new PointF(nextPoint.x, nextPoint.y + 0.6f)).isEmpty()
									&& mapView.calculateIntersections(nextPoint, 
											new PointF(nextPoint.x, nextPoint.y - 0.6f)).isEmpty()){
//								mapView.setUserPoint(thisPint.x, thisPint.y);
//								userInstruction = "keep following the line";
								mapView.setUserPoint(nextPoint.x, nextPoint.y);
								hitWall="";
								
							}
							else {
							// if the distance between the next step and the wall is less than 0.6, we gonna hit a wall
								hitWall = "You are gonna hit the wall, turn away from the wall";
							}
					    }
						
						
						
						
					}
					break;
				default:
					stateCounter = 0;
			}
		}
		
			
	}
	
	



//	This is the method we used to filter out values
	public static float LowPassFilter(float[] sensorInput ){
		float previousInput = sensorInput[0];
		float currentInput = sensorInput[1];
		currentInput = 0.3f*previousInput + 0.7f*currentInput;
		
		return currentInput;
		
		
	}
	
	// Resets stored maximum values for step counter, axis displacements, and accelerometer x, y, z axes, 
	public static void reset(){
		stepCounter = 0;
		northDisplace = 0;
		eastDisplace = 0;
		xMax = 0;
		yMax = 0;
		mapView.removeAllLabeledPoints();
		mapView.setUserPoint(new PointF(0,0));
		mapView.setSelectedPoint(new PointF(0,0));
		mapView.setStartPoint(new PointF(0,0));
		mapView.setDestPoint(new PointF(0,0));
	}
	
	public static float getAngle(){
		returnAngle = (float) (((list[0]  - referenceAngle)/Math.PI)*180f);
		return returnAngle;
	}
	
	// Class constructor specifying TextView and graph on which to display data 
//	public AccelerationSensorEventListener (TextView outputView, LineGraphView graph){
	public AccelerationSensorEventListener (TextView outputView, TextView instruction, Mapper map){
		output = outputView;
//		graphView = graph;
		instructionView = instruction;
		mapView = map;
	}
	
	@Override
	public void onAccuracyChanged(Sensor s, int i) {}

	@Override
	public void onSensorChanged(SensorEvent se) {
		

		//calculate intersection
		interceptPoints = mapView.calculateIntersections(mapView.getUserPoint(), mapView.getSelectedPoint());
		if (!interceptPoints.isEmpty()){
			// if there is a wall between user point and selected point, we do the following logic.
			firstInterceptPoint .set(interceptPoints.get(0).getPoint().x , interceptPoints.get(0).getPoint().y);
			
			// Decrement the user point y-value until we hit a wall; set that intersection as your ghost point 1
			ghostPoint1.set(firstInterceptPoint.x, firstInterceptPoint.y +0.1f);
			
			// list of intersections/walls between user point and ghost point 1
			ghostList = mapView.calculateIntersections(firstInterceptPoint, ghostPoint1);
			
			while (ghostList.isEmpty()){
				// while there is no walls, we go to the ghost point
				// set a second ghost point with the same y-value as the first, and the same x-value as your selected Point
				
				ghostPoint1.set(ghostPoint1.x, ghostPoint1.y+0.1f);
				ghostPoint2.set(mapView.getSelectedPoint().x, ghostPoint1.y);
				ghostList = mapView.calculateIntersections(firstInterceptPoint, ghostPoint1);
				
			}
			//add all points to the list 
			path.add(mapView.getUserPoint());
			path.add(firstInterceptPoint);
			path.add(ghostList.get(0).getPoint());
			path.add(ghostPoint2);
			//draw path
			path.add(mapView.getSelectedPoint());
			mapView.setUserPath(path);
			// calculate difference between points and set user instruction.
			if (path.get(0).x - path.get(1).x < 0 && path.get(0).y - path.get(1).y < 0){
				userInstruction = "go east or south, pick your direction";
			}
			else if (path.get(0).x - path.get(1).x < 0 && path.get(0).y - path.get(1).y > 0){
				userInstruction = "go east or north, pick your direction";
			}
			else if (path.get(0).x - path.get(1).x > 0 && path.get(0).y - path.get(1).y < 0){
				userInstruction = "go west or south, pick your direction";
			}
			else{
				userInstruction = "go west or north, pick your direction";
			} 
			path.clear();
			
		}
		
		else{
			// if there is no wall between userpoint and selected point, then we go directly to selected point
			path.add(mapView.getUserPoint());
			path.add(mapView.getSelectedPoint());
			//draw path
			mapView.setUserPath(path);
			path.clear();
		}
		
		
//		Define a rotation sensor event listener for the rotation matrix and orientation
		RotationalSensorEventListener rotationVector = new RotationalSensorEventListener();
		SensorManager.getRotationMatrixFromVector(R, rotationVector.getResult());
//		Get orientation; first value in 'list' is compass bearing, north=0
		SensorManager.getOrientation(R, list);
		
		
		if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
		
//			store the new value and previous value in to an array and pass to LowPass Filter Method for filtering
			xArray[0] = xArray [1];
			xArray[1] = se.values[0];
//			assign the Filtered value into an array called result
			result [0] = LowPassFilter(xArray);
			
			yArray[0] = yArray [1];
			yArray[1] = se.values[1];
			result [1] = LowPassFilter(yArray);
			
			zArray[0] = zArray [1];
			zArray[1] = se.values[2];
			result [2] = LowPassFilter(zArray);
			
//			implement the step counter method (which also calculates displacement)
			stepCounter(result);
			
//			check the new value and assign to variable if the new value is greater
			if (xMax < Math.abs(se.values[0])){
				xMax = Math.abs(se.values[0]);
			}

			if (yMax < Math.abs(se.values[1])){
				yMax = Math.abs(se.values[1]);
			}

			if (zMax < Math.abs(se.values[2])){
				zMax = Math.abs(se.values[2]);
			}
			
			if (zMin > se.values[2]){
				zMin = se.values[2];
			}
			
// 			Output step counter, step displacements, and 
//			new axes values and current axes maximums as text to three decimal places
			
			output.setText("--------------------Step Counter-------------------" + "\n" + 
//							"X Max: " + String.format("%.3f", xMax) + "\n" + 
//							"Y: " + String.format("%.3f", yMax) + "\n" + 
//							"Z: " + String.format("%.3f", zMax) + "\n" + 
//							"X Result: " + String.format("%.3f", result[0]) + "\n" + 
//							"Y Result: " + String.format("%.3f", result[1]) + "\n" + 
//							"Z Result: " + String.format("%.3f", result[2]) + "\n" +
							"StepCounter : " + stepCounter + "\n" +
//							"StateCounter: " +  stateCounter + "\n" +
							"North Displacement: " + String.format("%.3f", northDisplace) + "\n" +
							"East Displacement: " + String.format("%.3f", eastDisplace) + "\n" +
							"Angle: " + String.format("%.3f Degree", list[0]/Math.PI*180) + "\n" +
							"UserPoint: " + mapView.getEndPoint() + "\n" +
//							"SelectedPoint: " + mapView.getStartPoint()+ "\n" +
							"StartPoint: "+ mapView.getStartPoint() + "\n" + 
//							"EndPoint: " + mapView.getEndPoint() + "\n" + 
							"Angle: " + (angle/Math.PI)*180 + " degree" + "\n"+
							"firstPoint: " + firstInterceptPoint + "\n"+
							"UserInstruction: " + userInstruction + "\n"+
							"UserInstruction: " + hitWall + "\n"+
							"-------------------------");
//			mapView.setUserPath(mapView.userPath);
			// Add new values to displayed graph
//			graphView.addPoint(result);
			
			
//			public void createPath (PointF start, PointF end){
//				userPath.add(start);
//				currentPoint = start;
//				
//				float lineAngle = FloatHelper.angleBetween(start, new PointF(start.x + 1,start.y), end);
//				int stepCount = (int) Math.ceil( FloatHelper.distance(start, end)/stepLength );
//				
//				for (int i=0; i < stepCount; i++){
//					currentPoint.y += (float) Math.sin(lineAngle);
//					currentPoint.x += (float) Math.cos(lineAngle);
//					userPath.add(currentPoint);
//				}
//			}
			
			
//			for (InterceptPoint point : interceptPoints){
//				listString += point.getPoint() + "\n";
//			}

		}
	}
}
