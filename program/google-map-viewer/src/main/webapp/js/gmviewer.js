
var positionUrl = '/gmview/json/position';
var waypointsUrl = '/gmview/json/waypoints';
var map;
var markers = {};
//var pilots = {};
var position = {};


function updateMap() {

	var m;
	for (m in markers) {
		if (position[m]) {
			// do nothing
		} else {
			// obsolete one
//			map.removeOverlay(markers[m]);
			markers[m].setMap(null);
		}
	}
	
	for (m in position) {
		var a = position[m].position;
		var point = new google.maps.LatLng(a.latitude, a.longitude);
		if (markers[m]) {
			// update position
			markers[m].setPosition(point);
//			pilots[m].setPosition(point);
//			markers[m].openInfoWindowHtml(String(point));
		} else {
			// new one!
//			markers[m] = new google.maps.Marker({position: point, title: m, map: map});
//			pilots[m] = new PilotOverlay(point, map);
			markers[m] = new PilotOverlay(point, map);
//			map.addOverlay(markers[m]);
//			markers[m].openInfoWindowHtml(m, {maxWidth: 50});
//			markers[m].setMap(map);
		}
	}
	
}

function onLoad() {
//	if (!google.maps.BrowserIsCompatible())
//		return;

//	map.addControl(new google.maps.LargeMapControl());
	
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
//	if (mapTypeName == "Hybrid")
//		map.setMapType(google.maps.MapTypeId.HYBRID);
//	else if (mapTypeName == "Terrain")
//		map.setMapType(google.maps.MapTypeId.TERRAIN);
//	else if (mapTypeName == "Satellite")
//		map.setMapType(google.maps.MapTypeId.SATELLITE);
//	else
//		map.setMapType(google.maps.MapTypeId.ROADMAP);
	
//	map.setCenter(center, zoomLevel);
//	map.setUIToDefault();

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
	
}


function GUnload() {
//	alert("unload");
}




