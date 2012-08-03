package org.cpcc.interpreter.testing;

import org.cpcc.interpreter.runtime.base.PositonProvider;
import org.cpcc.interpreter.runtime.types.LatLngAlt;

public class StaticPositionProviderMock implements PositonProvider {

	private double mLat;
	private double mLng;
	private double mAlt;
	
	public StaticPositionProviderMock(double lat, double lng, double alt) {
		this.mLat = lat;
		this.mLng = lng;
		this.mAlt = alt;
	}
	
	@Override
	public LatLngAlt getCurrentPosition() {
		LatLngAlt position = new LatLngAlt(mLat, mLng, mAlt);
		return position;
	}

	@Override
	public Double getSpeedOverGround() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getCourseOverGround() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getAltitudeOverGround() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
