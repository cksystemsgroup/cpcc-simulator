#!/usr/bin/perl

use strict;
use Time::HiRes qw(usleep);

my $cfg = {
   url => 'http://localhost:9040/engine/vehicle/text/vehicleUpload',
   minLat => 47.82028159,
   maxLat => 47.82376633,
   minLon => 13.03839827,
   maxLon => 13.04324663,
   altitude => 5.0,
   tolerance => 2.0,
   sensors => [qw {Picture Temperature Airpressure}],
   lambda => 5,
   vehicleCount => 200,
   vvFile => 'gen-VV.zip',
};

my $vv = new VirtualVehicle ($cfg);

for (my $counter=1, my $l=$cfg->{vehicleCount}; $counter <= $l; ++$counter) {
   my $id = sprintf "%03d", $counter;
   print "Creating virtual vehicle $id\n";
   $vv->create($id);
   $vv->save($cfg->{vvFile});
   qx{ curl -o /dev/null -F file=\@$cfg->{vvFile} $cfg->{url} 2>&1 };
   my $t = 2 * $cfg->{lambda} * rand();
   printf "Sleeping %.2f seconds.\n", $t;
   usleep 1000000 * $t;
}

0;


################################################################################
package VirtualVehicle;
use strict;
use Archive::Zip qw( :ERROR_CODES :CONSTANTS );

sub new {
   my $classname = shift;
   my $self = bless {PRG => undef, PROP => undef}, $classname;
   $self->{CFG} = shift;
   return $self;
}

sub rnd {
   my $a = shift;
   my $b = shift;
   $a + ($b-$a)*rand();
}

sub create {
   my $self = shift;
   my $id = shift;

   $self->{PRG} = sprintf "Point %.8f %.8f %.1f tolerance %.1f %s\n",
      rnd($self->{CFG}->{minLat}, $self->{CFG}->{maxLat}),
      rnd($self->{CFG}->{minLon}, $self->{CFG}->{maxLon}),
      $self->{CFG}->{altitude},
      $self->{CFG}->{tolerance},
      join(' ',@{$self->{CFG}->{sensors}});

   $self->{PROP} = sprintf "vehicle.id=VV %s\nvehicle.creation.time=%d", $id, time();
}

sub save {
   my $self = shift;
   my $fileName = shift;

   my $zip = Archive::Zip->new();
   my $member = $zip->addString($self->{PRG}, 'vehicle.prg');
   $member->desiredCompressionMethod(COMPRESSION_DEFLATED);
   my $member = $zip->addString($self->{PROP}, 'vehicle.properties');
   $member->desiredCompressionMethod(COMPRESSION_DEFLATED);
   $zip->writeToFileNamed($fileName) == AZ_OK or die "Can not write file $fileName";
}

1;
