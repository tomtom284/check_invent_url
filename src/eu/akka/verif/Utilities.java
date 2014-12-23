//******************************************************************************
// SERAD
//******************************************************************************
//
//******************************************************************************
//  ::    FT Id     :   Date   : Nom - Description
//******************************************************************************
//V.::  :           :26/03/2012: TVE - Creation
//******************************************************************************
package eu.akka.verif;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

/**
 * <p> Biblioth�que de fonctions utilitaires. <p>
 *
 * @author AKKA Technologies (Thomas Verbeke)
 * @version 1.0
 */
public final class Utilities {

    /**
     * Propri�t�s configurant le programme.
     */
    public static final Properties PROPS = new Properties();
    /**
     * Variable pour g�rer la sortie standard.
     */
    public static final Logger LOGGER = Logger.getLogger("stdout");
    /**
     * Variable pour g�rer le log.
     */
    public static final Logger LOGGER_LOG = Logger.getLogger("log");
    /**
     * Variable pour sauvegarder la table entit� GLU/contenu.
     */
    public static final Logger LOGGER_TABLE = Logger.getLogger("table");
    /**
     * Variable pour g�rer le log.
     */
    public static final Logger LOGGER_SYNTHESE = Logger.getLogger("synthese");
//    /**
//     * Variable pour sauvegarder la liste des entit�s.
//     */
//    public static final Logger LOGGER_ENTITES = Logger.getLogger("entites");
    /**
     * Num�ro d'erreur utilis� lors du mauvais usage du programme.
     */
    public static final int ERROR_USAGE = -1;
    /**
     * Num�ro d'erreur utilis� si le fichier de configuration ne peut �tre lu.
     */
    public static final int ERROR_CONFIGURATION = -2;
    /**
     * Num�ro d'erreur utilis� lors d'une erreur de syntaxe du dictionnaire GLU.
     */
    public static final int ERROR_DICTIONNAIRE = -3;
    /**
     * Num�ro d'erreur utilis� lors d'une erreur d'acc�s au dictionnaire GLU.
     */
    public static final int ERROR_ACCES_DICTIONNAIRE = -4;
    /**
     * Num�ro d'erreur utilis� lors d'une erreur de fermeture du fichier
     * dictionnaire GLU.
     */
    public static final int ERROR_FERMETURE_DICTIONNAIRE = -5;
    /**
     * Num�ro d'erreur utilis� lors d'une erreur de format d'URL.
     */
    public static final int ERROR_URL_FORMAT_EXCEPTION = -6;
    /**
     * Num�ro d'erreur utilis� si l'URL ne pointe sur rien.
     */
    public static final int ERROR_URL_OPEN_EXCEPTION = -7;
    /**
     * Num�ro d'erreur utilis� lors d'un probl�me sur le fichier de la table
     * entit�/contenu.
     */
    public static final int ERROR_FILE_TABLE = -8;
    /**
     * Num�ro utilis� pour indiquer que l'URL est bonne.
     */
    public static final int URL_SUCCESS = -9;
    /**
     * Variable contenant la liste des fichiers � analyser.
     */
    private static List<String> listFiles = null;

    /**
     * M�thode renvoyant la liste de tous les noms des fichiers et r�pertoires
     * contenus dans le r�pertoire passe en entr�e (analyse r�cursive).
     *
     * @param repertoire Nom du r�pertoire � lister
     * @return La liste des fichiers du r�pertoire passe en entr�e
     */
    public static List<String> listRepository(final File repertoire) {
        if (listFiles == null) {
            listFiles = new ArrayList<String>();
            listeRepertoire(repertoire);
            Collections.sort(listFiles);
        }
        return listFiles;
    }

    /**
     * M�thode renvoyant la liste de tous les noms des fichiers et r�pertoires
     * contenus dans le r�pertoire passe en entr�e (analyse r�cursive).
     *
     * @param repertoire Nom du r�pertoire � lister
     */
    private static void listeRepertoire(final File repertoire) {
        if (repertoire.isDirectory()) {
            final File[] list = repertoire.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    listeRepertoire(list[i]);
                }
            }
        } else {
            listFiles.add(repertoire.getAbsolutePath());
        }
    }

    /**
     * M�thode permettant de fixer le fichier de r�ception d'un loggueur.
     *
     * @param pLog Loggueur dont on veut modifier le fichier
     * @param pAppender Nom de l'appender du loggueur
     * @param pFile Fichier de destination du loggueur
     * @return Loggueur pass� en entr�e avec son fichier modifi�
     */
    public Logger changeLogger(final Logger pLog, final String pAppender, final String pFile) {
        final FileAppender myFApp = (FileAppender) pLog.getAppender(pAppender);
        myFApp.setFile(pFile);
        myFApp.activateOptions();
        return pLog;
    }

    /**
     * M�thode permettant de v�rifier une URL.
     *
     * @param pUrl URL � v�rifier
     * @return Entier indiquant si l'URL est mal form�e ou si l'URL ne pointe
     * sur rien ou si l'URL est OK
     */
    public static int checkURL(final String pUrl) {
        URL url;
        try {
            url = new URL(pUrl);
        } catch (MalformedURLException exception) {
            return ERROR_URL_FORMAT_EXCEPTION;
        }
        try {
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // on ne traite que les erreurs de type 4XX ou 5XX

            if ((urlConnection.getResponseCode() / 100 == 4) || (urlConnection.getResponseCode() / 100 == 5)) {
                return ERROR_URL_OPEN_EXCEPTION;
            }
        } catch (IOException exception) {
            return ERROR_URL_OPEN_EXCEPTION;
        }
        return URL_SUCCESS;
    }

    /**
     * Constructeur priv�.
     *
     */
    private Utilities() {
    }

    /**
     * Constructeur du singleton.
     *
     * @return Le singleton construit
     */
    public static Utilities getInstance() {
        return UtilitiesHolder.INSTANCE;
    }

    /**
     * Constructeur de l'instance du singleton.
     *
     */
    private static class UtilitiesHolder {

        /**
         * Variable contenant l'instance du singleton.
         */
        private static final Utilities INSTANCE = new Utilities();
    }
}
