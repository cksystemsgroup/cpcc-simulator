
var map;
//var whiteIcon;
//var redIcon;
var strokeColor = "#FF0000";
var activeTrack;
var helperMarker;

var mission = {};	// track => [@WayPoint]
var polyLine = {};	// track => [@GPolyline]

function WayPoint (point, altitude, velocity) {
	this.point = point;
	this.altitude = altitude;
	this.velocity = velocity;
	this.toString = function () { return "point="+this.point+", altitude="+this.altitude+", velocity="+this.velocity; };
}

function editTrack(fragmentId) {
	var id = fragmentId.replace(/fragment/,"").replace(/rowInjector/,"");
	
	var elems = $(fragmentId).getElementsByTagName("input");
	for (var k=0; k < elems.length; k++) {
		if (elems[k].type == "checkbox")
			elems[k].checked = true;
	}
	
	var newTrack = 'track' + id;
	
	if (activeTrack == newTrack)
		return;
	
	if (polyLine[newTrack]) {
		polyLine[newTrack].enableEditing();
		GEvent.addListener(polyLine[newTrack], "lineupdated", function() { updateMission(); });
		polyLine[newTrack].show();
	}
	
	if (polyLine[activeTrack]) {
		polyLine[activeTrack].disableEditing();
		GEvent.clearListeners(polyLine[activeTrack]);
	}
	
	activeTrack = newTrack;
	hideHelperMarker();
}

function showTrack(fragmentId, checked) {
	var id = fragmentId.replace(/fragment/,"").replace(/rowInjector/,"");
	var trackId = 'track' + id;
	
	var elems = $(fragmentId).getElementsByTagName("input");
	for (var k=0; k < elems.length; k++) {
		if (elems[k].type == "radio" && !checked)
			elems[k].checked = false;
	}
	
	if (checked) {
		if (polyLine[trackId])
			polyLine[trackId].show();
	} else {
		if (activeTrack == trackId) {
			activeTrack = '';
		}
		
		if (polyLine[trackId]) {
			polyLine[trackId].hide();
			polyLine[trackId].disableEditing();
		}
	}
}

function deleteTrack (fragmentId) {
	var id = fragmentId.replace(/removerowlink/,"");
	var trackId = 'track' + id;

	if (activeTrack == trackId) {
		activeTrack = '';
	}

	if (polyLine[trackId]) {
		map.removeOverlay(polyLine[trackId]);
		polyLine[trackId] = 0;
	}
}


function deleteMarker (marker, p) {

	if (!polyLine[activeTrack]) {
		updateMission();
		return;
	}
	
	if (polyLine[activeTrack].getVertexCount() == 1) {
		map.removeOverlay(marker);
		polyLine[activeTrack] = 0;
		mission[activeTrack] = [];
		saveWaypoints();
		return;
	}
	
	if (polyLine[activeTrack].getVertexCount() == 2) {
		var point = polyLine[activeTrack].getVertex(0);
		if (p.equals(point))
			mission[activeTrack] = [mission[activeTrack][1]];
		else
			mission[activeTrack] = [mission[activeTrack][0]];
		
		var points = [];
		for (var i = 0, l = mission[activeTrack].length; i < l; i++) {
			points.push(mission[activeTrack][i].point);
		}
		
		map.removeOverlay(marker);
		polyLine[activeTrack] = new GPolyline (points, strokeColor, 3, 1, strokeColor, 0.2);
		map.addOverlay(polyLine[activeTrack]);
		GEvent.addListener(polyLine[activeTrack], "lineupdated", function() { updateMission(); });
		polyLine[activeTrack].enableEditing();
		saveWaypoints();
		return;
	}
	
	var k = -1;
	
	for (var i = 0; i < polyLine[activeTrack].getVertexCount(); i++) {
		var point = polyLine[activeTrack].getVertex(i);
		if (p.equals (point)) {
			k = i;
			break;
		}
	}
	
	if (k >= 0) {
		polyLine[activeTrack].deleteVertex (k);
		var nm = [];
		for (var i = 0, l = mission[activeTrack].length; i < l; i++)
			if (i != k)
				nm.push (mission[activeTrack][i]);
		mission[activeTrack] = nm;
	}
	
	updateMission();
}

function addMarker (point) {
	if (!mission[activeTrack])
		mission[activeTrack] = [];
	
	var op = mission[activeTrack][-1];
	var wp = op ? new WayPoint(point,op.altitude,op.velocity) : new WayPoint(point,0,0);
	mission[activeTrack].push(wp);
	
	if (polyLine[activeTrack])
		map.removeOverlay(polyLine[activeTrack]);
	
	var p = [];
	for (var i = 0, l = mission[activeTrack].length; i < l; i++)
		p.push (mission[activeTrack][i].point);
	
	polyLine[activeTrack] = new GPolyline (p, strokeColor, 3, 1, strokeColor, 0.2);
	map.addOverlay(polyLine[activeTrack]);
	polyLine[activeTrack].enableEditing();
	GEvent.addListener(polyLine[activeTrack], "lineupdated", function() { updateMission(); });
	saveWaypoints();
}

function updateMission () {
	var oldMission = mission[activeTrack];
	mission[activeTrack] = [];
	
	if (!polyLine[activeTrack]) {
		saveWaypoints();
		return;
	}
	
	var newPoint = oldMission.length != polyLine[activeTrack].getVertexCount();
	
	for (var i = 0, k = 0, l = polyLine[activeTrack].getVertexCount(); i < l; i++, k++) {
		var point = polyLine[activeTrack].getVertex(i);

		if (oldMission[k].point.equals(point)) {
			mission[activeTrack].push(oldMission[k]);
		} else {
			mission[activeTrack].push(new WayPoint(point,oldMission[k].altitude,oldMission[k].velocity));
			if (newPoint) --k;
		}
	}
	
	saveWaypoints();
	hideHelperMarker();
}

function saveWaypoints () {
	var id = activeTrack.substr(5);
	document.getElementsByName('waypoints'+id)[0].value = Object.toJSON(mission[activeTrack]);
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

	var inps = document.getElementsByTagName("input");
	var wp = [];
	for (var i=0; i < inps.length; i++) {
		if (inps[i].name.match(/^waypoints/)) {
			wp.push(inps[i]);
		}
	}
	
	for (var i=0; i < wp.length; i++) {
		var trackId = 'track' + wp[i].name.substr(9);
		mission[trackId] = [];
		var val = wp[i].value.evalJSON();
		var points = [];
		var x,y,h,v;
		for (var k=0, l=val.length; k < l; k++) {
			if (!val[k] || !val[k].point)
				continue;
			try {
				x = val[k].point.x;		if (!x) x=0;
				y = val[k].point.y;		if (!y) y=0;
				h = val[k].altitude;	if (!h) h=0;
				v = val[k].velocity;	if (!v) v=0;
			} catch (e) {
				alert(e);
			}
			var p = new GLatLng(y,x);
			mission[trackId].push (new WayPoint(p,h,v));
			points.push(p);
		}
		polyLine[trackId] = new GPolyline (points, strokeColor, 3, 1, strokeColor, 0.2);
		map.addOverlay(polyLine[trackId]);
	}
	
	GEvent.addListener(map, 'click', function(overlay, point, point2) {
		if (!activeTrack)
			return;
		if (overlay) {
			if (overlay instanceof GPolyline)
				deleteMarker (overlay, point2);
		} else if (point) {
			addMarker (point);
		}
	});

	GEvent.addListener(map, 'singlerightclick', function(point, src, overlay) {
		alert ("singlerightclick point='" + point + "', src='" + src + "', overlay='" + overlay + "', latLng=" + map.fromContainerPixelToLatLng(point));
	});
	
	GEvent.addListener(map, 'moveend', function() {
		var elems = document.getElementsByTagName("input");
		for (var k=0; k < elems.length; k++) {
			if (elems[k].name.match (/center/))
				elems[k].value = Object.toJSON(map.getCenter());
		}
	});
	
	GEvent.addListener(map, 'zoomend', function(oldLevel, newLevel) {
		var elems = document.getElementsByTagName("input");
		for (var k=0; k < elems.length; k++) {
			if (elems[k].name.match (/zoomLevel/))
				elems[k].value = Object.toJSON(newLevel);
		}
	});
	
	GEvent.addListener(map, 'maptypechanged', function () {
		var name = map.getCurrentMapType().getName();
		for (var k=0; k < elems.length; k++) {
			if (elems[k].name.match (/mapType/))
				elems[k].value = Object.toJSON(name);
		}
	});
	
}

//function velocityEditorSetUp () {
//	alert("Not yet implemented.");
//}

function altitudeWrapper () {
	this.length = function () { return mission[activeTrack] ? mission[activeTrack].length : 0; }
	this.get = function (i) { return mission[activeTrack][i].altitude; }
	this.set = function (i,v) { mission[activeTrack][i].altitude = v; }
}

function velocityWrapper () {
	this.length = function () { return mission[activeTrack] ? mission[activeTrack].length : 0; }
	this.get = function (i) { return mission[activeTrack][i].velocity; }
	this.set = function (i,v) { mission[activeTrack][i].velocity = v; }
}

var raph = 0;

function altitudeEditorSetUp (title,W,H,Ymax,valueWrapper) {
//    var W = 640, H = 480, Ymax = 50;
   
    if (!activeTrack) {
    	alert ("Please select a track!");
    	return;
    }
    
    if (helperMarker) {
    	map.removeOverlay(helperMarker);
    }
    	
    if (raph)
    	raph.remove();
    
    raph = Raphael("holder", W, H);
//    var len = mission[activeTrack].length;
    var len = valueWrapper.length();
    
//    if (!mission[activeTrack] || mission[activeTrack].length < 1) {
    if (len < 0) {
    	alert ("Please add some positions to this track!");
    	return;
    }
    
    var values = [];
    
    // for distances use GLatLng.distanceFrom(latlng)
    // see also http://econym.org.uk/gmap/reference.htm
    
    function translateX (x) {
    	return 50 + x * (W - 60) / (values.length - 1);
    }
    
    function translateY (y) {
    	return H - 10 - y * (H - 20) / Ymax;
    }
    
    function translate(x, y) {
        return [translateX (x), translateY (y)];
    }

    function rollback(yp) {
        return Ymax * (H - 10 - yp) / (H - 20);
    }
    
    for (var i = 0; i < len; i++) {
//        values.push(mission[activeTrack][i].altitude);
    	values.push (valueWrapper.get(i));
    }
    
    var p = [["M"].concat(translate(0, values[0]))],
        color = "#FF0000",
        blankets = raph.set(),
        buttons = raph.set(),
        texts = raph.set(),
        w = (W - 60) / values.length,
        isDrag = -1,
        start = null,
        path = raph.path(p.join(",")).attr({stroke: color, "stroke-width": 2}),
        unhighlight = function () {};
    
    var hx = 27;
    var dx = 7;
    var s = [["M", hx, translateY(0)]];
    var heightScale = raph.path(s.join(",")).attr({stroke: '#000000', "stroke-width": 2});
    s.push(["L", hx+dx, translateY(0), hx+dx, translateY(Ymax)]);
    for (var y=0; y < Ymax; y+=Ymax/10) {
    	var hy = translateY(y);
    	s.push (["M",hx,hy]);
    	s.push (["L",hx,hy,hx+dx,hy]);
    	texts.push(raph.text(10,hy+4,y).attr({'font-size':14}));
    }
    s.push (["M"],hx+dx,translateY(Ymax));
    s.push (["L"],hx+dx,translateY(Ymax),hx+dx-4,translateY(Ymax)+H/20);
    s.push (["M"],hx+dx,translateY(Ymax));
    s.push (["L"],hx+dx,translateY(Ymax),hx+dx+4,translateY(Ymax)+H/20);
    heightScale.attr({path: s.join(",")});
    
    var ruleHelper = raph.rect(hx+10,translateY(0),7,7,0).rotate(45,true).attr({fill:"#ff0"}).hide();
    var ruleHelperText = raph.text(hx+17,translateY(0),"0").attr({fill:"#ff0", 'font-size': 16}).hide();
    
    for (var i = 0, ii = values.length - 1; i < ii; i++) {
        var xy = translate(i, values[i]),
            xy1 = translate(i + 1, values[i + 1]),
            f;
        
        p.push(["L", xy[0], xy[1], xy1[0], xy1[1]]);
        
        (f = function (i, xy) {
            buttons.push(raph.circle(xy[0], xy[1], 5).attr({fill: color, stroke: "none"}));
            blankets.push(
            		raph.circle(xy[0], xy[1], w / 2).attr({stroke: "none", fill: "#fff", opacity: 0
            }).mouseover(function () {
                if (isDrag + 1) {
                    unhighlight = function () {};
                } else {
                    buttons.items[i].animate({r: 10}, 200);
                    showHelperMarker(i);
                }
                var t = valueWrapper.get(i); // mission[activeTrack][i].altitude;
//                var d = Math.round(translateY(t));
                var d = translateY(t);
                ruleHelper.attr({y: d-4});
                ruleHelperText.attr({y: d-15, text: t});
        		ruleHelper.show();
        		ruleHelperText.show();
            }).mouseout(function () {
                if (isDrag + 1) {
                    unhighlight = function () {
                        buttons.items[i].animate({r: 5}, 200);
                        ruleHelper.hide();
                        ruleHelperText.hide();
                    };
                    hideHelperMarker();
                } else {
                    buttons.items[i].animate({r: 5}, 200);
                }
                ruleHelper.hide();
            	ruleHelperText.hide();
            }).mousedown(function (e) {
                start = {m: e.clientY, p: p[i][4] || p[i][2]};
                isDrag = i;
                ruleHelper.show();
                ruleHelperText.show();
            }).mouseup(function (e) {
//            	ruleHelper.hide();
//            	ruleHelperText.hide();
            }));
            blankets.items[blankets.items.length - 1].node.style.cursor = "move";
        })(i, xy);
        
        if (i == ii - 1) {
            f(i + 1, xy1);
        }
    }
    
    path.attr({path: p.join(",")});
    
    var update = function (i, d) {
        (d > H - 10) && (d = H - 10);
        (d < 10) && (d = 10);
        if (i) {
            p[i][4] = d;
            p[i][6] = d;
            p[i + 1] && (p[i + 1][2] = d);
        } else {
            p[0][2] = d;
            p[1][2] = d;
        }
        path.attr({path: p.join(",")});
        buttons.items[i].attr({cy: d});
        blankets.items[i].attr({cy: d});
        ruleHelper.attr({y: d-4});
        var t = Math.round(10*rollback(d))/10;
        values[i] = t;
//      mission[activeTrack][i].altitude = Math.round(rollback(d));
//        mission[activeTrack][i].altitude = t; 
        valueWrapper.set(i,t);
        ruleHelperText.attr({y: d-15, text: t});
//		ruleHelper.show();
//		ruleHelperText.show();
        raph.safari();
        saveWaypoints();
    };
    
    document.onmousemove = function (e) {
        e = e || window.event;
        if (isDrag + 1) {
            update(isDrag, start.p + e.clientY - start.m);
        }
    };
    
    document.onmouseup = function () {
        isDrag = -1;
        unhighlight();
    };
    
    altitudeEditor.showCenter(true);
};

function showHelperMarker (i) {
	if (helperMarker)
		map.removeOverlay(helperMarker);
	
	var point = mission[activeTrack][i].point;
	helperMarker = new GMarker(point);
	map.addOverlay(helperMarker);
}

function hideHelperMarker () {
	if (helperMarker)
		map.removeOverlay(helperMarker);
	helperMarker = 0;
}
