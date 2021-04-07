package ar.com.federicomorenorodriguez.sitio.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ar.com.federicomorenorodriguez.sitio.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
	
	public Optional<User> findByUsername(String username);

	public Optional<User> findByIdAndPassword(Long id, String password);
}
