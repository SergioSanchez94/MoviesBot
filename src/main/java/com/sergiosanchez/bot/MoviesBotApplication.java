package com.sergiosanchez.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import com.sergiosanchez.configuration.Config;

@SpringBootApplication
public class MoviesBotApplication {

	public static void main(String[] args) {
		Config.initConfig();
		ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
            try {
				botsApi.registerBot(new MoviesBot());
			} catch (TelegramApiRequestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
