package es.ubu.lsi.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * Clase UserService.
 * 
 * Clase para construir los datos de los usuarios leídos desde un fichero /src/main/resources/csv/usuarios.csv
 * 
 * @author Daniel Fernández Barrientos
 * @author Ismael Manzanera López
 * 
 * @version 1.0
 */
@Service
public class UserService {

	/** Atributos de la clase. */
    private Map<String, String> users = new HashMap<>();
    private Map<String, Integer> usersLevel = new HashMap<>();
    
    /** Variable con el fichero usuarios.csv. */
    private final String file = "/csv/usuarios.csv";
    
    /**
     * Método init().
     * 
     * Inizializa la lista de usuarios, contraseñas y niveles leídos desde un csv.
     * 
     */
    @PostConstruct
    public void init() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(file)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length == 3) {
                    String username = fields[0].trim();
                    String password = fields[1].trim();
                    int level = Integer.parseInt(fields[2].trim());
                    // Store username and password
                    users.put(username, password);
                    usersLevel.put(username, level);

                }
            }
            /** Si se quiere comprobar el listado de usuarios y niveles leído, quitar comentario para verlo en consola */
            // System.out.println("---- Listado de usuarios: " + users + "  -----");
            // System.out.println("---- Listado de niveles: " + usersLevel + "  -----");
        } catch (IOException e) {
            e.toString();
        }
    }

    /**
     * Método userIsValid.
     * 
     * @param username
     * @param password
     * @return true or false
     */
    public boolean userIsValid(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
    
    /**
     * Método userLevel.
     * @param username
     * @return string - level of the user
     */
    public int userLevel(String username) {
        return usersLevel.get(username);
    }
}

