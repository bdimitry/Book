package com.bookdb.book.db;

import com.bookdb.book.entity.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public List<Book> getAllBooks() {
        String sql = "SELECT * FROM \"books\".\"book\"";
        List<Book> books = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setName(rs.getString("name"));
                book.setAuthor(rs.getString("age"));
                book.setWeight(rs.getInt("weight"));
                books.add(book);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return books;
    }

    public void createBooks(Book book) {
        String sql = " INSERT INTO \"books\".\"book\"(name, age, weight) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getName());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getWeight());
            pstmt.executeUpdate();
            System.out.println("Book added successfully");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public Book getBookById(int id) {
        String sql = "SELECT * FROM \"books\".\"book\" WHERE id = ?";
        Book book = new Book();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                book.setId(rs.getInt("id"));
                book.setName(rs.getString("name"));
                book.setAuthor(rs.getString("age"));
                book.setWeight(rs.getInt("weight"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return book;
    }

    public void updateBook(Book book) {
        String sql = "UPDATE \"books\".\"book\" SET name = ?, age = ?, weight = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getName());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getWeight());
            pstmt.setInt(4, book.getId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Book updated successfully");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteBook(int id) {
        String sql = "DELETE FROM \"books\".\"book\" WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Book deleted successfully");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

