package com.sergiosanchez.bot;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sergiosanchez.configuration.Config;
import com.sergiosanchez.connections.Library;
import com.sergiosanchez.connections.MoviesAPI;
import com.sergiosanchez.movies.Movie;
import com.vdurmont.emoji.EmojiParser;

/**
 * Clase que arranca un hilo que se encarga de vigilar los métodos dentro de si mismo
 * @author Sergio Sanchez
 *
 */
@SuppressWarnings("deprecation")
public class NotificationsThread extends Thread {

	private static HttpClient client;
	private static HttpClient client2;
	static ArrayList<String> fechasRecomendaciones = new ArrayList<String>();

	public void run() {

		while (true) {

			try {
				NotificationsThread.notifyComplete();
				NotificationsThread.notifyRecommendations();
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Notifica cuando se ha completado el proceso de añadir una película
	 */
	public static void notifyComplete() {

		String estado = null;
		String mensaje = "";

		JSONObject jObject;
		try {
			jObject = new JSONObject(Library.getInfo(Config.getIPADDRESS()));
			JSONArray torrents = jObject.getJSONArray("torrents");
			for (int i = 0; i < torrents.length(); i++) {
				JSONArray resultado = torrents.getJSONArray(i);

				String jsonString = resultado.toString();
				String responseArray[] = jsonString.split(",");

				String hash = responseArray[0];
				hash = hash.replace("\"", "");
				hash = hash.replace("[", "");

				String nombre = responseArray[2].replace("\"", "");
				estado = responseArray[21];

				if (estado.contains("Seeding")) {

					nombre = nombre.substring(0, nombre.indexOf("["));
					nombre = nombre.replace(" ", "%20");

					mensaje = nombre + "%20ya%20se%20ha%20completado%20" + EmojiParser.parseToUnicode(":tada:")
							+ EmojiParser.parseToUnicode(":tada:") + EmojiParser.parseToUnicode(":tada:")
							+ EmojiParser.parseToUnicode(":tada:") + EmojiParser.parseToUnicode(":tada:")
							+ EmojiParser.parseToUnicode(":tada:") + EmojiParser.parseToUnicode(":tada:")
							+ EmojiParser.parseToUnicode(":tada:") + EmojiParser.parseToUnicode(":tada:")
							+ EmojiParser.parseToUnicode(":tada:");

					client = new DefaultHttpClient();

					HttpGet get = new HttpGet("https://api.telegram.org/bot" + Config.getBOTAPIKEY()
							+ "/sendMessage?chat_id=395740029&text=" + mensaje);

					// Delete download
					Library.removeFile(Config.getIPADDRESS(), hash);

					try {
						client.execute(get);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (JSONException e) {
		}
	}

	/**
	 * Notifica cada viernes a las 12 de la mañana las últimas recomendaciones de películas
	 */
	public static void notifyRecommendations() {

		int dia = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
		int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int year = Calendar.getInstance().get(Calendar.YEAR);

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		String diaActual = dateFormat.format(date);

		boolean enviado = false;

		// Los viernes a las 12
		if (dia == 5 && hora == 12) {

			for (String fechaRecomendacion : fechasRecomendaciones) {
				if (fechaRecomendacion.equals(diaActual)) {
					enviado = true;
				}
			}

			if (!enviado) {
				String recomendaciones = "Hola! Hoy comienza el fin de semana :smile:, aqui tienes unas recomiendaciones%3A%0A%0A";
				fechasRecomendaciones.add(diaActual);

				// Recomendaciones semanales
				for (Movie movie : MoviesAPI.getMovies("https://api.themoviedb.org/3/discover/movie?api_key="+Config.getAPIKEY()+"&language=es-ES&primary_release_year="+year)) {
					recomendaciones = recomendaciones + "%20-%20" + movie.getName() + "%0A";
				}
				recomendaciones = recomendaciones.replace(" ", "%20");
				recomendaciones = recomendaciones.replace(",", "%2C");
				recomendaciones = recomendaciones.replace("!", "%21");
				// recomendaciones = recomendaciones.replace(":", "%3A");

				client2 = new DefaultHttpClient();

				HttpGet get = new HttpGet("https://api.telegram.org/bot" + Config.getBOTAPIKEY()
						+ "/sendMessage?chat_id=395740029&text=" + EmojiParser.parseToUnicode(recomendaciones));

				try {
					client2.execute(get);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
