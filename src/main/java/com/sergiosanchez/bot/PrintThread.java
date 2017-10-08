package com.sergiosanchez.bot;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sergiosanchez.configuration.Config;
import com.sergiosanchez.movies.IPConnection;
import com.vdurmont.emoji.EmojiParser;

public class PrintThread extends Thread {

	public void run() {

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

							mensaje = EmojiParser.parseToUnicode(":white_check_mark:") + "%20" + nombre + "ya%20se%20ha%20completado%20"
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
							
							IPConnection.removeDownload(Config.getIPADDRESS(), hash);
							try {
								client.execute(get);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} catch (JSONException e) {}

				Thread.sleep(50000);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(getName() + " is running");

		}

	}
}
