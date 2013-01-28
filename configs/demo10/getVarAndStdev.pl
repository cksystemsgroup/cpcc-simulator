#!/usr/bin/perl

use strict;


# my $st = new Statistics;
# $st->add(12,11,14,16,15);
# printf "avg=%.3f,  var=%.3f,  stddev=%.3f\n", $st->mean(), $st->variance(), sqrt($st->variance());

# VV;ACTION_POINT_NUMBER;LATITUDE;LONGITUDE;DISTANCE;ARRIVAL_TIME;ARRIVAL_DIFFERENCE;BUCKET_PASSED_TIME;BUCKET_DELAY;COMPLETED_TIME;EXECUTION_TIME;taskSize;gamma;mu_V;v_V;nb;TBsz
# VV 0001.00;0;47.69440395;13.38690193;;128;;128;0;166.960000038147;38.960000038147;35.146;0.000;10.000;10.000;0.000;100000

my $res = [];

@ARGV or @ARGV = ('/home/clem/new-jnavigator-workspace/cpcc-project/configs/demo10/rep-fcfs-0.010.csv');

foreach my $f (@ARGV) {
	my $stOld = new Statistics;
	my $stGamma = new Statistics;
	
 	print STDERR "Processing file $f\n";
	my $cmd = "< $f";
	$cmd =~ m/\.bz2/ and $cmd = "bzip2 -cd $f |";
	$cmd =~ m/\.gz/ and $cmd = "gzip -cd $f |";
	
	open IN, $cmd or die "buggerit!";
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
	
		$stOld->add($l{EXECUTION_TIME});
		$stGamma->add($l{gamma});
		
		$stOld->add2($vv, $task, \%l);
	}
	
	close IN;
	
#	printf "Old statistics $f: lambda=%.3f, gamma=%.3f, mean=%.3f, var=%.3f, stddev=%.3f\n", $stOld->lambda(), $stGamma->mean(), $stOld->mean(), $stOld->variance, sqrt($stOld->variance);
#	printf "New statistics $f: lambda=%.3f, gamma=%.3f, mean=%.3f, var=%.3f, stddev=%.3f\n", $stOld->lambda(), $stNew->count(), $stNew->mean(), $stNew->variance, sqrt($stNew->variance);
	$f =~ m/.*(0\.\d+).*/;
	push @$res, [$f, $1, $stOld->lambda(), $stGamma->mean(), $stOld->mean(), $stOld->variance, sqrt($stOld->variance)];
}

print "file;lambda;lambda_TB;gamma;mean;variance;stddev\n";
foreach my $r (@$res) {
	printf "%s;%.5f;%.5f;%.3f;%.3f;%.3f;%.3f\n", @$r;
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
	$me->{N} == 0 and return undef;
	($me->{SUM_X2} - $me->{SUM_X} * $me->{SUM_X} / $me->{N}) / ($me->{N} - 1);
}

sub mean {
	my $me = shift;
	$me->{N} == 0 and return undef;
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

