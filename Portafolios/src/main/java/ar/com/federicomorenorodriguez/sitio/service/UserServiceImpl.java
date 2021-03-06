package ar.com.federicomorenorodriguez.sitio.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import ar.com.federicomorenorodriguez.sitio.Exception.CustomeFieldValidationException;
import ar.com.federicomorenorodriguez.sitio.Exception.UsernameOrIdNotFound;
import ar.com.federicomorenorodriguez.sitio.dto.ChangePasswordForm;
import ar.com.federicomorenorodriguez.sitio.entity.User;
import ar.com.federicomorenorodriguez.sitio.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public Iterable<User> getAllUsers() {
		return userRepository.findAll();
	}

	private boolean checkUsernameAvailable(User user) throws CustomeFieldValidationException {
		Optional<User> userFound = userRepository.findByUsername(user.getUsername());
		if (userFound.isPresent()) {
			throw new CustomeFieldValidationException("Username already taken.", "username");
		}
		return true;
	}

	private boolean checkPasswordValid(User user) throws CustomeFieldValidationException {

		if (user.getConfirmPassword() == null || user.getConfirmPassword().isEmpty()) {
			throw new CustomeFieldValidationException("Confirm password es obligatorio", "confirmPassword");
		}

		if (!user.getPassword().equals(user.getConfirmPassword())) {
			throw new CustomeFieldValidationException("Password doesn't match.", "password");
		}
		return true;
	}

	@Override
	public User createUser(User user) throws Exception {
		if (checkUsernameAvailable(user) && checkPasswordValid(user)) {
			String encodePassword = bCryptPasswordEncoder.encode(user.getPassword());
			user.setPassword(encodePassword);
			user = userRepository.save(user);
		}
		return user;
	}

	@Override
	public User getUserById(Long id) throws UsernameOrIdNotFound {

		return userRepository.findById(id).orElseThrow(() -> new UsernameOrIdNotFound("El Id del usuario no existe"));
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
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
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public void deleteUser(Long id) throws UsernameOrIdNotFound {
		User user = userRepository.findById(id).orElseThrow(
				() -> new UsernameOrIdNotFound("User not Found in deleteUser -" + this.getClass().getName()));

		userRepository.delete(user);
	}

	@Override
	public User changePassword(ChangePasswordForm form) throws Exception {
		User storedUser = userRepository.findById(form.getId())
				.orElseThrow(() -> new Exception("User not Found in ChangePassword -" + this.getClass().getName()));

		if (!isLoggedUserADMIN() && !storedUser.getPassword().equals(form.getCurrentPassword())) {
			throw new Exception("Current Password Incorrect.");
		}

		if (storedUser.getPassword().equals(form.getNewPassword())) {
			throw new Exception("New Password must be different than Current Password!");
		}

		if (!form.getNewPassword().equals(form.getConfirmPassword())) {
			throw new Exception("New Password and Confirm Password does not match!");
		}

		String encodePassword = bCryptPasswordEncoder.encode(form.getNewPassword());
		storedUser.setPassword(encodePassword);
		return userRepository.save(storedUser);
	}

	private boolean isLoggedUserADMIN() {
		// Obtener el usuario logeado
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		UserDetails loggedUser = null;
		Object roles = null;

		// Verificar que ese objeto traido de sesion es el usuario
		if (principal instanceof UserDetails) {
			loggedUser = (UserDetails) principal;

			roles = loggedUser.getAuthorities().stream().filter(x -> "ROLE_ADMIN".equals(x.getAuthority())).findFirst()
					.orElse(null);
		}
		return roles != null ? true : false;
	}

	private User getLoggedUser() throws Exception {
		// Obtener el usuario logeado
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		UserDetails loggedUser = null;

		// Verificar que ese objeto traido de sesion es el usuario
		if (principal instanceof UserDetails) {
			loggedUser = (UserDetails) principal;
		}

		User myUser = userRepository.findByUsername(loggedUser.getUsername())
				.orElseThrow(() -> new Exception("Problemas obteniendo usuario de sesion"));

		return myUser;
	}
}
