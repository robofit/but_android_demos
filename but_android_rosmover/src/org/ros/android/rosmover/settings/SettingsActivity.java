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
 package org.ros.android.rosmover.settings;

import org.ros.android.rosmover.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class SettingsActivity extends Activity{
	private SettingsFragment SF;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		SF = new SettingsFragment();
		getFragmentManager().beginTransaction().replace(android.R.id.content, SF).commit();
		setContentView(R.layout.activity_settings);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    SF.onResume();
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    SF.onPause();
	    Toast.makeText(this, "The change will be apply after restarting application", Toast.LENGTH_SHORT).show();  
	}
	
	@Override
	public void onDestroy() {
		 super.onDestroy();
		 SF.onPause();
	}
}