package ca.uwaterloo.Lab4_202_26;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;
import android.util.Log;
import mapper.FloatHelper;
import mapper.InterceptPoint;
import mapper.LineSegment;
import mapper.Mapper;

/*
 * 1. HOW TO INCORPORATE USER MOVEMENTS INTO PATHPLANNING
 * - call getPath methods in the step counter state machine or in the onSensorChanged bit of AccelerationSensor
 * 		so that it updates itself. 
 * - this class uses "startPoint" and "destPoint" from the Mapper to see if you can get directly to the 
 * 		destination without running into a wall or smth
 * - SO, just change the value of startPoint in the Mapper object to correspond to user movements
 * 		aka increment x, y values of startPoint with the north-south and east-west displacement we got from the last lab
 * 
 * 2. HOW TO DRAW PATH ONTO SCREEN
 * - there's an onDraw method in Mapper that does this I think, tho not sure how to call it
 * - it takes a list of PointF objects, which luckily we have. I think if we somehow pass the "path" list from this class
 * 		we can draw out the path on the screen.
 * 
 */

public class PathPlanner {
	private Mapper map;
	private float stepLength;
//	public List<PointF> userPath = new ArrayList<PointF>();
	private PointF currentPoint = new PointF(), nextPoint = new PointF();
	private int vectDirection;
	
/*	public List<PointF> getUserPath (){
		return userPath;
	}*/
	
	
	public PathPlanner(Mapper mapView, float stepLength) {
		this.map = mapView;
		this.stepLength = stepLength;
		
		map.userPoint = map.startPoint;
	}
	
	//Makes a list of PointF's that follow a path from one point to another
	public void createPath (PointF start, PointF end){
		map.userPath.add(start);
		currentPoint = start;
		
		float lineAngle = FloatHelper.angleBetween(start, new PointF(start.x + 1,start.y), end);
		int stepCount = (int) Math.ceil( FloatHelper.distance(start, end)/stepLength );
		
		for (int i=0; i < stepCount; i++){
			currentPoint.y += (float) Math.sin(lineAngle);
			currentPoint.x += (float) Math.cos(lineAngle);
			map.userPath.add(currentPoint);
		}
	}
	

	public void getPath(){
		List<InterceptPoint> checkWalls = new ArrayList<InterceptPoint>();
		
	// 1. Draw shortest line segment from startPoint and destPoint (unneeded here)
	// 2. calculateIntersections to see if there are walls in between start and destination
		Log.d("Above", map.userPoint + "" + map.destPoint);
		checkWalls = map.calculateIntersections(map.userPoint, map.destPoint);
		//map.calculateIntersections(userPoint, selectedPoint);
		Log.d("Below", map.userPoint + "" + map.destPoint);
 
	/* 3. if calculateIntersections returns null
	 * 			draw this shortest line segment on screen, user can walk it now
	 * 	  else, walk to wall
	 */
		if (checkWalls == null) {
			createPath(map.userPoint, map.destPoint);
		}
		else {
		/* 4. After continuous calculateIntersections (always do this) and Collections.sort is 0,
		 * 		walk in same NS direction as the dest is in (+ or -, meaning). 
		 */
			
			if (checkWalls.get(0).line.m == 0){ //if the wall is EW
				//figure out which direction to increment 
				if (map.userPoint.x > currentPoint.x){
						vectDirection = 1;
					}
					else {
						vectDirection = -1;
					}
				
				//has path just a little bit away from wall so you're not actually on the wall
				nextPoint.y = (float) (checkWalls.get(0).getPoint().y - vectDirection*0.1);
				createPath(map.userPoint, nextPoint); //DOES THIS WORK??
				
				//as long as there's wall, keep going in that direction
				while ( checkWalls.get(0).line.theSame(map.calculateIntersections(currentPoint, map.getSelectedPoint()).get(0).line)  ){
					nextPoint = currentPoint;
					nextPoint.x += vectDirection;
					
					//check if there's a wall where you're planning to go. If there is, then turn back.
					if (map.calculateIntersections(currentPoint, nextPoint) != null){
					
						vectDirection = -vectDirection;
						nextPoint = currentPoint;
						nextPoint.x += vectDirection;
						}
					createPath(currentPoint, nextPoint);
				}
			}
			
			else { //wall is NS
				//figure out which direction to increment 
				if (map.userPoint.y > currentPoint.y){
						vectDirection = 1;
					}
					else {
						vectDirection = -1;
					}
				
				//has path just a little bit away from wall so you're not actually on the wall				
				nextPoint.x = (float) (checkWalls.get(0).getPoint().x - vectDirection*0.1);
				createPath(map.userPoint, nextPoint); //DOES THIS WORK??
				
				//as long as there's wall, keep going in that direction
				while ( checkWalls.get(0).line.theSame(map.calculateIntersections(currentPoint, map.getSelectedPoint()).get(0).line)  ){
					nextPoint = currentPoint;
					nextPoint.y += vectDirection;
					
					//check if there's a wall where you're planning to go. If there is, then turn back.
					if (map.calculateIntersections(currentPoint, nextPoint) != null){
					
						vectDirection = -vectDirection;
						nextPoint = currentPoint;
						nextPoint.y += vectDirection;
						}
					createPath(currentPoint, nextPoint);
				}
			}
		}
			
	}
		
}

