package ar.com.federicomorenorodriguez.sitio.Exception;

public class UsernameOrIdNotFound extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4370113218443460116L;

	public UsernameOrIdNotFound() {
		super("Usuario o Id no encontrado");
	}

	public UsernameOrIdNotFound(String message) {
		super(message);
	}
}
