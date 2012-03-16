Demonstration 4:
---------------

In this demonstration three Real Vehicles (RVs) fly along their set courses and
four Virtual Vehicles (VVs) collect data at several locations.  Each RV
provides the same set of sensors: a belly mounted camera, a thermometer, and a
barometer.

The blue lines in Picture demo4-all-VVs.png display the virtual paths of the
VVs.  Circles along the VV paths indicate Action Points (APs).  Blue APs
visualize that sensors values need capturing.  Red APs show that all sensor
values have been collected.

Green downward arrows display an active AP.  To the right of each arrow, it is
a grey rectangle that shows the name of the according VV and the sensors left
to process.  The pictured APs show the icons of a camera, a thermometer, and a
barometer that need processing.

Initially, the VVs idle on the central Engine and wait for the mapping
algorithm to assign an eligible RV.  The algorithm considers the current flight
plan segments of the RVs.  If the current AP of a VV is on a RV's flight plan
segment, the algorithm initiates a migration of this VV to the RV.  If there
are several RVs to choose for migration, the algorithm takes the RV that will
reach the AP at first.  After a VV has completed its mission, i.e., all APs
have been processed, the mapping algorithm initiates a migration back to the
central Engine.

Picture demo4-snapshot.png shows an advanced stage of this demonstration
mission displaying two VVs in action.  Green lines show VVs carried by RVs and
red lines indicate migrations of VVs among RVs.


Colors:
------
- Black: physical helicopters (RVs)
- Green Lines: VVs carried by physical helicopters.
- Red Lines: VV cyber moves.
- Blue Lines: intended VV paths.
- Grey Lines: physical helicopter set courses.
- Blue Circles: not completed VV action points, i.e., unfinished data collection.
- Red Circles: completed VV action points, i.e., all data collected.
- Yellow Text: names of active VVs
- Red Text: names of flying helicopters


Files:
-----
demo4-all-VVs.png
    All four Virtual Vehicle paths.

demo4.avi
    Demonstration video (6x speed up).

demo4-VV1.png
    Virtual Vehicle 4.1 intended path, as well as physical and cyber motions.

demo4-VV2.png
    Virtual Vehicle 4.2 intended path, as well as physical and cyber motions.

demo4-VV3.png
    Virtual Vehicle 4.3 intended path, as well as physical and cyber motions.

demo4-VV4.png
    Virtual Vehicle 4.4 intended path, as well as physical and cyber motions.

demo4-snapshot.png
    Snapshot of the demonstration video showing Virtual Vehicle 4.3 partly
    completed and Virtual Vehicle 4.2 unprocessed.

readme.txt
    This file.


