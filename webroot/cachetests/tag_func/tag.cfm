
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
	<title>Untitled</title>
</head>

<body>
<h1>Tags</h1><cfset USERIP=REMOTE_ADDR>
<cfdump var="#REMOTE_ADDR#">
<cftry>
<cfset intVersion=replace(server.railo.version,'.','','all')>
	<cfcatch><cfset intVersion=200029></cfcatch>
</cftry>
<cfset productName=server.coldfusion.productname>
<cfdump var="#intVersion#">
<cfset request.is7= intVersion gte 110000 or productName NEQ "railo">
<cfset request.is8= intVersion gte 300000 or productName NEQ "railo">
<cfset request.is31= intVersion gte 310000>
<cfset request.isRailo11= intVersion gte 110000 and productName EQ "railo">



<cfset dirName=GetDirectoryFromPath(GetCurrentTemplatePath())&"/tags/">
<cfoutput>#dirName#</cfoutput>

<cfdirectory action="LIST" directory="#dirName#" name="dirTag" filter="*.cfm">

<cfset request.xTags=structNew()>
<cfset searchDir="/Users/mic/temp/collections/Railo/CFrailoweb0/">
<cfloop query="dirTag"><cfflush>
	<cftry>
		<cfif not isDefined("url.tag") or url.tag EQ listFirst(dirTag.name,".")>
            <cfif dirTag.name NEQ "mustThrow.cfm" and dirTag.name NEQ "valueEquals.cfm" and find("_",dirTag.name) NEQ 1>
            <cfset request.xTags[listFirst(dirTag.name,".")]=1>
                <cfoutput>
                    <pre>---------------- #dirTag.name# (#dirTag.currentrow#) ----------------</pre>
                    </cfoutput>
                <cfsavecontent variable="includeContent">
                <cftry>
                    <cfinclude template="tags/#dirTag.name#">
                    <cfcatch>
                    <cfif isDefined("url.tag")><cfrethrow></cfif>
                    	<cfdump var="#cfcatch#">
                    </cfcatch>
                </cftry>
                </cfsavecontent>
                
                <cfif len(trim("s"&includeContent))>
                    <cfoutput>
                    #includeContent#
                    </cfoutput>
                </cfif>
            </cfif>
        </cfif>
        <cfcatch><cfif  request.is8><cfrethrow></cfif>
        	<cfdump var="#cfcatch#">
        </cfcatch>
    </cftry>
</cfloop>
<!---cfif server.ColdFusion.ProductName EQ "railo"><cfdump var="#serialize(request.xTags)#"></cfif>
<cfloop collection="#request.xTags#" item="key" >
	<cfoutput>&lt;cfset allTags['#key#']=#request.xTags[key]#&gt;<br></cfoutput>
</cfloop--->
</body>
</html>
