package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle{
	 private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

	    // Servicios inyectados (se podrían pasar por config() o crear aquí)
	    private final DatabaseService dbService;
	    // private final MqttService mqttService;

	    // public HttpServerVerticle(DatabaseService dbService, MqttService mqttService) {
	       public HttpServerVerticle(DatabaseService dbService) {
	        this.dbService = dbService;
	       // this.mqttService = mqttService;
	    }

	    @Override
	    public void start(Promise<Void> startPromise) {
	        LOGGER.info("Starting HTTP Server Verticle...");

	        // Crear Handlers (inyectando servicios)
	        CrudHandler crudHandler = new CrudHandler(dbService); // Necesita ser implementado
	        // BusinessLogicHandler businessLogicHandler = new BusinessLogicHandler(dbService, mqttService);
	        BusinessLogicHandler businessLogicHandler = new BusinessLogicHandler(dbService);

	        // Configurar el Router
	        Router router = ApiRouter.configureRouter(vertx, crudHandler, businessLogicHandler);

	        // Obtener puerto de la configuración del Verticle (pasada por MainVerticle)
	        int port = config().getInteger("http.port", 8888); // Puerto por defecto 8888

	        // Crear y arrancar el servidor HTTP
	        HttpServer server = vertx.createHttpServer();
	        server.requestHandler(router)
	              .listen(port)
	              .onSuccess(httpServer -> {
	                  LOGGER.info("HTTP server started successfully on port {}", port);
	                  startPromise.complete();
	              })
	              .onFailure(err -> {
	                  LOGGER.error("Failed to start HTTP server on port {}", port, err);
	                  startPromise.fail(err);
	              });
	    }
	}
