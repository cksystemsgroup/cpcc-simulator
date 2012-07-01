
function ActionPointOverlay(point, map, complete, engineUrl, vehicleDataUrl, vehicleName, actionPointIndex, infoWindow) {
	
	this.complete_ = !complete;
	var anchor = new google.maps.Point(4,5);
	this.completeIcon = new google.maps.MarkerImage ('/gmview/img/ActionPointRed_8.png', null, null, anchor);
	this.inCompleteIcon = new google.maps.MarkerImage ('/gmview/img/ActionPointBlue_8.png', null, null, anchor);

	this.setPosition(point);
	this.setMap(map);
	this.setComplete(complete);
	this.engineUrl = engineUrl;
	this.vehicleDataUrl = vehicleDataUrl;
	this.vehicleName = vehicleName;
	this.actionPointIndex = actionPointIndex;
	this.infoWindow = infoWindow;
	
	google.maps.event.addListener(this, 'click',
		function (event) {
			var actionPointUrl = engineUrl + "/" + vehicleName + "/" + actionPointIndex;
			var me = this;
			new Ajax.Request(actionPointUrl,
			{
				method:'get',
				onSuccess: function(transport){
					response = transport.responseText.evalJSON();
					infoWindow.setContent(me.actionPointToHTML(response));
					infoWindow.open(map, me);
				},
				onFailure: function(){ alert('Something went wrong...') },
			});
		}
	);
}

ActionPointOverlay.prototype = new google.maps.Marker();

ActionPointOverlay.prototype.constructor = ActionPointOverlay;

ActionPointOverlay.prototype.setComplete = function(complete) {
	if (this.complete_ == complete) {
		return;
	}
	
	this.complete_ = complete;
	
	if (complete) {
		this.setIcon(this.completeIcon);
	} else {
		this.setIcon(this.inCompleteIcon);
	}
	
//	infoWindow.close();
}

ActionPointOverlay.prototype.getEngineUrl = function() {
	return engineUrl;
}

ActionPointOverlay.prototype.getVehicleName = function() {
	return vehicleName;
}

ActionPointOverlay.prototype.getActionPointIndex = function() {
	return actionPointIndex;
}

ActionPointOverlay.prototype.actionTextMap = {
	airPressure: "AirPressure",
	altitude: "Altitude",
	course: "Course",
	photo: "Picture",
	random: "Random",
	sonar: "Sonar",
	speed: "Speed",
	temperature: "Temperature",
}

ActionPointOverlay.prototype.actionPointToHTML = function(actionPoint) {
	
	var me = this;
	
	function dateString(dateString){
		function pad(n){return n<10 ? '0'+n : n};
		function pad3(n){return n<10 ? '00'+n : n<100 ? '0' + n : n};
		if (!dateString) {
			return '';
		}
		var d = new Date(dateString);
		return d.getFullYear()+'-' + pad(d.getMonth()+1)+'-' + pad(d.getDate())+' '  
		+ pad(d.getHours())+':' + pad(d.getMinutes())+':' + pad(d.getSeconds()) + "." + pad3(d.getMilliseconds());
	}
	
	function actionToTR(a) {
		var value = a.value;
		if (a.type == "photo" && value) {
			var url = me.vehicleDataUrl + "/" + actionPoint.name + "/" + value;
//			value = '<a href="' + url + '" target="_blank"><img src="' + url + '" style="width:200px" /></a>';
			value = '<a href="#" target="_blank" onclick="javascript:return openImageWindow(\''+ url + '\')"><img src="' + url + '" style="width:250px" /></a>';
		}
		return '<tr><td>' + ActionPointOverlay.prototype.actionTextMap[a.type] + "</td><td>" + dateString(a.time) + "</td><td>" + value + '</td></tr>';
	}
	
	var table = '<table class="sensors"><thead><tr><th>Sensor</th><th>Completion Time</th><th>Value</th></tr></thead><tbody>';
	for (var k=0; k < actionPoint.actions.length; ++k) {
		table += actionToTR(actionPoint.actions[k]);
	}
	table += '</tbody></table>';
	
	return "<div>" + "Virtual Vehicle: " + actionPoint["vehicle.id"] + "</br>"
		+ "Action Point: " + actionPoint.actionPoint.latitude + " N, " + actionPoint.actionPoint.longitude + " E, " + actionPoint.actionPoint.altitude + " m </br>"
		+ "Tolerance: " + actionPoint.actionPoint.tolerance + "m </br>"
		+ table
		+ "</div>";
}
