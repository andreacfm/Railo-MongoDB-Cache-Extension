package railo.extension.io.cache.mongodb;

import com.mongodb.BasicDBObject;

public class MongoDBCacheDocument {

	private BasicDBObject dbObject;
	
	public MongoDBCacheDocument(BasicDBObject dbObject){
		this.dbObject = dbObject;
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

	public void setCreatedOn(int value) {
		dbObject.put("createdOn",value);
	}
	
	public String getCraetedOn(){
		return dbObject.getString("createdOn");
	}

	public void setLastAccessed(int value) {
		dbObject.put("lastAccessed",value);
	}
	
	public String getLastAccessed(){
		return dbObject.getString("lastAccessed");
	}

	public void setLastUpdated(int value) {
		dbObject.put("lastUpdated",value);
	}
	
	public String getLastUpdated(){
		return dbObject.getString("lastUpdated");
	}
	
	public void setLifeSpan(int value) {
		dbObject.put("lifeSpan",value);
	}
	
	public String getLifeSpan(){
		return dbObject.getString("lifeSpan");
	}
	
	public void setTimeIdle(int value) {
		dbObject.put("timeIdle",value);
	}
	
	public String getTimeIdle(){
		return dbObject.getString("timeIdle");
	}

	public void setHits(int value) {
		dbObject.put("hits",value);
	}
	
	public int getHits(){
		int hits = dbObject.getInt("hits",0);
		return hits;
	}

	public void setExpires(int value) {
		dbObject.put("expires",value);
	}
	
	public String getExpires(){
		return dbObject.getString("expires"); 		
	}

	public BasicDBObject getDbObject(){
		return dbObject;
	}
	
	public void addHit(){
		int hits = getHits();
		hits++;
		setHits(hits);		
	}


}
