package dev.mathuias.chessrepertoire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class ChessRepertoireApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChessRepertoireApplication.class, args);
		log.info("Chess Repertoire Application started successfully.");
	}

}
