package ar.com.federicomorenorodriguez.sitio.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.federicomorenorodriguez.sitio.dto.ChangePasswordForm;
import ar.com.federicomorenorodriguez.sitio.entity.User;
import ar.com.federicomorenorodriguez.sitio.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Override
	public Iterable<User> getAllUsers() {
		return userRepository.findAll();
	}

	private boolean checkUsernameAvailable(User user) throws Exception {
		Optional<User> userFound = userRepository.findByUsername(user.getUsername());
		if (userFound.isPresent()) {
			throw new Exception("Username already taken.");
		}
		return true;
	}

	private boolean checkPasswordValid(User user) throws Exception {

		if (user.getConfirmPassword() == null || user.getConfirmPassword().isEmpty()) {
			throw new Exception("Confirm password es obligatorio");
		}

		if (!user.getPassword().equals(user.getConfirmPassword())) {
			throw new Exception("Password doesn't match.");
		}
		return true;
	}

	@Override
	public User createUser(User user) throws Exception {
		if (checkUsernameAvailable(user) && checkPasswordValid(user)) {
			user = userRepository.save(user);
		}
		return user;
	}

	@Override
	public User getUserById(Long id) throws Exception {

		return userRepository.findById(id).orElseThrow(() -> new Exception("El usuario no existe"));
	}

	@Override
	public User updateUser(User fromUser) throws Exception {
		User toUser = getUserById(fromUser.getId());
		mapUser(fromUser, toUser);
		return userRepository.save(toUser);
	}

	/**
	 * Map everything but the password
	 * 
	 * @param from
	 * @param to
	 */
	protected void mapUser(User from, User to) {
		to.setUsername(from.getUsername());
		to.setFirstName(from.getFirstName());
		to.setLastName(from.getLastName());
		to.setEmail(from.getEmail());
		to.setRoles(from.getRoles());
	}

	@Override
	public void deleteUser(Long id) throws Exception {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new Exception("User not Found in deleteUser -" + this.getClass().getName()));

		userRepository.delete(user);
	}

	@Override
	public User changePassword(ChangePasswordForm form) throws Exception {
		User storedUser = userRepository.findById(form.getId())
				.orElseThrow(() -> new Exception("User not Found in ChangePassword -" + this.getClass().getName()));

		if (!storedUser.getPassword().equals(form.getCurrentPassword())) {
			throw new Exception("Current Password Incorrect.");
		}

		if (storedUser.getPassword().equals(form.getNewPassword())) {
			throw new Exception("New Password must be different than Current Password!");
		}

		if (!form.getNewPassword().equals(form.getConfirmPassword())) {
			throw new Exception("New Password and Confirm Password does not match!");
		}

		storedUser.setPassword(form.getNewPassword());
		return userRepository.save(storedUser);
	}
}
