package campusfind.service;

/** Thrown for any problem the user should be told about (bad input, rule violation, database error). */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) { super(message); }

    public ServiceException(String message, Throwable cause) { super(message, cause); }
}
