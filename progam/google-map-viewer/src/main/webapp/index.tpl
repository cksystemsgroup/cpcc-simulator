#parse( "html/prologue.vm" )

<div id="wrapper">
#parse( "html/header.vm" )

	<div id="page" class="container">
#include( "html/welcome.vm" )
#parse( "html/sidebar.vm" )
	</div>
	<!-- end #page -->
</div>

#include( "html/epilogue.vm" )

<script type="text/javascript">
onLoad(); Event.observe(window, 'unload', GUnload());
</script>
