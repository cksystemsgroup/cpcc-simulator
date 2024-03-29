
var positionUrl = '/gmview/json/position';
var waypointsUrl = '/gmview/json/waypoints';
var vehicleUrl = '/gmview/json/virtualVehicle';
var zonesUrl = '/gmview/json/zones';
var temperatureUrl = '/gmview/json/temperature';
var map;
var heatmap;
var markers = {};
var waypointPolylines = {};
var position = {};
var vehicles = {};
var pointsOfInterest = {};
var loadWaypoints = true;
var actionPointMap = {};
var vvPathMap = {};
var vvMovementMap = {};
var rvZones = new Array();
var mapRepositioned = false;
var infoWindow;

var motionToColorMap = {
		physical: {color: "green", visible: true},
		cyber: {color: "red", visible: true},
		dead: {color: "black", visible: false} 
}; 

function updateMap() {

	var m;
	for (m in markers) {
		if (position[m]) {
			// do nothing
		} else {
			// obsolete one
			markers[m].setMap(null);
			loadWaypoints = true;
			delete markers[m];
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
			markers[m] = new JAviatorOverlay(point, map);
			markers[m].setPilotName(position[m].name);
			loadWaypoints = true;
		}
		markers[m].setPilotFlying(a.autoPilotFlight);
		markers[m].setVehicles(vehicles[m] ? vehicles[m].vehicles : null);
	}
	
}

function updatePointsOfInterest () {
	
	var vehicleMap = {};
	for (id in vehicles) {
		var vs = vehicles[id].vehicles;
		for (v in vs) {
			var state = vs[v].state;
			var vid = vs[v]["vehicle.id"];
			var vin = vs[v];
			if (state == "active") {
				vehicleMap[vid] = vin;
			}
		}
	}
	
	for (id in pointsOfInterest) {
		if (vehicleMap[id]) {
			// do nothing
		} else {
			// obsolete one
			pointsOfInterest[id].setMap(null);
			delete pointsOfInterest[id];
		}
	}
	
	for (id in vehicleMap) {
		var v = vehicleMap[id];
		var point = new google.maps.LatLng(v.latitude, v.longitude);
		if ( pointsOfInterest[id] ) {
			// update position
			pointsOfInterest[id].setPosition(point);
			pointsOfInterest[id].setVehicle(v);
			pointsOfInterest[id].show();
		} else {
			// new one!
			pointsOfInterest[id] = new VvPointOverlay(point, map);
			pointsOfInterest[id].setVehicle(v);
			pointsOfInterest[id].show();
		}
	}
}

function updateVvPaths() {
	
	var vvIds = new Array();
	for (id in vehicles) {
		var vs = vehicles[id].vehicles;
		for (v in vs) {
			vvIds[v] = true;
			var aps = vs[v].actionPoints;
			var completedPath = new Array();
			var activePath = new Array();
			var lastCompleted = false;
			for (var k=0; k < aps.length; ++k) {
				var ap = new google.maps.LatLng(aps[k].latitude, aps[k].longitude);
				if (aps[k].completed) {
					completedPath.push(ap);
				} else {
					if (lastCompleted) {
						lastCompleted = false;
						completedPath.push(ap);
					}
					activePath.push(ap);
				}
				lastCompleted = aps[k].completed;
			}
			
			if (!vvPathMap[v]) {
				vvPathMap[v] = {completed: null, active: null}; 
			}
			
			if (completedPath.length > 0 && vvPathMap[v].completed) {
				vvPathMap[v].completed.setPath(completedPath);
			} else if (completedPath.length > 0 && !vvPathMap[v].completed) {
				vvPathMap[v].completed = new google.maps.Polyline ({
					clickable: false,
					map: map,
					path: completedPath,
					strokeColor: "#0090FF",	// #FF0000
					strokeOpacity: 1,
					strokeWeight: 2
				});
			} else if (completedPath.length == 0 && vvPathMap[v].completed) {
				vvPathMap[v].completed.setVisible(false);
				vvPathMap[v].completed.setMap(null);
				vvPathMap[v].completed = null;
			}
			
			if (activePath.length > 0 && vvPathMap[v].active) {
				vvPathMap[v].active.setPath(activePath);
			} else if (activePath.length > 0 && !vvPathMap[v].active) {
				vvPathMap[v].active = new google.maps.Polyline ({
					clickable: false,
					map: map,
					path: activePath,
					strokeColor: "#0090FF",
					strokeOpacity: 1,
					strokeWeight: 2
				});
			} else if (activePath.length == 0 && vvPathMap[v].active) {
				vvPathMap[v].active.setVisible(false);
				vvPathMap[v].active.setMap(null);
				vvPathMap[v].active = null;
			}
		}
	}
	
	for (id in vehicles) {
		var vs = vehicles[id].vehicles;
		var engineUrl = vehicles[id].engine;
		var vehicleDataUrl = vehicles[id].vehicleData;
		for (v in vs) {
			var aps = vs[v].actionPoints;
			if (!actionPointMap[v]) {
				actionPointMap[v] = new Array();
			}
			for (var k=0; k < aps.length; ++k) {
				var ap = new google.maps.LatLng(aps[k].latitude, aps[k].longitude);
				if (!actionPointMap[v][k]) {
					actionPointMap[v][k] = new ActionPointOverlay(ap, map, aps[k].completed, engineUrl, vehicleDataUrl, v, k, infoWindow);
				} else {
					actionPointMap[v][k].setComplete(aps[k].completed);
				}
			}
			
			if (!vvMovementMap[v]) {
				vvMovementMap[v] = new Array();
			}
			var vp = vs[v].vehiclePath;
			for (var k=vvMovementMap[v].length, l=vp.length-1; k < l; ++k) {
				var col = motionToColorMap[vp[k].motion];
				if (!col || !col.visible) {
					vvMovementMap[v][k] = null;
					continue;
				}
				var oap = new google.maps.LatLng(vp[k].latitude, vp[k].longitude);
				var nap = new google.maps.LatLng(vp[k+1].latitude, vp[k+1].longitude);
				vvMovementMap[v][k] = new google.maps.Polyline ({
					clickable: false,
					map: map,
					path: [oap, nap],
					strokeColor: col.color,
					strokeOpacity: 1,
					strokeWeight: 2
				});
			}
		}
	}
	
	for (v in vvPathMap) {
		if (vvIds[v]) continue;
		if (vvPathMap[v].completed) {
			vvPathMap[v].completed.setMap(null);
		}
		if (vvPathMap[v].active) {
			vvPathMap[v].active.setMap(null);
		}
		delete vvPathMap[v];
		for (var k=0, l=actionPointMap[v].length; k < l; ++k) {
			actionPointMap[v][k].setMap(null);
		}
		delete actionPointMap[v];
		for (var k=0, l=vvMovementMap[v].length; k < l; ++k) {
			if (!vvMovementMap[v][k]) {
				continue;
			}
			vvMovementMap[v][k].setVisible(false);
			vvMovementMap[v][k].setMap(null);
		}
		delete vvMovementMap[v];
	}
}


function updateWaypoints (waypoints) {
	for (m in waypoints) {
		var wp = waypoints[m].waypoints;
		var path = new Array();
		for (var k=0; k < wp.length; ++k) {
			path[k] = new google.maps.LatLng(wp[k].latitude, wp[k].longitude);
		}
		
		if (wp.length == 1 && position[m]) {
			var a = position[m].position;
			path[1] = new google.maps.LatLng(a.latitude, a.longitude);			
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


function updateZones(zones) {
	
	var newZones = new Array();
	
	for (var k=0, l=rvZones.length; k < l; ++k) {
		rvZones[k].setActive(false);
	}
	
	for (var k=0, zl=zones.length; k < zl; ++k) {
		var found = false;
		for (var i=0, rl=rvZones.length; i < rl; ++i) {
			if (rvZones[i].isEquivalent(zones[k])) {
				rvZones[i].setActive(true);
				found = true;
			}
		}
		if (!found) {
			newZones.push(zones[k]);
		}
	}
	
	for (var k=rvZones.length-1; k >= 0; --k) {
		if (!rvZones[k].getActive()) {
			var ovl = rvZones.splice(k,1);
			ovl.hide();
			ovl.setMap(null);
		}
	}
	
	for (var k=0, l=newZones.length; k < l; ++k) {
		var ovl = new ZoneOverlay(newZones[k], map);
		rvZones.push(ovl);
	}
	
	if (!mapRepositioned && rvZones.length > 0) {
		var bounds = rvZones[0].getBounds();
		
		for (var k=1, l=rvZones.length; k < l; ++k) {
			bounds.union(rvZones[k].getBounds());
		}
		
		map.setCenter(bounds.getCenter());
		mapRepositioned = true;
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
	
	infoWindow = new google.maps.InfoWindow();
	
	Ajax.Responders.register({ 
        onException: function(request, exception) { 
        	(function() { throw exception; }).defer(); 
        } 
	});
	
	heatmap = new Heatmap(temperatureUrl, map);
	
	new PeriodicalExecuter(
		function() {
			new Ajax.Request(positionUrl,
			  {
			    method:'get',
			    onSuccess: function(transport){
			      position = transport.responseText.evalJSON();
			      updateMap();
			    },
			    onFailure: function(){ alert('Something went wrong 1 ...') },
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
					updatePointsOfInterest();
					updateVvPaths();
			    },
			    onFailure: function(){ alert('Something went wrong 2 ...') },
			  });
		},
	1);
	
	new PeriodicalExecuter(
		function() {
			new Ajax.Request(waypointsUrl,
			  {
			    method:'get',
			    onSuccess: function(transport){
					var waypoints = transport.responseText.evalJSON();
					updateWaypoints(waypoints);
			    },
			    onFailure: function(){ alert('Something went wrong 3 ...') },
			  });
		},
	1);
	
	new PeriodicalExecuter(
		function(pe) {
			new Ajax.Request(zonesUrl,
			  {
			    method:'get',
			    onSuccess: function(transport){
					var zones = transport.responseText.evalJSON();
					updateZones(zones);
//					if (zones) {
//						pe.stop();
//			    	}
			    },
			    onFailure: function(){ alert('Something went wrong 4 ...') },
			  });
		},
	1);
}


function GUnload() {
	//	alert("unload");
}


function openImageWindow (url) {
	win = window.open(url, "Popupwindow", "resizable=yes");
	win.focus();
	return false;
} 

function toggleHeatmap() {
	heatmap.toggle();
}

