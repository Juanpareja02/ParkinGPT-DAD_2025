package parkingpt;

import io.vertx.core.Promise;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class ParkingSenderBroadcast extends AbstractVerticle{
	public void start(Promise<Void> startFuture) {
		EventBus eventBus=getVertx().eventBus();
		getVertx().setPeriodic(2000,_id->{
			eventBus.publish("mensaje-broadcast","Soy Broadcast, ¿alguien me escucha?");
		});
	}
	
	public void stop(Promise<Void> stopFuture) throws Exception {
		getVertx().undeploy(ParkingConsumerBroadcast.class.getName());
		super.stop(stopFuture);
	}
}
