#!/usr/bin/perl

use strict;


my $m = new SpreadSheetMerger;

########################################################################
# TB=41, RvV=05
########################################################################

$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=05/fcfs-TB=41-RvV=05.ds.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=05/gtsp-TB=41-RvV=05.ds.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=05/nn-TB=41-RvV=05.ds.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 0,	X_label => 'Arrival rate \\lambda',
	Y => 1,	Y_label => 'Mean delivered speed E[v^D]',
	ML_FIG_LABEL => 'Mean delivered speed with increasing arrival rate',
	ML_PREFIX => 'FIG_5_05',
});

$m->saveAsCsv('results-20121021/mds-TB_41-RvV_05.csv');
$m->saveAsMatlab('results-20121021/mds-TB_41-RvV_05.m');



$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=05/fcfs-TB=41-RvV=05.ds.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=05/gtsp-TB=41-RvV=05.ds.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=05/nn-TB=41-RvV=05.ds.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 0,	X_label => 'Arrival rate \\lambda',
	Y => 2,	Y_label => 'P(delivered speed > virtual speed)',
	ML_FIG_LABEL => 'Delivery ratio with increasing arrival rate',
	ML_PREFIX => 'FIG_6_05',
});

$m->saveAsCsv('results-20121021/delr-TB_41-RvV_05.csv');
$m->saveAsMatlab('results-20121021/delr-TB_41-RvV_05.m');




$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=05/fcfs-TB=41-RvV=05.ms.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=05/gtsp-TB=41-RvV=05.ms.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=05/nn-TB=41-RvV=05.ms.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 1,	X_label => 'Arrival rate \\lambda',
	Y => 4,	Y_label => 'E[T]',
	ML_FIG_LABEL => 'Expectation of system time with increasing arrival rate',
	ML_PREFIX => 'FIG_7_05',
});

$m->saveAsCsv('results-20121021/syst-TB_41-RvV_05.csv');
$m->saveAsMatlab('results-20121021/syst-TB_41-RvV_05.m');




$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=05/fcfs-TB=41-RvV=05.ms.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=05/gtsp-TB=41-RvV=05.ms.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=05/nn-TB=41-RvV=05.ms.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 1,	X_label => 'Arrival rate \\lambda',
	Y => 6,	Y_label => 'E[T]',
	ML_FIG_LABEL => 'Expectation of system time with increasing arrival rate',
	ML_PREFIX => 'FIG_8_05',
});

$m->saveAsCsv('results-20121021/stdd-TB_41-RvV_05.csv');
$m->saveAsMatlab('results-20121021/stdd-TB_41-RvV_05.m');



########################################################################
# TB=41, RvV=10
########################################################################

$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=10/fcfs-TB=41-RvV=10.ds.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=10/gtsp-TB=41-RvV=10.ds.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=10/nn-TB=41-RvV=10.ds.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 0,	X_label => 'Arrival rate \\lambda',
	Y => 1,	Y_label => 'Mean delivered speed E[v^D]',
	ML_FIG_LABEL => 'Mean delivered speed with increasing arrival rate',
	ML_PREFIX => 'FIG_5_10',
});

$m->saveAsCsv('results-20121021/mds-TB_41-RvV_10.csv');
$m->saveAsMatlab('results-20121021/mds-TB_41-RvV_10.m');



$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=10/fcfs-TB=41-RvV=10.ds.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=10/gtsp-TB=41-RvV=10.ds.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=10/nn-TB=41-RvV=10.ds.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 0,	X_label => 'Arrival rate \\lambda',
	Y => 2,	Y_label => 'P(delivered speed > virtual speed)',
	ML_FIG_LABEL => 'Delivery ratio with increasing arrival rate',
	ML_PREFIX => 'FIG_6_10',
});

$m->saveAsCsv('results-20121021/delr-TB_41-RvV_10.csv');
$m->saveAsMatlab('results-20121021/delr-TB_41-RvV_10.m');




$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=10/fcfs-TB=41-RvV=10.ms.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=10/gtsp-TB=41-RvV=10.ms.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=10/nn-TB=41-RvV=10.ms.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 1,	X_label => 'Arrival rate \\lambda',
	Y => 4,	Y_label => 'E[T]',
	ML_FIG_LABEL => 'Expectation of system time with increasing arrival rate',
	ML_PREFIX => 'FIG_7_10',
});

$m->saveAsCsv('results-20121021/syst-TB_41-RvV_10.csv');
$m->saveAsMatlab('results-20121021/syst-TB_41-RvV_10.m');




$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=10/fcfs-TB=41-RvV=10.ms.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=10/gtsp-TB=41-RvV=10.ms.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=10/nn-TB=41-RvV=10.ms.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 1,	X_label => 'Arrival rate \\lambda',
	Y => 6,	Y_label => 'E[T]',
	ML_FIG_LABEL => 'Expectation of system time with increasing arrival rate',
	ML_PREFIX => 'FIG_8_10',
});

$m->saveAsCsv('results-20121021/stdd-TB_41-RvV_10.csv');
$m->saveAsMatlab('results-20121021/stdd-TB_41-RvV_10.m');



########################################################################
# TB=41, RvV=15
########################################################################

$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=15/fcfs-TB=41-RvV=15.ds.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=15/gtsp-TB=41-RvV=15.ds.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=15/nn-TB=41-RvV=15.ds.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 0,	X_label => 'Arrival rate \\lambda',
	Y => 1,	Y_label => 'Mean delivered speed E[v^D]',
	ML_FIG_LABEL => 'Mean delivered speed with increasing arrival rate',
	ML_PREFIX => 'FIG_5_15',
});

$m->saveAsCsv('results-20121021/mds-TB_41-RvV_15.csv');
$m->saveAsMatlab('results-20121021/mds-TB_41-RvV_15.m');



$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=15/fcfs-TB=41-RvV=15.ds.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=15/gtsp-TB=41-RvV=15.ds.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=15/nn-TB=41-RvV=15.ds.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 0,	X_label => 'Arrival rate \\lambda',
	Y => 2,	Y_label => 'P(delivered speed > virtual speed)',
	ML_FIG_LABEL => 'Delivery ratio with increasing arrival rate',
	ML_PREFIX => 'FIG_6_15',
});

$m->saveAsCsv('results-20121021/delr-TB_41-RvV_15.csv');
$m->saveAsMatlab('results-20121021/delr-TB_41-RvV_15.m');




$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=15/fcfs-TB=41-RvV=15.ms.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=15/gtsp-TB=41-RvV=15.ms.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=15/nn-TB=41-RvV=15.ms.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 1,	X_label => 'Arrival rate \\lambda',
	Y => 4,	Y_label => 'E[T]',
	ML_FIG_LABEL => 'Expectation of system time with increasing arrival rate',
	ML_PREFIX => 'FIG_7_15',
});

$m->saveAsCsv('results-20121021/syst-TB_41-RvV_15.csv');
$m->saveAsMatlab('results-20121021/syst-TB_41-RvV_15.m');




$m->load({
	charts => [
		{file => 'exp-fcfs-TB=41-RvV=15/fcfs-TB=41-RvV=15.ms.csv',	value => 'FCFS'},
		{file => 'exp-gtsp-TB=41-RvV=15/gtsp-TB=41-RvV=15.ms.csv',	value => 'G-TSP'},
		{file => 'exp-nn-TB=41-RvV=15/nn-TB=41-RvV=15.ms.csv',		value => 'NN'},
	],
	X_ignore => [0.005],
	X => 1,	X_label => 'Arrival rate \\lambda',
	Y => 6,	Y_label => 'E[T]',
	ML_FIG_LABEL => 'Expectation of system time with increasing arrival rate',
	ML_PREFIX => 'FIG_8_15',
});

$m->saveAsCsv('results-20121021/stdd-TB_41-RvV_15.csv');
$m->saveAsMatlab('results-20121021/stdd-TB_41-RvV_15.m');















0;




###############################################################################
package SpreadSheetMerger;
use strict;
use Data::Dumper;

sub new {
	my $classname = shift;
	my $me = bless { SPREAD_SHEET => undef, PARAMS => undef }, $classname;
	return $me;
}

sub load {
	my ($me, $p) = @_;
	
	my $spreadSheet = {};
	
	foreach my $f (@{$p->{charts}}) {
		open IN, "<".$f->{file} or die "Can not open file ".$f->{file};
		my @hdr = split /;/, scalar(<IN>);
		while (my $line = <IN>) {
			chomp $line;
			my @c = split /;/, $line;
			exists $spreadSheet->{$c[$p->{X}]} or $spreadSheet->{$c[$p->{X}]} = {};
			$spreadSheet->{$c[$p->{X}]}->{$f->{value}} = $c[$p->{Y}];
#			print "load ", $c[$p->{X}], " -> ", $f->{value}, " = ", $c[$p->{Y}], "\n";
		}
		close IN;
	}
	
#	print Dumper($spreadSheet);
	$me->{SPREAD_SHEET} = $spreadSheet;
	$me->{PARAMS} = $p;
}

sub saveAsCsv {
	my ($me, $file) = @_;
	
	open OUT, "> $file" or die "Can not open file '$file'";
	print "Saving CSV file '$file'\n";
	
	my $spreadSheet = $me->{SPREAD_SHEET};
	
	my %hdrx = ();
	my @hdr = ();
	foreach my $x (sort {$a<=>$b} keys %$spreadSheet) {
		my $values = $spreadSheet->{$x};
		$values or next;
		foreach my $v (sort keys %$values) {
			$hdrx{$v} or push @hdr, $v;
			$hdrx{$v} = 1;
		}
	}
	
	print OUT join(';', $me->{PARAMS}->{X_label}, @hdr), "\n";
	
	foreach my $x (sort {$a<=>$b} keys %$spreadSheet) {
		my @i = grep { $x == $_ } @{$me->{PARAMS}->{X_ignore}};
		@i and next;
		my @cols = ($x); 
		my $values = $spreadSheet->{$x};
		foreach my $v (@hdr) {
			push @cols, $values->{$v};
		}
		print OUT join(';', @cols), "\n";
	}
	
	close OUT;
}

sub saveAsMatlab {
	my ($me, $file) = @_;
	
	open OUT, "> $file" or die "Can not open file '$file'";
	print "Saving Matlab file '$file'\n";
	
	my $spreadSheet = $me->{SPREAD_SHEET};
	
	my %mv = ();
	foreach my $x (sort {$a<=>$b} keys %$spreadSheet) {
		my @i = grep { $x == $_ } @{$me->{PARAMS}->{X_ignore}};
		@i and next;
		my $values = $spreadSheet->{$x};
		foreach my $v (sort {$a<=>$b} keys %$values) {
			exists $mv{$v} or $mv{$v} = {};
			$mv{$v}->{$x} = $values->{$v};
		}
	}
	
	my $pfx = $me->{PARAMS}->{ML_PREFIX};
	my $vi = 0;
	my @color = ('k', 'b--', 'r:');
	my ($minX, $maxX, $minY, $maxY) = (1000,0,0,0);

	print OUT "\nfigure\nhold on\n";
	foreach my $h (sort keys %mv) {
		my @xs = (sort {$a<=>$b} keys %{$mv{$h}});
		my @ys = ();
		foreach my $x (@xs) {
			push @ys, $mv{$h}->{$x};
			$minX > $x and $minX = $x;
			$maxX < $x and $maxX = $x;
			$minY > $mv{$h}->{$x} and $minY = $mv{$h}->{$x};
			$maxY < $mv{$h}->{$x} and $maxY = $mv{$h}->{$x};
		}
		printf OUT "%s_X_%d = [%s]\n", $pfx, $vi, join(' ',@xs);
		printf OUT "%s_Y_%d = [%s]\n", $pfx, $vi, join(' ',@ys);
		printf OUT "plot(%s_X_%d(1:%d),%s_Y_%d(1:%d),'%s','LineWidth',3)\n",
			$pfx, $vi, ~~@xs, $pfx, $vi, ~~@ys, $color[$vi];
		++$vi;
	}
	
	print OUT "hold off\n";
	print OUT "legend('FCFS','G-TSP','NN')\n";
	print OUT "xlabel('",$me->{PARAMS}->{X_label},"', 'FontSize',20)\n";
	print OUT "ylabel('",$me->{PARAMS}->{Y_label},"', 'FontSize',20)\n";
	print OUT "%title('",$me->{PARAMS}->{ML_FIG_LABEL},"', 'FontSize',20)\n";
#	print OUT "%axis([0.1 0.7 0 0.35])";
	printf OUT "%axis([%.3f %.3f %.3f %.3f])", $minX, $maxX, $minY, $maxY;
	print OUT "grid\n";
	
	
	close OUT;
}

1;

