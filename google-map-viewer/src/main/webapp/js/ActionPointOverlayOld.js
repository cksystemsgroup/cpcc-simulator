

function ActionPointOverlay(point, map, complete) {
	this.point_ = point;
	this.map_ = map;
	this.complete_ = complete;
	
	var div = document.createElement('DIV');
	div.style.border = "none";
	div.style.borderWidth = "0px";
	div.style.position = "absolute";
	div.style.opacity = "0.65";
	div.style.visibility = "visible";
	
	var img = document.createElement('IMG');
	if (complete) {
		img.title = 'complete';
		img.src = 'img/ActionPointRed_8.png';
		div.style.cursor = 'pointer';
	} else {
		img.title = 'incomplete';
		img.src = 'img/ActionPointBlue_8.png';
		div.style.cursor = 'auto';
	}
	div.appendChild(img);
	
	this.img_ = img;
	this.div_ = div;
	this.setMap(map);
	
	google.maps.event.addDomListener(this.div_, "click", function(event) {
//		google.maps.event.trigger(me, "click");
		alert("AAA");
    });
}

ActionPointOverlay.prototype = new google.maps.OverlayView();

ActionPointOverlay.prototype.onAdd = function() {
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
	if (!this.div_ || this.complete_ == complete) {
		return;
	}

	this.complete_ = complete;

	if (complete) {
		this.img_.title = 'complete';
		this.img_.src = 'img/ActionPointRed_8.png';
		this.div_.style.cursor = 'pointer';
	} else {
		this.img_.title = 'incomplete';
		this.img_.src = 'img/ActionPointBlue_8.png';
		this.div_.style.cursor = 'auto';
	}

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