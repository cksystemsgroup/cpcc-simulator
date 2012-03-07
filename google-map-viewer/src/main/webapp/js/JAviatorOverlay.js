

function JAviatorOverlay(point, map) {

	this.point_ = point;
	this.map_ = map;
	this.pilotName = '';
	this.pilotFlying = false;
	this.div_ = null;
	this.setMap(map);
}

JAviatorOverlay.prototype = new google.maps.OverlayView();

JAviatorOverlay.prototype.onAdd = function() {
	var div = document.createElement('DIV');
	div.style.border = "none";
	div.style.borderWidth = "0px";
	div.style.position = "absolute";
	this.div_ = div;
	var panes = this.getPanes();
	panes.overlayShadow.appendChild(div);
}

JAviatorOverlay.prototype.draw = function() {
	var overlayProjection = this.getProjection();
	var pos = overlayProjection.fromLatLngToDivPixel(this.point_);
	pos.x -= 19;
	pos.y -= 19;
	var div = this.div_;
	div.style.left = pos.x + 'px';
	div.style.top = pos.y + 'px';
}

JAviatorOverlay.prototype.setPosition = function(point) {
	this.point_ = point;
	this.draw();
}

JAviatorOverlay.prototype.setPilotName = function(name) {
	this.pilotName = name;
}

JAviatorOverlay.prototype.setPilotFlying = function(flying) {
	this.pilotFlying = flying;
}

JAviatorOverlay.prototype.setVehicles = function(vehicles) {
	while (this.div_.childNodes.length > 0) {
		this.div_.removeChild(this.div_.firstChild);
	}
	
	var outerTable = document.createElement('TABLE');
	var tr = document.createElement('TR');
	var td = document.createElement('TD');
	var img = document.createElement('img');
//	img.src = this.pilotFlying ? "img/JAviatorFlying_32.png" : "img/JAviatorGround_32.png";
	img.src = "img/JAviator-Black_32.png";
	td.appendChild(img);
	td.className = "pilot_symbol";
	tr.appendChild(td);
	outerTable.appendChild(tr);
	
	var td2 = document.createElement('TD');
	td2.className = "pilot_info";
	tr.appendChild(td2);

	var innerTable = document.createElement('TABLE');
	innerTable.className = "pilot";
	td2.appendChild(innerTable);
	
	var txt = document.createTextNode(this.pilotName);
	td = document.createElement('TD');
	td.appendChild(txt);
	td.appendChild(txt);
	tr = document.createElement('TR');
	tr.appendChild(td);
	innerTable.appendChild(tr);
	td.className = this.pilotFlying ? "pilot_name_flying" : "pilot_name_ground";	
	
	for (var entry in vehicles) {
		var tr = document.createElement('TR');
		var td = document.createElement('TD');
		var state = vehicles[entry].state;
		td.className = "vehicle_info_"+state;
		var id = vehicles[entry]["vehicle.id"];
		if (id.length > 10)
			id = id.substr(0,10)+"...";
		txt = document.createTextNode(id);
		td.appendChild(txt);
		tr.appendChild(td);
		innerTable.appendChild(tr);
		
	}
	
	this.div_.appendChild(outerTable);
	this.draw();
}

JAviatorOverlay.prototype.onRemove = function() {
	this.div_.parentNode.removeChild(this.div_);
	this.div_ = null;
}

JAviatorOverlay.prototype.hide = function() {
	if (this.div_) {
		this.div_.style.visibility = "hidden";
	}
}

JAviatorOverlay.prototype.show = function() {
	if (this.div_) {
		this.div_.style.visibility = "visible";
	}
}

JAviatorOverlay.prototype.toggle = function() {
	if (this.div_) {
		if (this.div_.style.visibility == "hidden") {
			this.show();
		} else {
			this.hide();
		}
	}
}

JAviatorOverlay.prototype.toggleDOM = function() {
	if (this.getMap()) {
		this.setMap(null);
	} else {
		this.setMap(this.map_);
	}
}