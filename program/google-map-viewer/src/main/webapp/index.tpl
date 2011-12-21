#parse( "html/prologue.vm" )

<div id="wrapper">
#parse( "html/header.vm" )

	<div id="page" class="container">
#include( "html/welcome.vm" )
#parse( "html/sidebar.vm" )
	</div>
	<!-- end #page -->
</div>

<script type="text/javascript">
onLoad(); Event.observe(window, 'unload', GUnload());
</script>

#include( "html/epilogue.vm" )
