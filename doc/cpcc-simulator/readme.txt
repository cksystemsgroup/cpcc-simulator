
CPCC Simulator README
=====================

1. Download
   --------

Download the current simulator and patches from
http://cs.uni-salzburg.at/~ckrainer/CPCC/cpcc-simulator


2. Installation
   ------------

Choose a directory in a filesystem with at least 600MB free space and unpack
the simulator by using TAR:

	cd /my/simulator/installdir
	tar -xjf /path/to/archive/cpcc-simulator-*.tar.bz2 

Set the CPCC_SIM_HOME environment variable to the path of your installation
directory and adjust your PATH environment variable as follows:

	export CPCC_SIM_HOME=/my/simulator/installdir
	export PATH=$CPCC_SIM_HOME/bin:$PATH

and add this commands to your ~/.profile and/or ~/.bashrc for future use.


The simulator only has three real vehicles. A fourth real vehicle is available
by installing file tomcat-pilot4-patch-*.tar.bz2 as follows:

	cd $CPCC_SIM_HOME
	tar -xjf /path/to/archive/tomcat-pilot4-patch-*.tar.bz2
	./deploy.sh



3. Startup
   -------

Start the simulator by

	rc_tomcat start All

For simulating belly mounted cameras run

	rc_xvfb


4. Shutdown
   --------

Run the following commands to shutdown the simulator:

	rc_tomcat stop All
	killall -TERM Xvfb firefox


5. Simulation:
   ----------

A newly installed simulator comprises of
- three real vehicles that carry a virtual vehicle run-time environment each,
- a ground station virtual vehicle run-time environment for parking completed
  virtual vehicles,
- four virtual vehicles,
- a virtual vehicle mapper,
- a Google Maps viewer, and
- a mission planner.


5.1 Web interfaces
    --------------

All three real vehicles provide web interfaces.  The base URLs are:

- Heli One: http://localhost:9010/pilot
- Heli Two: http://localhost:9020/pilot
- Heli Three: http://localhost:9030/pilot
- Heli Four: http://localhost:9050/pilot

Each real vehicle carries an associated virtual vehicle run-time environment,
i.e., virtual vehicle engine.  The ground station also runs a virtual vehicle
run-time environment, i.e., the central engine.  The base URLs are:

- Engine 1: http://localhost:9010/engine
- Engine 2: http://localhost:9020/engine
- Engine 3: http://localhost:9030/engine
- Engine 4: http://localhost:9050/engine
- Central Engine: http://localhost:9040/engine

The base URLs for the Google Maps viewer and the mapper are:

- Mapper: http://localhost:9040/mapper
- Google Maps viewer: http://localhost:9040/gmview
- Mission planner: http://localhost:9040/planner


5.2 Running missions
    ----------------

Engine web interface:
--------------------
Check the "Vehicles" page in the central engine web interface.  When starting
the simulator the first time, the central engine loads four virtual vehicles.
Since the central engine has no real vehicle and no sensors attached, all
virtual vehicles remain suspended.

Mapper web interface:
--------------------
The "Configuration" page of the mapper web interface shows the configured
mapping algorithm. By uploading a configuration file a user can change the
algorithm.  Details see below in "Custom mapping algorithms".  The "Status"
page shows the registered engines and available sensors. It also allows
deactivating and reactivating the mapping algorithm.

Real vehicle web interface:
--------------------------
The "Sensors" page views all available sensors and their current values.
Select the "Course" page in all real vehicle web interfaces and click "Start
Course" to fly around.  This page also allows uploading of new courses.

Google Maps viewer web interface:
--------------------------------
Switch to the Google Maps viewer to monitor your missions.


5.3 Configuration and unprocessed virtual vehicles
    ----------------------------------------------

Archive cpcc-sim-config-*.tar.bz2 contains configuration files for all real
vehicles (pilot-*.cfg), all virtual vehicle run-time environments
(engine-*.cfg), mapper (mapper-central.cfg), and Google Maps viewer
(gm-viewer-central.cfg).  Older versions of this archive may contain four
unprocessed virtual vehicles as ZIP files and the real vehicle's flight plans
(RV-*.crs).


5.4 Manual migration
    ----------------

Disable the mapping algorithm in the "Status" page of the mapper web interface.
Select the "Vehicles" page of the engine currently running the virtual vehicles
to be migrated, enter the migration URL of the target engine and press
"Migrate".

The migration URLs for this simulation setup are:
- Engine 1: http://localhost:9010/engine/vehicle/text/vehicleUpload
- Engine 2: http://localhost:9020/engine/vehicle/text/vehicleUpload
- Engine 3: http://localhost:9030/engine/vehicle/text/vehicleUpload
- Engine 4: http://localhost:9050/engine/vehicle/text/vehicleUpload
- Central Engine: http://localhost:9040/engine/vehicle/text/vehicleUpload


5.5 Custom mapping algorithms
    -------------------------

Download file mapper-algorithms-ext-*.bz2 from
http://cs.uni-salzburg.at/~ckrainer/CPCC/cpcc-simulator/
and unpack it in a directory of your choice:

	cd /my/algo/dev/folder
	tar -xjf /path/to/archive/mapper-algorithms-ext-*.bz2


Set your build environment:

	export CPCC_SIM_HOME=/my/simulator/installdir

	export JAVA_HOME=$CPCC_SIM_HOME/.jdk/jdk1.6.0_29-x64
	export JAVA_ROOT=$JAVA_HOME
	export JDK_HOME=$JAVA_HOME
	export JRE_HOME=$JAVA_HOME/jre
	export SDK_HOME=$JAVA_HOME

	export PATH=$CPCC_SIM_HOME/bin:$JAVA_HOME/bin:$PATH


Build your your project:

	mvn --settings=$CPCC_SIM_HOME/.m2/settings.xml clean package


Install your algorithms:

	cp target/mapper-algorithms-ext-0.0.2-SNAPSHOT.jar $CPCC_SIM_HOME/tomcat-central/external_algorithms/mapper-algorithms-0.0.2-SNAPSHOT.jar


Restart the ground station tomcat:

	rc_tomcat restart central


Select the mapper configuration page in your web browser and upload a new
mapper configuration that uses your algorithm.  The cfg sub-directory contains
configurations for the available algorithm of this module.  If desired, create
your own configuration by copying one of these files and change the
mapper.algorithm property to the class name of your algorithm.

Due to a bug, you have to re-register the central engine after uploading mapper
configurations.  To do  this, select the central engine configuration page and
click "Register".  After that, select the mapper status page and check for
"Central Engine URL".

If you want to change the algorithm class path change the "virtualClasspath"
property in file $CPCC_SIM_HOME/tomcat-central/conf/context.xml and restart
the ground station tomcat, as shown above.


6. Software update
   ---------------

To successfully run a sofware update, you have to change the "catalinaBase"
path in the tomcat server deployment configurations. To do this, change the
path /mnt/local_homes/ckrainer to the content of your $CPCC_SIM_HOME variable
in the files $CPCC_SIM_HOME/tomcat-*/pom.xml by using vi:

	vi $CPCC_SIM_HOME/tomcat-*/pom.xml

or by using perl:

	perl -pi -e 's#/mnt/local_homes/ckrainer#'"$CPCC_SIM_HOME"'#;' $CPCC_SIM_HOME/tomcat-*/pom.xml

After that, execute the following commands to update the simulator:

	cd $CPCC_SIM_HOME
	./deploy.sh


8. Internet web sites
   ------------------

Apache log4j:

	http://logging.apache.org/log4j/index.html

Apache Tomcat:

	http://tomcat.apache.org/

Apache Maven:

	http://maven.apache.org/

Eclipse IDE:

	http://www.eclipse.org/

Eclipse IDE plugin update sites:

	http://download.eclipse.org/technology/m2e/releases
	http://subclipse.tigris.org/update_1.6.x

