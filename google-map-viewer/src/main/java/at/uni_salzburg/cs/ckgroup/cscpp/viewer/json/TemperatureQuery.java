package at.uni_salzburg.cs.ckgroup.cscpp.viewer.json;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.EngineInfo;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.IMapperProxy;

public class TemperatureQuery implements IJsonQuery {
	
    private static DecimalFormat df = new DecimalFormat();
    private static DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	
	private static final Logger LOG = LoggerFactory.getLogger(TemperatureQuery.class);
	
	private IMapperProxy mapperProxy;
	private JSONParser parser = new JSONParser();
	
	public TemperatureQuery(IMapperProxy mapperProxy) {
		this.mapperProxy = mapperProxy;
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String execute(IServletConfig config, String[] parameters)
			throws IOException {
		// TODO Auto-generated method stub
		
//		for (String s : parameters) {
//			System.out.println("bb: " + s);
//		}
		
		double northEastLat;
		double northEastLng;
		double southWestLat;
		double southWestLng;
		
		try {
			northEastLat = df.parse(parameters[3]).doubleValue();
			northEastLng = df.parse(parameters[4]).doubleValue();
			southWestLat = df.parse(parameters[5]).doubleValue();
			southWestLng = df.parse(parameters[6]).doubleValue();
			
		} catch (ParseException e) {
			LOG.error("Parameter parsing: ", e);
			return "[]";
		}

		
		
		
//		bounds.getNorthEast().lat() + '/' + bounds.getNorthEast().lng() + '/' +
//		bounds.getSouthWest().lat() + '/' + bounds.getSouthWest().lng();
		
//		bb: 
//		bb: json
//		bb: temperature
//		bb: 47.69459883668643
//		bb: 13.39110364704652
//		bb: 47.690265823307
//		bb: 13.381554982953503
		
		if (mapperProxy.getEngineInfoList() == null) {
			return "";
		}
		
		JSONArray a = new JSONArray();
		
		for (EngineInfo engineInfo : mapperProxy.getEngineInfoList()) {
			String pilot = engineInfo.getPilotName();
			String temperatureString = null;
			try {
				temperatureString = HttpQueryUtils.simpleQuery(engineInfo.getTemperatureUrl());
				JSONArray x = (JSONArray) parser.parse(temperatureString);
				a.addAll(x);
			} catch (Exception e) {
				LOG.info("Can not query pilot " + pilot + ": " + temperatureString, e);
			}
		}
		
		return a.toJSONString();
	}

}
