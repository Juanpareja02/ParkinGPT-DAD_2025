package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class MainVerticle extends AbstractVerticle{
	private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting MainVerticle...");

        // 1. Cargar configuración (config.json)
        ConfigRetriever retriever = ConfigLoader.createConfigRetriever(vertx);

        retriever.getConfig()
            .onFailure(err -> {
                LOGGER.error("Failed to load configuration", err);
                startPromise.fail(err);
            })
            .onSuccess(config -> {
                LOGGER.info("Configuration loaded successfully.");

                // 2. Configurar Pool de Conexiones MySQL
                MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                    .setPort(config.getInteger("db.port", 3306))
                    .setHost(config.getString("db.host", "localhost"))
                    .setDatabase(config.getString("db.database", "parkingpt_db"))
                    .setUser(config.getString("db.user", "user"))
                    .setPassword(config.getString("db.password", "password"));

                PoolOptions poolOptions = new PoolOptions().setMaxSize(5); // Ajustar tamaño del pool
                MySQLPool sqlClientPool = MySQLPool.pool(vertx, connectOptions, poolOptions);
                LOGGER.info("MySQL connection pool configured for {}:{}/{}",
                    connectOptions.getHost(), connectOptions.getPort(), connectOptions.getDatabase());

                // 3. Crear instancias de Servicios (con dependencias)
                DatabaseService dbService = DatabaseService.create(sqlClientPool);
                // MqttService mqttService = new MqttServiceImpl(vertx, config); // Descomentar si se usa

                // 4. Opciones de Despliegue (pasando config a los Verticles)
                DeploymentOptions options = new DeploymentOptions().setConfig(config);

                // 5. Desplegar Verticles
                Future<String> httpVerticleDeployment = vertx.deployVerticle(() -> new HttpServerVerticle(dbService), options);
                // Future<String> mqttVerticleDeployment = vertx.deployVerticle(() -> new MqttClientVerticle(mqttService), options); // Descomentar si se usa

                // Esperar a que todos los Verticles se desplieguen
                // Future.all(httpVerticleDeployment, mqttVerticleDeployment) // Añadir más futures si hay más verticles
                   Future.all(httpVerticleDeployment)
                    .onSuccess(v -> {
                        LOGGER.info("All verticles deployed successfully!");
                        startPromise.complete();
                    })
                    .onFailure(err -> {
                        LOGGER.error("Failed to deploy one or more verticles", err);
                        startPromise.fail(err);
                    });
            });
    }

    // Método main para lanzar desde el IDE (opcional, Maven shade plugin es preferible)
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
