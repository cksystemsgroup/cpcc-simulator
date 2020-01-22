//
// @(#) Configuration.java
//
// This code is part of the CPCC project.
// Copyright (c) 2012 Clemens Krainer
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
//
package at.uni_salzburg.cs.ckgroup.cpcc.engmap.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.CircleZone;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.PolygonZone;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ConfigService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ConfigurationParser;

public class Configuration extends ConfigurationParser implements IConfiguration
{

    private static final String COMMA_SEPARATOR = "\\s*,\\s*";

    public static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    public static final String PROP_PILOT_AVAILABLE = "pilot.available";
    public static final String PROP_PILOT_URL = "pilot.url";

    /**
     * The prefix of the sensor properties.
     */
    public static final String PROP_SENSOR_PREFIX = "sensor.";
    public static final String PROP_SENSOR_LIST = PROP_SENSOR_PREFIX + "list";

    private static final String PROP_MAPPER_ALGORITHM = "mapper.algorithm";

    private static final String PROP_ZONE_PREFIX = "zone.";
    private static final String PROP_ZONE_LIST = PROP_ZONE_PREFIX + "list";
    private static final String PROP_ZONE_ENGINE_POSTFIX = ".engine";
    private static final String PROP_ZONE_GROUP_POSTFIX = ".group";
    private static final String PROP_ZONE_TYPE_POSTFIX = ".type";
    private static final String PROP_ZONE_VERTICES_POSTFIX = ".vertices";
    private static final String PROP_ZONE_POSITION_POSTFIX = ".position";
    private static final String PROP_ZONE_RADIUS_POSTFIX = ".radius";
    private static final String PROP_ZONE_DEPOT_POSTFIX = ".depot";

    private static final String PROP_CENTRAL_ENGINES = "mapper.central.engines";

    /**
     * The parameters and their default values.
     */
    public static final String[][] parameters = {
        {PROP_PILOT_AVAILABLE, "true"},
        {PROP_PILOT_URL, null, PROP_PILOT_AVAILABLE},
        {ConfigService.PROP_WEB_APP_BASE_URL},
        {PROP_MAPPER_ALGORITHM},
        {PROP_ZONE_LIST},
    };

    /**
     * The errors in the configuration as a map. Key is the configuration parameter and value is the according error
     * message. This map contains only messages of erroneous configuration parameters.
     */
    private static final Map<String, String> configErrors = Collections.unmodifiableMap(Stream.of(
        Pair.of(PROP_PILOT_URL, ERROR_MESSAGE_MISSING_VALUE),
        Pair.of(ConfigService.PROP_WEB_APP_BASE_URL, ERROR_MESSAGE_MISSING_VALUE),
        Pair.of(PROP_MAPPER_ALGORITHM, ERROR_MESSAGE_MISSING_VALUE),
        Pair.of(PROP_ZONE_LIST, ERROR_MESSAGE_MISSING_VALUE)).collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));

    /**
     * true if there is a pilot attached.
     */
    private boolean pilotAvailable;

    /**
     * The base URL of the associated auto pilot sensors.
     */
    private URI pilotUrl;

    /**
     * The URL of the central mapper registry.
     */
    private URI mapperRegistryUrl = null;

    /**
     * The class name of the mapping algorithm.
     */
    private List<Class<IMappingAlgorithm>> mapperAlgorithmClassList;

    /**
     * 
     */
    private Set<IZone> zoneSet;

    /**
     * 
     */
    private IGeodeticSystem geodeticSystem = new WGS84();

    /**
     * 
     */
    private Set<String> centralEngineUrls;

    /**
     * The URL of this webapplication.
     */
    private URI webApplicationBaseUrl;

    public Configuration()
    {
        super(parameters, configErrors);
        zoneSet = Collections.synchronizedSet(new HashSet<IZone>());
    }

    /**
     * Load a vehicle configuration from an <code>InputStream</code> and build it.
     * 
     * @param inStream the configuration's <code>InputStream</code>
     * @throws IOException thrown in case of errors.
     */
    @Override
    public void loadConfig(InputStream inStream) throws IOException
    {
        super.loadConfig(inStream);

        pilotAvailable = parseBool(PROP_PILOT_AVAILABLE).booleanValue();
        pilotUrl = pilotAvailable ? parseURI(PROP_PILOT_URL) : null;
        webApplicationBaseUrl = parseURI(ConfigService.PROP_WEB_APP_BASE_URL);

        mapperAlgorithmClassList = new ArrayList<>();
        mapperAlgorithmClassList.addAll(parseClassName(PROP_MAPPER_ALGORITHM, IMappingAlgorithm.class));

        List<String[]> pars = getParameters();

        centralEngineUrls = new HashSet<>();
        String propCenralEngines = parseString(PROP_CENTRAL_ENGINES, "").trim();
        if (!"".equals(propCenralEngines))
        {
            pars.add(new String[]{PROP_CENTRAL_ENGINES});
            String[] ces = propCenralEngines.split(COMMA_SEPARATOR);
            centralEngineUrls.addAll(Arrays.asList(ces));
        }

        zoneSet.clear();

        String zoneListString = parseString(PROP_ZONE_LIST);
        if (zoneListString == null)
        {
            return;
        }

        Pattern p = Pattern.compile("\\([^()]+\\)");

        String[] zoneNames = zoneListString.trim().split(COMMA_SEPARATOR);
        for (String name : zoneNames)
        {
            String propEngine = PROP_ZONE_PREFIX + name + PROP_ZONE_ENGINE_POSTFIX;
            String engine = parseString(propEngine, "");
            if (!"".equals(engine))
            {
                pars.add(new String[]{propEngine});
            }

            String propGroup = PROP_ZONE_PREFIX + name + PROP_ZONE_GROUP_POSTFIX;
            String group = parseString(propGroup, "local");
            pars.add(new String[]{propGroup});

            String propType = PROP_ZONE_PREFIX + name + PROP_ZONE_TYPE_POSTFIX;
            String type = parseString(propType);
            pars.add(new String[]{propType});

            IZone zone = null;

            if ("polygon".equals(type))
            {
                String propVertices = PROP_ZONE_PREFIX + name + PROP_ZONE_VERTICES_POSTFIX;
                pars.add(new String[]{propVertices});

                String verticesString = parseString(propVertices);
                if (verticesString == null)
                {
                    continue;
                }

                List<PolarCoordinate> vertices = new ArrayList<>();

                Matcher m = p.matcher(verticesString);

                while (m.find())
                {
                    String verticeString = verticesString.substring(m.start() + 1, m.end() - 1).trim();
                    String[] ll = verticeString.split(COMMA_SEPARATOR);
                    if (ll.length != 2)
                    {
                        configErrors.put(propVertices, ERROR_MESSAGE_INVALID_VALUE);
                        setConfigOk(false);
                    }
                    double latitude = Double.parseDouble(ll[0]);
                    double longitude = Double.parseDouble(ll[1]);
                    vertices.add(new PolarCoordinate(latitude, longitude, 0.0));
                }

                zone = new PolygonZone(vertices.toArray(new PolarCoordinate[0]));

            }
            else if ("circle".equals(type))
            {
                String propPosition = PROP_ZONE_PREFIX + name + PROP_ZONE_POSITION_POSTFIX;
                String propRadius = PROP_ZONE_PREFIX + name + PROP_ZONE_RADIUS_POSTFIX;
                pars.add(new String[]{propPosition});
                pars.add(new String[]{propRadius});

                String positionString = parseString(propPosition);
                if (positionString == null)
                {
                    continue;
                }

                if (!positionString.matches("\\s*\\(\\s*\\d+.\\d+\\s*,\\s*\\d+.\\d+\\)\\s*"))
                {
                    configErrors.put(propPosition, ERROR_MESSAGE_INVALID_VALUE);
                    setConfigOk(false);
                }

                String[] ll =
                    positionString.trim().replaceAll("\\(\\s*", "").replaceAll("\\s*\\)", "").split(COMMA_SEPARATOR);
                if (ll.length != 2)
                {
                    configErrors.put(propPosition, ERROR_MESSAGE_INVALID_VALUE);
                    setConfigOk(false);
                }
                double latitude = Double.parseDouble(ll[0]);
                double longitude = Double.parseDouble(ll[1]);
                double radius = parseDouble(propRadius);

                zone = new CircleZone(new PolarCoordinate(latitude, longitude, 0.0), radius, geodeticSystem);

            }
            else
            {
                configErrors.put(propType, ERROR_MESSAGE_INVALID_VALUE);
                setConfigOk(false);
            }

            if (zone != null)
            {
                String propDepot = PROP_ZONE_PREFIX + name + PROP_ZONE_DEPOT_POSTFIX;
                String depotString = parseString(propDepot);
                if (depotString != null)
                {
                    pars.add(new String[]{propDepot});

                    if (!depotString.matches("\\s*\\(\\s*\\d+.\\d+\\s*,\\s*\\d+.\\d+\\,\\s*\\d+.\\d+\\)\\s*"))
                    {
                        configErrors.put(propDepot, ERROR_MESSAGE_INVALID_VALUE);
                        setConfigOk(false);
                    }

                    String[] ll =
                        depotString.trim().replaceAll("\\(\\s*", "").replaceAll("\\s*\\)", "").split(COMMA_SEPARATOR);
                    if (ll.length != 3)
                    {
                        configErrors.put(propDepot, ERROR_MESSAGE_INVALID_VALUE);
                        setConfigOk(false);
                    }

                    double latitude = Double.parseDouble(ll[0]);
                    double longitude = Double.parseDouble(ll[1]);
                    double altitude = Double.parseDouble(ll[2]);
                    zone.setDepotPosition(new PolarCoordinate(latitude, longitude, altitude));
                }

                zone.setZoneEngineUrl(engine);
                zone.setZoneGroup(IZone.Group.valueOf(group.toUpperCase()));
                zoneSet.add(zone);
            }
        }
    }

    /* (non-Javadoc)
     * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.config.IConfiguration#isPilotAvailable()
     */
    public boolean isPilotAvailable()
    {
        return pilotAvailable;
    }

    /* (non-Javadoc)
     * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.config.IConfiguration#getPilotUrl()
     */
    @Override
    public URI getPilotUrl()
    {
        return pilotUrl;
    }

    /* (non-Javadoc)
     * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.config.AbstractConfiguration#getMapperRegistryUrl()
     */
    @Override
    public URI getMapperRegistryUrl()
    {
        return mapperRegistryUrl;
    }

    /* (non-Javadoc)
     * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.config.IConfiguration#getWebApplicationBaseUrl()
     */
    @Override
    public URI getWebApplicationBaseUrl()
    {
        return webApplicationBaseUrl;
    }

    @Override
    public List<Class<IMappingAlgorithm>> getMapperAlgorithmClassList()
    {
        return mapperAlgorithmClassList;
    }

    @Override
    public Set<IZone> getZoneSet()
    {
        return zoneSet;
    }

    @Override
    public Set<String> getCentralEngineUrls()
    {
        return centralEngineUrls;
    }

}
