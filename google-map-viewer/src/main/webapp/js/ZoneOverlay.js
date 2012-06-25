

function ZoneOverlay(zone, map) {

	this.zone_ = zone;
	this.map_ = map;
	this.mvcObject_ = null;
	this.active_ = true;
	this.onAdd();
	this.boundingBox = null;
}

// ZoneOverlay.prototype = new google.maps.OverlayView();

ZoneOverlay.prototype.onAdd = function() {
	
	if (this.zone_.type == "circle") {
		
		this.mvcObject_ = new google.maps.Circle({
			center: this.zone_.center,
			clickable: false,
			fillOpacity: 0,
			map: this.map_,
			radius: this.zone_.radius,
			strokeColor: "#6C6C6C",
			strokeOpacity: 0.3,
			strokeWeight: 1,
			visible: true
		});
		
		boundingBox = this.mvcObject_.getBounds();
		
	} else if (this.zone_.type == "polygon") {
		
		var rim = new Array();
		var vertices = this.zone_.vertices;

		var s = w = 180, n = e = -180; 
		
		for (var k=0, l = vertices.length; k < l; ++k) {
			var p = new google.maps.LatLng(vertices[k].lat, vertices[k].lon);
			rim.push(p);
			if (vertices[k].lat > n) n = vertices[k].lat;
			if (vertices[k].lat < s) s = vertices[k].lat;
			if (vertices[k].lon > e) e = vertices[k].lon;
			if (vertices[k].lon < w) w = vertices[k].lon;
		}
		
		this.mvcObject_ = new google.maps.Polyline ({
			clickable: false,
			map: map,
			path: rim,
			strokeColor: "#6C6C6C",
			strokeOpacity: 0.3,
			strokeWeight: 1
		});
		
		var sw = new google.maps.LatLng(s,w);
		var ne = new google.maps.LatLng(n,e);
		boundingBox = new google.maps.LatLngBounds(sw, ne);
		
	} else {
		alert("Unknown zone type: " + this.zone_.type);
	}
}


ZoneOverlay.prototype.isEquivalent = function(otherZone) {
	
	if (this.zone_.depot.lat != otherZone.depot.lat || this.zone_.depot.lon != otherZone.depot.lon || this.zone_.type != otherZone.type) {
		return false;
	}
	
	if (this.zone_.type == "circle") {
		if (this.zone_.center.lat != otherZone.center.lat || this.zone_.center.lon != otherZone.center.lon || this.zone_.radius != otherZone.radius) {
			return false;
		}
		
	} else if (this.zone_.type == "polygon") {
		if (this.zone_.vertices.length != otherZone.vertices.length) {
			return false;
		}
		
		for (var k=0, l=this.zone_.vertices.length; k < l; ++k) {
			if (this.zone_.vertices[k].lat != otherZone.vertices[k].lat || this.zone_.vertices[k].lon != otherZone.vertices[k].lon) {
				return false;
			}
		}
	}
	
	return true;
}

ZoneOverlay.prototype.getBounds = function () {
	return boundingBox;
}

ZoneOverlay.prototype.setActive = function (active) {
	this.active_ = active;
}

ZoneOverlay.prototype.getActive = function () {
	return this.active_;
}

ZoneOverlay.prototype.setMap = function (map) {
	if (this.mvcObject_) {
		this.mvcObject_.setMap(map);
	}
}

ZoneOverlay.prototype.hide = function() {
	if (this.mvcObject_) {
		this.mvcObject_.hide();
	}
}

ZoneOverlay.prototype.show = function() {
	if (this.mvcObject_) {
		this.mvcObject_.show();
	}
}
