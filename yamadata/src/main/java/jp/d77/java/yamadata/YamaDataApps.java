package jp.d77.java.yamadata;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YamaDataApps {
    private static String[] staticArgs;

    public static void main(String[] args) {
        staticArgs = args;
		SpringApplication.run(YamaDataApps.class, args);
    }

    public static Optional<String> getFilePath(){
		if ( YamaDataApps.staticArgs == null ) return Optional.empty();
		if ( YamaDataApps.staticArgs.length <= 0 ) return Optional.empty();
		return Optional.ofNullable( YamaDataApps.staticArgs[0] );
	}
}
