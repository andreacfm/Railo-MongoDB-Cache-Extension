<cfsetting requesttimeout="180">

<cfset total = 0>

<cfloop from="1" to="10" index="j">
	<cfset start = gettickcount()>
	<cfloop from="1" to="1000" index="i">
		<cfcache action="put" id="a#i#" value="#i#">
		<cfcache action="get" id="a#i#" name="v">
	</cfloop>
	<cfset end = gettickcount()>

	<cfset time = end -start>
	<cfset total = total + time >
	<cfoutput>#time# millis<br/></cfoutput>
	<cfflush>
</cfloop>

<cfoutput>Average : #total/10#</cfoutput>
