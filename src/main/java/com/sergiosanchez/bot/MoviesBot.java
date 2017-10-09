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
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import com.sergiosanchez.configuration.Config;
import com.sergiosanchez.connections.MoviesAPI;
import com.sergiosanchez.connections.Downloader;
import com.sergiosanchez.connections.Library;
import com.sergiosanchez.movies.Movie;
import com.vdurmont.emoji.EmojiParser;

/**
 * Se encarga de gestionar las distintas peticiones del chat así como de devolver los distintos
 * tipos de respuesta usando el resto de clases del paquete connections
 * @author Sergio Sanchez
 *
 */
public class MoviesBot extends TelegramLongPollingBot {

	ArrayList<Movie> movies = new ArrayList<Movie>();
	Movie movieSeleccionada;
	ArrayList<String> listaOpciones = new ArrayList<String>();
	String busqueda;

	@Override
	public String getBotUsername() {
		return Config.getBOTNAME();
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {

			SendMessage message = new SendMessage();
			ReplyKeyboardRemove keyboardremove = new ReplyKeyboardRemove();
			keyboardremove.setSelective(true);
			message.setReplyMarkup(keyboardremove);
			message.setChatId(update.getMessage().getChatId());

			if (update.getMessage().getText().startsWith("Recomiendame")) {
				String mensaje = ":first_place_medal: Te recomiendo estas últimas películas :movie_camera::\n\n";
				
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				
				for (Movie movie : MoviesAPI.getMovies("https://api.themoviedb.org/3/discover/movie?api_key="+Config.getAPIKEY()+"&language=es-ES&primary_release_year="+currentYear)) {
					mensaje = mensaje + " - " + movie.getName() + "\n";
				}

				message.setText(EmojiParser.parseToUnicode(mensaje));

				/**
				 * Busca la pelicula y muestra la lista de resultados
				 */
			} else if (update.getMessage().getText().contains("Busca ")
					|| update.getMessage().getText().equals("Enseñame la lista otra vez")) {

				movies = new ArrayList<Movie>();
				movieSeleccionada = null;
				listaOpciones = new ArrayList<String>();

				String search;

				// No es necesario buscar en el String otra vez porque ya tenemos la busqueda
				// almacenada
				if (update.getMessage().getText().equals("Enseñame la lista otra vez")) {
					search = busqueda;
					// Buscamos en el mensaje la película
				} else {
					search = update.getMessage().getText().substring(6, update.getMessage().getText().length());
					busqueda = search;
				}

				try {
					movies = Downloader.searchMovie(search, Config.getDOMAIN());
					ArrayList<KeyboardRow> rows = new ArrayList<KeyboardRow>();
					int contador = 1;

					if (movies.size() != 0) {
						ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

						for (Movie movie : movies) {
							KeyboardRow row = new KeyboardRow();
							KeyboardButton button = new KeyboardButton();
							button.setText(contador + ". " + movie.getName() + " (" + movie.getQuality() + ")");
							listaOpciones.add(contador + ". " + movie.getName() + " (" + movie.getQuality() + ")");
							row.add(button);
							rows.add(row);
							contador++;
						}

						List<KeyboardRow> list = new ArrayList<KeyboardRow>();
						for (KeyboardRow keyboardRow : rows) {
							list.add(keyboardRow);
						}

						KeyboardRow row = new KeyboardRow();
						KeyboardButton button = new KeyboardButton();
						button.setText("Cancelar");
						row.add(button);
						rows.add(row);
						list.add(row);

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

				/**
				 * Obtiene la información de esa película y pregunta si quiere descargarla
				 */
			} else if (update.getMessage().getText().equals("Si, añádela")) {

				try {
					Downloader.getMovie(Config.getIPADDRESS(), movieSeleccionada.getUrl(), Config.getDOMAIN());
					message.setText(EmojiParser.parseToUnicode("La película " + movieSeleccionada.getName()
							+ " se ha enviado a tu biblioteca :file_folder:"));

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				/**
				 * Cancela todas las variables
				 */
			} else if (update.getMessage().getText().equals("No, esa no")) {

				message.setText("OK, no añadiré " + movieSeleccionada.getName());
				movies = null;
				movieSeleccionada = null;
				listaOpciones = null;
				busqueda = null;
			
			} else if (update.getMessage().getText().equals("Ver Sinopsis")) {

				ArrayList <Movie> movieSearch = new ArrayList<Movie>();
				movieSearch = MoviesAPI.getMovies("https://api.themoviedb.org/3/search/movie?api_key=274474733b6e36dfdf3406071a9a4ae6&language=es-ES&query="+busqueda+"&Spain&year="+movieSeleccionada.getDate()+"&page=1'");
				message.setText(" - Sinopsis: " + movieSearch.get(0).getDescription());
				
				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

				KeyboardRow row1 = new KeyboardRow();
				KeyboardRow row2 = new KeyboardRow();
				KeyboardRow row3 = new KeyboardRow();

				KeyboardButton button1 = new KeyboardButton();
				KeyboardButton button2 = new KeyboardButton();
				KeyboardButton button3 = new KeyboardButton();

				button1.setText("Si, añádela");
				button2.setText("No, esa no");
				button3.setText("Enseñame la lista otra vez");

				row1.add(button1);
				row2.add(button2);
				row3.add(button3);

				List<KeyboardRow> list = new ArrayList<KeyboardRow>();

				list.add(row1);
				list.add(row2);
				list.add(row3);

				keyboard.setKeyboard(list);
				message.setReplyMarkup(keyboard);

			} else if (update.getMessage().getText().equals("Cancelar")) {

				message.setText("Vale!");
				movies = null;
				movieSeleccionada = null;
				listaOpciones = null;
				busqueda = null;

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
					
					if(torrents.length()!=0) {
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

							mensaje = mensaje + " - " + nombre + " está " + estadoTorrent + " con un " + porcentaje.trim()
									+ "\n\n";

						}
					}else {
						mensaje = "No hay novedades";
					}
					

				} catch (JSONException e) {
					mensaje = "No hay novedades";
				}

				message.setText(mensaje);

				movies = null;
				movieSeleccionada = null;
				listaOpciones = null;
				busqueda = null;

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

								try {
									SendPhoto sendPhoto = new SendPhoto();
									sendPhoto.setChatId(update.getMessage().getChatId());
									sendPhoto.setPhoto(movieSeleccionada.getImg());
									sendPhoto(sendPhoto);
								} catch (TelegramApiException e) {
									e.printStackTrace();
								}

								ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

								KeyboardRow row1 = new KeyboardRow();
								KeyboardRow row2 = new KeyboardRow();
								KeyboardRow row3 = new KeyboardRow();
								KeyboardRow row4 = new KeyboardRow();

								KeyboardButton button1 = new KeyboardButton();
								KeyboardButton button2 = new KeyboardButton();
								KeyboardButton button3 = new KeyboardButton();
								KeyboardButton button4 = new KeyboardButton();

								button1.setText("Si, añádela");
								button2.setText("No, esa no");
								button3.setText("Ver Sinopsis");
								button4.setText("Enseñame la lista otra vez");

								row1.add(button1);
								row2.add(button2);
								row3.add(button3);
								row4.add(button4);

								List<KeyboardRow> list = new ArrayList<KeyboardRow>();

								list.add(row1);
								list.add(row2);
								list.add(row3);
								list.add(row4);

								keyboard.setKeyboard(list);
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
				sendMessage(message); // Call method to send the message
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
