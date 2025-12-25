package io.github.livenne;

public interface ResponseStatus {
    Integer SUCCESS = 200;
    Integer UNAUTHORIZED = 401;
    Integer NOT_PERMISSION = 403;
    Integer NOT_FOUND = 404;
    Integer ERROR = 500;
}
