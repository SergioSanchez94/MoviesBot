package com.sergiosanchez.bot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import com.sergiosanchez.configuration.Config;

@SpringBootApplication
public class MoviesBotApplication {

	public static void main(String[] args) {
		Config.initConfig();

		NotificationsThread notificationsThread = new NotificationsThread();
		notificationsThread.setName("NotificationsThread");

		notificationsThread.start();

		ApiContextInitializer.init();
		TelegramBotsApi botsApi = new TelegramBotsApi();
		try {
			botsApi.registerBot(new MoviesBot());
		} catch (TelegramApiRequestException e) {
			e.printStackTrace();
		}
	}
}
