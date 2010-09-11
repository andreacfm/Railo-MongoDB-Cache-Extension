<cfcache action="flush">
<cfcache action="put" id="peter" value="Peter">
<cfcache action="put" id="susi" value="Susanna" timespan="#createTimeSpan(0,0,0,2)#">
<cfset sleep(3000)>
<cfcache action="get" id="susi" name="s">
<cfcache action="get" id="peter" name="p">

<cf_valueEquals left="#isDefined('s')#" right="false">
<cf_valueEquals left="#isDefined('p')#" right="true">

<cfcache action="flush">
<cfcache action="put" id="peter" value="Peter">
<cfcache action="put" id="susi" value="Susanna" idletime="#createTimeSpan(0,0,0,2)#">
<cfset sleep(3000)>
<cfcache action="get" id="susi" name="s">
<cfcache action="get" id="peter" name="p">

<cf_valueEquals left="#isDefined('s')#" right="false">
<cf_valueEquals left="#isDefined('p')#" right="true">
