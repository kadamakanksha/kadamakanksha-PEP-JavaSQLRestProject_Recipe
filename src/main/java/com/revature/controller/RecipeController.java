package com.revature.controller;

import io.javalin.http.Handler;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;
import com.revature.model.Recipe;
import com.revature.service.AuthenticationService;
import com.revature.service.RecipeService;
import com.revature.util.Page;

/**
 * The RecipeController class provides RESTful endpoints for managing recipes.
 * It interacts with the RecipeService to fetch, create, update, and delete recipes.
 * Handlers in this class are fields assigned to lambdas, which define the behavior for each endpoint.
 */

public class RecipeController {

    /** The service used to interact with the recipe data. */
    @SuppressWarnings("unused")
    private RecipeService recipeService;

    /** A service that handles authentication-related operations. */
    @SuppressWarnings("unused")
    private AuthenticationService authService;

    /**
     * TODO: Constructor that initializes the RecipeController with the parameters.
     * 
     * @param recipeService The service that handles the business logic for managing recipes.
     * * @param authService the service used to manage authentication-related operations
     */
    public RecipeController(RecipeService recipeService, AuthenticationService authService) {
        this.recipeService = recipeService;
        this.authService = authService;
    }

    /**
     * TODO: Handler for fetching all recipes. Supports pagination, sorting, and filtering by recipe name or ingredient.
     * 
     * Responds with a 200 OK status and the list of recipes, or 404 Not Found with a result of "No recipes found".
     */
    public Handler fetchAllRecipes = ctx -> {
            // Retrieve query parameters for pagination, sorting, and filtering
            int page = getParamAsClassOrElse(ctx, "page", Integer.class, 1);
            int pageSize = getParamAsClassOrElse(ctx, "pageSize", Integer.class, 10);
            String sortBy = getParamAsClassOrElse(ctx, "sortBy", String.class, "name");
            String sortDirection = getParamAsClassOrElse(ctx, "sortDirection", String.class, "asc");
            String searchTermRecipe = ctx.queryParam("name");
            String searchTermPaginated = getParamAsClassOrElse(ctx, "term", String.class, "");
            String ingredientTerm = ctx.queryParam("ingredient");
            // Log the parameters
            System.out.println("Parameters - page: " + page + ", pageSize: " + pageSize + ", sortBy: " + sortBy + ", sortDirection: " + sortDirection + ", searchTerm: " + searchTermRecipe + ", ingredientTerm: " + ingredientTerm);
        
            // Use the service to fetch the recipes
            System.out.println("Calling recipeService.searchRecipes with name: " + searchTermRecipe);            
            List<Recipe> recipes = recipeService.searchRecipes(searchTermRecipe);
            Page<Recipe> recipesPage = recipeService.searchRecipes(searchTermPaginated, page, pageSize, sortBy, sortDirection);
            // Log the results
            System.out.println("Recipes found: " + recipes);
            System.out.println("Paginated Recipes found: " + recipesPage);
           // System.out.println("Recipes paginated found: " + recipesPage);
        
            // If no recipes found, respond with 404
            if (recipes == null || recipes.isEmpty()) {
                ctx.status(404);
                ctx.result("No recipes found");
            } else if(recipesPage == null) {
                // Otherwise, respond with the recipes
                ctx.status(200);
                ctx.json(recipes);
            } else {
                ctx.status(200);
                ctx.json(recipesPage);

            }
    };

    /**
     * TODO: Handler for fetching a recipe by its ID.
     * 
     * If successful, responds with a 200 status code and the recipe as the response body.
     * 
     * If unsuccessful, responds with a 404 status code and a result of "Recipe not found".
     */
    public Handler fetchRecipeById = ctx -> {
        int id = Integer.parseInt(ctx.pathParam("id"));
        String token = ctx.header("Authorization");
        
        if (token != null ) {
            if(token.startsWith("Bearer ") && authService.getChefFromSessionToken(token.substring(7)) == null)
            {
                Optional<Recipe> recipe = recipeService.findRecipe(id);

                if (recipe.isPresent()) {
                    ctx.status(200);
                    ctx.json(recipe.get());
                } else {
                    ctx.status(404);
                    ctx.result("Recipe not found");
                }
            }
        } 
    
        Optional<Recipe> recipe = recipeService.findRecipe(id);

        if (recipe.isPresent()) {
            ctx.status(200);
            ctx.json(recipe.get());
        } else {
            ctx.status(404);
            ctx.result("Recipe not found");
        }
    };

    /**
     * TODO: Handler for creating a new recipe. Requires authentication via an authorization token taken from the request header.
     * 
     * If successful, responds with a 201 Created status.
     * If unauthorized, responds with a 401 Unauthorized status.
     */
    public Handler createRecipe = ctx -> {
        String token = ctx.header("Authorization");

        // Check if the token is null or doesn't start with "Bearer "
        if (token == null || !token.startsWith("Bearer ")) {
            ctx.status(401);
            ctx.result("Unauthorized - Invalid token format");
            return;
        }
    
        // Extract the actual token by removing the "Bearer " prefix
        token = token.substring(7);
        if (authService.getChefFromSessionToken(token) == null) {
            ctx.status(401);
            ctx.result("Unauthorized");
            return;
        }

        Recipe recipe = ctx.bodyAsClass(Recipe.class);
        System.out.println(recipe +"in create handler");
        recipeService.saveRecipe(recipe);
        ctx.status(201);
        ctx.json(recipe);
    };

    /**
     * TODO: Handler for deleting a recipe by its id.
     * 
     * If successful, responds with a 200 status and result of "Recipe deleted successfully."
     * 
     * Otherwise, responds with a 404 status and a result of "Recipe not found."
     */
    public Handler deleteRecipe = ctx -> {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Optional<Recipe> recipe = recipeService.findRecipe(id);
        
        if (recipe.isPresent()) {
            System.out.println("in delete handler"+ recipe.get());
            recipeService.deleteRecipe(id);
            ctx.status(200);
            ctx.result("Recipe deleted successfully");
        } else {
            ctx.status(404);
            ctx.result("Recipe not found");
        }
    };

    /**
     * TODO: Handler for updating a recipe by its ID.
     * 
     * If successful, responds with a 200 status code and the updated recipe as the response body.
     * 
     * If unsuccessfuly, responds with a 404 status code and a result of "Recipe not found."
     */
    public Handler updateRecipe = ctx -> {
        String token = ctx.header("Authorization");
        if (token == null || authService.getChefFromSessionToken(token) == null) {
            ctx.status(401);
            ctx.result("Unauthorized");
            return;
        }

        int id = Integer.parseInt(ctx.pathParam("id"));
        Recipe updatedRecipe = ctx.bodyAsClass(Recipe.class);

        Optional<Recipe> existingRecipe = recipeService.findRecipe(id);
        if (existingRecipe.isPresent()) {
            updatedRecipe.setId(id);
            recipeService.saveRecipe(updatedRecipe);
            ctx.status(200);
            ctx.json(updatedRecipe);
        } else {
            ctx.status(404);
            ctx.result("Recipe not found");
        }
    };

    /**
     * A helper method to retrieve a query parameter from the context as a specific class type, or return a default value if the query parameter is not present.
     * 
     * @param <T> The type of the query parameter to be returned.
     * @param ctx The context of the request.
     * @param queryParam The query parameter name.
     * @param clazz The class type of the query parameter.
     * @param defaultValue The default value to return if the query parameter is not found.
     * @return The value of the query parameter converted to the specified class type, or the default value.
     */
    private <T> T getParamAsClassOrElse(Context ctx, String queryParam, Class<T> clazz, T defaultValue) {
        String paramValue = ctx.queryParam(queryParam);
        if (paramValue != null) {
            if (clazz == Integer.class) {
                return clazz.cast(Integer.valueOf(paramValue));
            } else if (clazz == Boolean.class) {
                return clazz.cast(Boolean.valueOf(paramValue));
            } else {
                return clazz.cast(paramValue);
            }
        }
        return defaultValue;
    }

    /**
     * Configure the routes for recipe operations.
     *
     * @param app the Javalin application
     */
    public void configureRoutes(Javalin app) {
        app.get("/recipes", fetchAllRecipes);
        app.get("/recipes/{id}", fetchRecipeById);
        app.post("/recipes", createRecipe);
        app.put("/recipes/{id}", updateRecipe);
        app.delete("/recipes/{id}", deleteRecipe);
    }
}
