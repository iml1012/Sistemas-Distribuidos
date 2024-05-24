package es.ubu.lsi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.ubu.lsi.service.UserService;

/**
 * Clase UserController.
 * 
 * Clase para comprobar que los usuarios pueden iniciar sesión en el chat.
 * 
 * @author Daniel Fernández Barrientos
 * @author Ismael Manzanera López
 * 
 * @version 1.0
 */

@RestController
public class UserController {

	/** Inyección del UserService */
    @Autowired
    private UserService userService;

    /**
     * Método validateUser.
     * 
     * @param username
     * @param password
     * @return true o false - si el usuario puede iniciar seisón.
     */
    @GetMapping("/validateUser")
    public boolean validateUser(@RequestParam String username, @RequestParam String password) {
        return userService.userIsValid(username, password);
    }
    
    
    /**
     * Método getLevel.
     * 
     * @param username
     * @return int - el nivel que tiene asociado el usuario
     */
    @GetMapping("/getLevel")
    public int getLevel(@RequestParam String username) {
        return userService.userLevel(username);
    }

}