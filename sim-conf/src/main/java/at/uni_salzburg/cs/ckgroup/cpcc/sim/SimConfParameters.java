/*
 * @(#) SimConfParameters.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2012  Clemens Krainer
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
package at.uni_salzburg.cs.ckgroup.cpcc.sim;

import java.util.Map;

public class SimConfParameters {

	private Double zoneWidth;
	private Double zoneHeight;
	private Integer horizontalZones;
	private Integer verticalZones;
	private Double zoneCenterLat;
	private Double zoneCenterLng;
	private Integer rvsPerTomcat;

	public void loadParametersFromMap (Map parameterMap) {
		zoneWidth = parseDoubleFromMap(parameterMap, "zoneWidth");
		zoneHeight =  parseDoubleFromMap(parameterMap, "zoneHeight");
		horizontalZones =  parseIntegerFromMap(parameterMap, "horizontalZones");
		verticalZones =  parseIntegerFromMap(parameterMap, "verticalZones");
		zoneCenterLat =  parseDoubleFromMap(parameterMap, "zoneCenterLat");
		zoneCenterLng =  parseDoubleFromMap(parameterMap, "zoneCenterLng");
		rvsPerTomcat =  parseIntegerFromMap(parameterMap, "rvsPerTomcat");
	}

	private Integer parseIntegerFromMap(Map parameterMap, String name) {
		String[] str = (String[]) parameterMap.get(name);
		if (str == null || str.length < 1 || "".equals(str[0].trim())) {
			return null;
		}
		try {
			return Integer.valueOf(str[0]);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private Double parseDoubleFromMap(Map parameterMap, String name) {
		String[] str = (String[]) parameterMap.get(name);
		if (str == null || str.length < 1 || "".equals(str[0].trim())) {
			return null;
		}
		try {
			return Double.valueOf(str[0]);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public Double getZoneWidth() {
		return zoneWidth;
	}

	public void setZoneWidth(Double zoneWidth) {
		this.zoneWidth = zoneWidth;
	}

	public Double getZoneHeight() {
		return zoneHeight;
	}

	public void setZoneHeight(Double zoneHeight) {
		this.zoneHeight = zoneHeight;
	}

	public Integer getHorizontalZones() {
		return horizontalZones;
	}

	public void setHorizontalZones(Integer horizontalZones) {
		this.horizontalZones = horizontalZones;
	}

	public Integer getVerticalZones() {
		return verticalZones;
	}

	public void setVerticalZones(Integer verticalZones) {
		this.verticalZones = verticalZones;
	}

	public Double getZoneCenterLat() {
		return zoneCenterLat;
	}

	public void setZoneCenterLat(Double zoneCenterLat) {
		this.zoneCenterLat = zoneCenterLat;
	}

	public Double getZoneCenterLng() {
		return zoneCenterLng;
	}

	public void setZoneCenterLng(Double zoneCenterLng) {
		this.zoneCenterLng = zoneCenterLng;
	}

	public Integer getRvsPerTomcat() {
		return rvsPerTomcat;
	}
	
	public void setRvsPerTomcat(Integer rvsPerTomcat) {
		this.rvsPerTomcat = rvsPerTomcat;
	}
	
}
