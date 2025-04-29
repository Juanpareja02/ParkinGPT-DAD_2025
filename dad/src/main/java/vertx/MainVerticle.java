package vertx;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
// Importa tu MqttVerticle
// Importa tus otros Verticles (API CRUD, API Lógica)

public class MainVerticle extends AbstractVerticle {

    private MySQLPool dbPool;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // --- 1. Cargar Configuración (Ejemplo) ---
        JsonObject config = config(); // Asume que la config está disponible

        // --- 2. Crear Pool de Conexiones a BD ---
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(config.getInteger("db_port", 3306))
                .setHost(config.getString("db_host", "localhost"))
                .setDatabase(config.getString("db_name", "parkingpt"))
                .setUser(config.getString("db_user", "user"))
                .setPassword(config.getString("db_password", "password"));

        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        dbPool = MySQLPool.pool(vertx, connectOptions, poolOptions);

        // --- 3. Crear Router (si este Verticle también maneja HTTP) ---
        Router router = Router.router(vertx);
        // Configura tus rutas REST aquí (llamando a los Verticles/Handlers de API)

        // --- 4. Desplegar Verticles ---
        // Pasar configuración y pool de BD a los Verticles que lo necesiten
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        // Desplegar Verticle MQTT
        // ¡Importante! Pasa el dbPool al constructor si lo necesita para las suscripciones
        vertx.deployVerticle(new MqttVerticle(dbPool), options)
            .onSuccess(mqttDeploymentId -> {
                logger.info("MqttVerticle deployed successfully with ID: {}", mqttDeploymentId);

                // Desplegar otros Verticles (API Lógica, CRUD) después de que MQTT esté listo o en paralelo
                // vertx.deployVerticle(new ApiLogicVerticle(dbPool), options);
                // vertx.deployVerticle(new CrudApiVerticle(dbPool), options);

                // --- 5. Iniciar Servidor HTTP (si aplica) ---
                 vertx.createHttpServer()
                     .requestHandler(router) // Si tienes rutas REST
                     .listen(config.getInteger("http_port", 8888))
                     .onSuccess(server -> {
                          logger.info("HTTP server started on port " + server.actualPort());
                          startPromise.complete();
                     })
                     .onFailure(startPromise::fail);

            })
            .onFailure(err -> {
                 logger.error("Failed to deploy MqttVerticle", err);
                 startPromise.fail(err);
            });
    }
}