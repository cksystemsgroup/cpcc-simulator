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
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleStatus;

public class RandomTaskGenerator implements IMappingAlgorithm {
	
	private static final Logger LOG = Logger.getLogger(RandomTaskGenerator.class);
	
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
	
	
	public RandomTaskGenerator() {
		// TODO get from properties file
		this.minLatitude = 47.69163076;
		this.maxLatitude = 47.69323399;
		this.minLongitude = 13.38507593;
		this.maxLongitude = 13.38692824;
		this.minAltitude = 1.0;
		this.maxAltitude = 1.0;
		this.actionPointsPerVirtualVehicle = 10;
		this.actionPointsArrivalRate = 0.1;
		this.minimumIdleCyclesBeforeExport = 10;
		this.exportFileName = "/tmp/vvRandomTaskExport.txt";
		this.exportFile = new File(this.exportFileName);
	}
	
	
	@Override
	public void execute(IMapper mapper) {
		// TODO Auto-generated method stub
		
		tokenBucketTick();

		boolean idle = true;
		
		List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
		
		for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
			IVirtualVehicleStatus s = vehicleInfo.getVehicleStatus();
			String id = s.getId();
			
			VirtualVehicleInfo vvi = vvInfo.get(id);
			if (vvi == null) {
				vvi = new VirtualVehicleInfo(actionPointsPerVirtualVehicle, actionPointsArrivalRate);
				vvi.bucket = new TokenBucket();
				vvInfo.put(id, vvi);
				idle = false;
			}
			
			ActionPoint ap = vvi.getNextActionPoint();
			if (ap != null) {
				idle = false;
				try {
					addActionPointToVirtualVehicle(vehicleInfo, ap);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
			w.print("VV;ACTION_POINT_NUMBER;LATITUDE;LONGITUDE;ALTITUDE;DISTANCE;ARRIVAL_TIME;BUCKET_PASSED_TIME;BUCKET_DELAY;COMPLETED_TIME;DURATION\n");
//			
//			for (Entry<String, VirtualVehicleInfo> entry : vvInfo.entrySet()) {
//				String vvId = entry.getKey();
//				VirtualVehicleInfo info = entry.getValue();
//				int k=0;
//				ActionPoint oldAp = null;
//				for (ActionPoint ap : info.actionPoints) {
//					long duration = ap.bucketPassedTime - ap.arrivalTime;
//					String durationString = duration > 0 ? String.format(Locale.US, "%d", duration) : "";
//					String distanceString = "";
//					if (oldAp != null) {
//						CartesianCoordinate X0 = geodeticSystem.polarToRectangularCoordinates(oldAp.position);
//						CartesianCoordinate X1 = geodeticSystem.polarToRectangularCoordinates(ap.position);
//						double distance = X0.subtract(X1).norm();
//						distanceString = String.format(Locale.US, "%.2f", distance);
//					}
//					
//					w.printf(Locale.US, "\"%s\";%d;%.8f;%.8f;%.3f;%s;%d;%d;%s;;\n",
//						vvId,
//						k++,
//						ap.position.getLatitude(),
//						ap.position.getLongitude(),
//						ap.position.getAltitude(),
//						distanceString,
//						ap.arrivalTime,
//						ap.bucketPassedTime,
//						durationString
//					);
//					oldAp = ap;
//				}
//			}
			
			List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
			
			for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
				try {
					mergeVehicleData(vehicleInfo);
				} catch (Throwable e) {
					e.printStackTrace();
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
					if (oldAp != null) {
						CartesianCoordinate X0 = geodeticSystem.polarToRectangularCoordinates(oldAp.position);
						CartesianCoordinate X1 = geodeticSystem.polarToRectangularCoordinates(ap.position);
						double distance = X0.subtract(X1).norm();
						distanceString = String.format(Locale.US, "%.2f", distance);
					}
					String completedTimeString = "";
					String durationString = "";
					if (ap.completedTime > 0) {
						completedTimeString = String.format(Locale.US, "%d", ap.completedTime);
						durationString = String.format(Locale.US, "%d", ap.completedTime - ap.arrivalTime);
					}
					
					w.printf(Locale.US, "\"%s\";%d;%.8f;%.8f;%.3f;%s;%d;%d;%s;%s;%s\n",
						vvId,
						k++,
						ap.position.getLatitude(),
						ap.position.getLongitude(),
						ap.position.getAltitude(),
						distanceString,
						ap.arrivalTime,
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
		
//		{"name":"vehicle6114069914778644392",
//		"state":"active",
//		"tasks":[{"point":{"altitude":1.0,"longitude":13.38559184,"latitude":47.69169044},"tolerance":5.0,"actions":[{"time":1344197075483,"value":"img7801151936966193050.png","type":"photo"},{"time":1344197075489,"value":-13.5,"type":"temperature"},{"time":1344197075491,"value":1098.1,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38608991,"latitude":47.69181233},"tolerance":5.0,"actions":[{"time":1344197091892,"value":"img5125197823647211299.png","type":"photo"},{"time":1344197091898,"value":14.7,"type":"temperature"},{"time":1344197091900,"value":1092.5,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38551236,"latitude":47.6928778},"tolerance":5.0,"actions":[{"time":1344197153848,"value":"img995208744309139634.png","type":"photo"},{"time":1344197153854,"value":14.5,"type":"temperature"},{"time":1344197153858,"value":1088.9,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38519199,"latitude":47.69264412},"tolerance":5.0,"actions":[{"time":1344197170845,"value":"img6708509824137717687.png","type":"photo"},{"time":1344197170851,"value":-14.1,"type":"temperature"},{"time":1344197170854,"value":1083.8,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38538198,"latitude":47.69175718},"tolerance":5.0,"actions":[{"time":1344197228652,"value":"img6337382404544854778.png","type":"photo"},{"time":1344197228658,"value":7.5,"type":"temperature"},{"time":1344197228661,"value":1084.9,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38527799,"latitude":47.69256103},"tolerance":5.0,"actions":[{"time":1344197274160,"value":"img8551128644815870353.png","type":"photo"},{"time":1344197274166,"value":-4.7,"type":"temperature"},{"time":1344197274168,"value":1090.6,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38622913,"latitude":47.69211756},"tolerance":5.0,"actions":[{"time":1344197314083,"value":"img5603729638838145916.png","type":"photo"},{"time":1344197314088,"value":30.8,"type":"temperature"},{"time":1344197314091,"value":1084.7,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38585899,"latitude":47.69290802},"tolerance":5.0,"actions":[{"time":1344197355581,"value":"img7147246186003180169.png","type":"photo"},{"time":1344197355587,"value":1.1,"type":"temperature"},{"time":1344197355590,"value":1100.0,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38628099,"latitude":47.69188205},"tolerance":5.0,"actions":[{"time":1344197414291,"value":"img2693434768581985885.png","type":"photo"},{"time":1344197414297,"value":34.4,"type":"temperature"},{"time":1344197414303,"value":1085.3,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38689774,"latitude":47.69185787},"tolerance":5.0,"actions":[{"time":1344197435577,"value":"img8507869722737293927.png","type":"photo"},{"time":1344197435580,"value":32.3,"type":"temperature"},{"time":1344197435582,"value":1097.8,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38651746,"latitude":47.69230793},"tolerance":5.0,"actions":[{"time":1344198340384,"value":"img5750901791177347635.png","type":"photo"},{"time":1344198340389,"value":0.6,"type":"temperature"},{"time":1344198340393,"value":1095.7,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38596374,"latitude":47.69300401},"tolerance":5.0,"actions":[{"time":1344198371913,"value":"img9218789659199794553.png","type":"photo"},{"time":1344198371916,"value":1.1,"type":"temperature"},{"time":1344198371919,"value":1080.8,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.385993,"latitude":47.69188915},"tolerance":5.0,"actions":[{"time":1344198428745,"value":"img4894071846093504132.png","type":"photo"},{"time":1344198428749,"value":21.4,"type":"temperature"},{"time":1344198428751,"value":1090.1,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38591102,"latitude":47.69168916},"tolerance":5.0,"actions":[{"time":1344198442755,"value":"img4603493711831732714.png","type":"photo"},{"time":1344198442758,"value":16.7,"type":"temperature"},{"time":1344198442760,"value":1083.9,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38654427,"latitude":47.69210933},"tolerance":5.0,"actions":[{"time":1344198476375,"value":"img7530560669447343840.png","type":"photo"},{"time":1344198476379,"value":8.0,"type":"temperature"},{"time":1344198476381,"value":1080.3,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.3865473,"latitude":47.693226},"tolerance":5.0,"actions":[{"time":1344198542645,"value":"img4886348850730469294.png","type":"photo"},{"time":1344198542648,"value":-0.2,"type":"temperature"},{"time":1344198542650,"value":1098.2,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38533577,"latitude":47.69277428},"tolerance":5.0,"actions":[{"time":1344198598365,"value":"img6720185711380610805.png","type":"photo"},{"time":1344198598368,"value":31.9,"type":"temperature"},{"time":1344198598370,"value":1096.7,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38541659,"latitude":47.69195582},"tolerance":5.0,"actions":[{"time":1344198644947,"value":"img5390925962609468658.png","type":"photo"},{"time":1344198644953,"value":8.2,"type":"temperature"},{"time":1344198644958,"value":1080.5,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.3858172,"latitude":47.69234017},"tolerance":5.0,"actions":[{"time":1344198665946,"value":"img6668786785656101243.png","type":"photo"},{"time":1344198665952,"value":31.5,"type":"temperature"},{"time":1344198665958,"value":1098.7,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38555287,"latitude":47.6921492},"tolerance":5.0,"actions":[{"time":1344198680954,"value":"img3174597268956247789.png","type":"photo"},{"time":1344198680959,"value":-7.1,"type":"temperature"},{"time":1344198680965,"value":1087.3,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38511182,"latitude":47.69255631},"tolerance":5.0,"actions":[{"time":1344200180465,"value":"img21912120568943139.png","type":"photo"},{"time":1344200180469,"value":3.2,"type":"temperature"},{"time":1344200180472,"value":1084.6,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38613564,"latitude":47.69191406},"tolerance":5.0,"actions":[{"time":1344200212273,"value":"img5031767841469035773.png","type":"photo"},{"time":1344200212278,"value":15.2,"type":"temperature"},{"time":1344200212280,"value":1098.8,"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.38605557,"latitude":47.69261408},"tolerance":5.0,"actions":[{"type":"photo"},{"type":"temperature"},{"type":"airPressure"}]},
//		         {"point":{"altitude":1.0,"longitude":13.3855217,"latitude":47.69311234},"tolerance":5.0,"actions":[{"type":"photo"},{"type":"temperature"},{"type":"airPressure"}]}
//		       ],
//         "vehicle.id":"VV 0002"}
	
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

//		ByteArrayOutputStream bo = new ByteArrayOutputStream();
//		
//		HttpEntity entity = response.getEntity();
//		if (entity != null) {
//			InputStream instream = entity.getContent();
//			int l;
//			byte[] tmp = new byte[2048];
//			while ((l = instream.read(tmp)) != -1) {
//				bo.write(tmp, 0, l);
//			}
//		}
//
//		bo.toString();
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
				now += (Math.random() * lambda * 2.0 * 1000.0);
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
			String v = String.format(Locale.US, "Point %.8f %.8f %.3f Tolerance 5 Picture Temperature Airpressure", 
					position.getLatitude(), position.getLongitude(), position.getAltitude());
			return new ByteArrayInputStream(v.getBytes());
		}
	}
	
	private class TokenBucket {
		public double c;
		public double nb;
		public double B;
		
		public TokenBucket() {
			c = 0.1;
			nb = 1;
			B = 1;
		}
		
		public void tick() {
			if (nb < B) nb += c;
			if (nb > B) nb = B;
		}
		
		public boolean checkFreeTokens(int tokens) {
			return tokens <= nb;
		}
		
		public void consumeTokens(int tokens) {
			nb -= tokens;
		}
	}
	
}
