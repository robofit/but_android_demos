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

import org.ros.android.view.RosImageView;
import org.ros.concurrent.CancellableLoop;
import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import org.ros.node.topic.Publisher;

import sensor_msgs.CompressedImage;
import srs_env_model_percp.EstimateBBResponse;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;


public class MoverGest extends AbstractNodeMain {
	public static final String LOG_TAG = "MoverGest";
	private GestureDetector mDetector;
	private final int MIN_DISTANCE = 120;
    private final int DELTA_VELOCITY = 200;
    private boolean left,right,go,back,move;
    protected float x,y;
    protected ConnectedNode connectedNode;
    protected ServiceClient<srs_env_model_percp.EstimateBBRequest, srs_env_model_percp.EstimateBBResponse> serviceClient;
    protected int height, width;
    public Context ctxt;
    protected boolean flippedDepth;
    private String MoveTopic;
    private String GoalTopic;
    
    public MoverGest (final Context ctxt, final RosImageView<CompressedImage> image, final int height,final int width) {
    	this.height = height;
    	this.width  = width;
    	this.ctxt  = ctxt;
    	mDetector = new GestureDetector(ctxt, new GestureDetector.OnGestureListener(){
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				float deltaX = e1.getX() - e2.getX();
				float deltaY = e1.getY() - e2.getY();
				if(deltaX > MIN_DISTANCE && Math.abs(velocityX) > DELTA_VELOCITY) 
				{
					onRightToLeft();
			        return true;
				} 
				else if(Math.abs(deltaX) > MIN_DISTANCE && Math.abs(velocityX) > DELTA_VELOCITY) 
				{
					onLeftToRight();
					return true;
				}
				else if(deltaY > MIN_DISTANCE && Math.abs(velocityY) > DELTA_VELOCITY) 
				{
					onBottomToTop();
					return true;
				}
				else if(Math.abs(deltaY) > MIN_DISTANCE && Math.abs(velocityY) > DELTA_VELOCITY)
				{
					onTopToBottom();
					return true;
				}
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				((Activity) ctxt).openContextMenu(image);
			}

			@Override
			public void onShowPress(MotionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				float screenX = e.getX();
				float screenY = e.getY();
				double goalX  = 0;
				double goalY  = 0;
				double pomer = height/480.0;
				double okraj = (width-(pomer*640))/2.0;
				if(screenX <= okraj || screenX >= (width-okraj))
				{
					System.out.println("CLICK - MIMO ");
					return true;
				}
				goalY = screenY/(double)pomer;
				goalX = (screenX-okraj)/(double)pomer;
				if(flippedDepth)
				{
					goalY = 480 - goalY;
					goalX = 640 - goalX;
				}
				System.out.println("CLICK - X:"+goalX+",Y: "+goalY);
				onClickFunc(goalX,goalY);
				return true;
			}

			@Override
			public boolean onDown(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				// TODO Auto-generated method stub
				return false;
			}
    	});

    	image.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.d(LOG_TAG,"Singletouch event");
				mDetector.onTouchEvent(event);
				return true;
			}
	     });
    }
	
	@Override
	public GraphName getDefaultNodeName() {
	    return GraphName.of("android_mover");
	}
	
	public void onRightToLeft() {
		Log.d(LOG_TAG,"Left");
		this.back 	= false;
		this.go		= false;
		this.left 	= true;
		this.right 	= false;
	}
	
	public void onLeftToRight() {
		Log.d(LOG_TAG,"Right");
		this.back 	= false;
		this.go		= false;
		this.left 	= false;
		this.right 	= true;
	}
	
	public void onBottomToTop() {
		Log.d(LOG_TAG,"Back");
		this.back 	= true;
		this.go		= false;
		this.left 	= false;
		this.right 	= false;
	}
	
	public void onTopToBottom() {
		Log.d(LOG_TAG,"Go");
		this.back 	= false;
		this.go		= true;
		this.left 	= false;
		this.right 	= false;
	}
	
	public void onClickFunc(double goalX,double goalY) {
	    final srs_env_model_percp.EstimateBBRequest request = serviceClient.newMessage();
	    short[] bod1 = new short[2];
	    short[] bod2 = new short[2]; 
	    bod1[0] = (short) goalX;
	    bod1[1] = (short) goalY;
	    bod2[0] = (short) ((goalX > 600)?(goalX - 10) :(goalX + 10));
	    bod2[1] = (short) ((goalY > 440)?(goalY - 10) :(goalY + 10));
	    request.getHeader().setFrameId("/map");
	    request.getHeader().setStamp(connectedNode.getCurrentTime());
	    request.setP1(bod1);
	    request.setP2(bod2);
	    request.setMode((byte) 1);
	    serviceClient.call(request, new ServiceResponseListener<srs_env_model_percp.EstimateBBResponse>() {
		@Override
		public void onFailure(RemoteException arg0) {
			throw new RosRuntimeException(arg0);
		}

		@Override
		public void onSuccess(EstimateBBResponse response) {
			x = (response.getP1()[0]+response.getP2()[0]+response.getP3()[0]+response.getP4()[0]+response.getP5()[0]+response.getP6()[0]+response.getP7()[0]+response.getP8()[0])/8.0f ;//* 10;
			y = (response.getP1()[1]+response.getP2()[1]+response.getP3()[1]+response.getP4()[1]+response.getP5()[1]+response.getP6()[1]+response.getP7()[1]+response.getP8()[1])/8.0f ;//* 10;
			move = true;
		}
	    });
	}
	
	public void setTopicMove(String name) {
		this.MoveTopic = name;
	}
	
	public void setTopicGoal(String name) {
		this.GoalTopic = name;
	}
	
	public void setFlipDepth(boolean flipDepth) {
		this.flippedDepth = flipDepth;
	}
	
	@Override
	public void onStart(final ConnectedNode connectedNode) {
		this.connectedNode = connectedNode;
	    try {
	      serviceClient = connectedNode.newServiceClient("bb_estimator/estimate_bb", srs_env_model_percp.EstimateBB._TYPE);
	    } catch (ServiceNotFoundException e) {
	      throw new RosRuntimeException(e);
	    }
	    
		final Publisher<geometry_msgs.Twist> publisherRotate =  connectedNode.newPublisher(MoveTopic.trim(), geometry_msgs.Twist._TYPE);
		final Publisher<geometry_msgs.PoseStamped> publisherMove =  connectedNode.newPublisher(GoalTopic.trim(), geometry_msgs.PoseStamped._TYPE);
		final geometry_msgs.Twist rotace = publisherRotate.newMessage();
		final geometry_msgs.PoseStamped cil = publisherMove.newMessage();
		// This CancellableLoop will be canceled automatically when the node shuts down.
	    connectedNode.executeCancellableLoop(new CancellableLoop() {
	    	private boolean leftE, rightE, goE, backE, moveE;
	    	private int seq;
	    	
	    	@Override
	    	protected void setup() {
	    		leftE 	= false;
	    		rightE	= false;
	    		goE		= false;
	    		backE	= false;
	    		moveE 	= false;
	    		seq = 0;
	    	}
	    	
	    	@Override
	    	protected void loop() throws InterruptedException {
	    		/* ROTACE  */
	    		leftE 	= left;
	    		rightE	= right;
	    		goE		= go;
	    		backE	= back;
	    		moveE	= move;
	    		rotace.getAngular().setZ(0);
	    		rotace.getLinear().setX(0);
	    		if(leftE)
	    		{
	    			leftE	= false;
	    			left	= false;
	    			rotace.getAngular().setZ(-2);
	    			for(int i = 0; i<6 ; i++) {
	    				publisherRotate.publish(rotace);
	    				Thread.sleep(300);
	    			}
	    		}
	    		else if(rightE)
	    		{
	    			rightE	= false;
	    			right	= false;
	    			rotace.getAngular().setZ(2);
	    			for(int i = 0; i<6 ; i++) {
	    				publisherRotate.publish(rotace);
	    				Thread.sleep(300);
	    			}
	    		}
	    		else if(goE)
	    		{
	    			goE		= false;
	    			go		= false;
	    			rotace.getLinear().setX(1.3);
	    			publisherRotate.publish(rotace);
	    		}
	    		else if(backE)
	    		{
	    			backE	= false;
	    			back	= false;
	    			rotace.getLinear().setX(-1.3);
	    			publisherRotate.publish(rotace);
	    		}
	    		
	    		/* Pohyb k cily */
	    		if(moveE)
	    		{
	    			moveE 	= false;
	    			move 	= false;
	    			seq++;
	    			cil.getHeader().setFrameId("/map");
	    			cil.getHeader().setSeq(seq);
	    			cil.getPose().getPosition().setX(x);
	    			cil.getPose().getPosition().setY(y);
	    			System.out.println("PublisherMove: X:"+x+",Y:"+y);
	    			cil.getPose().getOrientation().setX(0);
	    			cil.getPose().getOrientation().setY(0);
	    			cil.getPose().getOrientation().setZ(0);
	    			cil.getPose().getOrientation().setW(1);
	    			publisherMove.publish(cil);
	    			//Toast.makeText(ctxt, "Robot is moving to goal", Toast.LENGTH_LONG).show(); 
	    		}

	    		Thread.sleep(300);
	    	}
	    });
	}
}