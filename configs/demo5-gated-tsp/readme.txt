Demonstration 5: Gated Traveling Salesman Problem (TSP)
--------------------------------------------------------

In this demonstration four Real Vehicles (RVs) are assigned to separate square
cells. The cells are:

Cell 1: Latitude from 47.82202396 N to 47.82376633 N and
        longitude from 13.03839827 E to 13.04082245 E.

Cell 2: Latitude from 47.82202396 N to 47.82376633 N and
        longitude from 13.04082245 E to 13.04324663 E.

Cell 3: Latitude from 47.82028159 N to 47.82202396 N and
        longitude from 13.03839827 E to 13.04082245 E.

Cell 4: Latitude from 47.82028159 N to 47.82202396 N and
        longitude from 13.04082245 E to 13.04324663 E.

The RVs are assigned to the cells as they register with the Mapper web
application and each RV serves one cell exclusively and independently.  The
depot of the each RV is the center of its assigned cell.

Each RV serves the Virtual Vehicle (VV) Action Points (APs) its own cell
according to a gated-TSP policy as follows:

 (1) With no APs to process, the RV returns to its depot and waits there for
     new APs to appear.

 (2) With APs to process, the gated-TSP mapping algorithm takes a snapshot of
     the unprocessed APs in a RV's cell and calculates a TSP tour of those
     APs and assigns a new flight plan to the RV.  The first waypoint of this
     tour is the current position of the RV and the last waypoint is the RV's
     depot position.  While RVs fly their calculated tours, the mapping
     algorithm ignores any newly arriving APs in the according cells.

 (3) After reaching the last AP of it's tour, a RV returns to its depot if no
     more APs are to be processed (step 1) or continues to process APs (step 2).


In this demonstration a small script creates 199 VVs with one randomly
generated action point within the given cells and uploads the VVs to the
central engine according to a Poisson process with arrival rate of one VV per 5
seconds.

In Picture demo5-snapshot.png and Video demo5-gated-tsp.avi green downward
arrows display an active AP.  To the right of each arrow, it is a Gray
rectangle that shows the name of the according VV and the sensors left to
process.  The pictured APs show the icons of a camera, a thermometer, and a
barometer that need processing.

Initially, the VVs idle on the central Engine and wait for the mapping
algorithm to assign an eligible RV.  The algorithm considers the current flight
plan segments of the RVs.  If the current AP of a VV is on a RV's flight plan
segment, the algorithm initiates a migration of this VV to the RV.  After a VV
has completed its mission, i.e., all APs have been processed, the mapping
algorithm initiates a migration back to the central Engine.

Picture demo5-snapshot.png shows an advanced stage of this demonstration
mission displaying four VVs in action.  Green lines show VVs carried by RVs and
red lines indicate migrations of VVs among RVs.


Colors:
------
- Black: physical helicopters (RVs)
- Green Lines: VVs carried by physical helicopters.
- Red Lines: VV cyber moves.
- Blue Lines: intended VV paths.
- Gray Lines: physical helicopter set courses.
- Gray Squares: cell boundaries.
- Blue Circles: not completed VV action points, i.e., unfinished data collection.
- Red Circles: completed VV action points, i.e., all data collected.
- Yellow Text: names of active VVs
- Red Text: names of flying helicopters


Files:
-----
demo5-gated-tsp.avi
    Demonstration video (speed up).

demo5-snapshot.png
    Demonstration in action.

readme.txt
    This file.


