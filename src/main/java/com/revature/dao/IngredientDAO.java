
package com.revature.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.revature.util.ConnectionUtil;
import com.revature.util.Page;
import com.revature.util.PageOptions;
import com.revature.model.Ingredient;

/**
 * The IngredientDAO class handles the CRUD operations for Ingredient objects. It provides methods for creating, retrieving, updating, and deleting Ingredient records from the database. 
 * 
 * This class relies on the ConnectionUtil class for database connectivity and also supports searching and paginating through Ingredient records.
 */

public class IngredientDAO {

    /** A utility class used for establishing connections to the database. */
    @SuppressWarnings("unused")
    private ConnectionUtil connectionUtil;

    /**
     * Constructs an IngredientDAO with the specified ConnectionUtil for database connectivity.
     * 
     * TODO: Finish the implementation so that this class's instance variables are initialized accordingly.
     * 
     * @param connectionUtil the utility used to connect to the database
     */
    public IngredientDAO(ConnectionUtil connectionUtil) {
        this.connectionUtil = connectionUtil;
    }

    /**
     * TODO: Retrieves an Ingredient record by its unique identifier.
     *
     * @param id the unique identifier of the Ingredient to retrieve.
     * @return the Ingredient object with the specified id.
     */
    public Ingredient getIngredientById(int id){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "SELECT * FROM INGREDIENT WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Ingredient(rs.getInt("id"), rs.getString("name"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching ingredient by ID: " + id, e);
        }
    }

    /**
     * TODO: Creates a new Ingredient record in the database.
     *
     * @param ingredient the Ingredient object to be created.
     * @return the unique identifier of the created Ingredient.
     */
    public int createIngredient(Ingredient ingredient){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "INSERT INTO INGREDIENT (name) VALUES (?)";
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, ingredient.getName());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating ingredient", e);
        }
    }

    /**
     * TODO: Deletes an ingredient record from the database, including references in related tables.
     *
     * @param ingredient the Ingredient object to be deleted.
     * @throws SQLException 
     */
    public void deleteIngredient(Ingredient ingredient) {
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "DELETE FROM INGREDIENT WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, ingredient.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting ingredient", e);
        }
    }

    /**
     * TODO: Updates an existing Ingredient record in the database.
     *
     * @param ingredient the Ingredient object containing updated information.
     */
    public void updateIngredient(Ingredient ingredient){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "UPDATE INGREDIENT SET name = ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, ingredient.getName());
            ps.setInt(2, ingredient.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating ingredient", e);
        }
    }

    /**
     * TODO: Retrieves all ingredient records from the database.
     *
     * @return a list of all Ingredient objects.
     */
    public List<Ingredient> getAllIngredients(){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "SELECT * FROM INGREDIENT ORDER BY id";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();
            while (rs.next()) {
                ingredients.add(new Ingredient(rs.getInt("id"), rs.getString("name")));
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all ingredients", e);
        }
    }

    /**
     * TODO: Retrieves all ingredient records from the database with pagination options.
     *
     * @param pageOptions options for pagination and sorting.
     * @return a Page of Ingredient objects containing the retrieved ingredients.
     * @throws SQLException 
     */
    public Page<Ingredient> getAllIngredients(PageOptions pageOptions){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "SELECT * FROM INGREDIENT ORDER BY id LIMIT ? OFFSET ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, pageOptions.getPageSize());
            ps.setInt(2, (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize());
            ResultSet rs = ps.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();
            while (rs.next()) {
                ingredients.add(new Ingredient(rs.getInt("id"), rs.getString("name")));
            }

            String countSql = "SELECT COUNT(*) FROM INGREDIENT";
            PreparedStatement countPs = connection.prepareStatement(countSql);
            ResultSet countRs = countPs.executeQuery();
            int totalCount = 0;
            if (countRs.next()) {
                totalCount = countRs.getInt(1);
            }

            int totalPages = (int) Math.ceil((double) totalCount / pageOptions.getPageSize());
            return new Page<>(pageOptions.getPageNumber(), pageOptions.getPageSize(), totalPages, totalCount, ingredients);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching paginated ingredients", e);
        }
    }

    /**
     * TODO: Searches for Ingredient records by a search term in the name.
     *
     * @param term the search term to filter Ingredient names.
     * @return a list of Ingredient objects that match the search term.
     */
    public List<Ingredient> searchIngredients(String term){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "SELECT * FROM INGREDIENT WHERE name LIKE ? ORDER BY id";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, "%" + term + "%");
            ResultSet rs = ps.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();
            while (rs.next()) {
                ingredients.add(new Ingredient(rs.getInt("id"), rs.getString("name")));
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching ingredients by term: " + term, e);
        }
    }

    /**
     * TODO: Searches for Ingredient records by a search term in the name with pagination options.
     *
     * @param term the search term to filter Ingredient names.
     * @param pageOptions options for pagination and sorting.
     * @return a Page of Ingredient objects containing the retrieved ingredients.
     * @throws SQLException 
     */
    public Page<Ingredient> searchIngredients(String term, PageOptions pageOptions){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "SELECT * FROM INGREDIENT WHERE name LIKE ? ORDER BY " + pageOptions.getSortBy() + " " + pageOptions.getSortDirection() + " LIMIT ? OFFSET ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, "%" + term + "%");
            ps.setInt(2, pageOptions.getPageSize());
            ps.setInt(3, (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize());
            ResultSet rs = ps.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();
            while (rs.next()) {
                ingredients.add(new Ingredient(rs.getInt("id"), rs.getString("name")));
            }

            String countSql = "SELECT COUNT(*) FROM INGREDIENT WHERE name LIKE ?";
            PreparedStatement countPs = connection.prepareStatement(countSql);
            countPs.setString(1, "%" + term + "%");
            ResultSet countRs = countPs.executeQuery();
            int totalCount = 0;
            if (countRs.next()) {
                totalCount = countRs.getInt(1);
            }

            int totalPages = (int) Math.ceil((double) totalCount / pageOptions.getPageSize());
            return new Page<>(pageOptions.getPageNumber(), pageOptions.getPageSize(), totalPages, totalCount, ingredients);
        } catch (SQLException e) {
            throw new RuntimeException("Error searching paginated ingredients by term: " + term, e);
        }
    }

    // below are helper methods for your convenience

    /**
     * Maps a single row from the ResultSet to an Ingredient object.
     *
     * @param resultSet the ResultSet containing Ingredient data.
     * @return an Ingredient object representing the row.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
    private Ingredient mapSingleRow(ResultSet resultSet) throws SQLException {
        return new Ingredient(resultSet.getInt("ID"), resultSet.getString("NAME"));
    }

    /**
     * Maps multiple rows from the ResultSet to a list of Ingredient objects.
     *
     * @param resultSet the ResultSet containing Ingredient data.
     * @return a list of Ingredient objects.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
    private List<Ingredient> mapRows(ResultSet resultSet) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<Ingredient>();
        while (resultSet.next()) {
            ingredients.add(mapSingleRow(resultSet));
        }
        return ingredients;
    }

    /**
     * Paginates the results of a ResultSet into a Page of Ingredient objects.
     *
     * @param resultSet the ResultSet containing Ingredient data.
     * @param pageOptions options for pagination and sorting.
     * @return a Page of Ingredient objects containing the paginated results.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
    private Page<Ingredient> pageResults(ResultSet resultSet, PageOptions pageOptions) throws SQLException {
        List<Ingredient> ingredients = mapRows(resultSet);
        int offset = (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize();
        int limit = offset + pageOptions.getPageSize();
        List<Ingredient> subList = ingredients.subList(offset, limit);
        return new Page<>(pageOptions.getPageNumber(), pageOptions.getPageSize(),
                (int) Math.ceil(ingredients.size() / ((float) pageOptions.getPageSize())), ingredients.size(), subList);
    }
}
