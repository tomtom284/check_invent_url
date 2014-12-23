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

import java.io.*;
import java.net.Authenticator;
import java.util.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.fusesource.jansi.AnsiConsole;

/**
 * <p> Classe principale permettant la vérification des références externes
 * d'une fiche d'inventaire au format XML. <p>
 *
 * @author AKKA Technologies (Thomas Verbeke)
 * @version 1.0
 */
public class Main {

    /**
     * Indicateur pour mode debug ou pas.
     */
    private static final boolean DEBUG = false;
    /**
     * Propriété indiquant le nombre de paramètres attendus.
     */
    private static final int NB_PARAMETRES = 6;
    /**
     * Propriété indiquant la position du répertoire contenant les fiches
     * d'inventaire dans la liste des paramètres en entrée.
     */
    private static final int POS_PARAM_REP_FICHES = 0;
    /**
     * Propriété indiquant la position du répertoire qui contiendra les logs.
     */
    private static final int POS_PARAM_REP_LOGS = 1;
    /**
     * Propriété indiquant la position du login de l'utilisateur dans la liste
     * des paramètres en entrée.
     */
    private static final int POS_PARAM_LOGIN_PROXY = 2;
    /**
     * Propriété indiquant la position du mot de passe de l'utilisateur dans la
     * liste des paramètres en entrée.
     */
    private static final int POS_PARAM_MDP_PROXY = 3;
    /**
     * Propriété indiquant la position du fichier de configuration dans la liste
     * des paramètres en entrée.
     */
    private static final int POS_PARAM_FICHIER_CONFIG = 4;
    /**
     * Propriété indiquant la position du dictionnaire GLU dans la liste des
     * paramètres en entrée.
     */
    private static final int POS_PARAM_DIC_GLU = 5;

    /**
     * Méthode principale.
     *
     * @param args[0] Répertoire où se trouvent les fiches d'inventaire en XML
     * @param args[1] Répertoire où se trouveront les logs
     * @param args[2] login de l'utilisateur pour le serveur proxy
     * @param args[3] Mot de passe decrypté de l'utilisateur pour le serveur
     * @param args[4] Chemin du fichier de configuration des installations     
     * @param args[5] Chemin du fichier dictionnaire GLU
     *
     */
    public static void main(final String[] args) {
        AnsiConsole.systemInstall();

        if (args.length != NB_PARAMETRES) {
            AnsiConsole.out.println(Display.BOLD + "\nMauvais usage de la commande\nUsage : java -jar Main.jar repertoire_des_fiches_XML repertoire_des_logs login_proxy mot_de_passe_pour_le_proxy fichier_configuration fichier_dictionnaire_GLU\n" + Display.OFF);
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "\nMauvais usage de la commande\nUsage : java -jar Main.jar fichier_configuration repertoire_des_fiches_XML fichier_dictionnaire_GLU login_proxy mot_de_passe_pour_le_proxy\n");
            System.exit(Utilities.ERROR_USAGE);
        }

        /**
         * *********************************************************************
         * Récupération de toutes les propriétés contenues dans le fichier de
         * configuration.
         * *********************************************************************
         */
        try {
            Utilities.PROPS.load(new FileInputStream(args[POS_PARAM_FICHIER_CONFIG]));
        } catch (IOException exception) {
            AnsiConsole.out.println(Display.BOLD + Display.ROUGE + "Impossible de charger le fichier de configuration " + args[POS_PARAM_FICHIER_CONFIG] + Display.OFF);
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Impossible de charger le fichier de configuration {0}", args[POS_PARAM_FICHIER_CONFIG]);
            System.exit(Utilities.ERROR_CONFIGURATION);
        }

        /**
         * *********************************************************************
         * Configuration du système de log.
         * *********************************************************************
         */
        if (new File(Utilities.PROPS.getProperty("log4j.config.file")).exists()) {
            PropertyConfigurator.configure(Utilities.PROPS.getProperty("log4j.config.file"));
            Utilities.getInstance().changeLogger(Utilities.LOGGER_LOG, "M1", args[POS_PARAM_REP_LOGS] + File.separatorChar + "log.txt");
            Utilities.getInstance().changeLogger(Utilities.LOGGER_SYNTHESE, "M2", args[POS_PARAM_REP_LOGS] + File.separatorChar + "synthese.txt");
            Utilities.getInstance().changeLogger(Utilities.LOGGER_TABLE, "M3", args[POS_PARAM_REP_LOGS] + File.separatorChar + "table.txt");
        } else {
            AnsiConsole.out.println(Display.BOLD + Display.ROUGE + "Impossible de charger le fichier de configuration des logs " + Utilities.PROPS.getProperty("log4j.config.file") + Display.OFF);
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Impossible de charger le fichier de configuration des logs {0}", Utilities.PROPS.getProperty("log4j.config.file"));
            System.exit(Utilities.ERROR_CONFIGURATION);
        }

        /**
         * *********************************************************************
         * Récupération du dictionnaire GLU.
         * *********************************************************************
         */
        /**
         * première passe : initialisation de table d'association.
         */
        final HashMap<String, String> tableAssociations = new HashMap<String, String>();
        boolean recupValeur = false;
        String cle = "";
        String valeur;
        String line;

        BufferedReader fichier = null;
        try {
            fichier = new BufferedReader(new FileReader(args[POS_PARAM_DIC_GLU]));
            line = fichier.readLine();
            while (line != null) {
                if (line.startsWith("%A ")) {
                    if (recupValeur) {
                        AnsiConsole.out.println(Display.BOLD + Display.ROUGE + "Erreur de syntaxe dans le fichier : " + args[POS_PARAM_DIC_GLU] + Display.OFF);
                        // Utilities.LOGGER.error("Erreur de syntaxe dans le fichier : " + args[POS_PARAM_DIC_GLU]);
                        System.exit(Utilities.ERROR_DICTIONNAIRE);
                    } else {
                        /**
                         * Récupération de la clef.
                         */
                        cle = line.substring(line.indexOf(' ') + 1);

                        /**
                         * Passage en mode récupération valeur.
                         */
                        recupValeur = true;
                    }
                }

                if ((line.startsWith("%U") || line.startsWith("%L")) && recupValeur) {
                    /**
                     * Récupération de la valeur.
                     */
                    valeur = line.substring(line.indexOf(' ') + 1);
                    String lineVal = fichier.readLine();
                    while ((!lineVal.trim().isEmpty()) && (lineVal.charAt(0) != '#')) {
                        valeur = valeur.concat(lineVal.trim());
                        lineVal = fichier.readLine();
                    }
                    tableAssociations.put("<&" + cle + ">", valeur);
                    recupValeur = false;
                } else {
                    line = fichier.readLine();
                }
            }
        } catch (IOException exception) {
            AnsiConsole.out.println(Display.BOLD + Display.ROUGE + "Fichier : " + args[POS_PARAM_DIC_GLU] + " inconnu" + Display.OFF);
            // Utilities.LOGGER.error("Fichier : " + args[POS_PARAM_DIC_GLU] + " inconnu");
            System.exit(Utilities.ERROR_ACCES_DICTIONNAIRE);
        } finally {
            if (fichier != null) {
                try {
                    fichier.close();
                } catch (IOException ex) {
                    AnsiConsole.out.println(Display.BOLD + Display.ROUGE + "Erreur lors de la fermeture du fichier : " + args[POS_PARAM_DIC_GLU] + Display.OFF);
                    // Utilities.LOGGER.error("Erreur lors de la fermeture du fichier : " + args[POS_PARAM_DIC_GLU]);
                    System.exit(Utilities.ERROR_FERMETURE_DICTIONNAIRE);
                }
            }
        }

        /**
         * Passes suivantes : substitution des entités GLU imbriquées.
         */
        int nbSubstitution;
        int indexPasse = 1;
        do {
            nbSubstitution = 0;
            for (String key : tableAssociations.keySet()) {
                if (tableAssociations.get(key).contains("<&")) {
                    /**
                     * Remplacement de l'entité GLU par son contenu.
                     */
                    final String val = tableAssociations.get(key);
                    final int index = val.indexOf("<&");
                    final String entityGlu = val.substring(index, val.indexOf(">", index + 1) + 1);
                    final String contentEntityGlu = tableAssociations.get(entityGlu);
                    if (contentEntityGlu != null) {
                        final String aux = val.replaceFirst(entityGlu, contentEntityGlu);
                        tableAssociations.put(key, aux);
                        nbSubstitution++;
                    }
                }
            }
            indexPasse++;
        } while (nbSubstitution != 0);


        /**
         * *********************************************************************
         * Sauvegarde de table d'association.
         * *********************************************************************
         */
        final ArrayList<String> cleTriees = new ArrayList<String>(tableAssociations.keySet());
        Collections.sort(cleTriees);
        for (String key : cleTriees) {
            Utilities.LOGGER_TABLE.info(key + " -> " + tableAssociations.get(key));
            Utilities.LOGGER_TABLE.info("");
        }

        /**
         * *********************************************************************
         * Traitement des fiches d'inventaire.
         * *********************************************************************
         */
        AnsiConsole.out.println(Display.BOLD + "Debut du traitement des fichiers du repertoire : " + args[POS_PARAM_REP_FICHES] + Display.OFF);
        // Utilities.LOGGER.info("Debut du traitement des fichiers du repertoire : " + args[POS_PARAM_REP_FICHES]);
        Utilities.LOGGER_SYNTHESE.info("RAPPORT DE SYNTHESE SUR LA VERIFICATION DES REFERENCES EXTERNES");
        Utilities.LOGGER_SYNTHESE.info("");

        final List<String> aux = Utilities.listRepository(new File(args[POS_PARAM_REP_FICHES]));
        Authenticator.setDefault(new ProxyAuthenticator(args[POS_PARAM_LOGIN_PROXY], args[POS_PARAM_MDP_PROXY]));
        System.setProperty("http.proxyHost", Utilities.PROPS.getProperty("http.proxy.host"));
        System.setProperty("http.proxyPort", Utilities.PROPS.getProperty("http.proxy.port"));

        int nbUrlOK = 0;
        int nbUrlNotOK = 0;
        int nbUrlBadFormat = 0;
        int nbNodeUrlLink = 0;
        int nbNodeGluInternetLab = 0;
        int nbNodeSource = 0;
        final Set<String> pbGLU = new HashSet<String>();
        final HashMap<String, HashSet<String>> pbGluFiles = new HashMap<String, HashSet<String>>();
        final HashMap<String, ArrayList<String>> pbURL_LINK = new HashMap<String, ArrayList<String>>();
        final HashMap<String, ArrayList<String>> pbSOURCE = new HashMap<String, ArrayList<String>>();

        for (String file : aux) {
            boolean pb_url = false;
            Utilities.LOGGER_LOG.info("");
            Utilities.LOGGER_LOG.info("Analyse du fichier : " + file);
            AnsiConsole.out.print(Display.BOLD + "Analyse du fichier : " + file + Display.OFF);
            // Utilities.LOGGER.info("Analyse du fichier : " + file);
            pbURL_LINK.put(file, new ArrayList<String>());
            pbSOURCE.put(file, new ArrayList<String>());
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final InputSource inputSource = new InputSource(file);

            try {
                final NodeList nodeList1 = (NodeList) xpath.evaluate(Utilities.PROPS.getProperty("xpath.url_link"), inputSource, XPathConstants.NODESET);
                final NodeList nodeList2 = (NodeList) xpath.evaluate(Utilities.PROPS.getProperty("xpath.glu_internet_lab"), inputSource, XPathConstants.NODESET);
                final NodeList nodeList3 = (NodeList) xpath.evaluate(Utilities.PROPS.getProperty("xpath.source"), inputSource, XPathConstants.NODESET);

                nbNodeUrlLink = nbNodeUrlLink + nodeList1.getLength();
                nbNodeGluInternetLab = nbNodeGluInternetLab + nodeList2.getLength();
                nbNodeSource = nbNodeSource + nodeList3.getLength();

                /**
                 * traitement des <URL_LINK>.
                 */
                for (int i = 0; i < nodeList1.getLength(); i++) {
                    final Node node = nodeList1.item(i);
                    final String contenuURL = node.getTextContent();

                    if (contenuURL.startsWith("http://")) {
                        switch (Utilities.checkURL(contenuURL)) {
                            case Utilities.URL_SUCCESS:
                                Utilities.LOGGER_LOG.info("URL valide (noeud : URL_LINK) : " + contenuURL);
                                nbUrlOK++;
                                break;
                            case Utilities.ERROR_URL_FORMAT_EXCEPTION:
                                Utilities.LOGGER_LOG.error("Format d'URL invalide (noeud : URL_LINK) : " + contenuURL);
                                pbURL_LINK.get(file).add("Format d'URL invalide (noeud : URL_LINK) : " + contenuURL);
                                nbUrlBadFormat++;
                                pb_url = true;
                                break;
                            case Utilities.ERROR_URL_OPEN_EXCEPTION:
                                Utilities.LOGGER_LOG.error("URL non joignable (noeud : URL_LINK) : " + contenuURL);
                                pbURL_LINK.get(file).add("URL non joignable (noeud : URL_LINK) : " + contenuURL);
                                nbUrlNotOK++;
                                pb_url = true;
                                break;
                            default:
                                Utilities.LOGGER_LOG.error("Valeur de retour de la fonction checkURL inconnue");
                                break;
                        }
                    }
                }

                /**
                 * traitement des <SOURCE>.
                 */
                for (int i = 0; i < nodeList3.getLength(); i++) {
                    final Node node = nodeList3.item(i);
                    final String contenuURL = node.getTextContent();

                    if (contenuURL.startsWith("http://")) {
                        switch (Utilities.checkURL(contenuURL.replaceAll(" ", ""))) {
                            case Utilities.URL_SUCCESS:
                                Utilities.LOGGER_LOG.info("URL valide (noeud : SOURCE) : " + contenuURL);
                                nbUrlOK++;
                                break;
                            case Utilities.ERROR_URL_FORMAT_EXCEPTION:
                                Utilities.LOGGER_LOG.error("Format d'URL invalide (noeud : SOURCE) : " + contenuURL);
                                pbSOURCE.get(file).add("Format d'URL invalide (noeud : SOURCE) : " + contenuURL);
                                nbUrlBadFormat++;
                                pb_url = true;
                                break;
                            case Utilities.ERROR_URL_OPEN_EXCEPTION:
                                Utilities.LOGGER_LOG.error("URL non joignable (noeud : SOURCE) : " + contenuURL);
                                pbSOURCE.get(file).add("URL non joignable (noeud : SOURCE) : " + contenuURL);
                                nbUrlNotOK++;
                                pb_url = true;
                                break;
                            default:
                                Utilities.LOGGER_LOG.error("Valeur de retour de la fonction checkURL inconnue");
                                break;
                        }
                    }
                }

                /**
                 * traitement des <GLU_INTERNET_LAB>.
                 */
                for (int i = 0; i < nodeList2.getLength(); i++) {
                    final Node node = nodeList2.item(i);
                    String contenuEntite = node.getTextContent();

                    /**
                     * Récupération des paramètres si présents.
                     */
                    final HashMap<String, String> parametres = new HashMap<String, String>();
                    if (contenuEntite.contains(" ")) {
                        final String[] tokens = contenuEntite.split("'");
                        int indexMap = 1;
                        for (int index = 1; index < tokens.length; index = index + 2) {
                            parametres.put("\\$" + indexMap, tokens[index]);
                            indexMap++;
                        }

                        contenuEntite = tokens[0].trim();
                    }

                    if (tableAssociations.get("<&" + contenuEntite + ">") == null) {
                        Utilities.LOGGER_LOG.error("Entite non definie dans le dictionnaire (noeud : GLU_INTERNET_LAB) : " + contenuEntite);
                        final String valKey0 = "Entite non definie dans le dictionnaire (noeud : GLU_INTERNET_LAB) : " + contenuEntite;
                        pbGLU.add(valKey0);
                        if (!pbGluFiles.keySet().contains(valKey0)) {
                            pbGluFiles.put(valKey0, new HashSet<String>());
                        }
                        pbGluFiles.get(valKey0).add("\t" + file);
                    } else {
                        String val = tableAssociations.get("<&" + contenuEntite + ">");

                        /**
                         * Substitution des paramètres si nécessaire.
                         */
                        if (!parametres.isEmpty()) {

                            for (String key : parametres.keySet()) {
                                val = val.replaceAll(key, parametres.get(key));
                            }
                        }

                        final int index = val.indexOf("http");
                        if (index != -1) {
                            final String contenuURL = val.substring(index, val.indexOf('\"', index));

                            if (contenuURL.startsWith("http://")) {
                                switch (Utilities.checkURL(contenuURL)) {
                                    case Utilities.URL_SUCCESS:
                                        Utilities.LOGGER_LOG.info("URL valide (noeud : GLU_INTERNET_LAB, entite : " + contenuEntite + ") : " + contenuURL);
                                        nbUrlOK++;
                                        break;
                                    case Utilities.ERROR_URL_FORMAT_EXCEPTION:
                                        Utilities.LOGGER_LOG.error("Format d'URL invalide (noeud : GLU_INTERNET_LAB, entite : " + contenuEntite + ") : " + contenuURL);
                                        String valKey1 = "Format d'URL invalide (noeud : GLU_INTERNET_LAB, entite : " + contenuEntite + ") : " + contenuURL;
                                        pbGLU.add(valKey1);
                                        if (pbGluFiles.keySet().contains(valKey1) == false) {
                                            pbGluFiles.put(valKey1, new HashSet<String>());
                                        }
                                        pbGluFiles.get(valKey1).add("\t" + file);
                                        nbUrlBadFormat++;
                                        pb_url = true;
                                        break;
                                    case Utilities.ERROR_URL_OPEN_EXCEPTION:
                                        Utilities.LOGGER_LOG.error("URL non joignable (noeud : GLU_INTERNET_LAB, entite : " + contenuEntite + ") : " + contenuURL);
                                        String valKey2 = "URL non joignable (noeud : GLU_INTERNET_LAB, entite : " + contenuEntite + ") : " + contenuURL;
                                        pbGLU.add(valKey2);
                                        if (pbGluFiles.keySet().contains(valKey2) == false) {
                                            pbGluFiles.put(valKey2, new HashSet<String>());
                                        }
                                        pbGluFiles.get(valKey2).add("\t" + file);
                                        nbUrlNotOK++;
                                        pb_url = true;
                                        break;
                                    default:
                                        Utilities.LOGGER_LOG.error("Valeur de retour de la fonction checkURL inconnue");
                                        break;
                                }
                            }
                        }
                    }
                }
            } catch (XPathExpressionException ex) {
                AnsiConsole.out.println(Display.BOLD + Display.ROUGE + "Probleme dans l'evaluation du XPATH" + Display.OFF);
                // Utilities.LOGGER.info("Probleme dans l'evaluation du XPATH");
            }

            if (pb_url) {
                AnsiConsole.out.println(Display.BOLD + Display.ROUGE + " => Pb avec au moins un lien externe" + Display.OFF);
            } else {
                AnsiConsole.out.println(Display.BOLD + Display.VERT + " => tous les liens externes sont joignables" + Display.OFF);
            }
        }

        AnsiConsole.out.println(Display.BOLD + "Fin du traitement des fichiers du repertoire : " + args[POS_PARAM_REP_FICHES] + Display.OFF);
        // Utilities.LOGGER.info("Fin du traitement des fichiers du repertoire : " + args[POS_PARAM_REP_FICHES]);

        if (nbNodeGluInternetLab == 0) {
            Utilities.LOGGER_LOG.info("");
            Utilities.LOGGER_LOG.warn("Pas d'element <GLU_INTERNET_LAB> trouve dans aucune fiche : verifiez la propriete 'xpath.glu_internet_lab' du fichier de configuration : " + args[0]);
            Utilities.LOGGER_SYNTHESE.warn("Pas d'element <GLU_INTERNET_LAB> trouve dans aucune fiche : verifiez la propriete 'xpath.glu_internet_lab' du fichier de configuration : " + args[0]);
        }

        if (nbNodeUrlLink == 0) {
            Utilities.LOGGER_LOG.info("");
            Utilities.LOGGER_LOG.warn("Pas d'element <URL_LINK> trouve dans aucune fiche : verifiez la propriete 'xpath.url_link' du fichier de configuration : " + args[0]);
            Utilities.LOGGER_SYNTHESE.warn("Pas d'element <URL_LINK> trouve dans aucune fiche : verifiez la propriete 'xpath.url_link' du fichier de configuration : " + args[0]);
        }

        if (nbNodeSource == 0) {
            Utilities.LOGGER_LOG.info("");
            Utilities.LOGGER_LOG.warn("Pas d'element <SOURCE> trouve dans aucune fiche : verifiez la propriete 'xpath.source' du fichier de configuration : " + args[0]);
            Utilities.LOGGER_SYNTHESE.warn("Pas d'element <SOURCE> trouve dans aucune fiche : verifiez la propriete 'xpath.source' du fichier de configuration : " + args[0]);
        }

        Utilities.LOGGER_SYNTHESE.info("*******************************************************************************");
        Utilities.LOGGER_SYNTHESE.info("* REFERENCES EXTERNES INJOIGNABLES CODEES EN DUR DANS LES FICHES D'INVENTAIRE *");
        Utilities.LOGGER_SYNTHESE.info("*******************************************************************************");
        Utilities.LOGGER_SYNTHESE.info("");
        for (String file : aux) {
            if (!pbURL_LINK.get(file).isEmpty()) {
                Utilities.LOGGER_SYNTHESE.info("Le fichier : " + file + " a les references externes suivantes non joignables");
                for (String pbURL : pbURL_LINK.get(file)) {
                    Utilities.LOGGER_SYNTHESE.info(pbURL);
                }
                Utilities.LOGGER_SYNTHESE.info("");
            }
        }
        Utilities.LOGGER_SYNTHESE.info("");
        Utilities.LOGGER_SYNTHESE.info("");
        Utilities.LOGGER_SYNTHESE.info("*****************************************************");
        Utilities.LOGGER_SYNTHESE.info("* SOURCES INJOIGNABLES DANS LES FICHES D'INVENTAIRE *");
        Utilities.LOGGER_SYNTHESE.info("*****************************************************");
        Utilities.LOGGER_SYNTHESE.info("");

        for (String file : aux) {
            if (!pbSOURCE.get(file).isEmpty()) {
                Utilities.LOGGER_SYNTHESE.info("Le fichier : " + file + " a des sources suivantes non joignables");
                for (String pbURL : pbSOURCE.get(file)) {
                    Utilities.LOGGER_SYNTHESE.info(pbURL);
                }
                Utilities.LOGGER_SYNTHESE.info("");
            }
        }
        Utilities.LOGGER_SYNTHESE.info("");
        Utilities.LOGGER_SYNTHESE.info("");
        Utilities.LOGGER_SYNTHESE.info("************************************************************");
        Utilities.LOGGER_SYNTHESE.info("* REFERENCES GLU INJOIGNABLES DANS LES FICHES D'INVENTAIRE *");
        Utilities.LOGGER_SYNTHESE.info("************************************************************");

        if (nbNodeGluInternetLab == 0) {
            Utilities.LOGGER_SYNTHESE.info("");
            Utilities.LOGGER_SYNTHESE.warn("Pas d'element <GLU_INTERNET_LAB> trouve dans aucune fiche : verifiez la propriete 'xpath.glu_internet_lab' du fichier de configuration : " + args[0]);
        }

        final ArrayList<String> listAux = new ArrayList<String>(pbGLU);
        Collections.sort(listAux);
        for (String pbStr : listAux) {
            Utilities.LOGGER_SYNTHESE.info("");
            Utilities.LOGGER_SYNTHESE.info(pbStr);
            for (String pbFile : pbGluFiles.get(pbStr)) {
                Utilities.LOGGER_SYNTHESE.info(pbFile);
            }
        }

        Utilities.LOGGER_SYNTHESE.info("");
        Utilities.LOGGER_SYNTHESE.info("");
        Utilities.LOGGER_SYNTHESE.info("Nombre d'URL valides : " + nbUrlOK);
        Utilities.LOGGER_SYNTHESE.info("Nombre d'URL injoignables : " + nbUrlNotOK);
        Utilities.LOGGER_SYNTHESE.info("Nombre d'URL malformees : " + nbUrlBadFormat);
    }
}
