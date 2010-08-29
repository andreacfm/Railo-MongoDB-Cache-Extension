<cfcomponent extends="Cache">
    <cfset fields=array(
			field(	displayName="Server Host",
					name="host",
					defaultValue="localhost",
					required=true,
					description="The host that the MongoDB server is running on",
					type="text"
				),
			field(	displayName="Server Port",
					name="port",
					defaultValue="27017",
					required=true,
					description="The port that the MongoDB server is running on",
					type="text"
				),
					
			field(	displayName="Database",
					name="database",
					defaultValue="",
					required=true,
					description="The name of the database on the MongoDB server to use",
					type="text"
				),

			field(	displayName="Collection",
					name="collection",
					defaultValue="",
					required=true,
					description="The name of the collection in the MongoDb database that will be used to store the data.",
					type="text"
				),
										
			field(	displayName="Persists over server restart",
					name="persist",
					defaultValue="",
					required=true,
					description="The name of the collection in the MongoDb database that will be used to store the data.",
					type="checkbox"
				),

			field(	displayName="Persists over server restart",
					name="persist",
					values="true,false",	
					defaultValue=false,
					required=true,
					description="",
					type="radio"
				)								

		)>

	<cffunction name="getClass" returntype="string">
    	<cfreturn "railo.extension.io.cache.mongodb.MongoDBCache">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="no">
    	<cfreturn "MongoDBCache">
    </cffunction>

	<cffunction name="getDescription" returntype="string" output="no">
    	<cfset var c="">
    	<cfsavecontent variable="c">
		This is the MongoDB Cache implementation for Railo. This allows you to cache objects, primitives and queries in a MongoDB server that can be used as a cache. 
        </cfsavecontent>
    	<cfreturn trim(c)>
    </cffunction>

</cfcomponent>