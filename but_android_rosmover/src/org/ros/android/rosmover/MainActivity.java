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
 package org.ros.android.rosmover;

import org.ros.address.InetAddressFactory;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.RosActivity;
import org.ros.android.rosmover.diagnostic.DiagnosticActivity;
import org.ros.android.rosmover.move.Mover;
import org.ros.android.rosmover.move.MoverGest;
import org.ros.android.rosmover.settings.SettingsActivity;
import org.ros.android.view.RosImageView;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.CameraControlLayer;
import org.ros.android.view.visualization.layer.CameraControlListener;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import sensor_msgs.CompressedImage;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.base.Preconditions;

public class MainActivity extends RosActivity {
	private RosImageView<CompressedImage> image;
	private Mover mover;
	private MoverGest moverGest; 
	private VisualizationView VisualPanel;
	private ToggleButton showMeToggleButton;
	private ToggleButton resizeMeToggleButton;
	private DisplayMetrics displaymetrics;
	private SharedPreferences sharedPref;
	
	/* TOPICS */
	private String TopicMove;
	private String TopicGoal;
	private String TopicMap;
	private String TopicCamera;
	private String TopicScan;
	private String Control;
	private String RobotFrame = "base_link";
	/* FLIPPED DEPTH DATA */
	private boolean FlipDepth; 
	
	/* Type of control */
	private final static String Street_view = "Street-view";
	private final static String G_sensor = "G-sensor";

	
	public MainActivity() {
		// The RosActivity constructor configures the notification title and ticker messages.
		super("ROS Mover for Android", "ROS Mover for Android");
	}
	
	 @SuppressWarnings("unchecked")
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    // Dimension of display
	    displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);	
	    // Get preferences
	    sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	    // Get name of topics
	 	this.getTopics();
	    // ImageView display video from robot
	    image = (RosImageView<CompressedImage>) findViewById(R.id.image);
	    image.setMessageType(CompressedImage._TYPE);
	    image.setMessageToBitmapCallable(new BitmapFromCompressedImage());
	    registerForContextMenu(image);
	    
	    if(this.Control.equals(Street_view)) {
			 moverGest = new MoverGest(this,image,displaymetrics.heightPixels,displaymetrics.widthPixels);
	    } else{
			 mover = new Mover(this);
	    }
	    VisualPanel = (VisualizationView) findViewById(R.id.visualization);
	    // Set size of VisualPanel
	    VisualPanel.getLayoutParams().height = displaymetrics.heightPixels/3;
	    VisualPanel.getLayoutParams().width = displaymetrics.widthPixels/4;

	    // Show me button - if enable center the robot layer in VisualPanel
	    showMeToggleButton = (ToggleButton) findViewById(R.id.show_me_toggle_button);
	    enableShowMe();
	    resizeMeToggleButton = (ToggleButton) findViewById(R.id.resize_me_toggle_button);
	    //disableResizeMe();
	 }

	 @Override
	  protected void init(NodeMainExecutor nodeMainExecutor) {
		 // Get name of topics
		 this.getTopics();
		 // Get if invert
		 this.getFlippedData();
		 // Visual panel
		 CameraControlLayer cameraControlLayer =  new CameraControlLayer(this, nodeMainExecutor.getScheduledExecutorService());
		 cameraControlLayer.addListener(new CameraControlListener() {
			      @Override
			      public void onZoom(double focusX, double focusY, double factor) {
			    	  disableShowMe();
			      }
			      @Override
			      public void onTranslate(float distanceX, float distanceY) {
			    	  disableShowMe();
			      }
			      @Override
			      public void onRotate(double focusX, double focusY, double deltaAngle) {
			    	  disableShowMe();
			      }
		 });
		 VisualPanel.addLayer(cameraControlLayer);
		 VisualPanel.addLayer(new OccupancyGridLayer(this.TopicMap.trim()));
		 VisualPanel.addLayer(new LaserScanLayer(this.TopicScan.trim()));
		 VisualPanel.addLayer(new RobotLayer(this.RobotFrame));
		 // Set topic name for video from robot
		 image.setTopicName(TopicCamera);
		 //Choose type of control
		 if(this.Control.equals(Street_view)) {
			 moverGest.setTopicGoal(TopicGoal);
			 moverGest.setTopicMove(TopicMove);
			 moverGest.setFlipDepth(FlipDepth);
		 }
		 else
		 {
			 mover.setTopic(TopicMove);
		 }
		 //
		 NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),getMasterUri());
	     // At this point, the user has already been prompted to either enter the URI
		 // of a master to use or to start a master locally.
		 if(this.Control.equals(Street_view)) {
			 nodeMainExecutor.execute(moverGest, nodeConfiguration);
		 }
		 else 
		 {
	    	nodeMainExecutor.execute(mover, nodeConfiguration);
		 }
		 nodeMainExecutor.execute(VisualPanel, nodeConfiguration.setNodeName("android/map_view"));
		 nodeMainExecutor.execute(image, nodeConfiguration.setNodeName("android/video_view"));
	  }
	 
	 public void getTopics() {
		 TopicCamera 	= sharedPref.getString("camera", "");
		 TopicMove		= sharedPref.getString("cmdvel", "");
		 TopicGoal		= sharedPref.getString("goal", "");
		 TopicMap		= sharedPref.getString("map", "");
		 TopicScan		= sharedPref.getString("scan", "");
		 Control		= sharedPref.getString("control", "");
	 }
	 
	 public void getFlippedData() {
		 FlipDepth		= sharedPref.getBoolean("flipped_data", false);
	 }
	 
	 public void SettingClick(View v) {
		 Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
		 MainActivity.this.startActivity(myIntent);
	 }
	 
	 public void DiagClick(View v) {
		 Intent myIntent = new Intent(MainActivity.this, DiagnosticActivity.class);
		 MainActivity.this.startActivity(myIntent);
	 }
	 
	 @Override
	  public void onDestroy() {
		 super.onDestroy();
		 //mover.close();
	 }
	 
	 @Override
	 public void onResume(){
		 super.onResume();
		 if(this.Control.equals(G_sensor)) {
			 this.mover.onResume();
		 }
	 }
	 
	 @Override
	 public void onPause(){
		 super.onPause();
		 if(this.Control.equals(G_sensor)) {
			 this.mover.onPause();
		 }
	 }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    */
	 
	 	@Override  
	    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
	    super.onCreateContextMenu(menu, v, menuInfo);  
	        menu.setHeaderTitle("Next options");  
	        menu.add(0, v.getId(), 0, "Follow person");  
	    }  
	  
	    @Override  
	    public boolean onContextItemSelected(MenuItem item) {  
	        if(item.getTitle()=="Follow person")
	        {
	        	function1(item.getItemId());
	        }  
	        else 
	        {
	        	return false;
	        }  
	    return true;  
	    }  
	  
	    public void function1(int id){  
	        Toast.makeText(this, "Follow person called", Toast.LENGTH_SHORT).show();  
	    }   
	    
	    
	    public void onResizeMeToggleButtonClicked(View view) {
	        boolean on = ((ToggleButton) view).isChecked();
	        if (on) 
	        {
	        	disableShowMe();
	        	enableResizeMe();
	        	enableShowMe();
	        } 
	        else
	        {
	        	disableShowMe();
	        	disableResizeMe();
	        	enableShowMe();
	        }
	      }	    
	    
	    private void disableResizeMe() {
	    	Preconditions.checkNotNull(VisualPanel);
	        Preconditions.checkNotNull(resizeMeToggleButton);
	        runOnUiThread(new Runnable() {
	          @Override
	          public void run() {
   	  
	        	  Animation mAnimation = new ScaleAnimation(2f,1f,2f,1f,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,1);
	        	  mAnimation.setFillAfter(true);
	        	  mAnimation.setFillEnabled(true);
	        	  mAnimation.setDuration(900);
	        	  mAnimation.setInterpolator(new DecelerateInterpolator());
	        	  VisualPanel.startAnimation(mAnimation);
	        	  resizeMeToggleButton.setChecked(false);
	        	  VisualPanel.getLayoutParams().height/=2;
	        	  VisualPanel.getLayoutParams().width/=2;
	          }
	        });
		}

		private void enableResizeMe() {
	    	Preconditions.checkNotNull(VisualPanel);
	        Preconditions.checkNotNull(resizeMeToggleButton);
	        runOnUiThread(new Runnable() {
	          @Override
	          public void run() {

	        	  Animation mAnimation = new ScaleAnimation(0.5f,1f,0.5f,1f,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,1);
	        	  mAnimation.setFillAfter(true);
	        	  mAnimation.setFillEnabled(true);
	        	  mAnimation.setDuration(900);
	        	  mAnimation.setInterpolator(new DecelerateInterpolator());
	        	  VisualPanel.startAnimation(mAnimation);
	        	  resizeMeToggleButton.setChecked(true);
	        	  VisualPanel.getLayoutParams().height*=2;
	        	  VisualPanel.getLayoutParams().width*=2;
	          }
	        });
		}

		/*
	     * Kod prevzaty z tutorialu ROSJavy - android_tutorial_map_viewer
	     */
	    
	    public void onShowMeToggleButtonClicked(View view) {
	        boolean on = ((ToggleButton) view).isChecked();
	        if (on) {
	          enableShowMe();
	        } else {
	          disableShowMe();
	        }
	      }

	     private void enableShowMe() {
	        Preconditions.checkNotNull(VisualPanel);
	        Preconditions.checkNotNull(showMeToggleButton);
	        runOnUiThread(new Runnable() {
	          @Override
	          public void run() {
	        	  VisualPanel.getCamera().jumpToFrame(RobotFrame);
	        	  showMeToggleButton.setChecked(true);
	          }
	        });
	      }

	      private void disableShowMe() {
	        Preconditions.checkNotNull(VisualPanel);
	        Preconditions.checkNotNull(showMeToggleButton);
	        runOnUiThread(new Runnable() {
	          @Override
	          public void run() {
	        	  VisualPanel.getCamera().setFrame(TopicMap);
	        	  showMeToggleButton.setChecked(false);
	          }
	        });
	      }
}