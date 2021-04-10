package ar.com.federicomorenorodriguez.sitio.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ar.com.federicomorenorodriguez.sitio.entity.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

	public Role findByName(String name);
}
