package com.example.repository;

//Definerar vad applikationen kan göra (affärslogik) utan att veta hur.
public interface AccountRepository {
    boolean authenticationForUser(String username, String password);
    int createAccount(String firstname,String lastname,String ssn, String password);
    boolean updateAccount(int userId, String newPassword);
    boolean deleteAccount(int userId);
}
