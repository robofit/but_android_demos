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
package org.ros.android.rosmover.diagnostic;

import java.util.ArrayList;
import org.ros.android.rosmover.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListView;

public class DiagnosticActivity extends Activity {
	
	private ExpandableListAdapter adapter;
	private static String GrpCPU 		= "CPU"; 
	private static String GrpNetwork 	= "Network"; 
	private static String GrpNtb 		= "Notebook"; 
	private static String GrpSensor		= "Sensor";
	private static String GrpMotor		= "Motors"; 
	private static String GrpOthers		= "Others"; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_diagnostic);
	    
	    ExpandableListView listView = (ExpandableListView) findViewById(R.id.exListView);
        
        // Initialize the adapter with blank groups and children
        // We will be adding children on a thread, and then update the ListView
        adapter = new ExpandableListAdapter(this, new ArrayList<String>(), new ArrayList<ArrayList<InfoData>>());

        // Set this blank adapter to the list view
        listView.setAdapter(adapter);
        // Set data for diagnostic activity
        setData();
	}
	
	public void setData() {
		// Fake data 
        // CPU data
        adapter.addItem(new InfoData("CPU 1 - temperature","49°C",GrpCPU));
        adapter.addItem(new InfoData("CPU 2 - temperature","51°C",GrpCPU));
        adapter.addItem(new InfoData("Swap utilization","0%",GrpCPU));
        // Notebook data
        adapter.addItem(new InfoData("Battery level","51%",GrpNtb));
        adapter.addItem(new InfoData("Free disk","30 GB",GrpNtb));
        adapter.addItem(new InfoData("RAM","3,6 GB",GrpNtb));
        // Network data
        adapter.addItem(new InfoData("IP address","192.168.1.4",GrpNetwork));
        adapter.addItem(new InfoData("Speed","48Mb/s",GrpNetwork));
        // Sensor data
        adapter.addItem(new InfoData("Laser-scan","Yes",GrpSensor));
        adapter.addItem(new InfoData("Map","Yes",GrpSensor));
        // Motor
        adapter.addItem(new InfoData("Data","Not found",GrpMotor).setError(true));
        //Other
        adapter.addItem(new InfoData("Data","Not specific",GrpOthers).setError(true));
	}
}