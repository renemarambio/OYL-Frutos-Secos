package cl.oyl.pedidos.exception;

/** ms-productos no responde. Responde 503. */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) { super(message); }
}
