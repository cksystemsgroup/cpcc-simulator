#parse( "html/prologue.vm" )

<div id="wrapper">
#parse( "html/header.vm" )

	<div id="page" class="container">
#parse( "html/status/status.vm" )
#parse( "html/status/sidebar.vm" )
	</div>
	<!-- end #page -->
</div>

#include( "html/epilogue.vm" )