package de.codecentric.psd.worblehat.codequality.service;

import java.util.HashMap;
import java.util.Map;

public class Mail {

    private String from;
    private String to;
    private String subject;
    private String body;
    private Map<String, String> arguments = new HashMap<>();

    private Mail() {
        // don't use this constructor directly, use the factory method instead
    }

    public static Mail aMail() {
        return new Mail();
    }


    public Mail from(String from) {
        this.from = from;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Mail to(String to) {
        this.to = to;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Mail withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public Mail withBody(String... line) {
        this.body = String.join(System.getProperty("line.separator"), line);
        return this;
    }

    public String getBody() {
        return body;
    }

    public Mail withArgument(String key, String value) {
        this.arguments.put(key, value);
        return this;
    }

    public Mail withArguments(Map<String, String> arguments) {
        this.arguments = arguments;
        return this;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }
}
