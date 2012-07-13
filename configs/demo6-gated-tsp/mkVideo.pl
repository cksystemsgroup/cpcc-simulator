#!/usr/bin/perl

use strict;
use Time::HiRes qw(usleep);


my $url = 'http://localhost:8080/pilot/sensor/photo';


my $ctr = 0;

while (1) {
   my $img = sprintf "img-%05d.png", $ctr++;
   print ".";
#  print "get image $img\n";
   qx { wget -O $img $url > /dev/null 2>&1 };
   usleep 100000;
}
