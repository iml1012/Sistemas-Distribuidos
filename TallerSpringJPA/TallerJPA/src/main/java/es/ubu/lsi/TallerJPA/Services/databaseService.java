package es.ubu.lsi.TallerJPA.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.ubu.lsi.TallerJPA.Model.User;
import es.ubu.lsi.TallerJPA.Repository.UserRepository;

/**
 * Clase databaseService.
 * 
 * Esta clase contendrá todos los métodos necesarios para las comprobaciones de las tablas de la base de datos.
 * 
 * @author Daniel Fernández Barrientos
 * @author Ismael Manazanera López
 * 
 * @version 1.0
 */
@Service
public class databaseService {
	
	@Autowired
	private UserRepository userRepository;
	
	
	/**
	 * Método checkRegistered.
	 * 
	 * @param email the email
	 * @param password the password
	 * @return true o false
	 */
	public boolean checkRegistered(String email, String password) {
		Optional<User> userOpt;
		User user = new User();
		
		if (userRepository.existsById(email)){
			userOpt = userRepository.findById(email);
			if (userOpt.isPresent()) {
				user = userOpt.get();
				
				return user.getPassword().equals(password);
			}
			
		}
		return false;		
		
	} 
	
	/**
	 * Metodo updateProfile.
	 * @param email the email
	 * @param password the password
	 */
	public void updateProfile(String email, String password, String firstname, String lastname) {
		
		User user = new User();
		
		user.setEmail(email);
		user.setPassword(password);
		user.setFirstname(firstname);
		user.setLastname(lastname);
		
		userRepository.save(user);
		
	}
	
	/**
	 * Método getUser.
	 * @param email the email
	 * @return the user
	 */
	public User getUser(String email) {
		Optional<User> userOpt;
		User user = new User();
		
		if (userRepository.existsById(email)){
			userOpt = userRepository.findById(email);
			if (userOpt.isPresent()) {
				user = userOpt.get();
			}
		}
		return user;
	}
	
	/**
	 * Método getAllUsers.
	 * @return lista de usuarios
	 */
	public List<User> getAllUsers(){
		List<User> listUsers = (List<User>) userRepository.findAll();
		
		for (User user : listUsers) {
			System.out.println(user.getEmail().toString());
		}
		return listUsers;
	}
	
	

}
