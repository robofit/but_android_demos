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
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment  implements OnSharedPreferenceChangeListener {
	private Preference pref;
    private String summaryStr;
    private SharedPreferences sharedPref;
    String prefixStr;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);     
        sharedPref = getPreferenceScreen().getSharedPreferences();
        //initSummary();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

	protected void initSummary() {
		for(int x = 0; x <this.getPreferenceScreen().getPreferenceCount(); x++)
		{
			Preference p = this.getPreferenceScreen().getPreference(x);
			if(p.getKey().equals("flipped_data"))
				continue;
			prefixStr = sharedPref.getString(p.getKey(),"");
			p.setSummary(p.getSummary().toString().concat("-").concat(prefixStr));
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		//initSummary(sharedPreferences);
		if(key.equals("flipped_data")){
			return;
		}
		//Get the current summary
        pref = findPreference(key);
        summaryStr = "Current";
        //Get the user input data
        prefixStr = sharedPreferences.getString(key, "");
        //Update the summary with user input data
        pref.setSummary(summaryStr.concat(": ").concat(prefixStr));
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes            
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
	}
}