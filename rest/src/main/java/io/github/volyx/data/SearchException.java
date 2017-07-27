package io.github.volyx.data;


public class SearchException extends Exception {
    public SearchException(String msg, Exception cause) {
        super(msg, cause);
    }

    public SearchException(String msg) {
        super(msg);
    }
}
