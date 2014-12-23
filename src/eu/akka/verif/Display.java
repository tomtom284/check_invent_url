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

import jline.ANSIBuffer.ANSICodes;

/**
 * <p> Bibliothèque de constantes pour l'affichage. <p>
 *
 * @author AKKA Technologies (Thomas Verbeke)
 * @version 1.0
 *
 */
public class Display {

    /**
     * Code d'effacement de l'écran.
     */
    public static final String CLS = ANSICodes.clrscr();
    /**
     * Code pour écriture en rouge.
     */
    public static final String ROUGE = ANSICodes.attrib(31);
    /**
     * Code pour écriture en vert.
     */
    public static final String VERT = ANSICodes.attrib(32);
    /**
     * Code pour écriture en gras.
     */
    public static final String BOLD = ANSICodes.attrib(1);
    /**
     * Code d'annulation de toutes les transformations.
     */
    public static final String OFF = ANSICodes.attrib(0);
}
