package ar.com.federicomorenorodriguez.sitio.service;

import ar.com.federicomorenorodriguez.sitio.Exception.UsernameOrIdNotFound;
import ar.com.federicomorenorodriguez.sitio.dto.ChangePasswordForm;
import ar.com.federicomorenorodriguez.sitio.entity.User;

public interface UserService {

	public Iterable<User> getAllUsers();

	public User createUser(User user) throws Exception;

	public User getUserById(Long id) throws Exception;
	
	public User updateUser(User user) throws Exception;
	
	public void deleteUser(Long id) throws UsernameOrIdNotFound;
	
	public User changePassword(ChangePasswordForm form) throws Exception;
}
