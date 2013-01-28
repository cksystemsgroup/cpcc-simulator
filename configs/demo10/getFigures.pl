#!/usr/bin/perl

use strict;


my $m = new SpreadSheetMerger;
my @t = (localtime(time()))[5,4,3,2,1,0]; $t[0]+=1900; ++$t[1];
my $dstDir = sprintf "results-%04d%02d%02d", @t;

!-d $dstDir and mkdir($dstDir), printf "Folder $dstDir created.";
-d $dstDir or die "Can not create folder $dstDir";

########################################################################
########################################################################

sub paperVisuals {
	my $tbSZ = sprintf "%02d", $_[0];
	my $speed = sprintf "%02d", $_[1];	
	
	$m->load({
		charts => [
			{file => 'exp-fcfs-TB='.$tbSZ.'-RvV='.$speed.'/fcfs-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',	value => 'FCFS: b=26'},
			{file => 'exp-gtsp-TB='.$tbSZ.'-RvV='.$speed.'/gtsp-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',	value => 'G-TSP: b=26'},
			{file => 'exp-nn-TB='.$tbSZ.'-RvV='.$speed.'/nn-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',		value => 'NN: b=26'},
		],
		X => 2,	X_label => 'Mean required speed E[v^R]',
		Y => 3,	Y_label => 'Mean delivered speed E[v^D]',
		ML_FIG_LABEL => '#VV=250, v^V=10, c=7, #RV=25, v=5, a=250',
		ML_PREFIX => 'FIG_DELIVERED_V1_'.$tbSZ.'_'.$speed,
	});
	
	$m->saveAsCsv($dstDir.'/fig_DeliveredV1-TB_'.$tbSZ.'-RvV_'.$speed.'.csv');
	$m->saveAsMatlab($dstDir.'/fig_DeliveredV1-TB_'.$tbSZ.'-RvV_'.$speed.'.m');
	
	
	$m->load({
		charts => [
			{file => 'exp-fcfs-TB='.$tbSZ.'-RvV='.$speed.'/fcfs-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',	value => 'FCFS: b=26'},
			{file => 'exp-gtsp-TB='.$tbSZ.'-RvV='.$speed.'/gtsp-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',	value => 'G-TSP: b=26'},
			{file => 'exp-nn-TB='.$tbSZ.'-RvV='.$speed.'/nn-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',		value => 'NN: b=26'},
		],
		X => 2,	X_label => 'Mean required speed E[v^D]',
		Y => 4,	Y_label => 'P(v^D_i \geq v^R_i)',
		ML_FIG_LABEL => '#VV=250, v^V=10, c=7, #RV=25, v=5, a=250',
		ML_PREFIX => 'FIG_DELIVERY_R1_'.$tbSZ.'_'.$speed,
	});
	
	$m->saveAsCsv($dstDir.'/fig_DelivereyR1-TB_'.$tbSZ.'-RvV_'.$speed.'.csv');
	$m->saveAsMatlab($dstDir.'/fig_DelivereyR1-TB_'.$tbSZ.'-RvV_'.$speed.'.m');
	
}


paperVisuals  7, 10;
#paperVisuals 41, 10;



########################################################################
########################################################################
sub meanDeliveredSpeed {
	my $tbSZ = sprintf "%02d", $_[0];
	my $speed = sprintf "%02d", $_[1];	


	
	$m->load({
		charts => [
			{file => 'exp-fcfs-TB='.$tbSZ.'-RvV='.$speed.'/fcfs-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',	value => 'FCFS'},
			{file => 'exp-gtsp-TB='.$tbSZ.'-RvV='.$speed.'/gtsp-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',	value => 'G-TSP'},
			{file => 'exp-nn-TB='.$tbSZ.'-RvV='.$speed.'/nn-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',		value => 'NN'},
		],
		X_ignore => [0.005],
		X => 1,	X_label => 'Post arrival rate \\lambda',
		Y => 3,	Y_label => 'Mean delivered speed E[v^D]',
		ML_FIG_LABEL => 'Mean delivered speed with increasing arrival rate',
		ML_PREFIX => 'FIG_5_'.$tbSZ.'_'.$speed,
	});
	
	$m->saveAsCsv($dstDir.'/mds-TB_'.$tbSZ.'-RvV_'.$speed.'.csv');
	$m->saveAsMatlab($dstDir.'/mds-TB_'.$tbSZ.'-RvV_'.$speed.'.m');
	
	
	
	$m->load({
		charts => [
			{file => 'exp-fcfs-TB='.$tbSZ.'-RvV='.$speed.'/fcfs-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',	value => 'FCFS'},
			{file => 'exp-gtsp-TB='.$tbSZ.'-RvV='.$speed.'/gtsp-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',	value => 'G-TSP'},
			{file => 'exp-nn-TB='.$tbSZ.'-RvV='.$speed.'/nn-TB='.$tbSZ.'-RvV='.$speed.'.ds.csv',		value => 'NN'},
		],
		X_ignore => [0.005],
		X => 1,	X_label => 'Post arrival rate \\lambda',
		Y => 4,	Y_label => 'P(delivered speed > virtual speed)',
		ML_FIG_LABEL => 'Delivery ratio with increasing arrival rate',
		ML_PREFIX => 'FIG_6_'.$tbSZ.'_'.$speed,
	});
	
	$m->saveAsCsv($dstDir.'/delr-TB_'.$tbSZ.'-RvV_'.$speed.'.csv');
	$m->saveAsMatlab($dstDir.'/delr-TB_'.$tbSZ.'-RvV_'.$speed.'.m');
	
	
	$m->load({
		charts => [
			{file => 'exp-fcfs-TB='.$tbSZ.'-RvV='.$speed.'/fcfs-TB='.$tbSZ.'-RvV='.$speed.'.ms.csv',	value => 'FCFS'},
			{file => 'exp-gtsp-TB='.$tbSZ.'-RvV='.$speed.'/gtsp-TB='.$tbSZ.'-RvV='.$speed.'.ms.csv',	value => 'G-TSP'},
			{file => 'exp-nn-TB='.$tbSZ.'-RvV='.$speed.'/nn-TB='.$tbSZ.'-RvV='.$speed.'.ms.csv',		value => 'NN'},
		],
		X_ignore => [0.005],
		X => 1,	X_label => 'Arrival rate \\lambda',
		Y => 4,	Y_label => 'E[T]',
		ML_FIG_LABEL => 'Expectation of system time with increasing arrival rate',
		ML_PREFIX => 'FIG_7_'.$tbSZ.'_'.$speed,
	});
	
	$m->saveAsCsv($dstDir.'/syst-TB_'.$tbSZ.'-RvV_'.$speed.'.csv');
	$m->saveAsMatlab($dstDir.'/syst-TB_'.$tbSZ.'-RvV_'.$speed.'.m');

	
	$m->load({
		charts => [
			{file => 'exp-fcfs-TB='.$tbSZ.'-RvV='.$speed.'/fcfs-TB='.$tbSZ.'-RvV='.$speed.'.ms.csv',	value => 'FCFS'},
			{file => 'exp-gtsp-TB='.$tbSZ.'-RvV='.$speed.'/gtsp-TB='.$tbSZ.'-RvV='.$speed.'.ms.csv',	value => 'G-TSP'},
			{file => 'exp-nn-TB='.$tbSZ.'-RvV='.$speed.'/nn-TB='.$tbSZ.'-RvV='.$speed.'.ms.csv',		value => 'NN'},
		],
		X_ignore => [0.005],
		X => 1,	X_label => 'Arrival rate \\lambda',
		Y => 6,	Y_label => 'E[T]',
		ML_FIG_LABEL => 'Expectation of system time with increasing arrival rate',
		ML_PREFIX => 'FIG_8_'.$tbSZ.'_'.$speed,
	});
	
	$m->saveAsCsv($dstDir.'/stdd-TB_'.$tbSZ.'-RvV_'.$speed.'.csv');
	$m->saveAsMatlab($dstDir.'/stdd-TB_'.$tbSZ.'-RvV_'.$speed.'.m');
}

meanDeliveredSpeed  7,  5;
meanDeliveredSpeed  7, 10;
# meanDeliveredSpeed  7, 15;

# meanDeliveredSpeed 15,  5;
meanDeliveredSpeed 15, 10;
# meanDeliveredSpeed 15, 15;

# meanDeliveredSpeed 30,  5;
meanDeliveredSpeed 30, 10;
# meanDeliveredSpeed 30, 15;

meanDeliveredSpeed 41,  5;
meanDeliveredSpeed 41, 10;
meanDeliveredSpeed 41, 15;



########################################################################
# TB=*, RvV=*: required vs. delivered speed
########################################################################

my $figNr = 100;

my @dsrss = glob 'exp-*/*.dsrs.csv';

foreach my $f (sort @dsrss) {
	$f =~ m/(.*)\.csv/;
	my $dst = $1.".m";
	
	$m->load({
		charts => [{file => $f, value => 'E[v^D]'}],
		X_ignore => [0.005],
		X => 2,	X_label => 'E[v^{Rn}]',
		Y => 3, Y_label => 'E[v^D]',
		ML_FIG_LABEL => 'Expectation of delivered speed vs. required speed from customer.',
		ML_PREFIX => sprintf ('FIG_%03d', ++$figNr)
	});
	
	$m->saveAsMatlab($dst);
}


########################################################################
# TB=*, RvV=*: rho vs. required speed
########################################################################


$figNr = 200;

@dsrss = glob 'exp-*/*.dsrs.csv';

foreach my $f (sort @dsrss) {
	$f =~ m/(.*)\.csv/;
	my $dst = $1.".m";
	$dst =~ s/dsrs/rhoRs/;
	
	$m->load({
		charts => [{file => $f, value => 'E[v^{Rn}]'}],
		X_ignore => [0.005],
		X => 1,	X_label => '\rho',
		Y => 2, Y_label => 'E[v^{Rn}]',
		ML_FIG_LABEL => 'Expectation of \rho vs. required speed from customer.',
		ML_PREFIX => sprintf ('FIG_%03d', ++$figNr)
	});
	
	$m->saveAsMatlab($dst);
}


########################################################################
# TB=*, RvV=*: rho vs. delivered speed
########################################################################

$figNr = 200;

@dsrss = glob 'exp-*/*.dsrs.csv';

foreach my $f (sort @dsrss) {
	$f =~ m/(.*)\.csv/;
	my $dst = $1.".m";
	$dst =~ s/dsrs/rhoDs/;
	
	$m->load({
		charts => [{file => $f, value => 'E[v^D]'}],
		X_ignore => [0.005],
		X => 1,	X_label => '\rho',
		Y => 3, Y_label => 'E[v^D]',
		ML_FIG_LABEL => 'Expectation of \rho vs. delivered speed.',
		ML_PREFIX => sprintf ('FIG_%03d', ++$figNr)
	});
	
	$m->saveAsMatlab($dst);
}


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
	@hdr = sort @hdr;
	
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
	print OUT "legend('FCFS: b=26','GTSP: b=26','NN: b=26')\n";
	print OUT "xlabel('",$me->{PARAMS}->{X_label},"', 'FontSize',20)\n";
	print OUT "ylabel('",$me->{PARAMS}->{Y_label},"', 'FontSize',20)\n";
	print OUT "%title('",$me->{PARAMS}->{ML_FIG_LABEL},"', 'FontSize',20)\n";
#	print OUT "%axis([0.1 0.7 0 0.35])";
	printf OUT "%axis([%.3f %.3f %.3f %.3f])", $minX, $maxX, $minY, $maxY;
	print OUT "grid\n";
	
	
	close OUT;
}

1;

