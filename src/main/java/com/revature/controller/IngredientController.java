package com.revature.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Optional;

import com.revature.model.Ingredient;
import com.revature.service.IngredientService;
import com.revature.util.Page;


/**
 * The IngredientController class handles operations related to ingredients. It allows for creating, retrieving, updating, and deleting individual ingredients, as well as retrieving a list of all ingredients. 
 * 
 * The class interacts with the IngredientService to perform these operations.
 */

public class IngredientController {

    /**
     * A service that manages ingredient-related operations.
     */

    @SuppressWarnings("unused")
    private IngredientService ingredientService;

    /**
     * Constructs an IngredientController with the specified IngredientService.
     *
     * TODO: Finish the implementation so that this class's instance variables are initialized accordingly.
     * 
     * @param ingredientService the service used to manage ingredient-related operations
     */

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    /**
     * TODO: Retrieves a single ingredient by its ID.
     * 
     * If the ingredient exists, responds with a 200 OK status and the ingredient data. If not found, responds with a 404 Not Found status.
     *
     * @param ctx the Javalin context containing the request path parameter for the ingredient ID
     */
    public void getIngredient(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Optional<Ingredient> ingredient = ingredientService.findIngredient(id);

        if (ingredient.isPresent()) {
            ctx.status(200).json(ingredient.get());
        } else {
            ctx.status(404).result("Ingredient not found");
        }
    }

    /**
     * TODO: Deletes an ingredient by its ID.
     * 
     * Responds with a 204 No Content status.
     *
     * @param ctx the Javalin context containing the request path parameter for the ingredient id
     */
    public void deleteIngredient(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Optional<Ingredient> ingredient = ingredientService.findIngredient(id);

        if (ingredient.isPresent()) {
            ingredientService.deleteIngredient(id);
            ctx.status(204).result("Ingredient deleted successfully");
        } else {
            ctx.status(404).result("Ingredient not found");
        }
    }

    /**
     * TODO: Updates an existing ingredient by its ID.
     * 
     * If the ingredient exists, updates it and responds with a 204 No Content status. If not found, responds with a 404 Not Found status.
     *
     * @param ctx the Javalin context containing the request path parameter and updated ingredient data in the request body
     */
    public void updateIngredient(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Ingredient updatedIngredient = ctx.bodyAsClass(Ingredient.class);

        Optional<Ingredient> existingIngredient = ingredientService.findIngredient(id);
        if (existingIngredient.isPresent()) {
            updatedIngredient.setId(id);
            ingredientService.saveIngredient(updatedIngredient);
            ctx.status(204).result("Ingredient updated successfully");
        } else {
            ctx.status(404).result("Ingredient not found");
        }
    }

    /**
     * TODO: Creates a new ingredient.
     * 
     * Saves the ingredient and responds with a 201 Created status.
     *
     * @param ctx the Javalin context containing the ingredient data in the request body
     */
    public void createIngredient(Context ctx) {
        Ingredient ingredient = ctx.bodyAsClass(Ingredient.class);
        ingredientService.saveIngredient(ingredient);
        ctx.status(201).json(ingredient);
    }

    /**
     * TODO: Retrieves a paginated list of ingredients, or all ingredients if no pagination parameters are provided.
     * 
     * If pagination parameters are included, returns ingredients based on page, page size, sorting, and filter term.
     *
     * @param ctx the Javalin context containing query parameters for pagination, sorting, and filtering
     */
    public void getIngredients(Context ctx) {
        String term = ctx.queryParam("term");
        String sortBy = ctx.queryParam("sortBy") != null ? ctx.queryParam("sortBy") : "id";
        String sortDirection = ctx.queryParam("sortDirection") != null ? ctx.queryParam("sortDirection") : "asc";
        Integer page = getParamAsClassOrElse(ctx, "page", Integer.class, null);
        Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(null);

        if (page == null && pageSize == null && term == null) {
            // Return all ingredients as a plain list
            ctx.status(200).json(ingredientService.searchIngredients(term));
        } else if (term != null && page == null && pageSize == null) {
            // Return filtered ingredients as a plain list
            ctx.status(200).json(ingredientService.searchIngredients(term));
        } else {
            // Return paginated and/or sorted ingredients
            Page<Ingredient> ingredients = ingredientService.searchIngredients(term, page, pageSize, sortBy, sortDirection);
            ctx.status(200).json(ingredients);
        }
    }

    /**
     * A helper method to retrieve a query parameter from the context as a specific class type, or return a default value if the query parameter is not present.
     *
     * @param <T> the type of the query parameter
     * @param ctx the Javalin context containing query parameters
     * @param queryParam the name of the query parameter to retrieve
     * @param clazz the class type of the parameter
     * @param defaultValue the default value to return if the parameter is absent
     * @return the query parameter value as the specified type, or the default value if absent
     */
    private <T> T getParamAsClassOrElse(Context ctx, String queryParam, Class<T> clazz, T defaultValue) {
        if(ctx.queryParam(queryParam) != null) {
            return ctx.queryParamAsClass(queryParam, clazz).get();
        } else {
            return defaultValue;
        }
    }
    /**
     * Configure the routes for ingredient operations.
     *
     * @param app the Javalin application
     */
    public void configureRoutes(Javalin app) {
        app.get("/ingredients", this::getIngredients);
        app.get("/ingredients/{id}", this::getIngredient);
        app.post("/ingredients", this::createIngredient);
        app.put("/ingredients/{id}", this::updateIngredient);
        app.delete("/ingredients/{id}", this::deleteIngredient);
    }
}

