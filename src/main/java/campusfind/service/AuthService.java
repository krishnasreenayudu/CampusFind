package campusfind.service;

import campusfind.dao.UserDAO;
import campusfind.model.Role;
import campusfind.model.User;
import campusfind.util.PasswordHasher;
import campusfind.util.Validator;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    /** Registers a new STUDENT account. */
    public void register(String name, String roll, String email, String password, String confirm) {
        if (Validator.isBlank(name) || name.trim().length() > 100) {
            throw new ServiceException("Please enter your name.");
        }
        if (!Validator.validRoll(roll)) {
            throw new ServiceException("Roll number must be 3-30 letters, digits, - or _.");
        }
        if (!Validator.validEmail(email)) {
            throw new ServiceException("Please enter a valid email address.");
        }
        if (!Validator.validPassword(password)) {
            throw new ServiceException("Password must be at least 6 characters.");
        }
        if (!password.equals(confirm)) {
            throw new ServiceException("Passwords do not match.");
        }
        String cleanRoll = roll.trim().toUpperCase();
        String cleanEmail = email.trim().toLowerCase();
        Db.exec(c -> {
            if (userDAO.emailExists(c, cleanEmail)) {
                throw new ServiceException("That email is already registered.");
            }
            if (userDAO.rollExists(c, cleanRoll)) {
                throw new ServiceException("That roll number is already registered.");
            }
            userDAO.insert(c, name.trim(), cleanRoll, cleanEmail, PasswordHasher.hash(password), Role.STUDENT);
        });
    }

    /** Logs in with email or roll number. The chosen role must match the account's role. */
    public User login(String identifier, String password, Role expectedRole) {
        if (Validator.isBlank(identifier) || password == null || password.isEmpty()) {
            throw new ServiceException("Enter your email (or roll number) and password.");
        }
        UserDAO.Credentials found = Db.read(c -> userDAO.findForLogin(c, identifier.trim()).orElse(null));
        if (found == null || !PasswordHasher.verify(password, found.passwordHash())) {
            throw new ServiceException("Invalid email/roll number or password.");
        }
        if (found.user().role() != expectedRole) {
            throw new ServiceException("This is not "
                    + (expectedRole == Role.ADMIN ? "an administrator" : "a student") + " account.");
        }
        return found.user();
    }
}
