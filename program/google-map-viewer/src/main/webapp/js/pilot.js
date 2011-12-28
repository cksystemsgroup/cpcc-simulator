

function PilotOverlay(point, /* image, */ map) {

	// Now initialize all properties.
//	this.bounds_ = bounds;
	this.point_ = point;
//	this.image_ = image;
	this.map_ = map;

	// We define a property to hold the image's
	// div. We'll actually create this div
	// upon receipt of the add() method so we'll
	// leave it null for now.
	this.div_ = null;
	
	// Explicitly call setMap() on this overlay
	this.setMap(map);
}

PilotOverlay.prototype = new google.maps.OverlayView();

PilotOverlay.prototype.onAdd = function() {

	// Note: an overlay's receipt of onAdd() indicates that
	// the map's panes are now available for attaching
	// the overlay to the map via the DOM.

	// Create the DIV and set some basic attributes.
	var div = document.createElement('DIV');
	div.style.border = "none";
	div.style.borderWidth = "0px";
	div.style.position = "absolute";

	// Create an IMG element and attach it to the DIV.
//	var img = document.createElement("img");
//	img.src = this.image_;
//	img.style.width = "100%";
//	img.style.height = "100%";
//	div.appendChild(img);

//	var table = document.createElement("table");
//	table.
//	div.innerHTML = "<table class=\"pilot\"><tr><td width=\"10px\">+</td><td>pilot one</td></tr><tr><td></td><td nowrap>vehicle one</td></tr><tr><td></td><td>vehicle two</td></tr></table>";
	
	// Set the overlay's div_ property to this DIV
	this.div_ = div;

	var txts = ["pilot tux","vehicle bugger","vehicle it!","lala","buggerit again!"];
	this.setTexts(txts);
	
	// We add an overlay to a map via one of the map's panes.
	// We'll add this overlay to the overlayImage pane.
	var panes = this.getPanes();
//	panes.overlayLayer.appendChild(div);
//	panes.overlayMouseTarget.appendChild(div);
//	panes.floatPane.appendChild(div);
	panes.overlayShadow.appendChild(div);
}

PilotOverlay.prototype.draw = function() {

	// Size and position the overlay. We use a southwest and northeast
	// position of the overlay to peg it to the correct position and size.
	// We need to retrieve the projection from this overlay to do this.
	var overlayProjection = this.getProjection();

	// Retrieve the southwest and northeast coordinates of this overlay
	// in latlngs and convert them to pixels coordinates.
	// We'll use these coordinates to resize the DIV.
//	var sw = overlayProjection.fromLatLngToDivPixel(this.bounds_.getSouthWest());
//	var ne = overlayProjection.fromLatLngToDivPixel(this.bounds_.getNorthEast());
	var pos = overlayProjection.fromLatLngToDivPixel(this.point_);

//	pos.x += div.style.height;
	pos.x -= 4;
	pos.y -= 9;
	var div = this.div_;
	div.style.left = pos.x + 'px';
	div.style.top = pos.y + 'px';
	// Resize the image's DIV to fit the indicated dimensions.
//	div.style.left = sw.x + 'px';
//	div.style.top = ne.y + 'px';
//	div.style.width = (ne.x - sw.x) + 'px';
//	div.style.height = (sw.y - ne.y) + 'px';
}

PilotOverlay.prototype.setPosition = function(point) {
	this.point_ = point;
	this.draw();
}

PilotOverlay.prototype.setTexts = function(lines) {

	while (this.div_.childNodes.length > 0) {
		this.div_.removeChild(this.div_.firstChild);
	}
	
	var table = document.createElement('TABLE');
	table.className = "pilot";

	var txt = document.createTextNode("+");

	for (var k=0; k < lines.length; ++k) {
		var tr = document.createElement('TR');
		var td = document.createElement('TD');
		if (k == 0) {
			td.appendChild(txt);
			td.style.color = "red";
			td.style.fontWeight = "bolder";
		}
		td.width = "10px";
		tr.appendChild(td);
		td = document.createElement('TD');
		td.noWrap = "true";
		if (k == 0) {
			td.style.color = "green";
			td.style.fontWeight = "bolder";
		} else {
			td.bgColor = "#e6e6e6";
		}
		txt = document.createTextNode(lines[k]);
		td.appendChild(txt);
		tr.appendChild(td);
		table.appendChild(tr);
	}
	this.div_.appendChild(table);
	
	this.draw();
}

PilotOverlay.prototype.onRemove = function() {
	this.div_.parentNode.removeChild(this.div_);
	this.div_ = null;
}

//Note that the visibility property must be a string enclosed in quotes
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