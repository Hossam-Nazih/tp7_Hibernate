package com.example;

import org.h2.tools.Server;

/**
 * Ouvre la console web H2 pour consulter les tables salles, utilisateurs et reservations.
 * Lancer d'abord un simulateur pour remplir la base, puis cette classe.
 */
public class H2Console {

    public static void main(String[] args) throws Exception {
        Server serveur = Server.createWebServer("-webPort", "8082").start();
        System.out.println("Console H2 : " + serveur.getURL());
        System.out.println("JDBC URL  : jdbc:h2:file:./data/reservationdb;AUTO_SERVER=TRUE");
        System.out.println("User      : sa (mot de passe vide)");
        System.out.println("Ctrl+C pour arrêter.");
        Server.openBrowser(serveur.getURL());
    }
}
