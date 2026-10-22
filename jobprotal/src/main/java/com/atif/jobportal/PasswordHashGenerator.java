package com.atif.jobportal;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.Console;
import java.util.Arrays;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException(
                    "Run this utility from an interactive console.");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        char[] passwordCharacters = console.readPassword("Password to hash: ");
        try {
            System.out.println(encoder.encode(new String(passwordCharacters)));
        } finally {
            Arrays.fill(passwordCharacters, '\0');
        }
    }
}