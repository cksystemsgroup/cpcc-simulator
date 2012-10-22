#!/usr/bin/perl

use strict;
use Time::HiRes qw(usleep);
use threads;

my $argsId = $ARGV[0];

my $startTime = $ARGV[1];
$startTime ||= time() + 30;

my $cfg = {
 	url => 'http://localhost:9000/engmap/vehicle/text/vehicleUpload',
#	url => 'http://localhost:8080/engmap/vehicle/text/vehicleUpload',
	
# 500 x 500
#	minLat => 47.69018658,	maxLat => 47.69467816,
#	minLon => 13.38173950,	maxLon => 13.38841213,

# 250m x 250m
#	minLat => 47.69130948,  maxLat => 47.69355526,
#	minLon => 13.38340771,  maxLon => 13.38674410,
	minLat => 47.69130954,	maxLat => 47.69355520,
	minLon => 13.38340777,	maxLon => 13.38674404,
	
# 125m x 125m
#	minLat => 47.69187092,  maxLat => 47.69299382,
#	minLon => 13.38424182,  maxLon => 13.38591003,

# 50m x 50m
#	minLat => 47.69220779,  maxLat => 47.69265695,
#	minLon => 13.38474229,  maxLon => 13.38540957,

	altitude => 1.0,		tolerance => 2.0,
	v_V => 10,

	max_TaskSize => 52,
	mu_V => 10,

	tokenBucket_active => 1,
	tokenBucket_fillRate => 1,
#	tokenBucket_size => 100000,
	tokenBucket_size => 41,
#	tokenBucket_size => 7,
#	tokenBucket_size => 15,
#	tokenBucket_size => 30,

	sensors => [qw {Temperature}],
	lambdaVVs => 0,
# NN			0.001, 0.002, 0.004, 0.006, 0.008, 0.010, 0.011, 0.012, 0.014, 0.015, 0.016, 0.017, 0.018, 0.019, 
# FCFS			0.002, 0.004, 0.006, 0.008, 0.010, 0.012, 0.013, 0.014, 0.015, 0.016, 0.017, 0.018, 0.019
# TB=15, NN	0.001 0.004 0.008 0.010 0.012 0.014
# TB=7, FCFS	0.005 0.008 0.010 0.012 0.014
# TB=30, FCFS	0.005 0.010 0.014
# TB=15, FCFS	0.001 0.005 0.008 0.010 0.012 0.014
	lambdaAPs => 0.004,
	simulationStart => $startTime,
	arrivalTime => $startTime,
	vehicleCount => 1,
	tasks => 20,
	vvFile => 'gen-VV-%1$s-%2$s.zip',
	taskDelayAlgorithm => '',
	testOnly => 0,
};

sub buggerit {
	my $cfg = shift;
	my $counter = shift;
	
#	my $gen = new GeneratorFullDistribution($cfg);
	my $gen = new GeneratorSmallTB($cfg);
	my $vv = new VirtualVehicle ($cfg, $gen);
	my $id = sprintf "%04d", $counter;
	print "Creating virtual vehicle $id\n";
	my $arrivalTime = $cfg->{arrivalTime};
	$vv->initTokenBucket();
	for (my $k=0; $k < $vv->{CFG}->{tasks}; ++$k) {
		my $t = 2 * rand() / $cfg->{lambdaAPs};
		$arrivalTime += $t;
		$vv->create($id, $k, $arrivalTime);
		my $fileName = sprintf $cfg->{vvFile}, $id, sprintf("%02d", $k);
		$vv->save($fileName);
#		my $of = sprintf "VVs/vv-%03d-%02d.out", $counter, $k;
		my $of = '/dev/null';
#		print "uploading VV $id  of=$of\n";
#		qx{ curl -o /dev/null -F file=\@$fileName $cfg->{url} > $of 2>&1 };
		$cfg->{testOnly} or qx{ curl -o /dev/null --retry 0 --retry-delay 15 -F file=\@$fileName $cfg->{url} > $of 2>&1 };
		unlink $fileName;
	}

#	my $t = 2 * rand() / $cfg->{lambdaVVs};
#	printf "Sleeping %.2f seconds.\n", $t;
#	usleep 1000000 * $t;
}

defined $argsId and buggerit ($cfg, $argsId), exit 0;

my ($x, $y) = (0, 0);
my @ts = ();
for (my $counter=1, my $l=$cfg->{vehicleCount}; $counter <= $l; ++$counter) {
	my $thread = threads->create(\&buggerit, $cfg, $counter);
	$x++;
	push @ts, $thread;
	if (@ts >= 1) {
		$_->join for @ts;
		@ts = ();
		$y+=10;
	}
}
$y += ~~@ts;
$_->join for @ts;

print "x=$x, y=$y\n";

0;

################################################################################
package Vector;
use strict;
use Math::Trig;

#sub new {
#	my $classname = shift;
#	my $self = bless {}, $classname;
##	$self->{CFG} = shift;
#	return $self;
#}


sub polarToRectancularCoordinates {
	my $lat = shift; $lat = deg2rad $lat;
	my $lon = shift; $lon = deg2rad $lon;
	my $alt = shift;	
	
	my $EQUATORIAL_AXIS = 6378137.0;
	my $POLAR_AXIS = 6356752.3142;	
	my $ANGULAR_ECCENTRICITY = acos ($POLAR_AXIS/$EQUATORIAL_AXIS);
	my $FIRST_ECCENTRICITY = 8.1819190842622E-2;
	
	my $u = sin ($lat) * $FIRST_ECCENTRICITY;
	my $N = $EQUATORIAL_AXIS / sqrt (1 - $u*$u);
		
	my $x = ($N + $alt) * cos ($lat) * cos ($lon);
	my $y = ($N + $alt) * cos ($lat) * sin ($lon);
	my $v = $POLAR_AXIS/$EQUATORIAL_AXIS;
	my $z = ($v*$v*$N + $alt) * sin ($lat);

	{x => $x, y => $y, z => $z};
}

sub rectangularToPolarCoordinates {
	my ($x, $y, $z) = @_;
	
	my $EQUATORIAL_AXIS = 6378137.0;
	my $POLAR_AXIS = 6356752.3142;	
	my $ANGULAR_ECCENTRICITY = acos ($POLAR_AXIS/$EQUATORIAL_AXIS);
	my $FIRST_ECCENTRICITY = 8.1819190842622E-2;
	
	my $newLatitude = 90;
	my $latitude = 0;
	my ($u, $v, $w, $N) = (0,0,0,0);
	my $sin2AE = sin (2*$ANGULAR_ECCENTRICITY);
	my $sinAE = sin ($ANGULAR_ECCENTRICITY);
	
	while (abs ($latitude - $newLatitude) > 1E-13) {
		$latitude = $newLatitude;
		
		$u = sin ($latitude) * sin ($ANGULAR_ECCENTRICITY);
		$N = $EQUATORIAL_AXIS / sqrt (1 - $u*$u);
		
		$v = $N * sin ($latitude);
		$w = $N * cos ($latitude);
		
		my $numerator = $EQUATORIAL_AXIS*$EQUATORIAL_AXIS*$z + $v*$v*$v*$sin2AE*$sin2AE/4.0;
		my $denominator = $EQUATORIAL_AXIS*$EQUATORIAL_AXIS*sqrt ($x*$x + $y*$y) - $w*$w*$w*$sinAE*$sinAE;
		$newLatitude = atan ($numerator/$denominator);
	}
	
	my $cosNLat = cos ($newLatitude);
	my $sinNLat = sin ($newLatitude);
	my $altitude = $cosNLat*sqrt ($x*$x + $y*$y) + $sinNLat*($z + $sinAE*$sinAE*$N*$sinNLat) - $N;
	my $longitude = asin ($y / (($N + $altitude)*$cosNLat));
	{lat => rad2deg($newLatitude), lon => rad2deg($longitude), alt => $altitude};
}

sub norm {
	my $a = shift;	
	sqrt($a->{x} * $a->{x} + $a->{y} * $a->{y} + $a->{z} * $a->{z});
}

sub subtract {
	my $a = shift;
	my $b = shift;
	{x => $a->{x} - $b->{x}, y => $a->{y} - $b->{y}, z => $a->{z} - $b->{z}};
}


sub calculateDistance {
	my $cartA = polarToRectancularCoordinates @_[0,1,2];
	my $cartB = polarToRectancularCoordinates @_[3,4,5];
	my $d = subtract $cartA, $cartB;
	norm $d;
}


sub walk {
	my ($lat, $lon, $alt, $x, $y, $z) = @_;
	$lat = deg2rad $lat;
	$lon = deg2rad $lon;
	$alt += $z;
	
	my $EQUATORIAL_AXIS = 6378137.0;
	my $N = $EQUATORIAL_AXIS;
	
	my $dLatitude = $x /  ($N+$alt);
	my $dLongitude = $y / (($N+$alt)*cos($lat));
	$lat -= $dLatitude;
	$lon += $dLongitude;
		
	{lat => rad2deg($lat), lon => rad2deg($lon), alt => $alt};
}


1;


################################################################################
package GeneratorFullDistribution;
use strict;
#use Math::Trig;
 
sub new {
	my $classname = shift;
	my $self = bless {}, $classname;
	$self->{CFG} = shift;
	return $self;
}

sub rnd {
	my $a = shift;
	my $b = shift;
	$a + ($b-$a)*rand();
}

sub generate {
	my $self = shift;
	
	my $lat = rnd($self->{CFG}->{minLat}, $self->{CFG}->{maxLat});
	my $lon = rnd($self->{CFG}->{minLon}, $self->{CFG}->{maxLon});
	my $taskSize = rnd(0, $self->{CFG}->{max_TaskSize});
	
	($lat, $lon, $taskSize);
}

1;


################################################################################
package GeneratorSmallTB;
use strict;
use Math::Trig;
 
sub new {
	my $classname = shift;
	my $self = bless { POS => undef }, $classname;
	$self->{CFG} = shift;
	return $self;
}

sub disk {
	my $distance = shift;
	my $r = rand();
	my $theta = 2.0 * pi * rand();
	my $x = $distance * sqrt($r) * cos($theta);	
	my $y = $distance * sqrt($r) * sin($theta);
	
	($x, $y);
}

sub rnd {
	my $a = shift;
	my $b = shift;
	$a + ($b-$a)*rand();
}

sub generate {
	my $self = shift;
	my $nb = shift;
	
	my ($lat, $lon);

	my $mts = $nb * $self->{CFG}->{mu_V};
	$mts > $self->{CFG}->{max_TaskSize} and $mts = $self->{CFG}->{max_TaskSize};

	my $taskSize = rnd(0, $mts);
	
	if (defined $self->{POS}) {
		my $pos = $self->{POS};
		my $maxDistance = ($nb - $taskSize / $self->{CFG}->{mu_V}) * $self->{CFG}->{v_V};
		my ($x, $y) = disk $maxDistance;
		my $c = Vector::walk($pos->[0], $pos->[1], 0, $x, $y, 0);
		($lat, $lon) = ($c->{'lat'}, $c->{'lon'});
	} else {
		$lat = rnd($self->{CFG}->{minLat}, $self->{CFG}->{maxLat});
		$lon = rnd($self->{CFG}->{minLon}, $self->{CFG}->{maxLon});
	}
	
	$lat < $self->{CFG}->{minLat} and $lat = $self->{CFG}->{minLat};
	$lat > $self->{CFG}->{maxLat} and $lat = $self->{CFG}->{maxLat};
	
	$lon < $self->{CFG}->{minLon} and $lon = $self->{CFG}->{minLon};
	$lon > $self->{CFG}->{maxLon} and $lon = $self->{CFG}->{maxLon};
	
	@{$self->{POS} = [$lat, $lon, $taskSize]};
}

1;


################################################################################
package VirtualVehicle;
use strict;
use Archive::Zip qw( :ERROR_CODES :CONSTANTS );
use Math::Trig;
 
sub new {
	my $classname = shift;
	my $self = bless {PRG => undef, PROP => undef}, $classname;
	$self->{CFG} = shift;
	$self->{GENERATOR} = shift;
	return $self;
}

sub initTokenBucket {
	my $self = shift;
	$self->{TB_fillRate} = $self->{CFG}->{tokenBucket_fillRate};
	$self->{TB_size} = $self->{CFG}->{tokenBucket_size};
	$self->{TB_active} = $self->{CFG}->{tokenBucket_active};
	$self->{TB_nb} = 0;
	$self->{TB_previousArrivalTime} = undef;
	$self->{TB_previousTokenBucketPassedTime} = undef;
	$self->{TB_previousLat} = undef;
	$self->{TB_previousLon} = undef;
	$self->{TB_previousAlt} = undef;
}

sub calculateTokenBucketPassedTime {
	my $self = shift;
	my $lat = shift;
	my $lon = shift;
	my $alt = shift;
	my $arrivalTime = shift;
	my $taskId = shift;
	my $taskSize = shift;

	my $tokenBucketPassedTime = undef;
	my $distance = '';
	my $gamma = 0;
#	my $dt = 0;

	my $prev = $taskId ? $self->{TB_previousTokenBucketPassedTime} : $arrivalTime;
	
	if ($taskId > 0) {
		my $dt = $arrivalTime - $self->{TB_previousArrivalTime};
		$self->{TB_nb} += $dt * $self->{TB_fillRate};
		$self->{TB_nb} > $self->{TB_size} and $self->{TB_nb} = $self->{TB_size};
		
		$distance = Vector::calculateDistance ($lat, $lon, $alt, $self->{TB_previousLat}, $self->{TB_previousLon}, $self->{TB_previousAlt});
		$gamma = $taskSize / $self->{CFG}->{mu_V} + $distance / $self->{CFG}->{v_V};
		$self->{TB_active} or $gamma = 0;
	} else {
		$self->{TB_nb} = ($arrivalTime-$self->{CFG}->{simulationStart}) * $self->{TB_fillRate};
		$self->{TB_nb} > $self->{TB_size} and $self->{TB_nb} = $self->{TB_size};
	}
	
	$gamma >= $self->{TB_size} and die "Illegal arrival rate gamma=".$gamma.", TB_size=".$self->{TB_size};
	if ($gamma <= $self->{TB_nb}) {
		$self->{TB_nb} -= $gamma;
#		$tokenBucketPassedTime = $arrivalTime;
		$tokenBucketPassedTime = $prev < $arrivalTime ? $arrivalTime : $prev;
#		$prev > $arrivalTime and $self->{TB_nb} += ($prev - $arrivalTime) * $self->{TB_fillRate};
#		if ($taskId) {
#			my $dt = $self->{TB_previousTokenBucketPassedTime} - $tokenBucketPassedTime;
#			$self->{TB_nb} += $dt * $self->{TB_fillRate};
#			$self->{TB_nb} > $self->{TB_size} and $self->{TB_nb} = $self->{TB_size};
#		}
	} else {
		my $waitTime = ($gamma - $self->{TB_nb}) / $self->{TB_fillRate};
		$self->{TB_nb} = 0;
		if ($arrivalTime <= $prev) {
			$tokenBucketPassedTime = $prev + $waitTime;
		} else {
			$tokenBucketPassedTime = $arrivalTime + $waitTime;	
		}
	}
	
	$self->{TB_previousArrivalTime} = $arrivalTime;
	$self->{TB_previousTokenBucketPassedTime} = $tokenBucketPassedTime;
	$self->{TB_previousLat} = $lat;
	$self->{TB_previousLon} = $lon;
	$self->{TB_previousAlt} = $alt;
	($tokenBucketPassedTime, $distance, $gamma);
}

sub rnd {
	my $a = shift;
	my $b = shift;
	$a + ($b-$a)*rand();
}

sub create {
	my $self = shift;
	my $id = shift;
	my $taskId = shift;
	my $arrivalTime = shift;
	
	my $nbBefore = $self->{TB_nb};
	my $prg = "";
	$id = sprintf "%s.%02d", $id, $taskId;

#	my $lat = rnd($self->{CFG}->{minLat}, $self->{CFG}->{maxLat});
#	my $lon = rnd($self->{CFG}->{minLon}, $self->{CFG}->{maxLon});
#	my $taskSize = rnd(0, $self->{CFG}->{max_TaskSize});
	
	my ($lat, $lon, $taskSize) = $self->{GENERATOR}->generate($self->{TB_nb});
	
	my $previousArrivalTime = $taskId ? (sprintf "%d", $self->{TB_previousArrivalTime}) : '';
	my $arrivalDifference = $taskId ? (sprintf "%d", $arrivalTime - $self->{TB_previousArrivalTime}) : '';
	my ($tokenBucketPassedTime, $distance, $gamma) = $self->calculateTokenBucketPassedTime($lat, $lon, $self->{CFG}->{altitude}, $arrivalTime, $taskId, $taskSize);
	$distance = $taskId ? sprintf "%.2f", $distance : '';

#	$prg .= sprintf "Point %.8f %.8f %.1f tolerance %.1f Arrival %d Delay %d %s\n",
#		$lat, $lon, $self->{CFG}->{altitude}, $self->{CFG}->{tolerance},
#		1000 * $tokenBucketPassedTime, 1000 * $taskSize / $self->{CFG}->{mu_V},
#		join(' ',@{$self->{CFG}->{sensors}});

	$prg .= sprintf "Process %.8f %.8f %.1f tolerance %.1f Arrival %d Delay %d\n",
		$lat, $lon, $self->{CFG}->{altitude}, $self->{CFG}->{tolerance},
		1000 * $tokenBucketPassedTime, 1000 * $taskSize / $self->{CFG}->{mu_V};

	$self->{PRG} = $prg;

	$self->{PROP} = sprintf "vehicle.id=VV %s\n".  "task.id=%d\n".  "vehicle.creation.time=%d\n".
							 "arrival.time=%d\n".  "tokenBucketPassedTime=%d\n".  "distance=%s\n".
							 "previous.arrival.time=%s\n".  "arrival.difference=%s\n".  "task.size=%.3f\n".
							 "gamma=%.3f\n".  "mu_V=%.3f\n".  "v_V=%.3f\n".  "nb=%.3f\n".  "TBsz=%d\n",
							 $id, $taskId, $self->{CFG}->{simulationStart},
							 $arrivalTime, $tokenBucketPassedTime, $distance,
							 $previousArrivalTime, $arrivalDifference, $taskSize,
							 $gamma, $self->{CFG}->{mu_V}, $self->{CFG}->{v_V}, $self->{TB_nb}, $self->{CFG}->{tokenBucket_size};

#	open OUT, "> VVs/nix-$id-$taskId" or die "buggerit!";
#	print OUT $gamma,"\n";
#	close OUT;
#
	if ($cfg->{testOnly}) {
		$taskId or print "VV        ;TSK;LAT        ;LON,       ;DIST  ;Arr      ;ArrDf;TBpsd    ;TBdelay;TaskSz ;Gamma  ;mu_V   ;v_V    ;nbBef.  ;nb      ;TBsz\n";
		printf "VV %s;%3d;%.8f;%.8f;%6s;%9.3f;%5s;%9.3f;%7.3f;%7.3f;%7.3f;%7.3f;%7.3f;%8.3f;%8.3f;%7.3f\n",
			$id, $taskId, $lat, $lon, $distance, $arrivalTime-$self->{CFG}->{simulationStart}, $arrivalDifference,
			$tokenBucketPassedTime-$self->{CFG}->{simulationStart}, $tokenBucketPassedTime-$arrivalTime, $taskSize,
			$gamma, $self->{CFG}->{mu_V}, $self->{CFG}->{v_V}, $nbBefore, $self->{TB_nb}, $self->{CFG}->{tokenBucket_size};
		;
	} 
}

sub save {
	my $self = shift;
	my $fileName = shift;

	my $zip = Archive::Zip->new();
	my $member = $zip->addString($self->{PRG}, 'vehicle.prg');
	$member->desiredCompressionMethod(COMPRESSION_DEFLATED);
	$member = $zip->addString($self->{PROP}, 'vehicle.properties');
	$member->desiredCompressionMethod(COMPRESSION_DEFLATED);
	$zip->writeToFileNamed($fileName) == AZ_OK or die "Can not write file $fileName";
}

1;
