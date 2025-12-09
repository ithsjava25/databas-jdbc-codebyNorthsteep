package com.example;

import java.sql.Connection;
import java.sql.SQLException;

//Standard interface som hanterar dataanslutningen till databasen
//Genom att göra detta så är inte all kod beroende av den nätverksanslutning som sätts i main
//När jag anropar getConnection så får jag direkt en Connection instans
//All min reporitory kod behöver bara känna till anslutningen som kommer från DriverManager
public interface DataSource {
    Connection getConnection() throws SQLException;
}
