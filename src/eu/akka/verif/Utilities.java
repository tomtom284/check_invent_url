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
 * <p> Bibliothèque de fonctions utilitaires. <p>
 *
 * @author AKKA Technologies (Thomas Verbeke)
 * @version 1.0
 */
public final class Utilities {

    /**
     * Propriétés configurant le programme.
     */
    public static final Properties PROPS = new Properties();
    /**
     * Variable pour gérer la sortie standard.
     */
    public static final Logger LOGGER = Logger.getLogger("stdout");
    /**
     * Variable pour gérer le log.
     */
    public static final Logger LOGGER_LOG = Logger.getLogger("log");
    /**
     * Variable pour sauvegarder la table entité GLU/contenu.
     */
    public static final Logger LOGGER_TABLE = Logger.getLogger("table");
    /**
     * Variable pour gérer le log.
     */
    public static final Logger LOGGER_SYNTHESE = Logger.getLogger("synthese");
//    /**
//     * Variable pour sauvegarder la liste des entités.
//     */
//    public static final Logger LOGGER_ENTITES = Logger.getLogger("entites");
    /**
     * Numéro d'erreur utilisé lors du mauvais usage du programme.
     */
    public static final int ERROR_USAGE = -1;
    /**
     * Numéro d'erreur utilisé si le fichier de configuration ne peut être lu.
     */
    public static final int ERROR_CONFIGURATION = -2;
    /**
     * Numéro d'erreur utilisé lors d'une erreur de syntaxe du dictionnaire GLU.
     */
    public static final int ERROR_DICTIONNAIRE = -3;
    /**
     * Numéro d'erreur utilisé lors d'une erreur d'accès au dictionnaire GLU.
     */
    public static final int ERROR_ACCES_DICTIONNAIRE = -4;
    /**
     * Numéro d'erreur utilisé lors d'une erreur de fermeture du fichier
     * dictionnaire GLU.
     */
    public static final int ERROR_FERMETURE_DICTIONNAIRE = -5;
    /**
     * Numéro d'erreur utilisé lors d'une erreur de format d'URL.
     */
    public static final int ERROR_URL_FORMAT_EXCEPTION = -6;
    /**
     * Numéro d'erreur utilisé si l'URL ne pointe sur rien.
     */
    public static final int ERROR_URL_OPEN_EXCEPTION = -7;
    /**
     * Numéro d'erreur utilisé lors d'un problème sur le fichier de la table
     * entité/contenu.
     */
    public static final int ERROR_FILE_TABLE = -8;
    /**
     * Numéro utilisé pour indiquer que l'URL est bonne.
     */
    public static final int URL_SUCCESS = -9;
    /**
     * Variable contenant la liste des fichiers à analyser.
     */
    private static List<String> listFiles = null;

    /**
     * Méthode renvoyant la liste de tous les noms des fichiers et répertoires
     * contenus dans le répertoire passe en entrée (analyse récursive).
     *
     * @param repertoire Nom du répertoire à lister
     * @return La liste des fichiers du répertoire passe en entrée
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
     * Méthode renvoyant la liste de tous les noms des fichiers et répertoires
     * contenus dans le répertoire passe en entrée (analyse récursive).
     *
     * @param repertoire Nom du répertoire à lister
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
     * Méthode permettant de fixer le fichier de réception d'un loggueur.
     *
     * @param pLog Loggueur dont on veut modifier le fichier
     * @param pAppender Nom de l'appender du loggueur
     * @param pFile Fichier de destination du loggueur
     * @return Loggueur passé en entrée avec son fichier modifié
     */
    public Logger changeLogger(final Logger pLog, final String pAppender, final String pFile) {
        final FileAppender myFApp = (FileAppender) pLog.getAppender(pAppender);
        myFApp.setFile(pFile);
        myFApp.activateOptions();
        return pLog;
    }

    /**
     * Méthode permettant de vérifier une URL.
     *
     * @param pUrl URL à vérifier
     * @return Entier indiquant si l'URL est mal formée ou si l'URL ne pointe
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
     * Constructeur privé.
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
