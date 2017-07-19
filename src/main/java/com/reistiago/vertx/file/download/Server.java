package com.reistiago.vertx.file.download;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class Server extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private String filePath;

    @Override
    public void start() throws Exception {

        Router router = Router.router(vertx);
        router.route(HttpMethod.GET, "/hello").handler(this::hello);
        router.route(HttpMethod.GET, "/download").handler(this::download);

        this.filePath = this.config().getString("file");

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        LOGGER.info("running");
    }

    private void download(RoutingContext routingContext) {
        vertx.fileSystem().open(this.filePath, new OpenOptions(), readEvent -> {

            if (readEvent.failed()) {
                routingContext.response().setStatusCode(500).end();
                return;
            }

            AsyncFile asyncFile = readEvent.result();

            routingContext.response().setChunked(true);

            Pump pump = Pump.pump(asyncFile, routingContext.response());

            pump.start();

            asyncFile.endHandler(aVoid -> {
                asyncFile.close();
                routingContext.response().end();
            });
        });
    }

    private void hello(RoutingContext routingContext) {
        routingContext.response().end("hello\n");
    }

    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            LOGGER.info("Missing file path");
            return;
        }

        Vertx.vertx(new VertxOptions()
                .setClustered(false)
                .setEventLoopPoolSize(1)
                .setInternalBlockingPoolSize(1)
                .setWorkerPoolSize(1))
                .deployVerticle(new Server(), new DeploymentOptions().setConfig(new JsonObject().put("file", args[0])));
    }
}
