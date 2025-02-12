package net.detalk.api.support.error;

public class ExpiredTokenException extends TokenException {
    public ExpiredTokenException() {
        super("Expired token");
    }
}
