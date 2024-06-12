package es.ubu.lsi.TallerJPA.Model;


import jakarta.persistence.*;


/**
 * Clase User.
 * 
 * Entidad de usuario para el login de la aplicación.
 * 
 * @author Daniel Fernández Barrientos
 * @author Ismael Manazanera López
 * 
 * @version 1.0
 */
@Entity
@Table(name = "Users")
public class User {
	
	
    /** The email. */
    @Id
    @Column(name = "email", length = 100)
    private String email;
    
    /** The password. */
    @Column(name = "password", length = 100)
    private String password;
    
    /** The firstname. */
    @Column(name = "firstname", length = 100)
    private String firstname;
    
    /** The lastname. */
    @Column(name = "lastname", length = 100)
    private String lastname;

    
    
	/**
	 * Gets the firstname.
	 *
	 * @return the firstname
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * Sets the firstname.
	 *
	 * @param firstname the new firstname
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	/**
	 * Gets the lastname.
	 *
	 * @return the lastname
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * Sets the lastname.
	 *
	 * @param lastname the new lastname
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email.
	 *
	 * @param email the new email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
    
    
    

}
