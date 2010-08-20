
<cfset server.enableCache=true>
<cflock scope="server" timeout="1">


	<cfset cacheName="sample">
	<cfset cacheRemove(arrayToList(cacheGetAllIds()))>
	
	<cfset cachePut('abc','123')>
	<cfset cachePut('def','123')>
    <cf_valueEquals left="#ListSort(arrayToList(cacheGetAllIds()),'textnocase')#" right="ABC,DEF" cs=true>
    
<cfif server.ColdFusion.ProductName EQ "railo"> 
	<cfset cacheClear()>   
	<cfset cachePut('abc','123')>
	<cfset cachePut('abd','123')>
	<cfset cachePut('def','123')>
    <cf_valueEquals left="#ListSort(arrayToList(cacheGetAllIds("ab*")),'textnocase')#" right="ABC,ABD" cs=true>
    <cf_valueEquals left="#ListSort(arrayToList(cacheGetAllIds("ab*")),'textnocase')#" right="ABC,ABD" cs=true>
</cfif>
</cflock>