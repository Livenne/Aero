package io.github.livenne;

public enum MatchType {
    EQUAL("="),LIKE("LIKE");

    final String string;

    MatchType(String string) {
        this.string = string;
    }


    @Override
    public String toString() {
        return string;
    }

}
