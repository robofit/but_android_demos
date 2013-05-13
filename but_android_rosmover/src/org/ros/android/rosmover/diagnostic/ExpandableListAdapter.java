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
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<String> groups;
	private ArrayList<ArrayList<InfoData>> children;
	public ExpandableListAdapter(Context context, ArrayList<String> groups, ArrayList<ArrayList<InfoData>> children) {
	        this.context = context;
	        this.groups = groups;
	        this.children = children;
	}
	 
	@Override
	public boolean areAllItemsEnabled() {
	        return true;
	}
	
	public void addItem(InfoData data) {
        if (!groups.contains(data.getGroup())) {
            groups.add(data.getGroup());
        }
        int index = groups.indexOf(data.getGroup());
        if (children.size() < index + 1) {
            children.add(new ArrayList<InfoData>());
        }
        children.get(index).add(data);
    }
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return children.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		InfoData item = (InfoData) getChild(groupPosition, childPosition);
	    if (convertView == null) 
	    {
	    	LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        convertView = infalInflater.inflate(R.layout.child_layout, null);
	    }
	    TextView tv = (TextView) convertView.findViewById(R.id.tvChild);
	    tv.setText("   " + item.getKey()+":   "+item.getValue());

	    // Depending upon the child type, set the imageTextView01
        tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick, 0, 0, 0);
        if(item.getError())
        {
        	tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cross, 0, 0, 0);
        }
        return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return children.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		 return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		 return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,	View convertView, ViewGroup parent) {
		String group = (String) getGroup(groupPosition);
		if (convertView == null) 
		{
			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        convertView = infalInflater.inflate(R.layout.group_layout, null);
	    }
        TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
        tv.setText(group);
        return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}