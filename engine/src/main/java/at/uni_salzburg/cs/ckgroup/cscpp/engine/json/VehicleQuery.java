/*
 * @(#) VehicleQuery.java This code is part of the JNavigator project. Copyright (c) 2011 Clemens Krainer This program
 * is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package at.uni_salzburg.cs.ckgroup.cscpp.engine.json;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IAction;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ITask;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class VehicleQuery implements IQuery
{

    private static final Logger LOG = LoggerFactory.getLogger(VehicleQuery.class);

    private static final String PROP_VEHICLE_LOCAL_NAME = "name";
    private static final String PROP_VEHICLE_STATE = "state";
    private static final String PROP_VEHICLE_LATITUDE = "latitude";
    private static final String PROP_VEHICLE_LONGITUDE = "longitude";
    private static final String PROP_VEHICLE_ALTITUDE = "altitude";
    private static final String PROP_VEHICLE_TOLERANCE = "tolerance";
    private static final String PROP_VEHICLE_ACTIONS = "actions";
    private static final String PROP_VEHICLE_ACTION_POINTS = "actionPoints";
    private static final String PROP_VEHICLE_PATH = "vehiclePath";

    private Map<String, IVirtualVehicle> vehicleMap;

    static Map<String, String> actionsMap = Stream.of(
        Pair.of("AIRPRESSURE", "airPressure"),
        Pair.of("ALTITUDE", "altitude"),
        Pair.of("COURSE", "course"),
        Pair.of("PICTURE", "photo"),
        Pair.of("RANDOM", "random"),
        Pair.of("SONAR", "sonar"),
        Pair.of("SPEED", "speed"),
        Pair.of("TEMPERATURE", "temperature")

    ).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

    @SuppressWarnings("serial")
    static Set<String> propSet = new HashSet<String>()
    {
        {
            add("vehicle.id");
        }
    };

    public void setVehicleMap(Map<String, IVirtualVehicle> vehicleMap)
    {
        this.vehicleMap = vehicleMap;
    }

    @SuppressWarnings("unchecked")
    public String execute(IServletConfig config, String[] parameters)
    {

        boolean sendAPs = true;
        boolean sendVVpath = true;

        if (parameters.length > 3)
        {
            for (String p : parameters[3].trim().split("\\s*,\\s*"))
            {
                if ("noAPs".equals(p))
                {
                    sendAPs = false;
                }
                else if ("noVvPath".equals(p))
                {
                    sendVVpath = false;
                }
            }
        }

        //		Map<String, Object> obj=new LinkedHashMap<String, Object>();
        JSONObject obj = new JSONObject();

        for (IVirtualVehicle vehicle : vehicleMap.values())
        {

            if (vehicle.isFrozen())
            {
                continue;
            }

            //			Map<String, Object> props = new LinkedHashMap<String, Object>();
            JSONObject props = new JSONObject();
            for (Entry<Object, Object> e : vehicle.getProperties().entrySet())
            {
                String propertyName = (String) e.getKey();
                if (propSet.contains(propertyName))
                {
                    props.put(propertyName, e.getValue());
                }
            }

            props.put(PROP_VEHICLE_LOCAL_NAME, vehicle.getWorkDir().getName());

            if (vehicle.isProgramCorrupted())
            {
                props.put(PROP_VEHICLE_STATE, "corrupt");
            }
            else if (vehicle.isCompleted())
            {
                props.put(PROP_VEHICLE_STATE, "completed");
            }
            else if (vehicle.isActive())
            {
                props.put(PROP_VEHICLE_STATE, "active");
            }
            else
            {
                props.put(PROP_VEHICLE_STATE, "suspended");
            }

            if (vehicle.getCurrentTask() != null)
            {
                PolarCoordinate p = vehicle.getCurrentTask().getPosition();
                props.put(PROP_VEHICLE_LATITUDE, Double.valueOf(p.getLatitude()));
                props.put(PROP_VEHICLE_LONGITUDE, Double.valueOf(p.getLongitude()));
                props.put(PROP_VEHICLE_ALTITUDE, Double.valueOf(p.getAltitude()));
                props.put(PROP_VEHICLE_TOLERANCE, Double.valueOf(vehicle.getCurrentTask().getTolerance()));

                List<IAction> al = vehicle.getCurrentTask().getActionList();
                if (al != null)
                {
                    JSONArray openActions = new JSONArray();
                    for (IAction action : al)
                    {
                        if (!action.isComplete())
                        {
                            openActions.add(actionsMap.get(action.toString().toUpperCase()));
                        }
                    }
                    props.put(PROP_VEHICLE_ACTIONS, openActions);
                }

            }

            if (sendAPs)
            {
                JSONArray actionPoints = new JSONArray();
                for (ITask cmd : vehicle.getTaskList())
                {
                    JSONObject p = new JSONObject();
                    p.put("latitude", cmd.getPosition().getLatitude());
                    p.put("longitude", cmd.getPosition().getLongitude());
                    //					p.put("altitude", cmd.getPosition().getAltitude());
                    p.put("completed", Boolean.valueOf(cmd.isComplete()));
                    actionPoints.add(p);
                }
                props.put(PROP_VEHICLE_ACTION_POINTS, actionPoints);
            }

            if (sendVVpath)
            {
                try
                {
                    String vehicleLog = vehicle.getLog();
                    VehicleLogConverter c = new VehicleLogConverter();
                    JSONArray a = c.convertToVirtualVehiclePath(vehicleLog);
                    props.put(PROP_VEHICLE_PATH, a);

                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                    LOG.error("Can not read log of vehicle " + vehicle.getWorkDir().getName());
                }
            }

            obj.put(vehicle.getWorkDir().getName(), props);
        }

        return obj.toJSONString();
    }

}
