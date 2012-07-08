#!/usr/bin/perl
#
# @(#) RV flight plan to VV program converter
#

use strict;
use Archive::Zip qw( :ERROR_CODES :CONSTANTS );

my $name = undef;
my @prg = map { sprintf "Point %s %s %s tolerance 5 Picture Temperature Airpressure\n", @$_ }
grep {$_} map { m/## \@\(#\) set course (\S+)(\s+\S+)?.*/ and $name = $1.$2; m/fly to\((\d+\.?\d*),(\d+\.?\d*),(\d+\.?\d*)\).*/i and $_ = [$1,$2,$3] or undef } <>;

my $zip = Archive::Zip->new();
my $string_member = $zip->addString( join('',@prg), 'vehicle.prg' );
$string_member->desiredCompressionMethod( COMPRESSION_DEFLATED );

$name ||= "nix-$$";
my $string_member = $zip->addString( "\nvehicle.id=$name\n\n", 'vehicle.properties' );
$string_member->desiredCompressionMethod( COMPRESSION_DEFLATED );


my $file = $name.'.zip';
$file =~ s/\s+/-/g;
$zip->writeToFileNamed($file) == AZ_OK or die "Can not write $file";
print "File $file written successfully\n".

0;
