package com.task.cat.db;

import com.task.cat.entity.Cat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class CatsDAO {
// проапдейтать sql(добавить
    public void createCat(Cat cat) {
        String sql = " INSERT INTO \"Cats\".\"Cat\"(name, age, weight) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cat.getName());
            pstmt.setInt(2, cat.getAge());
            pstmt.setInt(3, cat.getWeight());
            pstmt.executeUpdate();
// Узнать как получить айди после инсерта
            System.out.println("Cat added successfully");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public Cat getCatById(int id) {
        String sql = "SELECT * FROM \"Cats\".\"Cat\" WHERE id = ?";
        Cat cat = new Cat();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                cat.setId(rs.getInt("id"));
                cat.setName(rs.getString("name"));
                cat.setAge(rs.getInt("age"));
                cat.setWeight(rs.getInt("weight"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cat;
    }

    public void updateCat(Cat cat) {
        String sql = "UPDATE cats SET name = ?, age = ?, weight = ?, WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cat.getName());
            pstmt.setInt(2, cat.getAge());
            pstmt.setInt(3, cat.getWeight());
            pstmt.setInt(4, cat.getId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Cat updated successfully");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteCat(int id) {
        String sql = "DELETE FROM cats WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Cat deleted successfully");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

