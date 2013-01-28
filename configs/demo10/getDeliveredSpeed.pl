#!/usr/bin/perl

use strict;


# my $st = new Statistics;
# $st->add(12,11,14,16,15);
# printf "avg=%.3f,  var=%.3f,  stddev=%.3f\n", $st->mean(), $st->variance(), sqrt($st->variance());

# VV;ACTION_POINT_NUMBER;LATITUDE;LONGITUDE;DISTANCE;ARRIVAL_TIME;ARRIVAL_DIFFERENCE;BUCKET_PASSED_TIME;BUCKET_DELAY;COMPLETED_TIME;EXECUTION_TIME;taskSize;gamma;mu_V;v_V;nb;TBsz
# VV 0001.00;0;47.69440395;13.38690193;;128;;128;0;166.960000038147;38.960000038147;35.146;0.000;10.000;10.000;0.000;100000

my $res = [];

@ARGV or @ARGV = ('/home/clem/new-jnavigator-workspace/cpcc-project/configs/demo10/n');

my %res = ();
my ($lambda, $TBsz, $RV_V) = undef;
my $nrTasks = 20;
my $a1 = 50;
my $m = 5;
my $nrRVs = $m * $m;

print "lambda;postB_lambda;E[v_Rn];E[v_D];P(delivered speed > required speed);r_v;TBsz;RV_V;TotalDistance;TimeRV;TimeCPCC;Gain\n";

foreach my $f (@ARGV) {
 	print STDERR "Processing file $f\n";
 	$f =~ m/.*(0\.\d+)-TB=(\d+)-RvV=(\d+).*/  and  $lambda = $1, $TBsz = $2, $RV_V = $3;
	print STDERR "$lambda, $TBsz, $RV_V\n";
 	
	my $cmd = "< $f";
	$cmd =~ m/\.bz2/ and $cmd = "bzip2 -cd $f |";
	$cmd =~ m/\.gz/ and $cmd = "gzip -cd $f |";
	
	open IN, $cmd or die "Can not pipe to '$cmd'";
	my $hdr = <IN>;
	chomp $hdr;
	my @cols = split /;/, $hdr;
	
	while (<IN>) {
		m/VV (\d+)\.(\d+);/;
		my ($vv, $task) = ($1,$2);
		defined $vv or next;
		$vv =~ m/\d+/ or next;
		my @vals = split /;/, $_;
		my %l = map { $cols[$_] => $vals[$_] } (0..@vals);
		exists $res{$vv} or $res{$vv} = {};
		$res{$vv}->{$task} = \%l;	
	}
	
	close IN;

	my $stDeliveredSpeed = new Statistics;
	my $stRequiredSpeed = new Statistics;
	my $stDeliveryRatio = new Statistics;
	my $stArrivalDiff = new Statistics;
	my $stBucketPassedDiff = new Statistics;
	my $buggerIt = 0;
	my $violations = 0;
	my $totalNumberOfTasks = 0;
	my $totalDistance = 0;
	my $totalExecutionTime = 0;
	my $totalNumberVVs = 0;
	my $minArrivalTime = 1000000;
	my $maxCompletedTime = 0;
	my $cpccTime = 0;
	my $totalComputationTime = 0;
	
	foreach my $vv (sort keys %res) {
		my $t = [];
		my $prevBucketPassedTime;
		my $prevArrivalTime;
		foreach my $task (sort {$a<=>$b} keys %{$res{$vv}}) {
			push @$t, [$task, $res{$vv}->{$task}->{COMPLETED_TIME}];
			if ($task > 0) {
				my $newDiff = $res{$vv}->{$task}->{ARRIVAL_TIME} - $prevArrivalTime;
				$res{$vv}->{$task}->{ARRIVAL_DIFFERENCE} or $res{$vv}->{$task}->{ARRIVAL_DIFFERENCE} = $newDiff;
				$stArrivalDiff->add($res{$vv}->{$task}->{ARRIVAL_DIFFERENCE});				
				$stBucketPassedDiff->add($res{$vv}->{$task}->{BUCKET_PASSED_TIME} - $prevBucketPassedTime);
			}
			
			$prevArrivalTime = $res{$vv}->{$task}->{ARRIVAL_TIME};
			$prevBucketPassedTime = $res{$vv}->{$task}->{BUCKET_PASSED_TIME};
			
			my $g = $res{$vv}->{$task}->{gamma};
			my $nb = $res{$vv}->{$task}->{nb};		# nb after task
			my $sz = $res{$vv}->{$task}->{TBsz};
			2*$g + $nb > $sz and ++$violations;
		}
 
		if (@$t != $nrTasks) {
			printf STDERR "VV $vv has %2d tasks, but should have %2d tasks. Ignoring VV $vv\n", ~~@$t, $nrTasks;
			next;
		}
		
		++$totalNumberVVs;



		
		my @sortedTasks = sort {$a->[1]<=>$b->[1]} @$t;
		
		my @d = grep { $sortedTasks[0] != $t->[0] } (0..@sortedTasks); 
#		@d and print "$lambda $vv ",join(',',map{$_->[0]}@sortedTasks),"\n";
		
		
		my $prevCompletedTime;
		my $sumL_i = 0;
		my $sumT_i_ex = 0;
		my $sumT_i_req = 0;
		
#		my $mu_V = 0;
#		foreach my $task (sort {$a<=>$b} keys %{$res{$vv}}) {
		foreach my $task (map{$_->[0]}@sortedTasks) {
	#		printf "%s %d\n", $vv, $task;

			$minArrivalTime > $res{$vv}->{$task}->{ARRIVAL_TIME} and
				$minArrivalTime = $res{$vv}->{$task}->{ARRIVAL_TIME};
			
			$maxCompletedTime < $res{$vv}->{$task}->{COMPLETED_TIME} and
				$maxCompletedTime = $res{$vv}->{$task}->{COMPLETED_TIME};

			if ($task > 0) {
				$res{$vv}->{$task}->{COMPLETED_TIME} < $prevCompletedTime and ++$buggerIt, print STDERR "$lambda $vv $task\n";
				
				$sumL_i += $res{$vv}->{$task}->{DISTANCE};
				
				my $t1 = $res{$vv}->{$task}->{EXECUTION_TIME};
				my $t2 = $res{$vv}->{$task}->{COMPLETED_TIME} - $prevCompletedTime;
				my $T_si = $t1 < $t2 ? $t1 : $t2;
				my $T_ci = $res{$vv}->{$task}->{taskSize} / $res{$vv}->{$task}->{mu_V}; 		
				$sumT_i_ex += $T_si - $T_ci;
				
#				$mu_V = $res{$vv}->{$task}->{mu_V};

				$sumT_i_req += $res{$vv}->{$task}->{ARRIVAL_DIFFERENCE} - $T_ci;
				
				$cpccTime += $T_si;
				$totalComputationTime += $T_ci;
				
			}
			
			$prevCompletedTime = $res{$vv}->{$task}->{COMPLETED_TIME};
			
#			$res{$vv}->{$task}->{BUCKET_DELAY} > 0 and ++$violations;
			++$totalNumberOfTasks;
		}
		my $delSpeed = $sumL_i / $sumT_i_ex;
		$stDeliveredSpeed->add($delSpeed);
		
		my $reqSpeed = $sumL_i / $sumT_i_req;
		$stRequiredSpeed->add($reqSpeed);
		
#		$stDeliveryRatio->add($delSpeed <= 0 || $delSpeed >= $mu_V ? 1 : 0);
		$stDeliveryRatio->add($delSpeed <= 0 || $delSpeed >= $reqSpeed ? 1 : 0);
		
		$totalDistance += $sumL_i;
	}
	

	my $timeRV = $totalDistance/($RV_V * $nrRVs) + (0.52 * $a1 * $totalNumberVVs) / ($RV_V * $nrRVs) + $totalComputationTime / $nrRVs;
#	my $cpccTime = $maxCompletedTime - $minArrivalTime;
	$cpccTime /= $nrRVs;
	
	printf "%.5f;%.5f;%.5f;%.5f;%.5f;%.5f;%.5f;%.5f;%.2f;%.2f;%.5f;%.5f\n",
		$lambda, 1/$stBucketPassedDiff->mean(),
		$stRequiredSpeed->mean(), $stDeliveredSpeed->mean(),
		$stDeliveryRatio->mean(), $violations/$totalNumberOfTasks,
		$TBsz, $RV_V, $totalDistance, $timeRV, $cpccTime, $timeRV / $cpccTime;

	$buggerIt and warn "buggerit! $lambda";
}


0;


###############################################################################
package Statistics;
use strict;

sub new {
	my $classname = shift;
	my $me = bless { SUM_X2 => 0, SUM_X => 0, N => 0, VVS => {} }, $classname;
	$me->{CFG} = shift;
	return $me;
}

sub add {
	my $me = shift;
	map { $me->{SUM_X} += $_; $me->{SUM_X2} += $_ * $_; $me->{N}++ } @_;
}

sub variance {
	my $me = shift;
#	printf "X=%.3f, X^2=%.3f\n", $me->{SUM_X}, $me->{SUM_X2};
	($me->{SUM_X2} - $me->{SUM_X} * $me->{SUM_X} / $me->{N}) / ($me->{N} - 1);
}

sub mean {
	my $me = shift;
	$me->{SUM_X} / $me->{N};
}

sub count {
	my $me = shift;
	$me->{N};
}

sub add2 {
	my $me = shift;
	my $vv = shift;
	my $task = shift;
	$task != 0 && $task != 19 and return;
	my $line = shift;
	my $ix = $task == 0 ? 'first' : 'last';
	$me->{VVS}->{$vv}->{$ix} = $line->{BUCKET_PASSED_TIME};
}

sub lambda {
	my $me = shift;
	
	my $sum = 0;
	my $counter = 0;
	for my $vv (sort keys %{$me->{VVS}}) {
	   my $first = $me->{VVS}->{$vv}->{first};
	   defined $first or next;
	   my $last = $me->{VVS}->{$vv}->{last};
	   defined $last or next;
#	   $sum += 19/($last-$first);
           $sum += 20/$last;
	   ++$counter;
	#   printf "%4d  %.3f  \n", $vv, 99/($last-$first);
	}
	
	$counter ? $sum / $counter : -100;
}

1;
