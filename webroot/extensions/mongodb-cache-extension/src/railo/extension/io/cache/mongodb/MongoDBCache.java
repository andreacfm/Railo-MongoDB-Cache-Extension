package railo.extension.io.cache.mongodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.generic.ISHL;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import railo.commons.io.cache.Cache;
import railo.commons.io.cache.CacheEntry;
import railo.commons.io.cache.CacheEntryFilter;
import railo.commons.io.cache.CacheKeyFilter;
import railo.commons.io.cache.exp.CacheException;
import railo.loader.engine.CFMLEngine;
import railo.loader.engine.CFMLEngineFactory;
import railo.runtime.config.Config;
import railo.runtime.exp.PageException;
import railo.runtime.type.Struct;
import railo.runtime.util.Cast;
import railo.extension.util.*;

public class MongoDBCache implements Cache{

	private String cacheName = "";
	private String host = "";
	private int port = 27017;
	private String database = "";
	private String collectionName = "";
	//Mongo Instance
	private Mongo mongo;
	private DB db;
	private Functions func = new Functions();
	
	//counters
	private int hits = 0;
	private int misses = 0;
	
	public void init(String cacheName, Struct arguments) throws IOException {
		this.cacheName = cacheName;
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		Cast caster = engine.getCastUtil();
	
		try {
			this.host = caster.toString(arguments.get("host"));
			this.port = caster.toIntValue(arguments.get("port"));
			this.database = caster.toString(arguments.get("database"));
			this.collectionName = caster.toString(arguments.get("collection"));
			this.mongo = new Mongo(host,port);
			this.db = mongo.getDB(database);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init(Config config ,String[] cacheName,Struct[] arguments){
		//Not used at the moment
	}
	
	public void init(Config config, String cacheName, Struct arguments) {
		try {
		init(cacheName,arguments);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean contains(String key) {
		DBCollection coll = db.getCollection(collectionName);
		BasicDBObject query = new BasicDBObject();
        query.put("key", key.toLowerCase());
        DBCursor cur = coll.find(query);
		return cur.hasNext();
	}

	@Override
	public List entries() {
		List result = new ArrayList<CacheEntry>();
		DBCursor cur = listAll();
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				MongoDBCacheDocument doc = (MongoDBCacheDocument) cur.next();
				result.add(new MongoDBCacheEntry(doc));
			}			
		}
		
		return result;
	}

	@Override
	public List entries(CacheKeyFilter filter) {
		List result = new ArrayList<CacheEntry>();
		DBCursor cur = listAll();
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				MongoDBCacheDocument doc = (MongoDBCacheDocument) cur.next();
				if(filter.accept(doc.getKey())){
					result.add(new MongoDBCacheEntry(doc));	
				}
			}			
		}		
		return result;
	}

	@Override
	public List entries(CacheEntryFilter filter) {
		List result = new ArrayList<CacheEntry>();
		DBCursor cur = listAll();
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				MongoDBCacheDocument doc = (MongoDBCacheDocument) cur.next();
				MongoDBCacheEntry entry = new MongoDBCacheEntry(doc);
				if(filter.accept(entry)){
					result.add(entry);	
				}
			}			
		}		
		return result;
	}

	@Override
	public MongoDBCacheEntry getCacheEntry(String key) throws CacheException {
		DBCollection coll = db.getCollection(collectionName);
		hits++;
		BasicDBObject query = new BasicDBObject();
        query.put("key", key.toLowerCase());
        //if doc exists but is invalid flush it before read
        flushInvalid(query);
        DBCursor cur = coll.find(query);
        
        if(cur.count() > 0){
        	MongoDBCacheDocument doc = new MongoDBCacheDocument((BasicDBObject) cur.next());
        	doc.addHit();
        	//update the statistic and persist
        	save(doc);
        	return new MongoDBCacheEntry(doc);	
        }
        misses++;
        throw(new CacheException("The document with key [" + key  +"] has not been found int this cache."));
        
 	}

	@Override
	public CacheEntry getCacheEntry(String key, CacheEntry defaultValue) {
		try{
			MongoDBCacheEntry entry = getCacheEntry(key);
			return entry;
		}catch(CacheException e){
			MongoDBCacheDocument doc = new MongoDBCacheDocument(new BasicDBObject());
			try{
				doc.setData(func.serialize(defaultValue.getValue()));
			}catch(PageException px){
				px.printStackTrace();
			}
			return new MongoDBCacheEntry(doc);
		}
	}

	@Override
	public Struct getCustomInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(String key) throws CacheException {
		try{
			MongoDBCacheEntry entry = getCacheEntry(key);
			Object result = entry.getValue();
			return  result;			
		}catch(CacheException e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object getValue(String key, Object defaultValue){
		try{
			Object value = getValue(key);
			return value;	
		}catch(CacheException e){
			return defaultValue;
		}
	}

	@Override
	public long hitCount() {
		return hits;
	}

	@Override
	public List keys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List keys(CacheKeyFilter arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List keys(CacheEntryFilter arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long missCount() {
		return misses;
	}

	@Override
	public void put(String key, Object value, Long idleTime, Long lifeSpan) {
		DBCollection coll = db.getCollection(collectionName);
		Long created = System.currentTimeMillis();
		long idle = idleTime==null?0:idleTime.longValue();
		long life = lifeSpan==null?0:lifeSpan.longValue();

		BasicDBObject obj = new BasicDBObject();
		MongoDBCacheDocument doc = new MongoDBCacheDocument(obj);
		
		try{
			Long span = new Long(life).longValue();
			Long timeIdle = new Long(idle).longValue();
			
			doc.setData(func.serialize(value));
			doc.setCreatedOn(created.toString());
			doc.setTimeIdle(timeIdle.toString());
			doc.setLifeSpan(span.toString());
			doc.setHits("0");
			
			Long expires = span + created;
			if(span == 0){
				doc.setExpires(span.toString());				
			}else{
				doc.setExpires(expires.toString());								
			}

		}
		catch(PageException e){
			e.printStackTrace();
		}
			
		doc.setKey(key.toLowerCase());
		save(doc);
				
	}

	@Override
	public boolean remove(String key) {
		DBCollection coll = db.getCollection(collectionName);
		BasicDBObject query = new BasicDBObject();
		query.put("key", key);
		DBCursor cur = coll.find(query);
		if(cur.hasNext()){
			coll.remove(cur.next());
			return true;
		}
		return false;
	}

	@Override
	public int remove(CacheKeyFilter arg0) {
		DBCollection coll = db.getCollection(collectionName);
		Long counter = coll.getCount();
		/*
		 * We drop the collection. This will be recreated at the very next request.
		 * This is really faster than looping the collection. 
		 */
		coll.drop();	
		return counter.intValue();
	}

	@Override
	public int remove(CacheEntryFilter arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List values(CacheKeyFilter arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List values(CacheEntryFilter arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void flushInvalid(BasicDBObject query){
		DBCollection coll = db.getCollection(collectionName);
		Long now = System.currentTimeMillis();
		BasicDBObject q = (BasicDBObject) query.clone();
		//add to the query the terms to check if the item/items are not valid. Anything returned must be flushed.
		q.append("$where","this.expires > 0 && this.expires > " + now + " && this.timeIdle > 0 && this.timeIdle + this.lastAccessed > " + now);
		
		//execute the query
		DBCursor cur = coll.find(q);
		
		System.out.println(cur.count());
		if(cur.count() > 0){
			while(cur.hasNext()){
				DBObject doc = cur.next();
				coll.remove(doc);
			}						
		}
		
	}
	
	private void save(MongoDBCacheDocument doc){
		DBCollection coll = db.getCollection(collectionName);
		
		Long now = System.currentTimeMillis();
		
		doc.setLastAccessed(now.toString());
		doc.setLastUpdated(now.toString());	
		doc.addHit();
		/*
		 *  very atomic updated. Just the changed values are sent to db.
		 *  If the doc do not exists is inserted.
		 */
		BasicDBObject q = new BasicDBObject("key",doc.getKey());
		coll.update(q, doc.getDbObject(),true,false);
	}
	
	private DBCursor listAll(){
		DBCollection coll = db.getCollection(collectionName);
		// remove invalid docs
		flushInvalid(new BasicDBObject());
		return coll.find();
		
	}
	
}
