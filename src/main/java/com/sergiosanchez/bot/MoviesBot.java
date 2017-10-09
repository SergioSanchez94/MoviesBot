package com.sergiosanchez.bot;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.sergiosanchez.configuration.Config;
import com.sergiosanchez.connections.Downloader;
import com.sergiosanchez.connections.Library;
import com.sergiosanchez.connections.MoviesAPI;
import com.sergiosanchez.movies.Cast;
import com.sergiosanchez.movies.Movie;
import com.sergiosanchez.utils.Util;
import com.vdurmont.emoji.EmojiParser;

/**
 * Se encarga de gestionar las distintas peticiones del chat así como de
 * devolver los distintos tipos de respuesta usando el resto de clases del
 * paquete connections
 * 
 * @author Sergio Sanchez
 *
 */
public class MoviesBot extends TelegramLongPollingBot {

	// Variables que almacenan el estado de las busquedas
	public static ArrayList<Movie> movies = new ArrayList<Movie>();
	public static Movie movieSeleccionada;
	public static ArrayList<String> listaOpciones = new ArrayList<String>();
	public static String busqueda;

	@Override
	public String getBotUsername() {
		return Config.getBOTNAME();
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {

			// Creacion del mensaje
			SendMessage message = new SendMessage();

			// Borra los teclados de optiones en caso de que estén abiertos
			ReplyKeyboardRemove keyboardremove = new ReplyKeyboardRemove();
			keyboardremove.setSelective(true);
			message.setReplyMarkup(keyboardremove);

			// Recoge el Id del chat
			message.setChatId(update.getMessage().getChatId());

			// Obtiene una lista de películas del año corriente (en pruebas)
			if (update.getMessage().getText().startsWith("Recomiendame")) {

				String mensaje = ":first_place_medal: Te recomiendo estas últimas películas :movie_camera::\n\n";

				int currentYear = Calendar.getInstance().get(Calendar.YEAR);

				for (Movie movie : MoviesAPI.getMovies("https://api.themoviedb.org/3/discover/movie?api_key="
						+ Config.getAPIKEY() + "&language=es-ES&primary_release_year=" + currentYear)) {
					mensaje = mensaje + " - " + movie.getName() + "\n";
				}

				message.setText(EmojiParser.parseToUnicode(mensaje));

				// Busca y obtiene una lista de peliculas
			} else if (update.getMessage().getText().contains("Busca ")
					|| update.getMessage().getText().equals("Enseñame la lista otra vez")) {

				movies = new ArrayList<Movie>();
				movieSeleccionada = null;
				listaOpciones = new ArrayList<String>();
				ArrayList<String> keyboardButtons = new ArrayList<String>();

				if (update.getMessage().getText().equals("Enseñame la lista otra vez")) {
					// No es necesario buscar en el String otra vez porque ya
					// tenemos la busqueda almacenada
				} else {
					// Buscamos en el mensaje la película
					busqueda = update.getMessage().getText().substring(6, update.getMessage().getText().length());
				}

				try {
					movies = Downloader.searchMovie(busqueda, Config.getDOMAIN());
					int contador = 1;

					if (movies.size() != 0) {
						ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

						for (Movie movie : movies) {

							keyboardButtons.add(contador + ". " + movie.getName() + " (" + movie.getQuality() + ")");
							listaOpciones.add(contador + ". " + movie.getName() + " (" + movie.getQuality() + ")");
							contador++;
						}

						List<KeyboardRow> list = Util.generateKeyboard(keyboardButtons, true);

						keyboard.setKeyboard(list);
						message.setReplyMarkup(keyboard);

						String smile_emoji = EmojiParser.parseToUnicode("Aqui tienes los resultados :smiley:");
						message.setText(smile_emoji);
					} else {
						message.setText("No se han encontrado resultados");
					}

				} catch (MalformedURLException e) {
					message.setText(
							"La página de la que obtenemos la información de las películas no se encuentra disponible en este momento, intentalo más tarde.");
				}

				//Añade la pelicula a la libreria
			} else if (update.getMessage().getText().equals("Si, añádela")) {
				
				String mensaje;

				try {
					//Obtiene la url de la pelicula seleccionada
					String urlMovie = Downloader.getMovie(Config.getIPADDRESS(), movieSeleccionada.getUrl(),
							Config.getDOMAIN());
					//Añade la película a la librería en base a su URL
					Library.addFile(Config.getIPADDRESS(), "http://" + Config.getDOMAIN() + "" + urlMovie);
					
					mensaje = "La película " + movieSeleccionada.getName() + " se ha enviado a tu biblioteca :file_folder:";
					
				} catch (Exception e) {
					mensaje = "Ha habido un error al intentar añadir " + movieSeleccionada.getName() + " a la biblioteca";
				}

				message.setText(mensaje);

				// Elimina la información de las variables
				Util.deleteData();

			} else if (update.getMessage().getText().equals("No, esa no")) {

				message.setText("OK, no añadiré " + movieSeleccionada.getName());

				// Elimina la información de las variables
				Util.deleteData();

			} else if (update.getMessage().getText().equals("Ver Sinopsis")) {

				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

				// Coge la descripcion del primer resultado de la búsqueda
				message.setText(" - Sinopsis: " + movieSeleccionada.getDescription());

				// Genera un teclado de opciones
				ArrayList<String> optionsKeyboard = new ArrayList<String>();
				optionsKeyboard.add("Si, añádela");
				optionsKeyboard.add("No, esa no");
				optionsKeyboard.add("Ver Sinopsis");
				optionsKeyboard.add("Ver Casting");
				optionsKeyboard.add("Enseñame la lista otra vez");
				keyboard.setKeyboard(Util.generateKeyboard(optionsKeyboard, false));
				message.setReplyMarkup(keyboard);

			} else if (update.getMessage().getText().equals("Ver Casting")) {

				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
				String mensaje = "Aqui tienes el casting de " + movieSeleccionada.getName() + ":\n\n";
				
				ArrayList<Cast> castList = MoviesAPI.getCastList(movieSeleccionada.getId());
				for (Cast cast : castList) {
					mensaje = mensaje + " - " + cast.getActor() + " como " + cast.getCharacter() + "\n";
				}

				// Coge la descripcion del primer resultado de la búsqueda
				message.setText(mensaje);

				// Genera un teclado de opciones
				ArrayList<String> optionsKeyboard = new ArrayList<String>();
				optionsKeyboard.add("Si, añádela");
				optionsKeyboard.add("No, esa no");
				optionsKeyboard.add("Ver Sinopsis");
				optionsKeyboard.add("Ver Casting");
				optionsKeyboard.add("Enseñame la lista otra vez");
				keyboard.setKeyboard(Util.generateKeyboard(optionsKeyboard, false));
				message.setReplyMarkup(keyboard);	
			
			} else if (update.getMessage().getText().equals("Cancelar")) {

				message.setText("Vale!");

				// Elimina la información de las variables
				Util.deleteData();

			} else if (update.getMessage().getText().equals("Dime el estado")) {

				String estado = null;
				int init = 0;
				String mensaje = "";
				String estadoTorrent = "";

				JSONObject jObject;
				try {
					jObject = new JSONObject(Library.getInfo(Config.getIPADDRESS()));
					JSONArray torrents = jObject.getJSONArray("torrents");
					mensaje = "Aquí tienes el estado de tu biblioteca:\n\n";

					if (torrents.length() != 0) {
						for (int i = 0; i < torrents.length(); i++) {
							JSONArray resultado = torrents.getJSONArray(i);

							String jsonString = resultado.toString();
							String responseArray[] = jsonString.split(",");

							String nombre = responseArray[2].replace("\"", "");
							nombre = nombre.substring(0, nombre.indexOf("[") - 1);
							estado = responseArray[21];

							if (estado.contains("Paused")) {
								init = estado.indexOf("Paused") + 6;
								estadoTorrent = "pausada";
							} else if (estado.contains("Seeding")) {
								init = estado.indexOf("Seeding") + 7;
								estadoTorrent = "completada";
							} else if (estado.contains("Downloading")) {
								init = estado.indexOf("Downloading") + 11;
								estadoTorrent = "en proceso";
							} else if (estado.contains("Stopped")) {
								init = estado.indexOf("Stopped") + 7;
								estadoTorrent = "parada";
							}

							String porcentaje = estado.substring(init, estado.length());
							porcentaje = porcentaje.replace(",", "");
							porcentaje = porcentaje.replace("\"", "");

							mensaje = mensaje + " - " + nombre + " está " + estadoTorrent + " con un "
									+ porcentaje.trim() + "\n\n";

						}
					} else {
						mensaje = "No hay novedades";
					}

				} catch (JSONException e) {
					mensaje = "No hay novedades";
				}

				message.setText(mensaje);

				// Elimina la información de las variables
				Util.deleteData();

			} else {

				if (listaOpciones != null) {

					for (String Opcion : listaOpciones) {

						if (update.getMessage().getText().equals(Opcion)) {

							int numero;

							String numeroString = update.getMessage().getText().substring(0, 3);
							numeroString = numeroString.replace(".", "");
							numero = Integer.parseInt(numeroString.trim()) - 1;

							try {
								movieSeleccionada = Downloader.getMovieInfo(movies.get(numero).getUrl(),
										Config.getDOMAIN());

								movieSeleccionada.setName(movies.get(numero).getName());
								movieSeleccionada.setQuality(movies.get(numero).getQuality());
								movieSeleccionada.setUrl(movies.get(numero).getUrl());

								String trailer = MoviesAPI.getTrailer(busqueda, movieSeleccionada.getDate());

								String mensaje = EmojiParser.parseToUnicode(
										"Esta es la información de la película que has seleccionado:\n\n"
												+ ":movie_camera: " + movieSeleccionada.getName() + " :popcorn:\n"
												+ " - Fecha: " + movieSeleccionada.getDate() + " :date:\n"
												+ " - Calidad: " + movieSeleccionada.getQuality() + " :thumbsup:\n"
												+ " - Tamaño: " + movieSeleccionada.getSize() + " :dvd:\n"
												+ " - Trailer: " + trailer + "\n\n"
												+ "¿Quieres añadirla a tu biblioteca? (Aquí te dejo un trailer por si no te decides :wink:)\n\n");

								message.setText(mensaje);
								
								// Pasa la busqueda a formato URL
								String busquedaURL = busqueda.replace(" ", "%20");

								// Hace la busqueda
								ArrayList<Movie> movieSearchAPI = new ArrayList<Movie>();
								movieSearchAPI = MoviesAPI.getMovies(
										"https://api.themoviedb.org/3/search/movie?api_key=274474733b6e36dfdf3406071a9a4ae6&language=es-ES&query="
												+ busquedaURL + "&Spain&year=" + movieSeleccionada.getDate() + "&page=1'");
								
								movieSeleccionada.setDescription(movieSearchAPI.get(0).getDescription());
								movieSeleccionada.setId(movieSearchAPI.get(0).getId());

								try {
									SendPhoto sendPhoto = new SendPhoto();
									sendPhoto.setChatId(update.getMessage().getChatId());
									sendPhoto.setPhoto(movieSeleccionada.getImg());
									sendPhoto(sendPhoto);
								} catch (TelegramApiException e) {
									e.printStackTrace();
								}

								ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
								
								// Genera un teclado de opciones
								ArrayList<String> optionsKeyboard = new ArrayList<String>();
								optionsKeyboard.add("Si, añádela");
								optionsKeyboard.add("No, esa no");
								optionsKeyboard.add("Ver Sinopsis");
								optionsKeyboard.add("Ver Casting");
								optionsKeyboard.add("Enseñame la lista otra vez");
								keyboard.setKeyboard(Util.generateKeyboard(optionsKeyboard, false));
								message.setReplyMarkup(keyboard);

							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						} else {

						}
					}

				} else {

				}

			}

			try {
				sendMessage(message);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public String getBotToken() {
		return Config.getBOTAPIKEY();
	}

}
