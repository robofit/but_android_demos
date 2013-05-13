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

public class InfoData {
	 private String key;
	 private String value;
	 private String group;
	 private boolean error;
	 
	 public InfoData(String key,String val,String grp) {
	        this.key = key;
	        this.value = val;
	        this.group = grp;
	        this.error=false;
	 }
	 
	 public String getGroup() {
	        return this.group;
	 }
	 public InfoData setGroup(String group) {
	        this.group = group;
	        return this;
	 }
	 
	 public boolean getError() {
	        return this.error;
	 }
	 public InfoData setError(boolean err) {
	        this.error = err;
	        return this;
	 }
	 
	 public  String getKey() {
	        return this.key;
	 }
	 
	 public InfoData setKey(String key) {
	        this.key = key;
	        return this;
	 }
	 
	 public String getValue() {
	        return this.value;
	 }
	 
	 public InfoData setValue(String val) {
	        this.value = val;
	        return this;
	 }
}