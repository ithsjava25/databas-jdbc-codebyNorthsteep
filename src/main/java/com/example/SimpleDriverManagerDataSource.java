package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Fungerar som en fabrik för dataanslutningar baserat från JDBC-Uppgifter från start
//När jag skapar en instans i main av denna så kräver den att jag skickar med
//de konfigurationsvärdera = url, lösenord och användarnamn som jag vill använda
// När en Repository-klass anropar getConnection så skickas automatiskt en anslutning härifrån
public class SimpleDriverManagerDataSource implements DataSource {
    String url;
    String user;
    String password;

    public SimpleDriverManagerDataSource(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);

    }
}
