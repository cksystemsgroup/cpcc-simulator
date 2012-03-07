

function ActionPointOverlay(point, map, complete) {
	this.point_ = point;
	this.map_ = map;
	
	var div = document.createElement('DIV');
	div.style.border = "none";
	div.style.borderWidth = "0px";
	div.style.position = "absolute";
	div.style.opacity = "0.65";
	div.style.visibility = "visible";
	this.div_ = div;
	
//	this.div_ = null;
	this.setMap(map);
}

ActionPointOverlay.prototype = new google.maps.OverlayView();

ActionPointOverlay.prototype.onAdd = function() {
//	var div = document.createElement('DIV');
//	div.style.border = "none";
//	div.style.borderWidth = "0px";
//	div.style.position = "absolute";
//	div.style.opacity = "0.65";
//	div.style.visibility = "visible";
//	this.div_ = div;
	var panes = this.getPanes();
	panes.overlayShadow.appendChild(this.div_);
}

ActionPointOverlay.prototype.draw = function() {
	var overlayProjection = this.getProjection();
	var pos = overlayProjection.fromLatLngToDivPixel(this.point_);
	pos.x -= 4;
	pos.y -= 9;
	var div = this.div_;
	div.style.left = pos.x + 'px';
	div.style.top = pos.y + 'px';
}

ActionPointOverlay.prototype.setComplete = function(complete) {
	if (!this.div_) {
		return;
	}
	while (this.div_.childNodes.length > 0) {
		this.div_.removeChild(this.div_.firstChild);
	}
		
	var img = document.createElement('IMG');
	if (complete) {
		img.title = 'complete';
		img.src = 'img/ActionPointRed_8.png';
	} else {
		img.title = 'incomplete';
		img.src = 'img/ActionPointBlue_8.png';
	}
	this.div_.appendChild(img);
	
	this.draw();
}

ActionPointOverlay.prototype.setPosition = function(point) {
	this.point_ = point;
	this.draw();
}

ActionPointOverlay.prototype.onRemove = function() {
	this.div_.parentNode.removeChild(this.div_);
	this.div_ = null;
}

ActionPointOverlay.prototype.hide = function() {
	if (this.div_) {
		this.div_.style.visibility = "hidden";
	}
}

ActionPointOverlay.prototype.show = function() {
	if (this.div_) {
		this.div_.style.visibility = "visible";
	}
}

ActionPointOverlay.prototype.toggle = function() {
	if (this.div_) {
		if (this.div_.style.visibility == "hidden") {
			this.show();
		} else {
			this.hide();
		}
	}
}

ActionPointOverlay.prototype.toggleDOM = function() {
	if (this.getMap()) {
		this.setMap(null);
	} else {
		this.setMap(this.map_);
	}
}