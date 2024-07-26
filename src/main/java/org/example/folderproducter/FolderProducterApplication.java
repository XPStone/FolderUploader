package org.example.folderproducter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FolderProducterApplication {

    public static void main(String[] args) {
        SpringApplication.run(FolderProducterApplication.class, args);
    }

}
