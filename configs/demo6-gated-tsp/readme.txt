Demonstration 6: Gated Traveling Salesman Problem (TSP)
--------------------------------------------------------

In this demonstration seven Real Vehicles (RVs) are assigned to separate
hexagonal cells. The cells are:

Cell 1:
    (47.69243237 N, 13.38507593 E), (47.69243237 N, 13.38573986 E),
    (47.69216517 N, 13.38599735 E), (47.69189796 N, 13.38573986 E),
    (47.69189796 N, 13.38507593 E), (47.69216517 N, 13.38481844 E),
    (47.69243237 N, 13.38507593 E)

Cell 2:
    (47.69296678 N, 13.38503838 E), (47.69296678 N, 13.38573039 E),
    (47.69269958 N, 13.38599735 E), (47.69243237 N, 13.38573986 E),
    (47.69243237 N, 13.38507593 E), (47.69269958 N, 13.38481844 E),
    (47.69296678 N, 13.38503838 E)

Cell 3:
    (47.69323399 N, 13.38599735 E), (47.69323399 N, 13.38666128 E),
    (47.69296678 N, 13.38692824 E), (47.69269958 N, 13.38666128 E),
    (47.69269958 N, 13.38599735 E), (47.69296678 N, 13.38573039 E),
    (47.69323399 N, 13.38599735 E)

Cell 4:
    (47.69269958 N, 13.38599735 E), (47.69269958 N, 13.38666128 E),
    (47.69243237 N, 13.38692824 E), (47.69216517 N, 13.38666128 E),
    (47.69216517 N, 13.38599735 E), (47.69243237 N, 13.38573986 E),
    (47.69269958 N, 13.38599735 E)

Cell 5:
    (47.69216517 N, 13.38599735 E), (47.69216517 N, 13.38666128 E),
    (47.69189796 N, 13.38692824 E), (47.69163076 N, 13.38666128 E),
    (47.69163076 N, 13.38599735 E), (47.69189796 N, 13.38573986 E),
    (47.69216517 N, 13.38599735 E)

Cell 6:
    (47.69296678 N, 13.38692824 E), (47.69296678 N, 13.38758270 E),
    (47.69269958 N, 13.38784019 E), (47.69243237 N, 13.38758270 E),
    (47.69243237 N, 13.38692824 E), (47.69269958 N, 13.38666128 E),
    (47.69296678 N, 13.38692824 E)

Cell 7:
    (47.69243237 N, 13.38692824 E), (47.69243237 N, 13.38758270 E),
    (47.69216517 N, 13.38784019 E), (47.69189796 N, 13.38758270 E),
    (47.69189796 N, 13.38692824 E), (47.69216517 N, 13.38666128 E),
    (47.69243237 N, 13.38692824 E)


The RVs are assigned to the cells as they register with the Mapper web
application. Each RV serves one cell exclusively and independently.  The depot
of the each RV is the center of its assigned cell.

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


In Picture demo6-snapshot.png and Video demo6-gated-tsp.avi green downward
arrows display an active AP.  To the right of each arrow, it is a Gray
rectangle that shows the name of the according VV and the sensors left to
process.  The pictured APs show the icons of a camera, a thermometer, and a
barometer that need processing.

In this demonstration four VVs collect data at several locations.  Initially,
the VVs idle on the central Engine and wait for the mapping algorithm to assign
an eligible RV.  The algorithm considers the current flight plan segments of
the RVs.  If the current AP of a VV is on a RV's flight plan segment, the
algorithm initiates a migration of this VV to the RV.  After a VV has completed
its mission, i.e., all APs have been processed, the mapping algorithm initiates
a migration back to the central Engine.

Picture demo6-snapshot.png shows an advanced stage of this demonstration mission
displaying three VVs in action.  Green lines show VVs carried by RVs and red
lines indicate migrations of VVs among RVs.

Video demo6-heatmap.avi visualizes the measured temperature values of the APs
as a heatmap overlay.  The values vary from -15 °C (transparent blue) to 35 °C
(red).  Image demo6-heatmap-snapshot.png presents an advanced stage of this
video.


Colors:
------
- Black: physical helicopters (RVs)
- Green Lines: VVs carried by physical helicopters.
- Red Lines: VV cyber moves.
- Blue Lines: intended VV paths.
- Grey Lines: physical helicopter set courses.
- Grey Hexagons: cell boundaries.
- Light Blue Circles: not completed VV action points, i.e., unfinished data collection.
- Red Circles: completed VV action points, i.e., all data collected.
- Yellow Text: names of active VVs
- Red Text: names of flying helicopters
- Gradient from Blue to Red: heat map

Files:
-----
demo6-gated-tsp.avi
    Demonstration video (speed up).

demo6-heatmap.avi
    Demonstration video like demo6-gated-tsp.avi with heatmap visualization enabled.

demo6-snapshot.png
    Demonstration in action.

demo6-heatmap-snapshot.png
    Heatmap demonstration in action.

demo6-info-window.png
    Action point info window.

readme.txt
    This file.


