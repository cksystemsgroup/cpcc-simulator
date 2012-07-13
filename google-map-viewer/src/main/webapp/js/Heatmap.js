
function Heatmap(url, map) {
	this.url_ = url;
	this.map_ = map;
	this.visible_ = false;
	this.pe_ = null;
	var gradient = [
//	                'rgba(0, 255, 255, 0)',
//	                'rgba(0, 255, 255, 1)',
//	                'rgba(0, 191, 255, 1)',
//	                'rgba(0, 127, 255, 1)',
//	                'rgba(0, 63, 255, 1)',
	                'rgba(0, 0, 255, 0)',
	                'rgba(0, 0, 223, 1)',
	                'rgba(0, 0, 191, 1)',
	                'rgba(0, 0, 159, 1)',
	                'rgba(0, 0, 127, 1)',
	                'rgba(63, 0, 91, 1)',
	                'rgba(127, 0, 63, 1)',
	                'rgba(191, 0, 31, 1)',
	                'rgba(255, 0, 0, 1)'
	              ];
	this.heatmap_ = new google.maps.visualization.HeatmapLayer({
		gradient: gradient,
		dissipating: false,
		maxIntensity: 50,
		radius: 0.0003
	});
}

Heatmap.prototype.hide = function() {
	this.heatmap_.setMap(null);
	this.visible_ = false;
	if (this.pe_) {
		this.pe_.stop();
		this.pe_ = null;
	}
}

Heatmap.prototype.show = function() {
	this.heatmap_.setMap(this.map_);
	this.visible_ = true;
	var me = this;
	var bounds = this.map_.getBounds();
	var url = this.url_ + '/' + 
		bounds.getNorthEast().lat() + '/' + bounds.getNorthEast().lng() + '/' +
		bounds.getSouthWest().lat() + '/' + bounds.getSouthWest().lng();
	
	this.pe_ = 	new PeriodicalExecuter(
		function() {
			new Ajax.Request(url, {
			    method:'get',
			    onSuccess: function(transport) {
					var aps = transport.responseText.evalJSON();
					var taps = new Array();
					for (var k=0; k < aps.length; ++k) {
						taps.push({
							location: new google.maps.LatLng(aps[k].lat, aps[k].lon),
							weight: aps[k].temp + 15
						});
					}
					me.heatmap_.setData(taps);
			    },
			    onFailure: function(){ alert('Something went wrong when loading Heatmap ...'); me.hide(); },
			 });
		},
	1);
}

Heatmap.prototype.toggle = function() {
	if (this.visible_) {
		this.hide();
	} else {
		this.show();
	}
}
