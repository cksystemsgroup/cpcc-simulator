#!/usr/bin/perl
use strict;

my $dir = $ARGV[0];
# $dir ||= '/home/clem/new-jnavigator-workspace/cpcc-project/engine/work';
# $dir ||= '/www/tomcat-central/work/Catalina/localhost/engmap';
$dir ||= '/media/ramdisk/ckrainer/tomcat-central/work/Catalina/localhost/engmap';

my @ds = glob $dir.'/vehicle[0-9]*';
push @ds, glob $dir.'/*/vehicle[0-9]*';
push @ds, glob $dir.'/*/*/vehicle[0-9]*';

sub getVehicleProps {
	my $f = shift;
	my $rc = {};
	open IN, "< $f" or die "Can not unzip vehicle.properties from file $f";
	while (<IN>) {
		m/vehicle.creation.time\s*=\s*(.*)$/ and $rc->{creationTime} = $1, next;
		m/vehicle.id\s*=\s*(.*)$/ and $rc->{id} = $1, next;
		m/tokenBucketPassedTime\s*=\s*(.*)$/ and $rc->{tokenBucketPassedTime} = $1, next;
		m/arrival.time\s*=\s*(.*)$/ and $rc->{arrivalTime} = $1, next;
		m/task.id\s*=\s*(.*)$/ and $rc->{taskId} = $1, next;
		m/distance\s*=\s*(.*)$/ and $rc->{distance} = $1, next;
		m/previous.arrival.time\s*=\s*(.*)$/ and $rc->{previousArrivalTime} = $1, next;
		m/arrival.difference\s*=\s*(.*)$/ and $rc->{arrivalDifference} = $1, next;
		m/task.size\s*=\s*(.*)$/ and $rc->{taskSize} = $1, next;
		m/gamma\s*=\s*(.*)$/ and $rc->{gamma} = $1, next;
		m/mu_V\s*=\s*(.*)$/ and $rc->{mu_V} = $1, next;
		m/v_V\s*=\s*(.*)$/ and $rc->{v_V} = $1, next;
		m/nb\s*=\s*(.*)$/ and $rc->{nb} = $1, next;
		m/TBsz\s*=\s*(.*)$/ and $rc->{TBsz} = $1, next;
	}
	close IN;
	$rc;
}

sub calculateDistance {
	my ($oldLat, $oldLon, $lat, $lon) = @_;
	$oldLat && $oldLon && $lat && $lon or return '';
	my $pi = 3.14159265358979323846;
	my $earthRadius = 6371000.0;
	my $latMean = ($oldLat + $lat) / 2;
	my $latMeanRad = ($oldLat + $lat) * $pi / 360.0;
	my $dLat = $earthRadius * ($lat - $oldLat) *$pi / 180.0;
	my $dLon = cos($latMeanRad) * $earthRadius * ($lon - $oldLon) * $pi / 180.0;
	my $dist = sqrt($dLat*$dLat + $dLon*$dLon);
#print "## latMean=$latMean, latMeanRad=$latMeanRad, cos=",cos($latMeanRad),", dLat=$dLat, dLon=$dLon, dist=$dist\n";
	sqrt($dLat*$dLat + $dLon*$dLon);
}

sub calculateArrivalDiff {
	my ($oldArr, $arr) = @_;
	$oldArr && $arr or return '';
	$arr - $oldArr;
}

sub printValues {
	my $vehicleProps = shift;
	my $f = shift;
	my $vehicleId = $vehicleProps->{id};
	my $creationTime = $vehicleProps->{creationTime};
	my $taskId = $vehicleProps->{taskId};
	my $tokenBucketPassedTime = $vehicleProps->{tokenBucketPassedTime};
	my $arrivalTime = $vehicleProps->{arrivalTime};
	my $apNr = $taskId;
	my $distance = $vehicleProps->{distance};
	my $previousArrivalTime = $vehicleProps->{previousArrivalTime};
	my $arrivalDifference = $vehicleProps->{arrivalDifference};
	
	my ($oldLat, $oldLon, $dist, $oldArr) = (undef, undef, undef, undef);
	my ($lat, $lon, $arr, $arrDiff, $act, $del, $cpl) = (undef, undef, undef, undef, undef, undef, undef);
	open IN, "< $f" or die "Can not unzip vehicle-status.txt from file $f";
	while (<IN>) {
		m/Point\s+(\S+)\s+(\S+)\s+.*$/ and $lat = $1, $lon = $2;
		m/Process\s+(\S+)\s+(\S+)\s+.*$/ and $lat = $1, $lon = $2;
		m/Arrival\s+(\S+)\s+Activation\s+(\S+)\s+Delay\s+(\S+)\s+Complete\s+(\S+).*/ and $arr = $1/1000.0, $act = $2/1000.0, $del = $3/1000.0, $cpl = $4/1000.0;
		m/Arrival\s+(\S+)\s+Activation\s+(\S+)\s+Delay\s+(\S+).*/ and $arr = $1/1000.0, $act = $2/1000.0, $del = $3/1000.0;
		m/Temperature\s+\((\S+),\s+.*$/ and $cpl = $1/1000.0;

		if ($cpl) {
			# $dist = calculateDistance $oldLat, $oldLon, $lat, $lon;
			$dist = $distance;
			# $arrDiff = calculateArrivalDiff $oldArr, $arr;
			$arrDiff = $arrivalDifference ? $arrivalDifference : '';
			$del = sprintf "%d", ($tokenBucketPassedTime - $arrivalTime);
			print join(';',$vehicleId, $apNr++, $lat, $lon, $dist, $arrivalTime - $creationTime, $arrDiff, $tokenBucketPassedTime - $creationTime,
				$del, $cpl-$creationTime, $cpl-$tokenBucketPassedTime,
				$vehicleProps->{taskSize}, $vehicleProps->{gamma}, 
				$vehicleProps->{mu_V}, $vehicleProps->{v_V}, $vehicleProps->{nb}, $vehicleProps->{TBsz}
								), "\n";
			($oldLat, $oldLon, $oldArr) = ($lat, $lon, $arr);
			($lat, $lon, $arr, $act, $del, $cpl) = (undef, undef, undef, undef, undef, undef);
		}
	}
	close IN;
}



#VV;ACTION_POINT_NUMBER;LATITUDE;LONGITUDE;ALTITUDE;DISTANCE;ARRIVAL_TIME;ARRIVAL_TIME_DIFF;BUCKET_PASSED_TIME;BUCKET_DELAY;COMPLETED_TIME;DURATION

#print join(';', 'VV ID', 'AP Number', 'Latitude', 'Longitude', 'Distance', 'Arrival Time', 'Activation Time', 'Delay', 'Completed Time'), "\n";
print "VV;ACTION_POINT_NUMBER;LATITUDE;LONGITUDE;DISTANCE;ARRIVAL_TIME;ARRIVAL_DIFFERENCE;BUCKET_PASSED_TIME;".
		"BUCKET_DELAY;COMPLETED_TIME;EXECUTION_TIME;".
		"taskSize;gamma;mu_V;v_V;nb;TBsz\n";


for my $d (@ds) {
	my $vehicleProps = getVehicleProps $d.'/vehicle.properties';
	printValues $vehicleProps, $d.'/vehicle-status.txt';
}



