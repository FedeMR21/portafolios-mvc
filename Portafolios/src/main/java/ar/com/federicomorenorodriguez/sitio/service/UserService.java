package ar.com.federicomorenorodriguez.sitio.service;

import javax.validation.Valid;

import ar.com.federicomorenorodriguez.sitio.entity.User;

public interface UserService {

	public Iterable<User> getAllUsers();

	public User createUser(User user) throws Exception;
}
