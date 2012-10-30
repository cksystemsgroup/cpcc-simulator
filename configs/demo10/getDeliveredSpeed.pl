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
my $lambda = undef;
my $nrTasks = 20;

print "lambda;E[v_Rn];E[v_D];P(delivered speed > required speed);r_v\n";

foreach my $f (@ARGV) {
 	print STDERR "Processing file $f\n";
 	$f =~ m/.*(0\.\d+).*/ and $lambda  =$1;
 	
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
	my $buggerIt = 0;
	my $violations = 0;
	my $totalNumberOfTasks = 0;
	
	foreach my $vv (sort keys %res) {
		my $t = [];
		foreach my $task (sort keys %{$res{$vv}}) {
			push @$t, [$task, $res{$vv}->{$task}->{COMPLETED_TIME}];
			
			my $g = $res{$vv}->{$task}->{gamma};
			my $nb = $res{$vv}->{$task}->{nb};		# nb after task
			my $sz = $res{$vv}->{$task}->{TBsz};
			2*$g + $nb > $sz and ++$violations;
		}
 
		if (@$t != $nrTasks) {
			printf STDERR "VV $vv has %2d tasks, but should have %2d tasks. Ignoring VV $vv\n", ~~@$t, $nrTasks;
			next;
		}
		
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
		
	}
	

	
	printf "%.5f;%.5f;%.5f;%.5f;%.5f\n", $lambda, $stRequiredSpeed->mean(), $stDeliveredSpeed->mean(), $stDeliveryRatio->mean(), $violations/$totalNumberOfTasks;

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
