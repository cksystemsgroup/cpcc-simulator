
var positionUrl = '/gmview/json/position';
var waypointsUrl = '/gmview/json/waypoints';
var vehicleUrl = '/gmview/json/virtualVehicle';
var map;
var markers = {};
var waypointPolylines = {};
var position = {};
var vehicles = {};
var loadWaypoints = true;

function updateMap() {

	var m;
	for (m in markers) {
		if (position[m]) {
			// do nothing
		} else {
			// obsolete one
			markers[m].setMap(null);
			loadWaypoints = true;
		}
	}
	
	for (m in position) {
		var a = position[m].position;
		var point = new google.maps.LatLng(a.latitude, a.longitude);
		if (markers[m]) {
			// update position
			markers[m].setPosition(point);
		} else {
			// new one!
			markers[m] = new PilotOverlay(point, map);
			markers[m].setPilotName(position[m].name);
			loadWaypoints = true;
		}
		markers[m].setPilotFlying(a.autoPilotFlight);
		markers[m].setVehicles(vehicles[m].vehicles);
	}
}

function updateWaypoints (waypoints) {
	for (m in waypoints) {
		var wp = waypoints[m].waypoints;
		var path = new Array();
		for (var k=0; k < wp.length; ++k) {
			path[k] = new google.maps.LatLng(wp[k].latitude, wp[k].longitude);
		}
		
		if (!waypointPolylines[m]) {
			// new one!
			waypointPolylines[m] = new google.maps.Polyline ({
				clickable: false,
				path: path,
				strokeColor: "#6C6C6C",
				strokeOpacity: 0.6,
				strokeWeight: 2
			});
		} else {
			waypointPolylines[m].setPath(path);
		}
		waypointPolylines[m].setMap(map);
	}
	
	for (m in waypointPolylines) {
		if (!waypoints[m]) {
			// obsolete one
			waypointPolylines[m].setMap(null);
		}
	}
	
}

function onLoad() {
	var center = 0;
	var zoomLevel = 0;
	var mapType = '';
	var elems = document.getElementsByTagName("input");
	for (var k=0; k < elems.length; k++) {
		if (elems[k].name.match (/center/))
			center = elems[k].value;
		if (elems[k].name.match (/zoomLevel/))
			zoomLevel = elems[k].value;
		if (elems[k].name.match (/mapType/))
			mapType = elems[k].value;
	}
	
	if (center) {
		var a = center.evalJSON();
		center = new google.maps.LatLng(a.y, a.x);
	} else {
		center = new google.maps.LatLng(47.821881, 13.040328);
	}
	
	if (zoomLevel)
		zoomLevel = zoomLevel.evalJSON();
	else
		zoomLevel = 17;
	
	var mapTypeName = mapType ? mapType.evalJSON() : '';

    var myOptions = {
      zoom: zoomLevel,
      center: center,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };

	map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	
	new PeriodicalExecuter(
		function() {
			new Ajax.Request(positionUrl,
			  {
			    method:'get',
			    onSuccess: function(transport){
			      position = transport.responseText.evalJSON();
			      updateMap();
			    },
			    onFailure: function(){ alert('Something went wrong...') }
			  });
		},
	1);
	
	new PeriodicalExecuter(
		function() {
			new Ajax.Request(vehicleUrl,
			  {
			    method:'get',
			    onSuccess: function(transport){
					vehicles = transport.responseText.evalJSON();
					updateMap();
			    },
			    onFailure: function(){ alert('Something went wrong...') }
			  });
		},
	1);
	
	new PeriodicalExecuter(
		function() {
			new Ajax.Request(waypointsUrl,
			  {
			    method:'get',
			    onSuccess: function(transport){
			    	if (loadWaypoints) {
						loadWaypoints = false;
						var waypoints = transport.responseText.evalJSON();
						updateWaypoints(waypoints);
				    }
			    },
			    onFailure: function(){ alert('Something went wrong...') }
			  });
		},
	1);
}


function GUnload() {
	//	alert("unload");
}




