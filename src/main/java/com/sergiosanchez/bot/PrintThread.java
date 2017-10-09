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
import com.sergiosanchez.movies.AnalyzerService;
import com.sergiosanchez.movies.IPConnection;
import com.sergiosanchez.movies.Movie;
import com.vdurmont.emoji.EmojiParser;

public class PrintThread extends Thread {

	public void run() {
		
		ArrayList<String> fechasRecomendaciones = new ArrayList<String>();

		while (true) {

			try {

				String estado = null;
				String mensaje = "";

				JSONObject jObject;
				try {
					jObject = new JSONObject(IPConnection.getInfo(Config.getIPADDRESS()));
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

							mensaje = nombre + "%20ya%20se%20ha%20completado%20"
									+ EmojiParser.parseToUnicode(":tada:")
									+ EmojiParser.parseToUnicode(":tada:")
									+ EmojiParser.parseToUnicode(":tada:")
									+ EmojiParser.parseToUnicode(":tada:")
									+ EmojiParser.parseToUnicode(":tada:")
									+ EmojiParser.parseToUnicode(":tada:")
									+ EmojiParser.parseToUnicode(":tada:")
									+ EmojiParser.parseToUnicode(":tada:")
									+ EmojiParser.parseToUnicode(":tada:")
									+ EmojiParser.parseToUnicode(":tada:");
							
							HttpClient client = new DefaultHttpClient();

							HttpGet get = new HttpGet(
									"https://api.telegram.org/bot"+Config.getBOTAPIKEY()+"/sendMessage?chat_id=395740029&text="+mensaje);
							
							//Delete download
							IPConnection.removeDownload(Config.getIPADDRESS(), hash);
							
							try {
								client.execute(get);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

				} catch (JSONException e) {}
				
				int dia = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
				int hora =  Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				
				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				Date date = new Date();
				String diaActual = dateFormat.format(date);
				
				boolean enviado = false;
				
				//Los viernes a las 12
				if(dia == 5 && hora == 12){
					
					for (String fechaRecomendacion : fechasRecomendaciones) {
						if(fechaRecomendacion.equals(diaActual)){
							enviado = true;
						}
					}
					
					if(!enviado){
						String recomendaciones = "Hola! Hoy comienza el fin de semana :smile:, aqui tienes unas recomiendaciones%3A%0A%0A";
						fechasRecomendaciones.add(diaActual);
						
						//Recomendaciones semanales
						for (Movie movie : AnalyzerService.getLasReleaseMovies()) {
							recomendaciones = recomendaciones + "%20-%20" + movie.getName() + "%0A";
						}
						recomendaciones = recomendaciones.replace(" ", "%20");
						recomendaciones = recomendaciones.replace(",", "%2C");
						recomendaciones = recomendaciones.replace("!", "%21");
						//recomendaciones = recomendaciones.replace(":", "%3A");
						
						HttpClient client = new DefaultHttpClient();

						HttpGet get = new HttpGet(
								"https://api.telegram.org/bot"+Config.getBOTAPIKEY()+"/sendMessage?chat_id=395740029&text="+EmojiParser.parseToUnicode(recomendaciones));
						
						try {
							client.execute(get);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				
				}

				Thread.sleep(50000);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(getName() + " is running");

		}

	}
}
