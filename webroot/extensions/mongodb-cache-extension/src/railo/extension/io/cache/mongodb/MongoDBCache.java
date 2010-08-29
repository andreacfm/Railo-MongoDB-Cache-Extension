package railo.extension.io.cache.mongodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.generic.ISHL;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

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
	private String database = "";
	private String collectionName = "";
	private Boolean persists = false;
	//Mongo Instance
	private Mongo mongo;
	private DB db;
	private DBCollection coll;
	private Functions func = new Functions();
	private MongoOptions opts = new MongoOptions();
	private List<ServerAddress> addr = new ArrayList<ServerAddress>();
	
	//counters
	private int hits = 0;
	private int misses = 0;
	
	public void init(String cacheName, Struct arguments) throws IOException {
		this.cacheName = cacheName;
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		Cast caster = engine.getCastUtil();
	
		try {
			//options
			opts.connectionsPerHost = caster.toIntValue(arguments.get("connectionsPerHost"));
					
			for(int i=1; i < 2; i++){
				String host = "host" + i;
				if(arguments.containsKey(host)){
					addr.add(new ServerAddress(caster.toString(arguments.get(host))));
				}
			}
			
			//create mongo instance
			try{
				this.mongo = new Mongo(addr,opts);
			}catch(MongoException e){
				e.printStackTrace();
			}
			
			this.database = caster.toString(arguments.get("database"));
			this.db = mongo.getDB(database);
			this.collectionName = caster.toString(arguments.get("collection"));
			this.coll = db.getCollection(collectionName);
			this.persists = caster.toBoolean(arguments.get("persists"));
			
			//clean the collection on startup if required
			if(!persists){
				coll.drop();				
			}
			this.coll = db.getCollection(collectionName);
			
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
		BasicDBObject query = new BasicDBObject();
        query.put("key", key.toLowerCase());
        DBCursor cur = coll.find(query);
		return cur.count() > 0;
	}

	@Override
	public List entries() {
		List result = new ArrayList<CacheEntry>();
		DBCursor cur = qAll();
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				MongoDBCacheDocument doc = new MongoDBCacheDocument((BasicDBObject) cur.next());
				result.add(new MongoDBCacheEntry(doc));
			}			
		}
		
		return result;
	}

	@Override
	public List entries(CacheKeyFilter filter) {
		List result = new ArrayList<CacheEntry>();
		DBCursor cur = qAll();
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				MongoDBCacheDocument doc = new MongoDBCacheDocument((BasicDBObject) cur.next());
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
		DBCursor cur = qAll();
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				MongoDBCacheDocument doc = new MongoDBCacheDocument((BasicDBObject) cur.next());
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
		BasicDBObject query = new BasicDBObject();
        query.put("key", key.toLowerCase());
        //if doc exists but is invalid flush it before read
        flushInvalid(query);
        DBCursor cur = coll.find(query);
        
        if(cur.count() > 0){
    		hits++;
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
				if(defaultValue != null){
					doc.setData(func.serialize(defaultValue.getValue()));					
				}
			}catch(PageException px){
				return new MongoDBCacheEntry(doc);
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
			Object value = getValue(key.toLowerCase());
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
		List result = new ArrayList<String>();
		DBCursor cur = qAll_Keys();
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				String key = new MongoDBCacheDocument((BasicDBObject) cur.next()).getKey(); 
				result.add(key);
			}
		}
		return result;
	}

	@Override
	public List keys(CacheKeyFilter filter) {
		List result = new ArrayList<String>();
		DBCursor cur = qAll_Keys();
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				String key = new MongoDBCacheDocument((BasicDBObject) cur.next()).getKey();
				if(filter.accept(key)){
					result.add(key);					
				}
			}
		}
		return result;
	}

	@Override
	public List keys(CacheEntryFilter filter) {
		List result = new ArrayList<String>();
		DBCursor cur = qAll();
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				MongoDBCacheEntry entry = new MongoDBCacheEntry(new MongoDBCacheDocument((BasicDBObject) cur.next()));
				if(filter.accept(entry)){
					result.add(entry.getKey());					
				}
			}
		}
		return result;
	}

	@Override
	public long missCount() {
		return misses;
	}

	@Override
	public void put(String key, Object value, Long idleTime, Long lifeSpan) {
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
			doc.setHits(0);
			
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
		BasicDBObject query = new BasicDBObject();
		query.put("key", key.toLowerCase());
		DBCursor cur = coll.find(query);
		if(cur.hasNext()){
			coll.remove(cur.next());
			return true;
		}
		return false;
	}

	@Override
	public int remove(CacheKeyFilter filter) {
		DBCursor cur = qAll_Keys();
		int counter = 0;
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				String key = new MongoDBCacheDocument((BasicDBObject) cur.next()).getKey(); 
				if(filter.accept(key)){
					coll.remove(cur.next());
					counter++;					
				}
			}
		}
		
		return counter;
	}

	@Override
	public int remove(CacheEntryFilter filter) {		
		DBCursor cur = qAll();
		int counter = 0;
		
		if(cur.count() > 0){
			while(cur.hasNext()){
				MongoDBCacheEntry entry = new MongoDBCacheEntry(new MongoDBCacheDocument((BasicDBObject) cur.next()));
				if(filter.accept(entry)){
					coll.remove(cur.next());
					counter++;					
				}
			}
		}
		
		return counter;
	}

	@Override
	public List values() {
		DBCursor cur = qAll_Values();
		List result = new ArrayList<Object>();
		
		if( cur.count() > 0 ){
			while(cur.hasNext()){
				Object value = new MongoDBCacheDocument((BasicDBObject) cur.next()).getData();
				try{
					result.add(func.evaluate(value));					
				}catch(PageException e){
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}

	@Override
	public List values(CacheKeyFilter filter) {
		DBCursor cur = qAll_Keys_Values();
		List result = new ArrayList<Object>();
		
		if( cur.count() > 0 ){
			while(cur.hasNext()){
				MongoDBCacheDocument doc = new MongoDBCacheDocument((BasicDBObject) cur.next());

				if(filter.accept(doc.getKey())){
					Object value = new MongoDBCacheDocument((BasicDBObject) cur.next()).getData();
					try{
						result.add(func.evaluate(value));					
					}catch(PageException e){
						e.printStackTrace();
					}					
				}
				
			}
		}
		
		return result;
	}

	@Override
	public List values(CacheEntryFilter filter) {
		DBCursor cur = qAll_Keys_Values();
		List result = new ArrayList<Object>();
		
		if( cur.count() > 0 ){
			while(cur.hasNext()){
				MongoDBCacheEntry entry = new MongoDBCacheEntry(new MongoDBCacheDocument((BasicDBObject) cur.next()));

				if(filter.accept(entry)){
					Object value = new MongoDBCacheDocument((BasicDBObject) cur.next()).getData();
					try{
						result.add(func.evaluate(value));					
					}catch(PageException e){
						e.printStackTrace();
					}					
				}
				
			}
		}
		
		return result;
	}
	
	private void flushInvalid(BasicDBObject query){		
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
	
	private DBCursor qAll(){
		// remove invalid docs
		flushInvalid(new BasicDBObject());
		return coll.find();
		
	}

	private DBCursor qAll_Keys(){	
		// remove invalid docs
		flushInvalid(new BasicDBObject());
		//get all entries but retrieve just the keys for better performance
		return coll.find(new BasicDBObject().append("key",1));	
	}

	private DBCursor qAll_Values(){
		// remove invalid docs
		flushInvalid(new BasicDBObject());
		//get all entries but retrieve just the values for better performance
		return coll.find(new BasicDBObject().append("data",1));	
	}

	private DBCursor qAll_Keys_Values(){
		// remove invalid docs
		flushInvalid(new BasicDBObject());
		//get all entries but retrieve just the values for better performance
		return coll.find(new BasicDBObject().append("data",1).append("data",1));	
	}

}
