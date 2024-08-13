package com.task.cat.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.task.cat.entity.Cat;
import java.util.ArrayList;
import java.util.List;


public class CatsDAO {
// надо разобрать эту часть кода
    public List<Cat> getAllCats() {
        String sql = "SELECT * FROM \"Cat\".\"CatT\"";
        List<Cat> cats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Cat cat = new Cat();
                cat.setId(rs.getInt("id"));
                cat.setName(rs.getString("name"));
                cat.setAge(rs.getInt("age"));
                cat.setWeight(rs.getInt("weight"));
                cats.add(cat);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return cats;
    }

    public void createCat(Cat cat) {
        String sql = " INSERT INTO \"Cat\".\"CatT\"(name, age, weight) VALUES (?, ?, ?)";

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
        String sql = "SELECT * FROM \"Cat\".\"CatT\" WHERE id = ?";
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
        String sql = "UPDATE \"Cat\".\"CatT\" SET name = ?, age = ?, weight = ? WHERE id = ?";

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
        String sql = "DELETE FROM \"Cat\".\"CatT\" WHERE id = ?";

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

