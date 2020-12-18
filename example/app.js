/*
 * GLSprite module test
 */


/*
 * Create window with label
 */
var win = Ti.UI.createWindow({backgroundColor:'white'});
var label = Ti.UI.createLabel();
win.add(label);
win.open();

/*
 * Create GLSprite module
 */
var glsprite = require('fr.dzzd.glsprite');
Ti.API.info("GLSprite.test", "Module is => " + glsprite);
label.text = glsprite.example();

Ti.API.info("GLSprite.test","Module exampleProp is => " + glsprite.exampleProp);
glsprite.exampleProp = "This is a test value";

if (Ti.Platform.name == "android") 
{
	var n=0;

	function onDraw(event)
	{
		Ti.API.info("OnDrawCallback" + n++);
		//Ti.API.info(event);
	}
	
	var proxy = glsprite.createExample(
		{/*
			message: "Creating an example Proxy",
			backgroundColor: "red",*/
			width: Ti.UI.FILL,
			height: Ti.UI.FILL,
			top: 0,
			left: 0,
			ondraw: onDraw

		});


	proxy.printMessage("Hello world!");
	proxy.message = "Hi world!.  It's me again.";
	proxy.printMessage("Hello world!");
	win.add(proxy);
	
	//proxy.setCallbacks({ondraw: function(){Ti.API.info("OnDrawCallback");}});
		
	//proxy.setOndraw( function(){Ti.API.info("OnDrawCallback" + n++);});
}

