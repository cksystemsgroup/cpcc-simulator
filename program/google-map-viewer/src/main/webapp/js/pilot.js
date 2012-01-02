

function PilotOverlay(point, map) {

	this.point_ = point;
	this.map_ = map;
	this.pilotName = '';
	this.pilotFlying = false;
	this.div_ = null;
	this.setMap(map);
}

PilotOverlay.prototype = new google.maps.OverlayView();

PilotOverlay.prototype.onAdd = function() {
	var div = document.createElement('DIV');
	div.style.border = "none";
	div.style.borderWidth = "0px";
	div.style.position = "absolute";
	this.div_ = div;
	var panes = this.getPanes();
	panes.overlayShadow.appendChild(div);
}

PilotOverlay.prototype.draw = function() {
	var overlayProjection = this.getProjection();
	var pos = overlayProjection.fromLatLngToDivPixel(this.point_);
	pos.x -= 7;
	pos.y -= 11;
	var div = this.div_;
	div.style.left = pos.x + 'px';
	div.style.top = pos.y + 'px';
}

PilotOverlay.prototype.setPosition = function(point) {
	this.point_ = point;
	this.draw();
}

PilotOverlay.prototype.setPilotName = function(name) {
	this.pilotName = name;
}

PilotOverlay.prototype.setPilotFlying = function(flying) {
	this.pilotFlying = flying;
}

PilotOverlay.prototype.setVehicles = function(vehicles) {
	while (this.div_.childNodes.length > 0) {
		this.div_.removeChild(this.div_.firstChild);
	}
	
	var outerTable = document.createElement('TABLE');
	var tr = document.createElement('TR');
	var td = document.createElement('TD');
	var txt = document.createTextNode("+");
	td.appendChild(txt);
	td.className = "pilot_symbol";
	tr.appendChild(td);
	outerTable.appendChild(tr);
	
	var td2 = document.createElement('TD');
	tr.appendChild(td2);

	var innerTable = document.createElement('TABLE');
	innerTable.className = "pilot";
	td2.appendChild(innerTable);
	
	txt = document.createTextNode(this.pilotName);
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
		td.className = "vehicle_info";
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

PilotOverlay.prototype.onRemove = function() {
	this.div_.parentNode.removeChild(this.div_);
	this.div_ = null;
}

PilotOverlay.prototype.hide = function() {
	if (this.div_) {
		this.div_.style.visibility = "hidden";
	}
}

PilotOverlay.prototype.show = function() {
	if (this.div_) {
		this.div_.style.visibility = "visible";
	}
}

PilotOverlay.prototype.toggle = function() {
	if (this.div_) {
		if (this.div_.style.visibility == "hidden") {
			this.show();
		} else {
			this.hide();
		}
	}
}

PilotOverlay.prototype.toggleDOM = function() {
	if (this.getMap()) {
		this.setMap(null);
	} else {
		this.setMap(this.map_);
	}
}