# TP : Verrouillage optimiste avec JPA / Hibernate (`@Version`)

Gestion de réservations de salles avec JPA 2.2, Hibernate 5.4 et une base H2 en mémoire.
On provoque des modifications concurrentes pour observer l'`OptimisticLockException`, puis on la gère avec un retry.

## Modèle

| Entité        | Champs                                                                          | Versionnée |
|---------------|---------------------------------------------------------------------------------|------------|
| `Salle`       | `nomSalle`, `capacite`, `etage`, `numeroVersion`                                | oui        |
| `Utilisateur` | `nomComplet`, `email`                                                           | non        |
| `Reservation` | `salleReservee` → Salle, `reservePar` → Utilisateur, `heureDebut`, `heureFin`, `objet`, `numeroVersion` | oui        |

## Structure

```
src/main/java/com/example
├── model/        Salle, Utilisateur, Reservation
├── service/      ReservationService(+Impl), ReferentielService(+Impl)
├── util/         JpaUtil (EntityManagerFactory partagée)
├── ReservationRetryHandler.java          retry sur conflit
├── ConcurrentReservationSimulator.java   3 scénarios de conflit
└── ConcurrentReservationWithRetry.java   conflit résolu par retry
src/main/resources/META-INF/persistence.xml
src/test/java/com/example/OptimisticLockingTest.java
```

## Lancer

```bash
mvn test
mvn compile exec:java -Dexec.mainClass=com.example.ConcurrentReservationSimulator
mvn compile exec:java -Dexec.mainClass=com.example.ConcurrentReservationWithRetry
```

## Base H2

La base est stockée dans `./data/reservationdb.mv.db` (ignorée par git). Elle est recréée à chaque lancement d'un simulateur.
Les tests utilisent une base en mémoire séparée, configurée dans le `pom.xml`.

Pour voir les tables après une simulation :

```bash
mvn compile exec:java -Dexec.mainClass=com.example.H2Console
```

La console s'ouvre sur http://localhost:8082. Paramètres de connexion :

- JDBC URL : `jdbc:h2:file:./data/reservationdb;AUTO_SERVER=TRUE`
- User : `sa`, mot de passe vide

`AUTO_SERVER=TRUE` permet aussi d'ouvrir la base depuis l'onglet Database d'IntelliJ pendant que l'application tourne.

## Résultat attendu

Simulation sans retry :

```
Transaction 1 : modification réussie -> Reservation#1 [Salle B, Réunion d'équipe, v1]
Transaction 2 : conflit détecté - Version obsolète : v0 lue, v1 en base
EntityManager 1 : commit réussi (version 2)
EntityManager 2 : conflit détecté par Hibernate - Row was updated or deleted by another transaction ...
Gestionnaire 2 : conflit détecté - Row was updated or deleted by another transaction ... [Salle#1]
```

Avec retry :

```
Conflit détecté (tentative 1/3) : Version obsolète : v0 lue, v1 en base
Transaction 2 : modification réussie après retry -> Reservation#1 [Salle C, Cours microservices, v2]
```

## Fonctionnement

- Hibernate ajoute `and numero_version=?` à chaque `UPDATE` et incrémente la version. Si aucune ligne n'est touchée, la donnée a changé entre-temps et une `OptimisticLockException` est levée.
- `modifierReservation` reçoit une copie détachée. Il compare d'abord sa version avec celle en base, puis s'appuie sur la clause SQL pour le cas où une écriture arrive entre la lecture et le commit.
- `em.flush()` avant `commit()` fait remonter l'`OptimisticLockException` telle quelle, au lieu d'une `RollbackException` qui l'envelopperait.
- Le retry recharge la dernière version et y recopie les valeurs voulues. C'est une stratégie « le dernier gagne ». Dans une vraie application, on préviendrait plutôt l'utilisateur et on lui proposerait de recharger.

**Avantages :** pas de verrou en base ni de deadlock, bonne montée en charge.
**Limites :** les conflits sont détectés après coup, pas évités, et il faut toujours gérer l'exception.

## Captures

<img width="1702" height="705" alt="image" src="https://github.com/user-attachments/assets/df94862e-33dd-49bc-a34d-28cece62b78b" />
<img width="1513" height="677" alt="image" src="https://github.com/user-attachments/assets/b783e2d6-4407-499c-8b9e-78481af89cc0" />
<img width="1622" height="692" alt="image" src="https://github.com/user-attachments/assets/4f097adb-910c-4144-8af5-4d27a531ea9c" />
<img width="1919" height="609" alt="image" src="https://github.com/user-attachments/assets/e2d9d7df-dd8d-46bf-87e5-1785c3b3611d" />
<img width="1918" height="775" alt="image" src="https://github.com/user-attachments/assets/4c0256a1-6d1c-4c13-8bfe-49273b2f0df8" />





