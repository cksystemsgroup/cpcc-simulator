/*
 * @(#) RandomTaskGenerator.java
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
package at.uni_salzburg.cs.ckgroup.cpcc.mapper.algorithms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleStatus;

public class RandomTaskGenerator implements IMappingAlgorithm {
	
	private static final Logger LOG = LoggerFactory.getLogger(RandomTaskGenerator.class);
	
	private Map<String, VirtualVehicleInfo> vvInfo = new HashMap<String, RandomTaskGenerator.VirtualVehicleInfo>();

	private double minLatitude;

	private double maxLatitude;

	private double minLongitude;

	private double maxLongitude;

	private double minAltitude;

	private double maxAltitude;

	private int actionPointsPerVirtualVehicle;

	private double actionPointsArrivalRate;
	
	private IGeodeticSystem geodeticSystem = new WGS84();

	private long minimumIdleCyclesBeforeExport;
	
	private long idleCycles = 0;

	private String exportFileName;

	private File exportFile;

	private boolean tokenBucketDisabled;
	
	
	public RandomTaskGenerator() {
		// TODO get from properties file
		this.minLatitude = 47.69018658;
		this.maxLatitude = 47.69467816;
		this.minLongitude = 13.38173950;
		this.maxLongitude = 13.38841213;
		this.minAltitude = 1.0;
		this.maxAltitude = 1.0;
		this.actionPointsPerVirtualVehicle = 100;
		this.actionPointsArrivalRate = 0.1;
		this.minimumIdleCyclesBeforeExport = 10;
		this.exportFileName = "/tmp/vvRandomTaskExport.txt";
		this.exportFile = new File(this.exportFileName);
		this.tokenBucketDisabled = false;
	}
	
	
	@Override
	public void execute(IMapper mapper) {
		
		tokenBucketTick();

		boolean idle = true;
		
		List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
		
		for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
			IVirtualVehicleStatus s = vehicleInfo.getVehicleStatus();
			String id = s.getId();
			
			VirtualVehicleInfo vvi = vvInfo.get(id);
			if (vvi == null) {
				vvi = new VirtualVehicleInfo(actionPointsPerVirtualVehicle, actionPointsArrivalRate);
				vvi.bucket = new TokenBucket(tokenBucketDisabled);
				vvInfo.put(id, vvi);
				idle = false;
			}
			
			ActionPoint ap = vvi.getNextActionPoint();
			if (ap != null) {
				idle = false;
				try {
					addActionPointToVirtualVehicle(vehicleInfo, ap);
				} catch (IOException e) {
					e.printStackTrace();
					LOG.error("Bugger in addActionPointToVirtualVehicle", e);
				}
			}
		}
		
		if (idle) {
			if (idleCycles <= minimumIdleCyclesBeforeExport) {
				++idleCycles;
			}
		} else {
			idleCycles = 0;
		}
		
		if (idleCycles > minimumIdleCyclesBeforeExport && !exportFile.exists()) {
			exportStatus(mapper);
		}
	}

	private void exportStatus(IMapper mapper) {
		LOG.info("Exporting VVs to file " + exportFile.getAbsolutePath());
		
		try {
			PrintWriter w = new PrintWriter(exportFile);
			w.print("VV;ACTION_POINT_NUMBER;LATITUDE;LONGITUDE;ALTITUDE;DISTANCE;ARRIVAL_TIME;ARRIVAL_TIME_DIFF;BUCKET_PASSED_TIME;BUCKET_DELAY;COMPLETED_TIME;DURATION\n");
			
			List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
			
			for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
				try {
					mergeVehicleData(vehicleInfo);
				} catch (Throwable e) {
					e.printStackTrace();
					LOG.error("Bugger in exportStatus 1", e);
				}
				
				IVirtualVehicleStatus s = vehicleInfo.getVehicleStatus();
				String vvId = s.getId();

				VirtualVehicleInfo info = vvInfo.get(vvId);
				
				int k=0;
				ActionPoint oldAp = null;
				for (ActionPoint ap : info.actionPoints) {
					long bucketDelay = ap.bucketPassedTime - ap.arrivalTime;
					String bucketDelayString = bucketDelay > 0 ? String.format(Locale.US, "%d", bucketDelay) : "";
					String distanceString = "";
					String arrivalTimeDiffString = "";
					if (oldAp != null) {
						CartesianCoordinate X0 = geodeticSystem.polarToRectangularCoordinates(oldAp.position);
						CartesianCoordinate X1 = geodeticSystem.polarToRectangularCoordinates(ap.position);
						double distance = X0.subtract(X1).norm();
						distanceString = String.format(Locale.US, "%.2f", distance);
						arrivalTimeDiffString = String.format(Locale.US, "%d", ap.arrivalTime - oldAp.arrivalTime);
					}
					String completedTimeString = "";
					String durationString = "";
					if (ap.completedTime > 0) {
						completedTimeString = String.format(Locale.US, "%d", ap.completedTime);
						durationString = String.format(Locale.US, "%d", ap.completedTime - ap.arrivalTime);
					}
					
					w.printf(Locale.US, "\"%s\";%d;%.8f;%.8f;%.3f;%s;%d;%s;%d;%s;%s;%s\n",
						vvId,
						k++,
						ap.position.getLatitude(),
						ap.position.getLongitude(),
						ap.position.getAltitude(),
						distanceString,
						ap.arrivalTime,
						arrivalTimeDiffString,
						ap.bucketPassedTime,
						bucketDelayString,
						completedTimeString,
						durationString
					);
					oldAp = ap;
				}
				
			}
			
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("Bugger in exportStatus 2", e);
		}

	}
	
	private void mergeVehicleData (IVirtualVehicleInfo vehicleInfo) throws IOException, IllegalStateException, ParseException {
		
		String url = vehicleInfo.getEngineUrl() + "/json/vehicleDetails/" + vehicleInfo.getVehicleName();
		
		HttpGet httpGet = new HttpGet(url);
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpGet);
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			JSONParser parser = new JSONParser();
			JSONObject vv = (JSONObject) parser.parse(new InputStreamReader(entity.getContent()));
			
			String vvId = (String) vv.get("vehicle.id");
			VirtualVehicleInfo info = vvInfo.get(vvId);
			if (info == null) {
				LOG.error("Virtual Vehicle " + vvId + " not known! URL=" + url);
				return;
			}
			
			JSONArray tasks = (JSONArray) vv.get("tasks");
			int k=0;
			for (ActionPoint ap : info.actionPoints) {
				if (k >= tasks.size()) {
					break;
				}
				JSONObject actionPoint = (JSONObject) tasks.get(k++);
				JSONArray actions = (JSONArray) actionPoint.get("actions");
				
				long time = 0;
				for (int j=0; j < actions.size(); ++j) {
					JSONObject a = (JSONObject) actions.get(j);
					Long newTime = (Long) a.get("time");
					if (newTime != null && newTime > time) {
						time = newTime;
					}
				}
				
				if (time > 0) {
					ap.completedTime = time;
				}
			}
		}
	}

	private void addActionPointToVirtualVehicle(IVirtualVehicleInfo vehicleInfo, ActionPoint ap) throws IOException {
		
		String url = vehicleInfo.getEngineUrl() + "/vehicle/text/vehicleAddTask/" + vehicleInfo.getVehicleName();
		
		HttpPost httppost = new HttpPost(url);
		BasicHttpEntity entity1 = new BasicHttpEntity();
		entity1.setContent(ap.getInputStream());
		httppost.setEntity(entity1);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httppost);

		LOG.info("Add AP: " + response.getStatusLine());
	}

	private void tokenBucketTick() {
		for (Entry<String, VirtualVehicleInfo> entry : vvInfo.entrySet()) {
			entry.getValue().bucket.tick();
		}
	}


	private class VirtualVehicleInfo {
		public TokenBucket bucket = null;
		public List<ActionPoint> actionPoints = new ArrayList<RandomTaskGenerator.ActionPoint>();
		public int lastProcessedActionPoint = -1;
		
		public VirtualVehicleInfo(int apsToGenerate, double lambda) {
			long now = System.currentTimeMillis();
			for (int k=0; k < apsToGenerate; ++k) {
				ActionPoint ap = new ActionPoint(now);
				ap.position = generatePosition();
				actionPoints.add(ap);
				now += (Math.random() * 2.0 * 1000.0 / lambda);
			}
		}
		
		private PolarCoordinate generatePosition() {
			double latitude = minLatitude + Math.random() * (maxLatitude - minLatitude);
			double longitude = minLongitude + Math.random() * (maxLongitude - minLongitude);
			double altitude = minAltitude + Math.random() * (maxAltitude - minAltitude);
			return new PolarCoordinate(latitude, longitude, altitude);
		}
		
		public ActionPoint getNextActionPoint () {
			int l = lastProcessedActionPoint + 1;
			if (l >= actionPoints.size()) {
				return null;
			}
			
			long now = System.currentTimeMillis();
			
			ActionPoint ap = actionPoints.get(l);
			if (ap.arrivalTime >= now) {
				return null;
			}

//			The first has free pass, it just passes the bucket as though the bucket does not exists, i.e. A1_1 = A2_1.
			
			if (l == 0) {
				++lastProcessedActionPoint;
				ap.bucketPassedTime = now;
				return ap;
			}

//			For i >1, calculate V1_i = (X_i - X_(i-1)) / (A1_i - A2_(i-1)),
			
			ActionPoint oldAp = actionPoints.get(l-1);
			
			CartesianCoordinate X0 = geodeticSystem.polarToRectangularCoordinates(oldAp.position);
			CartesianCoordinate X1 = geodeticSystem.polarToRectangularCoordinates(ap.position);
			
			double distance = X0.subtract(X1).norm();
			
			double v1 = 1000.0 * distance / (now - oldAp.bucketPassedTime);
			
//			if V1_i <= nb, the i-th task passes the bucket, A1_i = A2_i, V1_i tokens are consumed.

			if (bucket.checkFreeTokens((int)v1)) {
				bucket.consumeTokens((int)v1);
				++lastProcessedActionPoint;
				ap.bucketPassedTime = now;
				return ap;
			}
			
//			If V1_i > nb, increase A2_i until V2_i = (X_i - X_(i-1)) / (A2_i - A2_(i-1)) <= nb, then the tasks passes the bucket.
			
			return null;
		}
	}
	
	private class ActionPoint {
		public PolarCoordinate position = null;
		public long arrivalTime;
		public long bucketPassedTime = 0;
		public long completedTime = 0;
		
		public ActionPoint(long arrivalTime) {
			this.arrivalTime = arrivalTime;
		}
		
		public InputStream getInputStream() {
//			Point 47.69271040 13.38747686 20.00 tolerance 5 Picture Temperature Airpressure
//			String v = String.format(Locale.US, "Point %.8f %.8f %.3f Tolerance 5 Picture Temperature Airpressure", position.getLatitude(), position.getLongitude(), position.getAltitude());
			String v = String.format(Locale.US, "Point %.8f %.8f %.3f Tolerance 5 Temperature", position.getLatitude(), position.getLongitude(), position.getAltitude());
			return new ByteArrayInputStream(v.getBytes());
		}
	}
	
	private class TokenBucket {
		private double c;
		private double nb;
		private double B;
		private boolean passThrough;
		
		public TokenBucket(boolean passThrough) {
			c = 0.1;
			nb = 1;
			B = 1;
			this.passThrough = passThrough;
		}
		
		public void tick() {
			if (nb < B) nb += c;
			if (nb > B) nb = B;
		}
		
		public boolean checkFreeTokens(int tokens) {
			return passThrough ? true : tokens <= nb;
		}
		
		public void consumeTokens(int tokens) {
			if (passThrough) {
				return;
			}
			nb -= tokens;
		}
	}
	
}
