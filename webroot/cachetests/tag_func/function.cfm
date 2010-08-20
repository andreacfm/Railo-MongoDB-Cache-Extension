<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html><cfset server.enableCache=true>
<head>
	<title>Untitled</title>
</head>

<body>

<cfset variables.abc=1>
<cfset def='def'>

<cfif isDefined('server.railo.version')>
	<cfset intVersion=replace(server.railo.version,'.','','all')>
<cfelse>
	<cfset intVersion=200000>
</cfif>
<cfset productName=server.coldfusion.productname>
<cfset is7 = intVersion gte 110000 or productName NEQ "railo">

<cfset request.is7= is7>
<cfset request.isRailo11= intVersion gte 110000 and productName EQ "railo">
<cfset request.isRailo31= intVersion gte 310000 and productName EQ "railo">

<cfset request.is8= intVersion gte 300000 or productName NEQ "railo">
<cfset runtime=createObject('java','java.lang.Runtime').getRuntime()>


<cfset test=1>
<cfparam name="url.loop" default="1">
<h1>Functions</h1>
<cfset dirName=GetDirectoryFromPath(GetCurrentTemplatePath())&"/funcs/">
<cfoutput>#dirName#</cfoutput>
<cfset xfuncs=structNew()>
<cfdirectory action="LIST" directory="#dirName#" name="dir" filter="*.cfm">
<cfset request.abc=1>

<cfloop query="dir"  ><cfflush>

<cfif not isDefined("url.function") or ListFindNoCase(url.function,listFirst(dir.name,'.'))>
	<cfif dir.name NEQ "mustThrow.cfm" and dir.name NEQ "valueEquals.cfm" and find("_",dir.name) NEQ 1>
			
            <cfsavecontent variable="includeContent">
			<cfoutput>
			<pre>---------------- #dir.name# (#dir.currentrow#) ----------------</pre>
			</cfoutput>
			<cfif request.is8>
				<cfinclude template="function8.cfm">
			</cfif>
			<cfmodule template="funcs/#dir.name#">
		
			<!--- <cftry><cfcatch>
				<cfdump var="#cfcatch#">
			</cfcatch>
			</cftry> --->
		</cfsavecontent>
		<cfif len(trim(includeContent))>
			<cfoutput>
			#includeContent#
			</cfoutput>
		</cfif>
        

	</cfif>
	
<!--- </cf_stopwatch> --->
</cfif></cfloop>
<cfif not isDefined("url.function")>
<cfloop collection="#xfuncs#" item="key" >
	<cfoutput>&lt;cfset allFunctions['#key#']=0&gt;<br></cfoutput>
    <cfflush>
</cfloop>
</cfif>
</body>
</html>
