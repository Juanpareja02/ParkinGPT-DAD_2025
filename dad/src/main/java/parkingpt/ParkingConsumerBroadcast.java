package parkingpt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;


public class ParkingConsumerBroadcast extends AbstractVerticle{
	public void start(Promise<Void> startFuture) {
		getVertx().eventBus().consumer("mensaje-broadcast", message ->{
			String customMessage=(String) message.body();
			System.out.println("Mensaje Recibido1: "+customMessage);
		});
	}
	
	public void stop(Promise<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}
}
