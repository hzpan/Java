package ca.uwaterloo.Lab4_202_26;

import java.util.Arrays;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.uwaterloo.Lab4_202_26.R;
import mapper.*;




public class MainActivity extends Activity {
	
	static Mapper mapView;
	static PointF userPoint;
	static MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        // Add a new mapView to screen display
        mapView = new Mapper(getApplicationContext(), 700, 700, 20, 20);
        registerForContextMenu(mapView);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onCreateContextMenu ( ContextMenu menu , View v, ContextMenuInfo menuInfo ) {
	    super . onCreateContextMenu (menu , v, menuInfo );
	    mapView. onCreateContextMenu (menu , v, menuInfo );
    }
    @Override
    public boolean onContextItemSelected ( MenuItem item ) {
    	return super . onContextItemSelected ( item ) || mapView . onContextItemSelected ( item );
    }

    

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }
//        LineGraphView graph;
    	Button button;
    	Button button2;
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	
        	View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        	
        	
        	SensorManager sensorManager = (SensorManager)rootView.getContext().getSystemService(SENSOR_SERVICE);
        	//init sensors
        	Sensor linearSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        	Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        	Sensor accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        	Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        	
        	//make a main view with linear text, graph, and map
            LinearLayout main1 = (LinearLayout) rootView.findViewById(R.id.Linear1);
          	main1.setOrientation(LinearLayout.VERTICAL);
          	
//          	graph = new LineGraphView(rootView.getContext(), 100, Arrays.asList("x", "y", "z"));
//          	main1.addView(graph);
//          	graph.setVisibility(View.VISIBLE);
          	
            
          	
          	MapLoader mapLoader = new MapLoader();
          	PedometerMap map = mapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "E2-3344-Lab-room-S15-tweaked.svg");
          	mapView.setMap(map);
          	main1.addView(mapView);
          	mapView.setVisibility(View.VISIBLE);
          	
          	
          	final TextView textView1 = (TextView) rootView.findViewById(R.id.label1);
          	textView1.setText("Magentic: " + "\n" +
					"X: " + "\n" +
					"Y: " + "\n" +
					"Z:" + "\n" + 
					"-------------------------");
          	
          	final TextView instructionView = new TextView (rootView.getContext());
          	Log.d("output", ""+mapView.getUserPoint() + "  " + mapView.getEndPoint());
          	instructionView.setText("" + mapView.getUserPoint() + "   " + mapView.getEndPoint());
            main1.addView(instructionView);

          	// Linear Acceleration Sensor for step count detection
//          	AccelerationSensorEventListener linear = new AccelerationSensorEventListener(textView1, graph);
          	AccelerationSensorEventListener linear = new AccelerationSensorEventListener(textView1, instructionView  ,mapView);
          	sensorManager.registerListener(linear, linearSensor,SensorManager.SENSOR_DELAY_UI);
          	
          	// Magnetic, Rotational Accelerometer Sensors for getOrientation method for displacement calculations
          	MagneticSensorEventListener magnet = new MagneticSensorEventListener();
          	sensorManager.registerListener(magnet, magneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
          	
          	AccelerometerSensorEventListener accelerometer = new AccelerometerSensorEventListener();
          	sensorManager.registerListener(accelerometer, accelerationSensor,SensorManager.SENSOR_DELAY_NORMAL);
          	
          	RotationalSensorEventListener rotational = new RotationalSensorEventListener();
          	sensorManager.registerListener(rotational, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
          	
          	IMapperListener iMapperListener = new IMapperListener() {
				
				@Override
				public void locationChanged(Mapper source, PointF loc) {
					mapView.setUserPoint(loc);
					
				}
				
				@Override
				public void DestinationChanged(Mapper source, PointF dest) {
					mapView.setSelectedPoint(dest);
					
				}
			};
          	
          	mapView.addListener(iMapperListener);
          	
          	// Reset button
            
            
            final Button but1 = (Button) rootView.findViewById(R.id.button1);
            but1.setText("Reset");
            OnClickListener onclk = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AccelerationSensorEventListener.reset();
					
				}
			};
			
			but1.setOnClickListener(onclk);
			
			final Button but2 = (Button) rootView.findViewById(R.id.button2);
            but2.setText("Angle: ");
            OnClickListener onAngleChange = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					but2.setText("Angle: " + AccelerationSensorEventListener.getAngle());
					
				}
			};
			
			but2.setOnClickListener(onAngleChange);
          	
          	
            return rootView;
        }
        
//        @Override
//    	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
//    		super.onCreateContextMenu(menu, v, menuInfo);
//    		mapView.onCreateContextMenu(menu, v, menuInfo);
//    	}
//    	
//    	@Override
//    	public boolean onContextItemSelected(MenuItem item){
//    		return super.onContextItemSelected(item) || mapView.onContextItemSelected(item);
//    	}
    }
}
