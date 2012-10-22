#!/usr/bin/perl
use strict;

my $script = './genVVsTB.pl';
my $nrVVs = 250;

my $startTime = time() + 80;

my $cs = {};

sub joinEm {
	print "Child joining\n";
	while (keys %$cs) {
		my $p = wait;
		print "joined $cs->{$p} -> $p\n";
		delete $cs->{$p};
	}
	$cs = {};
}

for (my $k=1; $k <= $nrVVs; ++$k) {
	keys %$cs >= 18 and joinEm;
	printf "Starting upload of VV %03d\n", $k;
	my $pid = fork();
	if ($pid) {
		print "forked $k -> $pid\n";
		$cs->{$pid} = $k;
	} else {
		print qx{ $script $k $startTime };
		exit 0;
	}
}

joinEm;

