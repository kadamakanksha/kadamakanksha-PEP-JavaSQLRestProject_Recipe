package com.revature;

import com.revature.controller.AuthenticationController;
import com.revature.controller.IngredientController;
import com.revature.controller.RecipeController;
import com.revature.dao.ChefDAO;
import com.revature.dao.IngredientDAO;
import com.revature.dao.RecipeDAO;
import com.revature.service.AuthenticationService;
import com.revature.service.ChefService;
import com.revature.service.IngredientService;
import com.revature.service.RecipeService;
import com.revature.util.AdminMiddleware;
import com.revature.util.ConnectionUtil;
import com.revature.util.JavalinAppUtil;
import com.revature.util.DBUtil;

import io.javalin.Javalin;

/**
 * The Main class serves as the entry point for the application.
 * It initializes and manages various components related to recipes, chefs, 
 * ingredients, and authentication services. This class contains static 
 * references to the utility classes and controllers required for the 
 * application to function correctly.
 */

public class Main {

    /** A utility class used for establishing connections to the database. */
    @SuppressWarnings("unused")
    private static ConnectionUtil CONNECTION_UTIL = new ConnectionUtil();

    /** Instance of JavalinAppUtil for application utility functions. */
    @SuppressWarnings("unused")
    private static JavalinAppUtil JAVALIN_APP_UTIL;

    /** Controller for managing recipe-related requests. */
    @SuppressWarnings("unused")    
    private static RecipeController RECIPE_CONTROLLER;

    /** Service class for handling recipe business logic. */
    @SuppressWarnings("unused")    
    private static RecipeService RECIPE_SERVICE;

    /** Data Access Object for interacting with recipe data storage. */
    @SuppressWarnings("unused")    
    private static RecipeDAO RECIPE_DAO;

    /** Data Access Object for interacting with chef data storage. */
    @SuppressWarnings("unused")    
    private static ChefDAO CHEF_DAO;

    /** Service class for handling chef-related business logic. */
    @SuppressWarnings("unused")    
    private static ChefService CHEF_SERVICE;

    /** Service class for managing user authentication. */
    @SuppressWarnings("unused")    
    private static AuthenticationService AUTH_SERVICE;

    /** Controller for managing authentication-related requests. */
    @SuppressWarnings("unused")    
    private static AuthenticationController AUTH_CONTROLLER;

    /** Data Access Object for interacting with ingredient data storage. */
    @SuppressWarnings("unused")    
    private static IngredientDAO INGREDIENT_DAO;

    /** Service class for handling ingredient-related business logic. */
    @SuppressWarnings("unused")    
    private static IngredientService INGREDIENT_SERVICE;

    /** Controller for managing ingredient-related requests. */
    @SuppressWarnings("unused")    
    private static IngredientController INGREDIENT_CONTROLLER;

    /** Middleware for administering administrative functionalities. */
    @SuppressWarnings("unused")    
    private static AdminMiddleware ADMIN_MIDDLEWARE;

    /**
     * The main method serves as the entry point for the application.
     * It initializes the application context and starts the server.
     *
     * @param args Command line arguments passed during application startup.
     */
    public static void main(String[] args) {

    INGREDIENT_DAO = new IngredientDAO(CONNECTION_UTIL);
		
		CHEF_DAO = new ChefDAO(CONNECTION_UTIL);
		
		RECIPE_DAO = new RecipeDAO(CHEF_DAO, INGREDIENT_DAO, CONNECTION_UTIL);
		
		CHEF_SERVICE = new ChefService(CHEF_DAO);
		
		AUTH_SERVICE = new AuthenticationService(CHEF_SERVICE);
		
		RECIPE_SERVICE = new RecipeService(RECIPE_DAO);
		
		RECIPE_CONTROLLER = new RecipeController(RECIPE_SERVICE, AUTH_SERVICE);
		
		INGREDIENT_SERVICE = new IngredientService(INGREDIENT_DAO);
		
		INGREDIENT_CONTROLLER = new IngredientController(INGREDIENT_SERVICE);
		
		AUTH_CONTROLLER = new AuthenticationController(CHEF_SERVICE, AUTH_SERVICE);
		
		JAVALIN_APP_UTIL = new JavalinAppUtil(RECIPE_CONTROLLER, AUTH_CONTROLLER, INGREDIENT_CONTROLLER);
		
		DBUtil.RUN_SQL();
		
		Javalin app = JAVALIN_APP_UTIL.getApp();
		
		app.start(8081);
    }
}

