package org.example;

import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

public class AuthService {
    private Driver driver;

    public AuthService(Driver driver) {
        this.driver = driver;
    }

    /**
     * Gestisce il login dell'utente
     * @param email Email dell'utente
     * @param password Password in chiaro
     * @return LoginResult contenente esito e userId se successo
     */
    public LoginResult login(String email, String password) {
        try (Session session = driver.session()) {
            String query = "MATCH (u:User {email: $email}) RETURN u.userId AS id, u.password AS password";

            Result result = session.run(query, Values.parameters("email", email));

            if (result.hasNext()) {
                Record record = result.next();
                String storedHashedPassword = record.get("password").asString();

                // Verifica password usando BCrypt
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    String userId = record.get("id").asString();
                    return new LoginResult(true, userId, "Login riuscito");
                } else {
                    return new LoginResult(false, null, "Password errata");
                }
            } else {
                return new LoginResult(false, null, "Utente non trovato");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResult(false, null, "Errore del server");
        }
    }

    /**
     * Gestisce la registrazione di un nuovo utente
     * @param email Email dell'utente
     * @param password Password in chiaro (verrà hashata)
     * @return LoginResult contenente esito e userId se successo
     */
    public LoginResult  register(String email, String password) {
        try (Session session = driver.session()) {
            // Verifica se l'email esiste già
            String checkQuery = "MATCH (u:User {email: $email}) RETURN u";
            Result checkResult = session.run(checkQuery, Values.parameters("email", email));

            if (checkResult.hasNext()) {
                return new LoginResult(false, null, "Email già registrata");
            }

            // Hash della password con BCrypt
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // Crea nuovo utente
            String createQuery = "CREATE (u:User {userId: randomUUID(), email: $email, password: $password, createdAt: datetime()}) RETURN u.userId AS id";

            Result result = session.run(createQuery,
                    Values.parameters("email", email, "password", hashedPassword));

            if (result.hasNext()) {
                String userId = result.next().get("id").asString();
                return new LoginResult(true, userId, "Registrazione completata");
            } else {
                return new LoginResult(false, null, "Errore durante la registrazione");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResult(false, null, "Errore del server");
        }
    }

    /**
     * Classe per rappresentare il risultato di login/registrazione
     */
    public static class LoginResult {
        private boolean success;
        private String userId;
        private String message;

        public LoginResult(boolean success, String userId, String message) {
            this.success = success;
            this.userId = userId;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getUserId() {
            return userId;
        }

        public String getMessage() {
            return message;
        }
    }
}

