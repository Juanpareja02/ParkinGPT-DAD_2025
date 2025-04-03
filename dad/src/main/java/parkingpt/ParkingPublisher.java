package parkingpt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class ParkingPublisher extends AbstractVerticle{
	public void start(Promise<Void> startFuture) {
		String name1 = ParkingConsumerBroadcast.class.getName();
		getVertx().deployVerticle(name1, deployResult -> {
			if (deployResult.succeeded()) {
				System.out.println(name1 + " (" + deployResult.result() + ") ha sido desplegado correctamente");
			} else {
				deployResult.cause().printStackTrace();
			}
		});
		
		String name2 = ParkingSenderBroadcast.class.getName();
		getVertx().deployVerticle(name2, deployResult -> {
			if (deployResult.succeeded()) {
				System.out.println(name2 + " (" + deployResult.result() + ") ha sido desplegado correctamente");
			} else {
				deployResult.cause().printStackTrace();
			}
		});
	}

	public void stop(Promise<Void> stopFuture) throws Exception {
		getVertx().undeploy(ParkingConsumerBroadcast.class.getName());
		getVertx().undeploy(ParkingSenderBroadcast.class.getName());
		super.stop();
	}
}
