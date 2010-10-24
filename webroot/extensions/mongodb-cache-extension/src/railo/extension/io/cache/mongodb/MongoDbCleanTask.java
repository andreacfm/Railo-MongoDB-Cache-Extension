package railo.extension.io.cache.mongodb;

import java.util.TimerTask;

public class MongoDbCleanTask extends TimerTask {
	
	private MongoDBCache cache;
	
	public MongoDbCleanTask(MongoDBCache cache) {
		this.cache = cache;
	}

	@Override
	public void run() {
		try{
			this.cache.flushInvalid();	
		}catch(Exception e){
			//just fails so that task does not die
		}
	}

}
