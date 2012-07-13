

function VvPointOverlay(point, map) {

	this.point_ = point;
	this.map_ = map;
//	this.pilotName = '';
//	this.pilotFlying = false;
	this.div_ = null;
	this.setMap(map);
}

VvPointOverlay.prototype = new google.maps.OverlayView();

VvPointOverlay.prototype.onAdd = function() {
	var div = document.createElement('DIV');
	div.style.border = "none";
	div.style.borderWidth = "0px";
	div.style.position = "absolute";
	this.div_ = div;
	var panes = this.getPanes();
	panes.overlayShadow.appendChild(div);
}

VvPointOverlay.prototype.draw = function() {
	var overlayProjection = this.getProjection();
	var pos = overlayProjection.fromLatLngToDivPixel(this.point_);
	pos.x -= 11;
	pos.y -= 19;
	var div = this.div_;
	div.style.left = pos.x + 'px';
	div.style.top = pos.y + 'px';
}

VvPointOverlay.prototype.setPosition = function(point) {
	this.point_ = point;
	this.draw();
}

VvPointOverlay.prototype.setVehicle = function(vehicle) {
	if (!this.div_) {
		return;
	}
	while (this.div_.childNodes.length > 0) {
		this.div_.removeChild(this.div_.firstChild);
	}

	var outerTable = document.createElement('TABLE');
	var tr = document.createElement('TR');
	var td = document.createElement('TD');
	var img = document.createElement('IMG');
	img.title = 'test';
	img.src = 'img/markers/arrow_down.png';
	td.appendChild(img);
	td.className = "vvpoint_symbol";
	tr.appendChild(td);
	outerTable.appendChild(tr);

	var td2 = document.createElement('TD');
	tr.appendChild(td2);

	var innerTable = document.createElement('TABLE');
	innerTable.className = "vvpoint";
	td2.appendChild(innerTable);

	var id = vehicle["vehicle.id"];
	if (id.length > 10)
		id = id.substr(0, 10) + "...";
	txt = document.createTextNode(id);
	td = document.createElement('TD');
	td.appendChild(txt);
	td.appendChild(txt);
	tr = document.createElement('TR');
	tr.appendChild(td);
	innerTable.appendChild(tr);

	var tr = document.createElement('TR');
	var td = document.createElement('TD');
	var state = vehicle.state;
	td.className = "vehicle_info_" + state;

	td.appendChild(txt);
	tr.appendChild(td);
	innerTable.appendChild(tr);

	var tr = document.createElement('TR');
	var td = document.createElement('TD');
	tr.appendChild(td);
	for (var k=0; k < vehicle.actions.length; ++k) {
		var entry = vehicle.actions[k];
		var img = document.createElement('IMG');
		img.title = entry;
		img.src = 'img/markers/'+entry+'.png';
		td.appendChild(img);
	}
	innerTable.appendChild(tr);

	this.div_.appendChild(outerTable);
	this.draw();
}

//VvPointOverlay.prototype.setPilotName = function(name) {
//	this.pilotName = name;
//}
//
//VvPointOverlay.prototype.setPilotFlying = function(flying) {
//	this.pilotFlying = flying;
//}
//
//VvPointOverlay.prototype.setVehicles = function(vehicles) {
//	while (this.div_.childNodes.length > 0) {
//		this.div_.removeChild(this.div_.firstChild);
//	}
//	
//	var outerTable = document.createElement('TABLE');
//	var tr = document.createElement('TR');
//	var td = document.createElement('TD');
//	var txt = document.createTextNode("+");
//	td.appendChild(txt);
//	td.className = "pilot_symbol";
//	tr.appendChild(td);
//	outerTable.appendChild(tr);
//	
//	var td2 = document.createElement('TD');
//	tr.appendChild(td2);
//
//	var innerTable = document.createElement('TABLE');
//	innerTable.className = "pilot";
//	td2.appendChild(innerTable);
//	
//	txt = document.createTextNode(this.pilotName);
//	td = document.createElement('TD');
//	td.appendChild(txt);
//	td.appendChild(txt);
//	tr = document.createElement('TR');
//	tr.appendChild(td);
//	innerTable.appendChild(tr);
//	td.className = this.pilotFlying ? "pilot_name_flying" : "pilot_name_ground";	
//	
//	for (var entry in vehicles) {
//		var tr = document.createElement('TR');
//		var td = document.createElement('TD');
//		var state = vehicles[entry].state;
//		td.className = "vehicle_info_"+state;
//		var id = vehicles[entry]["vehicle.id"];
//		if (id.length > 10)
//			id = id.substr(0,10)+"...";
//		txt = document.createTextNode(id);
//		td.appendChild(txt);
//		tr.appendChild(td);
//		innerTable.appendChild(tr);
//		
//	}
//	
//	this.div_.appendChild(outerTable);
//	this.draw();
//}

VvPointOverlay.prototype.onRemove = function() {
	this.div_.parentNode.removeChild(this.div_);
	this.div_ = null;
}

VvPointOverlay.prototype.hide = function() {
	if (this.div_) {
		this.div_.style.visibility = "hidden";
	}
}

VvPointOverlay.prototype.show = function() {
	if (this.div_) {
		this.div_.style.visibility = "visible";
	}
}

VvPointOverlay.prototype.toggle = function() {
	if (this.div_) {
		if (this.div_.style.visibility == "hidden") {
			this.show();
		} else {
			this.hide();
		}
	}
}

VvPointOverlay.prototype.toggleDOM = function() {
	if (this.getMap()) {
		this.setMap(null);
	} else {
		this.setMap(this.map_);
	}
}