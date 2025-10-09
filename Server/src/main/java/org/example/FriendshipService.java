package org.example;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;

public class FriendshipService {
    private final Driver driver;

    public FriendshipService(Driver driver) {
        this.driver = driver;
    }

    // Mostra tutti gli utenti
    public List<String> getAllUsers() {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run("MATCH (u:User) RETURN u.email AS email");
                List<String> users = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    users.add(record.get("email").asString());
                }
                return users;
            });
        }
    }

    // Ricerca utente per email
    public boolean userExists(String email) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run("MATCH (u:User {email: $email}) RETURN u",
                        Values.parameters("email", email));
                return result.hasNext();
            });
        }
    }

    // Invia richiesta di amicizia
    public void sendFriendRequest(String fromEmail, String toEmail) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (a:User {email: $from}), (b:User {email: $to}) " +
                                "CREATE (a)-[:FRIEND_REQUEST]->(b)",
                        Values.parameters("from", fromEmail, "to", toEmail));
                return null;
            });
        }
    }

    // Accetta richiesta di amicizia
    public void acceptFriendRequest(String userEmail, String requesterEmail) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (a:User {email: $requester})-[r:FRIEND_REQUEST]->(b:User {email: $user}) " +
                                "DELETE r " +
                                "CREATE (a)-[:FRIENDS]->(b), (b)-[:FRIENDS]->(a)",
                        Values.parameters("user", userEmail, "requester", requesterEmail));
                return null;
            });
        }
    }

    // Rifiuta richiesta di amicizia
    public void rejectFriendRequest(String userEmail, String requesterEmail) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (a:User {email: $requester})-[r:FRIEND_REQUEST]->(b:User {email: $user}) " +
                                "DELETE r",
                        Values.parameters("user", userEmail, "requester", requesterEmail));
                return null;
            });
        }
    }

    // Elimina amicizia
    public void removeFriendship(String email1, String email2) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (a:User {email: $email1})-[r:FRIENDS]-(b:User {email: $email2}) " +
                                "DELETE r",
                        Values.parameters("email1", email1, "email2", email2));
                return null;
            });
        }
    }

    // Lista amici
    public List<String> getFriends(String email) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run("MATCH (u:User {email: $email})-[:FRIENDS]->(friend) " +
                                "RETURN friend.email AS email",
                        Values.parameters("email", email));
                List<String> friends = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    friends.add(record.get("email").asString());
                }
                return friends;
            });
        }
    }

    // Lista richieste di amicizia ricevute
    public List<String> getPendingFriendRequests(String email) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run("MATCH (requester:User)-[:FRIEND_REQUEST]->(u:User {email: $email}) " +
                                "RETURN requester.email AS email",
                        Values.parameters("email", email));
                List<String> requests = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    requests.add(record.get("email").asString());
                }
                return requests;
            });
        }
    }
}
