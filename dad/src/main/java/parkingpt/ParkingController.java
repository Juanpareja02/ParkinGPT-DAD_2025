package parkingpt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class ParkingController extends AbstractVerticle{
	public void start(Promise<Void> startFuture) {
		vertx.createHttpServer().requestHandler(r -> {
	          r.response().end("<h1>Bienvenido a ParkinGPT</h1>"
	          + "La aplicacion para aparcar realizado por matrakas y para matrakas");
	        })
	        .listen(8089, result -> {
	        	if (result.succeeded()) {
	                startFuture.complete();
	                } else {
	            startFuture.fail(result.cause());
	                }

	        });
	
	vertx.deployVerticle(ParkingSenderHeader.class.getName());
	vertx.deployVerticle(ParkingConsumerHeader.class.getName());
	}
}