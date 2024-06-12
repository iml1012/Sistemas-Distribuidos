package es.ubu.lsi.TallerJPA.Repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import es.ubu.lsi.TallerJPA.Model.User;

/**
 * Interface UserRespository.
 * 
 * Interfaz que sirve de repositorio para la entidad User.
 * 
 * @author Daniel Fernández Barrientos
 * @author Ismael Manazanera López
 * 
 * @version 1.0
 */
@Repository
public interface UserRepository extends CrudRepository<User, String>{

}
