package at.uni_salzburg.cs.ckgroup.cscpp.engine.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class VehicleLogConverter {
	
	private enum Action {
		upload, resume, suspend, position, unknown;
	}
	
	private enum Motion {
		physical, cyber, dead;
	}

	private static final Motion[][] automaton = {
				//   upload,          resume,          suspend,         position
/* physical */	{Motion.physical, Motion.physical, Motion.dead,     Motion.physical},
/* cyber */		{Motion.physical, Motion.physical, Motion.physical, Motion.physical},
/* dead */		{Motion.cyber,    Motion.physical, Motion.dead,     Motion.dead},
	};
	
	private Motion oldMotion = Motion.physical;

	private static final String positionPattern = "\\((-?\\d+.\\d+)\\s*,\\s*(-?\\d+.\\d+)\\s*,\\s*(-?\\d+)(.\\d+)?\\s*\\)";
	
	@SuppressWarnings("unchecked")
	public JSONArray convertToVirtualVehiclePath(String log) {
		JSONArray obj = new JSONArray();
		JSONObject lastEntry = null;
		
		String[] msgs = log.split("\\s*\\r?\\n\\s*");
		for (String m : msgs) {
			String[] parts = m.split("\\s+", 4);
			if (parts.length < 4) {
				continue;
			}
			Action action = Action.valueOf(parts[1]);
			if (action == Action.upload && lastEntry != null) {
				lastEntry.put("motion", Motion.cyber.toString());
			}
			
			Motion motion = automaton[oldMotion.ordinal()][action.ordinal()];
			
			if ("at".equalsIgnoreCase(parts[2])) {
				String[] posString = parts[3].replaceAll(positionPattern, "$1:$2:$3$4").split(":");
				JSONObject entry = new JSONObject();
				entry.put("latitude", posString[0]);
				entry.put("longitude", posString[1]);
				entry.put("motion", motion.toString());
				obj.add(entry);
				lastEntry = entry;
			}
			
			oldMotion = motion;
		}
		
		return obj;
	}

}
