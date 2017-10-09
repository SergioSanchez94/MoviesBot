package com.sergiosanchez.utils;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import com.sergiosanchez.bot.MoviesBot;

public class Util {

	/**
	 * Genera un teclado de botones con las opciones pasadas en el ArrayList por parametros
	 * @param options
	 * @param cancelOption
	 * @return List<KeyboardRow>
	 */
	public static List<KeyboardRow> generateKeyboard(ArrayList<String> options, boolean cancelOption){
		
		List<KeyboardRow> list = new ArrayList<KeyboardRow>();
		
		for (String option : options) {
			KeyboardRow row = new KeyboardRow();
			KeyboardButton button = new KeyboardButton();
			button.setText(option);
			row.add(button);
			list.add(row);
		}
		
		if(cancelOption){
			KeyboardRow row = new KeyboardRow();
			KeyboardButton button = new KeyboardButton();
			button.setText("Cancelar");
			row.add(button);
			list.add(row);
		}
		
		return list;	
	}
	
	public static void deleteData(){
		MoviesBot.movies = null;
		MoviesBot.movieSeleccionada = null;
		MoviesBot.listaOpciones = null;
		MoviesBot.busqueda = null;
	}

}
