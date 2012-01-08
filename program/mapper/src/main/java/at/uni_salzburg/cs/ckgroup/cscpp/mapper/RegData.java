/*
 * @(#) RegData.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2011  Clemens Krainer, Michael Kleber, Andreas Schroecker, Bernhard Zechmeister
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package at.uni_salzburg.cs.ckgroup.cscpp.mapper;

import java.util.List;

import at.uni_salzburg.cs.ckgroup.cscpp.mapper.course.WayPoint;

public class RegData {
    
    private String eng_uri;
	private String pilotUri;
    private List<WayPoint> waypoints;
    private List<String> sensors;
    
    public RegData(String eu, String pu, List<WayPoint> wp, List<String> sens) {
        eng_uri = eu;
        pilotUri = pu;
        waypoints = wp;
        sensors = sens;
    }
    
    public String getEngineUri() {
        return eng_uri;
    }

    public String getPilotUri() {
        return pilotUri;
    }

    public List<WayPoint> getWaypoints() {
        return waypoints;
    }
    
    public List<String> getSensors() {
        return sensors;
    }
    
    //TODO find waypoint etc
}
