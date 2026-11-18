package cl.oyl.pedidos.exception;

/** Regla de negocio violada (stock insuficiente, código de barras duplicado, etc.). Responde 409. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
