package com.sergiosanchez.connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sergiosanchez.configuration.Config;
import com.sergiosanchez.movies.Cast;
import com.sergiosanchez.movies.Movie;

/**
 * Clase encargada de realizar peticiones a la API externa de información de
 * películas
 * 
 * @author Sergio Sanchez
 *
 */
public class MoviesAPI {

	// Constantes a compartir por las distinas clases
	public static String IP;
	public static String NAME;
	public static String DOMINIO;

	/**
	 * Obtiene un array de objetos Movie con la información de la URL de la API
	 * que le paseamos por parámetro
	 * 
	 * @param direccionAPI
	 * @return ArrayList<Movie>
	 */
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
						movie = new Movie();
						JSONObject resultado = results.getJSONObject(i);
						movie.setName(resultado.getString("title"));
						movie.setId(resultado.getInt("id"));
						movie.setDescription(resultado.getString("overview"));
						movie.setVoteAverage(resultado.getInt("vote_average"));
						movie.setDate(resultado.getString("release_date"));
						movie.setImg("https://image.tmdb.org/t/p/w500/" + resultado.getString("poster_path"));
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

	/**
	 * Obtiene la URL del trailer a Youtube de la película
	 * 
	 * @param name
	 * @param year
	 * @return String
	 * @throws MalformedURLException
	 */
	public static String getTrailer(int id) throws MalformedURLException {

		String trailer = "";
		String key = "";
		HttpURLConnection conn = null;

		try {
			URL url = new URL("http://api.themoviedb.org/3/movie/" + id + "/videos?api_key=" + Config.getAPIKEY()
					+ "&language=es-ES");
			conn = (HttpURLConnection) url.openConnection();
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

			JSONObject jObject;
			while ((output = br.readLine()) != null) {
				try {
					jObject = new JSONObject(output);
					JSONArray results = jObject.getJSONArray("results");
					JSONObject resultado = results.getJSONObject(0);
					key = resultado.getString("key");
					trailer = "https://www.youtube.com/watch?v=" + key;

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		conn.disconnect();

		return trailer;
	}

	/**
	 * Devuelve la lista de actores de una película por su ID
	 * 
	 * @param idPelicula
	 * @return ArrayList<Cast>
	 */
	public static ArrayList<Cast> getCastList(int idPelicula) {

		ArrayList<Cast> castList = new ArrayList<Cast>();

		try {
			URL url = new URL("https://api.themoviedb.org/3/movie/" + idPelicula + "/credits?api_key="
					+ Config.getAPIKEY() + "&language=es-ES&page=1");
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
					JSONArray results = jObject.getJSONArray("cast");

					// Maximo 10 actores
					for (int i = 0; i < 15; i++) {
						Cast cast = new Cast(0, null, null, null);
						JSONObject resultado = results.getJSONObject(i);
						cast.setId(resultado.getInt("cast_id"));
						cast.setCharacter(resultado.getString("character"));
						cast.setActor(resultado.getString("name"));
						cast.setImg("profile_path");
						castList.add(cast);
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
		return castList;

	}
}
