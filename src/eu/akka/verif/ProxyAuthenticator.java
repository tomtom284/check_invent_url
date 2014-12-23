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

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * <p> Classe permettant l'identification sur un serveur proxy. <p>
 *
 * @author AKKA Technologies (Thomas Verbeke)
 * @version 1.0
 */
class ProxyAuthenticator extends Authenticator {

    /**
     * Variable contenant l'utilisateur.
     */
    private final transient String user;
    /**
     * Variable contenant le mot de passe.
     */
    private final transient String password;

    /**
     * Constructeur.
     *
     * @param pUser Nom de l'utilisateur
     * @param pPassword Mot de passe de l'utilisateur
     */
    public ProxyAuthenticator(final String pUser, final String pPassword) {
        super();
        this.user = pUser;
        this.password = pPassword;
    }

    /**
     * Créer un nouvel objet PasswordAuthentication.
     *
     * @return l'objet PasswordAuthentication
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password.toCharArray());
    }
}
