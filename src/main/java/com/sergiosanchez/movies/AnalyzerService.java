package com.sergiosanchez.movies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.sergiosanchez.configuration.Config;

public class AnalyzerService {

	// Constantes a compartir por las distinas clases
	public static String IP;
	public static String NAME;
	public static String SERVICE;
	public static String DOMINIO;

	// Windows: "./download"
	// Mac: "//Users//Sergio//Desktop"
	public static String PATH = "//Users//Sergio//Desktop";

	public static ArrayList<Movie> searchMovie(String name, String dominio) throws MalformedURLException {

		SERVICE = "searchMovie";
		NAME = name;
		DOMINIO = dominio;

		name = name.replace(" ", "%20");
		name = name.replace("á", "a");
		name = name.replace("é", "e");
		name = name.replace("í", "i");
		name = name.replace("ó", "o");
		name = name.replace("ú", "u");
		name = name.replace("ñ", "%F1");
		name = name.replace(":", "%3a");

		String llamada = "http://" + DOMINIO + "/secciones.php?sec=buscador&valor=" + name;
		ArrayList<Movie> movies = new ArrayList<Movie>();
		try {

			Document document;
			document = Jsoup.connect(llamada).get();

			Elements table = document.getElementsByTag("table");

			table = table.get(4).getElementsByTag("table");
			table = table.get(0).getElementsByTag("table");
			table = table.get(0).getElementsByTag("table");
			table = table.get(8).getElementsByTag("table");
			table = table.get(0).getElementsByTag("table");
			table = table.get(0).getElementsByTag("table");
			table = table.get(0).getElementsByTag("table");
			table = table.get(0).getElementsByTag("table");
			table = table.get(3).getElementsByTag("table");

			Elements table1 = table.get(0).getElementsByTag("tr");

			for (int i = 0; i < table.get(0).getElementsByTag("tr").size(); i++) {
				Elements table2 = table1.get(i).getElementsByTag("tr");
				Elements td = table2.get(0).getElementsByTag("td");
				if (td.toString().contains("Película")) {

					Elements td2 = td.get(0).getElementsByTag("td");
					int iniQu = td2.toString().indexOf("<span style=");
					int finQu = td2.toString().indexOf("</span></td>");
					String calidad = td2.toString().substring(iniQu + 26, finQu);
					Elements enlace = td2.get(0).getElementsByTag("a");
					String nombre = enlace.get(0).toString();

					String url = nombre.substring(nombre.indexOf("<a href=") + 10, nombre.indexOf(".html"));

					nombre = nombre.substring(nombre.indexOf("<font color=") + 23, nombre.length());
					nombre = nombre.replace("</font>", "");
					nombre = nombre.replace("</a>", "");
					nombre = nombre.replace("(", "");
					nombre = nombre.replace(")", "");

					// Quita los parentesis de la calidad
					calidad = calidad.substring(1, calidad.length() - 1);

					if (nombre.substring(nombre.length() - 1, nombre.length()).equals(".")) {
						nombre = nombre.substring(0, nombre.length() - 1);
					}

					Movie movie = new Movie(nombre, null, null, calidad, url, null, null);

					// Solo añade estas dos calidades
					if (calidad.equals("MicroHD-1080p") || calidad.equals("DVDRip")) {
						movies.add(movie);
					}

				}

			}

		} catch (SocketTimeoutException e) {
			movies = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return movies;

	}

	public static Movie getMovieInfo(String url, String dominio) throws MalformedURLException {

		DOMINIO = dominio;
		URL direccion = new URL("http://" + DOMINIO + "/" + url + ".html");

		// Informacion
		String imagenPelicula = "";
		String GB = "";
		String date = "";
		String description = "";

		// Variables temporales
		String strTemp;
		int iniURL;
		int finURL;

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(direccion.openStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {

			br = new BufferedReader(new InputStreamReader(direccion.openStream()));

			while (null != (strTemp = br.readLine())) {

				// GB
				if (strTemp.indexOf("Tama\ufffdo:</b>") != -1) {
					finURL = strTemp.indexOf("GB");
					iniURL = strTemp.indexOf("Tama\ufffdo:</b>") + 18;

					for (int x = iniURL; x <= finURL + 1; x++) {
						GB = GB + strTemp.charAt(x);
					}
				}

				// Date
				if (strTemp.indexOf("<b>A\ufffdo:</b>") != -1) {
					finURL = strTemp.indexOf("<b>A\ufffdo:</b>") + 21; // 4 mas
					iniURL = strTemp.indexOf("<b>A\ufffdo:</b>") + 18;

					for (int x = iniURL; x <= finURL; x++) {
						date = date + strTemp.charAt(x);
					}
				}

				// IMG
				if (strTemp.indexOf("http://" + DOMINIO + "/uploads/imagenes/peliculas") != -1) {
					finURL = strTemp.indexOf("><div style");
					iniURL = strTemp.indexOf("http://" + DOMINIO + "/uploads/imagenes/peliculas");

					for (int x = iniURL; x <= finURL - 2; x++) {
						imagenPelicula = imagenPelicula + strTemp.charAt(x);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Movie movie = new Movie(null, date, description, null, url, imagenPelicula, GB);

		return movie;

	}

	public static String getMovie(String ip, String address, String dominio) throws MalformedURLException {

		SERVICE = "getMovie";
		IP = ip;
		DOMINIO = dominio;

		URL url = new URL("http://" + DOMINIO + "/" + address + ".html");
		String URL = null;
		String strTemp;
		int iniURL;
		int finURL;
		String salida = "";

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(url.openStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {

			br = new BufferedReader(new InputStreamReader(url.openStream()));

			while (null != (strTemp = br.readLine())) {

				if (strTemp.indexOf("<a href='secciones.php?sec=descargas") != -1) {

					finURL = strTemp.indexOf("&link_bajar=1");
					iniURL = strTemp.indexOf("secciones.php?sec=descargas");

					URL = "";

					for (int i = iniURL; i <= finURL + 12; i++) {
						URL = URL + strTemp.charAt(i);
					}
				}
			}

			url = new URL("http://" + DOMINIO + "/" + URL);
			br = new BufferedReader(new InputStreamReader(url.openStream()));

			while (null != (strTemp = br.readLine())) {

				if (strTemp.indexOf("<a href='/uploads/torrents/") != -1) {

					finURL = strTemp.indexOf(".torrent");
					iniURL = strTemp.indexOf("/uploads/torrents/");

					URL = "";

					for (int i = iniURL; i <= finURL + 7; i++) {
						URL = URL + strTemp.charAt(i);
					}

					salida = "se ha añadido a tu biblioteca";

					try {
						IPConnection.addFile(IP, "http://" + DOMINIO + "" + URL);
					} catch (Exception e) {
					}

				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return salida;
	}

	public static ArrayList<Movie> getMovies(String direccionAPI) {
		
		ArrayList<Movie> movies = new ArrayList<Movie>();

		try {
			URL url = new URL(direccionAPI);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			while ((output = br.readLine()) != null) {
				JSONObject jObject;
				Movie movie;
				try {
					jObject = new JSONObject(output);
					JSONArray results = jObject.getJSONArray("results");
					
					for (int i = 0; i < results.length(); i++) {
						movie = new Movie(null, null, null, null, null, null, null);
						JSONObject resultado = results.getJSONObject(i);
						movie.setName(resultado.getString("title"));
						movie.setDescription(resultado.getString("overview"));
						movies.add(movie);
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (ProtocolException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return movies;

	}

	public static String getTrailer(String name, String year) throws MalformedURLException {

		int idMovie;
		String key;
		String trailer = null;

		name = name.replace(" ", "%20");
		name = name.replace("á", "a");
		name = name.replace("é", "e");
		name = name.replace("í", "i");
		name = name.replace("ó", "o");
		name = name.replace("ú", "u");
		name = name.replace("ñ", "%F1");
		name = name.replace(":", "%3a");

		try {
			URL url = new URL(
					"https://api.themoviedb.org/3/search/movie?api_key="+Config.getAPIKEY()+"&language=es-ES&query="
							+ name + "&page=1&include_adult=false&region=Spain&year=2009&sort_by=popularity.desc&year="
							+ year);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				JSONObject jObject;
				try {
					jObject = new JSONObject(output);
					JSONArray results = jObject.getJSONArray("results");
					JSONObject resultado = results.getJSONObject(0);
					idMovie = resultado.getInt("id");

					url = new URL("http://api.themoviedb.org/3/movie/" + idMovie
							+ "/videos?api_key="+ Config.getAPIKEY());
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Accept", "application/json");

					if (conn.getResponseCode() != 200) {
						throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
					}

					br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

					while ((output = br.readLine()) != null) {
						try {
							jObject = new JSONObject(output);
							results = jObject.getJSONArray("results");
							resultado = results.getJSONObject(0);
							key = resultado.getString("key");
							trailer = "https://www.youtube.com/watch?v=" + key;

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return trailer;
	}

}
