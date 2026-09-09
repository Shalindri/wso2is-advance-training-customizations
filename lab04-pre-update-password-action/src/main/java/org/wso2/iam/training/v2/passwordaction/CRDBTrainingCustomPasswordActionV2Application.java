package org.wso2.iam.training.v2.passwordaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Starts the web service. Nothing to change in here.
 * Run it with:  java -jar target/lab04-pre-update-password-action-v2-2.0.0.jar
 */
@SpringBootApplication
public class CRDBTrainingCustomPasswordActionV2Application {

    public static void main(String[] args) {

        SpringApplication.run(CRDBTrainingCustomPasswordActionV2Application.class, args);
    }
}
