package com.sergiosanchez.connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.sergiosanchez.movies.Movie;

/**
 * Clase encargada de buscar en Internet la información de las películas oportunas en base al dominio de las propiedades
 * @author Sergio Sanchez
 *
 */
public class Downloader {

	// Constantes a compartir por las distinas clases
	public static String IP;
	public static String NAME;
	public static String DOMINIO;

	/**
	 * Obtiene la lista de busqueda de esa película del dominio
	 * @param name
	 * @param dominio
	 * @return ArrayList<Movie>
	 * @throws MalformedURLException
	 */
	public static ArrayList<Movie> searchMovie(String name, String dominio) throws MalformedURLException {

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

					Movie movie = new Movie(0, nombre, null, null, calidad, url, null, null);

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

	/**
	 * Devuelve la información de la película del dominio
	 * @param url
	 * @param dominio
	 * @return Movie
	 * @throws MalformedURLException
	 */
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

		Movie movie = new Movie(0, null, date, description, null, url, imagenPelicula, GB);

		return movie;

	}

	/**
	 * Devuelve la URL de la película seleccionada
	 * @param ip
	 * @param address
	 * @param dominio
	 * @return String url
	 * @throws MalformedURLException
	 */
	public static String getMovie(String ip, String address, String dominio) throws MalformedURLException {

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

					salida = URL;

				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return salida;
	}

}
