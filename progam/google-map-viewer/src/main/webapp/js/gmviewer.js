
var positionUrl = '/gmview/json/position';
var waypointsUrl = '/gmview/json/waypoints';
var map;
var markers = {};
var position = {};


function updateMap() {

	var m;
	for (m in markers) {
		if (position[m]) {
			// do nothing
		} else {
			// obsolete one
			map.removeOverlay(markers[m]);
		}
	}
	
	for (m in position) {
		var a = position[m].position;
		var point = new GLatLng(a.latitude, a.longitude);
		if (markers[m]) {
			// update position
			markers[m].setLatLng(point);
//			markers[m].openInfoWindowHtml(String(point));
		} else {
			// new one!
			markers[m] = new GMarker(point, {title: m});
			map.addOverlay(markers[m]);
//			markers[m].openInfoWindowHtml(m, {maxWidth: 50});
		}
	}
	
}

function onLoad() {
	if (!GBrowserIsCompatible())
		return;

	map = new GMap2(document.getElementById("map_canvas"));
	map.addControl(new GLargeMapControl());
	
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
		center = new GLatLng(a.y, a.x);
	} else {
		center = new GLatLng(47.821881, 13.040328);
	}
	
	if (zoomLevel)
		zoomLevel = zoomLevel.evalJSON();
	else
		zoomLevel = 17;
	
	var mapTypeName = mapType ? mapType.evalJSON() : '';
	if (mapTypeName == "Hybrid")
		map.setMapType(G_HYBRID_MAP);
	else if (mapTypeName == "Terrain")
		map.setMapType(G_PHYSICAL_MAP);
	else if (mapTypeName == "Satellite")
		map.setMapType(G_SATELLITE_MAP);
	else
		map.setMapType(G_NORMAL_MAP);
	
	map.setCenter(center, zoomLevel);
	map.setUIToDefault();

//	new Ajax.PeriodicalUpdater('position', positionUrl,
//	  {
//	    method: 'get',
//	    insertion: Element.update,
//	    frequency: 10,
//	    onSuccess: function(){ updateMap(); },
//	  });
	
	/*
	new Ajax.Request('/gmview/json/position',
	  {
	    method:'get',
	    onSuccess: function(transport){
	      var response = transport.responseText || "no response text";
	      alert("Success! \n\n" + response);
	    },
	    onFailure: function(){ alert('Something went wrong...') }
	  });
	*/
	
	
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







