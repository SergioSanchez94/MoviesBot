package com.sergiosanchez.bot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import com.sergiosanchez.configuration.Config;

/**
 * Se encarga de inicializar la configuración del Bot en base a un fichero externo y arrancar el Bot junto con
 * el hilo que vigila ciertos cambios del aplicativo
 * @author Sergio Sanchez
 *
 */
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
