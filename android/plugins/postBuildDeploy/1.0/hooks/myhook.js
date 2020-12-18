exports.init = function (logger, config, cli, nodeappc) 
{
    cli.on("build.pre.construct", function () 
	{
		//var rv = exec("bash foo.sh");
		//if (rv.code) 
		{
			logger.error("Plugin hook build.pre.construct");
			process.exit();
		}
          
    });
	
	cli.on("build.finalize", function () 
	{
		//var rv = exec("bash foo.sh");
		//if (rv.code) 
		{
			logger.error("Plugin hook build.finalize");
			process.exit();
		}
          
    });
};