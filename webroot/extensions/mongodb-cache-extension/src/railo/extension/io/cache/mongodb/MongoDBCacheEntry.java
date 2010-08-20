package railo.extension.io.cache.mongodb;

import java.util.Date;

import railo.commons.io.cache.CacheEntry;
import railo.extension.util.Functions;
import railo.loader.engine.CFMLEngine;
import railo.loader.engine.CFMLEngineFactory;
import railo.runtime.exp.PageException;
import railo.runtime.type.Struct;

public class MongoDBCacheEntry implements CacheEntry {
	
	private MongoDBCacheDocument doc; 
	private Functions func = new Functions();
	
	public MongoDBCacheEntry(MongoDBCacheDocument doc) {
		this.doc = doc;
	}
	
	@Override
	public Date created() {
		Date date = new Date(new Long(doc.getCraetedOn()));
		return date;
	}

	@Override
	public Struct getCustomInfo() {
		Struct metadata = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
		metadata.setEL("hits", hitCount());
		return metadata;
	}

	@Override
	public String getKey() {
		String key = doc.getKey();
		return key;
	}

	@Override
	public Object getValue() {
		try{
			return func.evaluate(doc.getData());
		}
		catch(PageException e){
			e.printStackTrace();
			return "";
		}	
	}

	@Override
	public int hitCount() {
		String hits = doc.getHits();
		return Integer.parseInt(doc.getHits());
	}

	@Override
	public long idleTimeSpan() {
		return new Long(doc.getTimeIdle());
	}

	@Override
	public Date lastHit() {
		return new Date(new Long(doc.getLastAccessed()));
	}

	@Override
	public Date lastModified() {
		return new Date(new Long(doc.getLastUpdated()));
	}

	@Override
	public long liveTimeSpan() {
		return new Long(doc.getExpires());
	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
