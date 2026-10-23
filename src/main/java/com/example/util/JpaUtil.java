package com.example.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Fournit une seule EntityManagerFactory pour toute l'application :
 * sa création est coûteuse, on la partage entre les services.
 */
public final class JpaUtil {

    private static final String PROPRIETE_URL = "javax.persistence.jdbc.url";

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("reservationPU", surcharges());

    private JpaUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }

    public static void fermer() {
        if (EMF.isOpen()) {
            EMF.close();
        }
    }

    // Permet de changer de base sans toucher à persistence.xml (les tests utilisent une base en mémoire)
    private static Map<String, String> surcharges() {
        Map<String, String> proprietes = new HashMap<>();
        String url = System.getProperty(PROPRIETE_URL);
        if (url != null) {
            proprietes.put(PROPRIETE_URL, url);
        }
        return proprietes;
    }
}
