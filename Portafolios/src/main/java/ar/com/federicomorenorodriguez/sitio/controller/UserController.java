package ar.com.federicomorenorodriguez.sitio.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import ar.com.federicomorenorodriguez.sitio.Exception.CustomeFieldValidationException;
import ar.com.federicomorenorodriguez.sitio.Exception.UsernameOrIdNotFound;
import ar.com.federicomorenorodriguez.sitio.dto.ChangePasswordForm;
import ar.com.federicomorenorodriguez.sitio.entity.Role;
import ar.com.federicomorenorodriguez.sitio.entity.User;
import ar.com.federicomorenorodriguez.sitio.repository.RoleRepository;
import ar.com.federicomorenorodriguez.sitio.repository.UserRepository;
import ar.com.federicomorenorodriguez.sitio.service.UserService;

@Controller
public class UserController {

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserService userService;

	@GetMapping({ "/", "/login" })
	public String index() {
		return "index";
	}

	@GetMapping("/signup")
	public String signup(ModelMap model) {
		Role userRole = roleRepository.findByName("USER");
		List<Role> roles = Arrays.asList(userRole);
		model.addAttribute("userForm", new User());
		model.addAttribute("roles", roles);
		model.addAttribute("signup", true);
		return "user-form/user-signup";
	}

	@PostMapping("/signup")
	public String postSignup(@Valid @ModelAttribute("userForm") User user, BindingResult result, ModelMap model) {

		Role userRole = roleRepository.findByName("USER");
		List<Role> roles = Arrays.asList(userRole);
		model.addAttribute("userForm", user);
		model.addAttribute("roles", roles);
		model.addAttribute("signup", true);

		if (result.hasErrors()) {
			return "user-form/user-signup";
		} else {
			try {
				userService.createUser(user);
			} catch (CustomeFieldValidationException e) {
				result.rejectValue(e.getFieldName(), null, e.getMessage());
				return "user-form/user-signup";
			} catch (Exception e) {
				model.addAttribute("formErrorMessage", e.getMessage());
				return "user-form/user-signup";
			}
		}
		return "index";
	}

	@GetMapping("/userForm")
	public String getUserForm(Model model) {
		model.addAttribute("userForm", new User());
		model.addAttribute("roles", roleRepository.findAll());
		model.addAttribute("userList", userService.getAllUsers());
		model.addAttribute("listTab", "active");
		return "user-form/user-view";
	}

	@PostMapping("/userForm")
	public String createUser(@Valid @ModelAttribute("userForm") User user, BindingResult result, ModelMap model) {
		if (result.hasErrors()) {
			model.addAttribute("userForm", user);
			model.addAttribute("formTab", "active");
		} else {
			try {
				userService.createUser(user);
				model.addAttribute("userForm", new User());
				model.addAttribute("listTab", "active");
			} catch (CustomeFieldValidationException e) {
				result.rejectValue(e.getFieldName(), null, e.getMessage());
				model.addAttribute("userForm", user);
				model.addAttribute("formTab", "active");
				model.addAttribute("userList", userService.getAllUsers());
				model.addAttribute("roles", roleRepository.findAll());
			} catch (Exception e) {
				model.addAttribute("formErrorMessage", e.getMessage());
				model.addAttribute("userForm", user);
				model.addAttribute("formTab", "active");
				model.addAttribute("userList", userService.getAllUsers());
				model.addAttribute("roles", roleRepository.findAll());
			}
		}
		model.addAttribute("userList", userService.getAllUsers());
		model.addAttribute("roles", roleRepository.findAll());
		return "user-form/user-view";
	}

	@GetMapping("/editUser/{id}")
	public String getEditUserForm(Model model, @PathVariable(name = "id") Long id) throws Exception {
		User userToEdit = userService.getUserById(id);
		model.addAttribute("userForm", userToEdit);
		model.addAttribute("userList", userService.getAllUsers());
		model.addAttribute("formTab", "active");
		model.addAttribute("editMode", "true");
		model.addAttribute("passwordForm", new ChangePasswordForm(id));
		model.addAttribute("roles", roleRepository.findAll());
		return "user-form/user-view";
	}

	@PostMapping("/editUser")
	public String postEditUserForm(@Valid @ModelAttribute("userForm") User user, BindingResult result, ModelMap model) {
		if (result.hasErrors()) {
			model.addAttribute("userForm", user);
			model.addAttribute("formTab", "active");
			model.addAttribute("editMode", "true");
			model.addAttribute("passwordForm", new ChangePasswordForm(user.getId()));
		} else {
			try {
				userService.updateUser(user);
				model.addAttribute("userForm", new User());
				model.addAttribute("listTab", "active");
			} catch (Exception e) {
				model.addAttribute("formErrorMessage", e.getMessage());
				model.addAttribute("userForm", user);
				model.addAttribute("formTab", "active");
				model.addAttribute("userList", userService.getAllUsers());
				model.addAttribute("roles", roleRepository.findAll());
				model.addAttribute("editMode", "true");
				model.addAttribute("passwordForm", new ChangePasswordForm(user.getId()));
			}
		}
		model.addAttribute("userList", userService.getAllUsers());
		model.addAttribute("roles", roleRepository.findAll());
		return "user-form/user-view";
	}

	@GetMapping("/userForm/cancel")
	public String cancelEditUser(ModelMap model) {
		return "redirect:/userForm";
	}

	@GetMapping("/deleteUser/{id}")
	public String deleteUser(Model model, @PathVariable(name = "id") Long id) {
		try {
			userService.deleteUser(id);
		} catch (UsernameOrIdNotFound e) {
			model.addAttribute("listErrorMessage", e.getMessage());
		}

		return getUserForm(model);
	}

	@PostMapping("/editUser/changePassword")
	public ResponseEntity<String> postEditUseChangePassword(@Valid @RequestBody ChangePasswordForm form,
			Errors errors) {

		try {
			// If error, just return a 400 bad request, along with the error message
			if (errors.hasErrors()) {
				String result = errors.getAllErrors().stream().map(x -> x.getDefaultMessage())
						.collect(Collectors.joining(""));

				throw new Exception(result);
			}
			userService.changePassword(form);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok("success");
	}

}
