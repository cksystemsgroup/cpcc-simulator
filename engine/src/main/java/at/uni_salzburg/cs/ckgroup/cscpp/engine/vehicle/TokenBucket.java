/*
 * @(#) TokenBucket.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle;


public class TokenBucket implements ITaskDelayAlgorithm {

	private double bucketInflowRate;	// token bucket inflow (c)
	private double numberOfTokens;		// number of tokens in bucket (nb)
	private double bucketSize;			// token bucket size (B)
	private IVirtualVehicle virtualVehicle;

	public TokenBucket(String initialValues, IVirtualVehicle virtualVehicle) {
		
		String[] v = initialValues.split(":");
		
		if (!"tokenBucket".equals(v[0])) {
			throw new IllegalArgumentException("expected 'tokenBucket' as first parameter.");
		}
		
		bucketInflowRate = Double.parseDouble(v[1]);
		numberOfTokens = Double.parseDouble(v[2]);
		bucketSize = Double.parseDouble(v[3]);
		
		this.virtualVehicle = virtualVehicle;
	}
	
	@Override
	public void tick() {
		if (numberOfTokens < bucketSize) {
			numberOfTokens += bucketInflowRate;
		}
		if (numberOfTokens > bucketSize) {
			numberOfTokens = bucketSize;
		}
	}

	@Override
	public boolean checkFreeResources(int tokens) {
		return tokens <= numberOfTokens;
	}

	@Override
	public void consumeResources(int tokens) {
		numberOfTokens -= tokens;
		virtualVehicle.saveProperties();
	}

	@Override
	public String getCurrentState() {
		StringBuilder b = new StringBuilder();
		b.append("tokenBucket:").append(bucketInflowRate).append(":").append(numberOfTokens).append(":").append(bucketSize);
		return b.toString();
	}

	
}
