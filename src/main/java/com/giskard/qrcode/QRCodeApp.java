package com.giskard.qrcode;

import com.giskard.qrcode.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Controller
@EnableCaching
@SpringBootApplication
@CrossOrigin(methods = {RequestMethod.GET})
public class QRCodeApp {

    public static final String QRCODE_ENDPOINT = "/qrcode";

    @Autowired
    QRCodeService imageService;

    public static void main(String[] args) {
        SpringApplication.run(QRCodeApp.class, args);
    }

    @GetMapping(value = QRCODE_ENDPOINT, produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<ResponseEntity<byte[]>> getQRCode(@RequestParam(value = "text", required = true) String text) {
        return imageService.generateQRCode(text).map(imageBuff ->
                ResponseEntity.ok().cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES)).body(imageBuff)
        );
    }

    @Bean
    public RouterFunction<ServerResponse> indexRouter(@Value("classpath:/static/index.html") final Resource indexHtml) {
        return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(500).contentType(MediaType.TEXT_PLAIN).body(ex.getMessage());
    }
}