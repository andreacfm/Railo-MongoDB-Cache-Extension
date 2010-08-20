package railo.extension.io.cache.mongodb;

import com.mongodb.BasicDBObject;

public class MongoDBCacheDocument {

	private String id = "";
	private BasicDBObject dbObject;
	private Long expires;
	
	public MongoDBCacheDocument(BasicDBObject dbObject){
		this.dbObject = dbObject;
		this.id = getId();
	}

	public String getId(){
		return dbObject.getString("_id");
	}

	public void setData(String value) {
		dbObject.put("data", value);
	}
	public String getData(){
		return dbObject.getString("data");
	}

	public void setKey(String value) {
		dbObject.put("key",value);
	}
	
	public String getKey(){
		return dbObject.getString("key");
	}

	public void setCreatedOn(String value) {
		dbObject.put("createdOn",value);
	}
	
	public String getCraetedOn(){
		return dbObject.getString("createdOn");
	}

	public void setLastAccessed(String value) {
		dbObject.put("lastAccessed",value);
	}
	
	public String getLastAccessed(){
		return dbObject.getString("lastAccessed");
	}

	public void setLastUpdated(String value) {
		dbObject.put("lastUpdated",value);
	}
	
	public String getLastUpdated(){
		return dbObject.getString("lastUpdated");
	}
	
	public void setLifeSpan(String value) {
		dbObject.put("lifeSpan",value);
	}
	
	public String getLifeSpan(){
		return dbObject.getString("lifeSpan");
	}
	
	public void setTimeIdle(String value) {
		dbObject.put("timeIdle",value);
	}
	
	public String getTimeIdle(){
		return dbObject.getString("timeIdle");
	}

	public void setHits(String value) {
		dbObject.put("hits",value);
	}
	
	public String getHits(){
		String hits = dbObject.getString("hits");
		return hits;
	}

	public void setExpires(String value) {
		dbObject.put("expires",value);
	}
	
	public String getExpires(){
		return dbObject.getString("expires"); 		
	}

	public BasicDBObject getDbObject(){
		return dbObject;
	}
	
	public void addHit(){
		int hits = Integer.parseInt(getHits());
		hits++;
		setHits(Integer.toString(hits));		
	}


}
