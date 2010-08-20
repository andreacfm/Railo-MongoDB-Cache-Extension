<cflock scope="server" timeout="1">
<cfif server.ColdFusion.ProductName EQ "railo">

	<cfset cacheName="sample">
	<cfset cacheClear()>
	
	<cfset cachePut('abc','123')>
	<cfset cachePut('def','123')>
    <cf_valueEquals left="#ListSort(StructKeyList(cacheGetAll()),'textnocase')#" right="ABC,DEF" cs=true>
    
	<cfset cachePut('abc','123')>
	<cfset cachePut('abd','123')>
	<cfset cachePut('def','123')>
    <cf_valueEquals left="#ListSort(StructKeyList(cacheGetAll("ab*")),'textnocase')#" right="ABC,ABD" cs=true>
    <cf_valueEquals left="#ListSort(StructKeyList(cacheGetAll("ab*")),'textnocase')#" right="ABC,ABD" cs=true>
</cfif>
</cflock>