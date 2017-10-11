package com.sergiosanchez.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Clase encargada de recuperar la información de configuración del fichero externo, almacenarla en sus variables para
 * poder ser usadas por el resto de clases.
 * @author Sergio Sanchez
 *
 */
public class Config {

	static String DOMAIN;
	static String IPADDRESS;
	static String BOTAPIKEY;
	static String APIKEY;
	static String PORT;
	static String USER;
	static String PASSWORD;
	static String BOTNAME;
	static String USERSLIST[];

	public static void initConfig() {
		try {
			File file = new File("configuration.properties");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();

			Enumeration<Object> enuKeys = properties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);

				switch (key) {
				case "dominio":
					DOMAIN = value;
					break;

				case "ipaddress":
					IPADDRESS = value;
					break;

				case "botapikey":
					BOTAPIKEY = value;
					break;

				case "apikey":
					APIKEY = value;
					break;

				case "port":
					PORT = value;
					break;

				case "user":
					USER = value;
					break;

				case "password":
					PASSWORD = value;
					break;
					
				case "botName":
					BOTNAME = value;
					break;
				case "usersList":
					USERSLIST = value.split(";");
				default:
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the dOMAIN
	 */
	public static String getDOMAIN() {
		return DOMAIN;
	}

	/**
	 * @param dOMAIN the dOMAIN to set
	 */
	public static void setDOMAIN(String dOMAIN) {
		DOMAIN = dOMAIN;
	}

	/**
	 * @return the iPADDRESS
	 */
	public static String getIPADDRESS() {
		return IPADDRESS;
	}

	/**
	 * @param iPADDRESS the iPADDRESS to set
	 */
	public static void setIPADDRESS(String iPADDRESS) {
		IPADDRESS = iPADDRESS;
	}

	/**
	 * @return the bOTAPIKEY
	 */
	public static String getBOTAPIKEY() {
		return BOTAPIKEY;
	}

	/**
	 * @param bOTAPIKEY the bOTAPIKEY to set
	 */
	public static void setBOTAPIKEY(String bOTAPIKEY) {
		BOTAPIKEY = bOTAPIKEY;
	}

	/**
	 * @return the aPIKEY
	 */
	public static String getAPIKEY() {
		return APIKEY;
	}

	/**
	 * @param aPIKEY the aPIKEY to set
	 */
	public static void setAPIKEY(String aPIKEY) {
		APIKEY = aPIKEY;
	}

	/**
	 * @return the pORT
	 */
	public static String getPORT() {
		return PORT;
	}

	/**
	 * @param pORT the pORT to set
	 */
	public static void setPORT(String pORT) {
		PORT = pORT;
	}

	/**
	 * @return the uSER
	 */
	public static String getUSER() {
		return USER;
	}

	/**
	 * @param uSER the uSER to set
	 */
	public static void setUSER(String uSER) {
		USER = uSER;
	}

	/**
	 * @return the pASSWORD
	 */
	public static String getPASSWORD() {
		return PASSWORD;
	}

	/**
	 * @param pASSWORD the pASSWORD to set
	 */
	public static void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}

	/**
	 * @return the bOTNAME
	 */
	public static String getBOTNAME() {
		return BOTNAME;
	}

	/**
	 * @param bOTNAME the bOTNAME to set
	 */
	public static void setBOTNAME(String bOTNAME) {
		BOTNAME = bOTNAME;
	}

	/**
	 * @return the uSERSLIST
	 */
	public static String[] getUSERSLIST() {
		return USERSLIST;
	}

	/**
	 * @param uSERSLIST the uSERSLIST to set
	 */
	public static void setUSERSLIST(String[] uSERSLIST) {
		USERSLIST = uSERSLIST;
	}	

}
