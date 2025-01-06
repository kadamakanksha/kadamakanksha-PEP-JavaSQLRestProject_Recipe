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
import com.revature.model.Chef;
import com.revature.model.Recipe;



/**
 * The RecipeDAO class abstracts the CRUD operations for Recipe objects.
 * This class utilizes the previously created classes and primarily functions as a pure functional class, meaning it doesn't store state apart from a  reference to ConnectionUtil for database connection purposes. 
 * 
 * Although the implementation may seem extensive for simple functionality, this design improves testability, maintainability, and extensibility of the overall infrastructure.
 */

public class RecipeDAO {

    /**
	 * DAO for managing Chef entities, used for retrieving chef details associated with recipes.
	 */
	private ChefDAO chefDAO;
	

	/**
	 * DAO for managing Ingredient entities, used for retrieving ingredient details for recipes.
	 */
    @SuppressWarnings("unused")
	private IngredientDAO ingredientDAO;

    /** A utility class for establishing connections to the database. */
    @SuppressWarnings("unused")
    private ConnectionUtil connectionUtil;

	

    /**
	 * Constructs a RecipeDAO instance with specified ChefDAO and IngredientDAO.
	 *
	 * TODO: Finish the implementation so that this class's instance variables are initialized accordingly.
	 * 
	 * @param chefDAO - the ChefDAO used for retrieving chef details.
	 * @param ingredientDAO - the IngredientDAO used for retrieving ingredient details.
     * @param connectionUtil - the utility used to connect to the database
	 */
	public RecipeDAO(ChefDAO chefDAO, IngredientDAO ingredientDAO, ConnectionUtil connectionUtil) {
        this.chefDAO = chefDAO;
		this.ingredientDAO = ingredientDAO;
        this.connectionUtil = connectionUtil;
    }
	

    /**
     * TODO: Retrieves all recipes from the database.
     * 
     * @return a list of all Recipe objects
     */

    public List<Recipe> getAllRecipes(){
		try (Connection connection = connectionUtil.getConnection()) {
            String sql = "SELECT * FROM RECIPE ORDER BY id";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            List<Recipe> recipes = new ArrayList<>();
            recipes = mapRows(rs);
            return recipes;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all recipes", e);
        }
    }

    /**
     * TODO: Retrieves a paginated list of all recipes from the database.
     * 
     * @param pageOptions options for pagination, including page size and page number
     * @return a paginated list of Recipe objects
     */
    public Page<Recipe> getAllRecipes(PageOptions pageOptions){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "SELECT * FROM RECIPE ORDER BY id LIMIT ? OFFSET ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, pageOptions.getPageSize());
            ps.setInt(2, (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize());
            ResultSet rs = ps.executeQuery();

            List<Recipe> recipes = new ArrayList<>();
            while (rs.next()) {
                Chef chef = chefDAO.getChefById(rs.getInt("chef_id"));
                recipes.add(new Recipe(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("instructions"),
                        chef
                ));
            }

            String countSql = "SELECT COUNT(*) FROM RECIPE";
            PreparedStatement countPs = connection.prepareStatement(countSql);
            ResultSet countRs = countPs.executeQuery();
            int totalCount = 0;
            if (countRs.next()) {
                totalCount = countRs.getInt(1);
            }

            int totalPages = (int) Math.ceil((double) totalCount / pageOptions.getPageSize());
            return new Page<>(pageOptions.getPageNumber(), pageOptions.getPageSize(), totalPages, totalCount, recipes);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching paginated recipes", e);
        }  
    }

    /**
     * TODO: Searches for recipes that match a specified term.
     * 
     * @param term the search term to filter recipes by
     * @return a list of Recipe objects that match the search term
     */

    public List<Recipe> searchRecipesByTerm(String term){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "SELECT * FROM RECIPE WHERE name LIKE ? OR instructions LIKE ? ORDER BY id";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, "%" + term + "%");
            ps.setString(2, "%" + term + "%");
            ResultSet rs = ps.executeQuery();

            List<Recipe> recipes = new ArrayList<>();
            while (rs.next()) {
                Chef chef = chefDAO.getChefById(rs.getInt("chef_id"));
                recipes.add(new Recipe(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("instructions"),
                        chef
                ));
            }
            return recipes;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching recipes by term: " + term, e);
        }
    }

    /**
     * TODO: Searches for recipes that match a specified term and returns a paginated result.
     * 
     * @param term the search term to filter recipes by
     * @param pageOptions options for pagination, including page size and page number
     * @return a paginated list of Recipe objects that match the search term
     */

    public Page<Recipe> searchRecipesByTerm(String term, PageOptions pageOptions){
        try (Connection connection = connectionUtil.getConnection()) {
            // Construct the SQL query with default sorting by id
            String sql = "SELECT * FROM RECIPE WHERE name LIKE ? ORDER BY id LIMIT ? OFFSET ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, "%" + term + "%");
            ps.setInt(2, pageOptions.getPageSize());
            ps.setInt(3, (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize());
    
            ResultSet rs = ps.executeQuery();
            List<Recipe> recipes = new ArrayList<>();
            while (rs.next()) {
                recipes.add(new Recipe(rs.getInt("id"), rs.getString("name"), rs.getString("instructions"), chefDAO.getChefById(rs.getInt("chef_id"))));
            }
    
            // Count the total matching elements
            String countSql = "SELECT COUNT(*) FROM RECIPE WHERE name LIKE ?";
            PreparedStatement countPs = connection.prepareStatement(countSql);
            countPs.setString(1, "%" + term + "%");
            ResultSet countRs = countPs.executeQuery();
            int totalCount = 0;
            if (countRs.next()) {
                totalCount = countRs.getInt(1);
            }
    
            // Calculate total pages
            int totalPages = (int) Math.ceil((double) totalCount / pageOptions.getPageSize());
            return new Page<>(pageOptions.getPageNumber(), pageOptions.getPageSize(), totalPages, totalCount, recipes);
        } catch (SQLException e) {
            throw new RuntimeException("Error searching paginated recipes by term: " + term, e);
        }
    }

    /**
     * TODO: Retrieves a specific recipe by its ID.
     * 
     * @param id the ID of the recipe to retrieve
     * @return the Recipe object corresponding to the given ID
     * @throws SQLException 
     */

    public Recipe getRecipeById(int id){
        Recipe recipe;
		try (Connection connection = connectionUtil.getConnection()) {
            String sql = "SELECT * FROM RECIPE WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                recipe = mapSingleRow(rs);
                System.err.println(recipe + "recipe in dao getRecipeById");
                return recipe;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching recipe by ID: " + id, e);
        }
    }
        

    /**
     * TODO: Creates a new recipe in the database.
     * 
     * @param recipe the Recipe object to create
     * @return the ID of the newly created recipe
     */

    public int createRecipe(Recipe recipe){
		try (Connection connection = connectionUtil.getConnection()) {
            String sql = "INSERT INTO RECIPE (name, instructions, chef_id) VALUES (?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, recipe.getName());
            ps.setString(2, recipe.getInstructions());
            ps.setInt(3, recipe.getAuthor() != null ? recipe.getAuthor().getId() : null);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating recipe failed, no rows affected.");
            }

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                connection.commit();
                return newId;
            } 
        return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating recipe", e);
        }
    }

    /**
     * TODO: Updates an existing recipe's instructions and chef_id in the database.
     * 
     * @param recipe the Recipe object with updated data
     */

    public void updateRecipe(Recipe recipe){
        try (Connection connection = connectionUtil.getConnection()) {
            String sql = "UPDATE RECIPE SET instructions = ?, chef_id = ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, recipe.getInstructions());
            ps.setInt(2, recipe.getAuthor().getId());
            ps.setInt(3, recipe.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating recipe", e);
        }
    }

    /**
     * TODO: Deletes a specific recipe from the database.
     * 
     * @param recipe the Recipe object to delete
     */

    public void deleteRecipe(Recipe recipe){
		try (Connection connection = connectionUtil.getConnection()) {
            connection.setAutoCommit(false);

            // Delete dependencies in RECIPE_INGREDIENT first
            String deleteDependencies = "DELETE FROM RECIPE_INGREDIENT WHERE recipe_id = ?";
            PreparedStatement ps1 = connection.prepareStatement(deleteDependencies);
            ps1.setInt(1, recipe.getId());
            ps1.executeUpdate();

            // Delete the recipe itself
            String deleteRecipe = "DELETE FROM RECIPE WHERE id = ?";
            PreparedStatement ps2 = connection.prepareStatement(deleteRecipe);
            ps2.setInt(1, recipe.getId());
            ps2.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting recipe", e);
        }
    }


    // below are helper methods for your convenience
	
	/**
	 * Maps a single row from the ResultSet to a Recipe object.
	 * This method extracts the recipe details such as ID, name, instructions,
	 * and associated chef from the ResultSet and constructs a Recipe instance.
	 *
	 * @param set the ResultSet containing the recipe data
	 * @return a Recipe object representing the mapped row
	 * @throws SQLException if there is an error accessing the ResultSet
	 */
	private Recipe mapSingleRow(ResultSet set) throws SQLException {
		int id = set.getInt("id");
		String name = set.getString("name");
		String instructions = set.getString("instructions");
		Chef author = chefDAO.getChefById(set.getInt("chef_id"));
		return new Recipe(id, name, instructions, author);
	}

	/**
	 * Maps multiple rows from a ResultSet to a list of Recipe objects.
	 * This method iterates through the ResultSet and calls mapSingleRow
	 * for each row, adding the resulting Recipe objects to a list.
	 *
	 * @param set the ResultSet containing multiple recipe rows
	 * @return a list of Recipe objects representing the mapped rows
	 * @throws SQLException if there is an error accessing the ResultSet
	 */
	private List<Recipe> mapRows(ResultSet set) throws SQLException {
		List<Recipe> recipes = new ArrayList<>();
		while (set.next()) {
			recipes.add(mapSingleRow(set));
		}
		return recipes;
	}

	/**
	 * Pages the results from a ResultSet into a Page object for the Recipe entity.
	 * This method processes the ResultSet to retrieve recipes, then slices the list
	 * based on the provided pagination options, and returns a Page object
	 * containing
	 * the paginated results.
	 *
	 * @param set the ResultSet containing recipe data
	 * @param pageOptions the PageOptions object containing pagination details
	 * @return a Page object containing the paginated list of Recipe objects
	 * @throws SQLException if there is an error accessing the ResultSet
	 */
	private Page<Recipe> pageResults(ResultSet set, PageOptions pageOptions) throws SQLException {
		List<Recipe> recipes = mapRows(set);
		int offset = (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize();
		int limit = offset + pageOptions.getPageSize();
		List<Recipe> slicedList = sliceList(recipes, offset, limit);
		return new Page<>(pageOptions.getPageNumber(), pageOptions.getPageSize(),
				recipes.size() / pageOptions.getPageSize(), recipes.size(), slicedList);
	}

	/**
	 * Slices a list of Recipe objects from a specified start index to an end index.
	 * This method creates a sublist of the provided list, which can be used for
	 * pagination.
	 *
	 * @param list  the original list of Recipe objects
	 * @param start the starting index (inclusive) for the slice
	 * @param end   the ending index (exclusive) for the slice
	 * @return a list of Recipe objects representing the sliced portion
	 */
	private List<Recipe> sliceList(List<Recipe> list, int start, int end) {
		List<Recipe> sliced = new ArrayList<>();
		for (int i = start; i < end; i++) {
			sliced.add(list.get(i));
		}
		return sliced;
	}
}

