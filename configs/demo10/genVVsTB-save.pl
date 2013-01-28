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
	minLat => 47.69130948,	maxLat => 47.69355526,
	minLon => 13.38340771,	maxLon => 13.38674410,
	
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

	sensors => [qw {Temperature}],
	lambdaVVs => 0,
#			0.011, 0.018, 0.016, 0.014, 0.0014, 0.012, 0.008, 0.006, 0.004, 0.002, 0.015, 0.010, 0.019,
#			
	lambdaAPs => 0.1,
	simulationStart => $startTime,
	arrivalTime => $startTime,
	vehicleCount => 1,
	tasks => 20,
	vvFile => 'gen-VV-%1$s-%2$s.zip',
	taskDelayAlgorithm => '',
};

sub buggerit {
	my $cfg = shift;
	my $counter = shift;
	
	my $vv = new VirtualVehicle ($cfg);
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
		qx{ curl -o /dev/null --retry 0 --retry-delay 15 -F file=\@$fileName $cfg->{url} > $of 2>&1 };
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
package VirtualVehicle;
use strict;
use Archive::Zip qw( :ERROR_CODES :CONSTANTS );
use Math::Trig;
 
sub new {
	my $classname = shift;
	my $self = bless {PRG => undef, PROP => undef}, $classname;
	$self->{CFG} = shift;
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
	my $dt = 0;
	
	if ($taskId) {
		$dt = $arrivalTime - $self->{TB_previousArrivalTime};
		$self->{TB_nb} += $dt * $self->{TB_fillRate};
		$self->{TB_nb} > $self->{TB_size} and $self->{TB_nb} = $self->{TB_size};
		
		$distance = calculateDistance ($lat, $lon, $alt, $self->{TB_previousLat}, $self->{TB_previousLon}, $self->{TB_previousAlt});
		$gamma = $taskSize / $self->{CFG}->{mu_V} + $distance / $self->{CFG}->{v_V};
		$self->{TB_active} or $gamma = 0;
	}
	
	$gamma >= $self->{TB_size} and die "Illegal arrival rate gamma=".$gamma.", TB_size=".$self->{TB_size};
	 
	if ($gamma <= $self->{TB_nb}) {
		$self->{TB_nb} -= $gamma;
		$tokenBucketPassedTime = $arrivalTime;
	} else {
		$self->{TB_nb} = 0;
		my $waitTime = ($gamma - $self->{TB_nb}) / $self->{TB_fillRate};
		$tokenBucketPassedTime = $arrivalTime + $waitTime;
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

	my $prg = "";
	$id = sprintf "%s.%02d", $id, $taskId;

	my $lat = rnd($self->{CFG}->{minLat}, $self->{CFG}->{maxLat});
	my $lon = rnd($self->{CFG}->{minLon}, $self->{CFG}->{maxLon});
	my $taskSize = rnd(0, $self->{CFG}->{max_TaskSize});

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
