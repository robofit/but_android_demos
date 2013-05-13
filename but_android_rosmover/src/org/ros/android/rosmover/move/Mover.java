/******************************************************************************
 * \file
 *
 * $Id:$
 *
 * Copyright (C) Leopold Podmolik
 *
 * This file is part of software developed by Robo@FIT group.
 *
 * Author: Leopold Podmolik (xpodmo01@stud.fit.vutbr.cz)
 * Supervised by: Michal Spanel (spanel@fit.vutbr.cz)
 * Date: 13/05/2013
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ros.android.rosmover.move;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Mover extends AbstractNodeMain {
	private SensorManager sensorManager;
	public static final String LOG_TAG = "Mover";
	private String MoveTopic;
	private double x_raw;
    private double y_raw;
    
    public Mover (Context ctxt) {
    	sensorManager = (SensorManager)ctxt.getSystemService(Context.SENSOR_SERVICE);
    	sensorManager.registerListener(listener,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }
	
	@Override
	public GraphName getDefaultNodeName() {
	    return GraphName.of("gsensor_mover");
	}
	
	@Override
	public void onStart(final ConnectedNode connectedNode) {
		final Publisher<geometry_msgs.Twist> publisher =  connectedNode.newPublisher(MoveTopic.trim(), geometry_msgs.Twist._TYPE);
	    // This CancellableLoop will be canceled automatically when the node shuts
	    // down.
		final geometry_msgs.Twist pohyb = publisher.newMessage();
	    connectedNode.executeCancellableLoop(new CancellableLoop() {
	    	private double x,y;
	    	
	    	@Override
	    	protected void setup() {
	    		x = 0.0;
	    		y = 0.0;
	    	}
	    	
	    	@Override
	    	protected void loop() throws InterruptedException {
	    		x = x_raw;
	    		y = y_raw;
	    		
	    		pohyb.getLinear().setX(0.0);
	    		pohyb.getAngular().setZ(0.0);

	    		if(Math.abs(y)> 1 )
	    		{
	    			pohyb.getLinear().setX(-y/6.0);
	    		}

	    		if(x>1)
	    		{
	    			pohyb.getAngular().setZ(-2.5);
	    			// Poslani pohybu
		    		publisher.publish(pohyb);
	    		}
	    		else if(x<-1)
	    		{
	    			pohyb.getAngular().setZ(2.5);
	    			// Poslani pohybu
		    		publisher.publish(pohyb);
	    		}
	    		else
	    		{
	    			if(Math.abs(y)> 1 )
		    		{
		    			// Poslani pohybu
			    		publisher.publish(pohyb);
		    		}
	    		}
	    		Thread.sleep(100);
	    	}
	    });

	}
	
	private SensorEventListener listener=new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
	    	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	    		getAccelerometer(event);
	    	}
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		      // unused
		}
	  };
	  
	private void getAccelerometer(SensorEvent event) {
			float[] values = event.values;
			// Movement
			this.x_raw = (-1)*values[0];
			this.y_raw = values[1];
	}
	
	public void setTopic(String topic) {
		this.MoveTopic = topic;
	}
	
	public void close() {
		sensorManager.unregisterListener(listener);
	  }
	
	public void onResume() {
		sensorManager.registerListener(listener,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
	}
	public void onPause() {
		// unregister listener
	    sensorManager.unregisterListener(listener);
	}
	
	public void onDestroy() {
         sensorManager.unregisterListener(listener);       
    }
}