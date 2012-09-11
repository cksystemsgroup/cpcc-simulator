#parse( "html/prologue.vm" )

<div id="wrapper">
#parse( "html/header.vm" )

	<div id="page" class="container">
#parse( "html/vehicle/vehicle.vm" )
#parse( "html/vehicle/sidebar.vm" )
	</div>
	<!-- end #page -->
</div>

#include( "html/epilogue.vm" )