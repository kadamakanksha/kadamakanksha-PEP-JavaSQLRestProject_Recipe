package com.revature.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;

import com.revature.model.Chef;
import com.revature.service.AuthenticationService;
import com.revature.service.ChefService;



/**
 * The AuthenticationController class handles user authentication-related operations. This includes login, logout, and registration.
 * 
 * It interacts with the ChefService and AuthenticationService for certain functionalities related to the user.
 */
public class AuthenticationController {

    /** A service that handles chef-related operations. */
    @SuppressWarnings("unused")
    private ChefService chefService;

    /** A service that handles authentication-related operations. */
    @SuppressWarnings("unused")
    private AuthenticationService authService;

    /**
     * Constructs an AuthenticationController with its parameters.
     * 
     * TODO: Finish the implementation so that this class's instance variables are initialized accordingly.
     * 
     * @param chefService the service used to manage chef-related operations
     * @param authService the service used to manage authentication-related operations
     */
    public AuthenticationController(ChefService chefService, AuthenticationService authService) {
        this.authService = authService;
        this.chefService = chefService;
    }

    /**
     * TODO: Registers a new chef in the system.
     * 
     * If the username already exists, responds with a 409 Conflict status and a result of "Username already exists".
     * 
     * Otherwise, registers the chef and responds with a 201 Created status and the registered chef details.
     *
     * @param ctx the Javalin context containing the chef information in the request body
     */
    public void register(Context ctx) {
        try {
            Chef newChef = ctx.bodyAsClass(Chef.class);
            Chef registeredChef = authService.registerChef(newChef);

            if (registeredChef != null) {
                ctx.status(201).json(registeredChef);
            } else {
                ctx.status(409).result("Username already exists");
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).result("Invalid input: " + e.getMessage());
        }
    }

    /**
     * TODO: Authenticates a chef and uses a generated authorization token if the credentials are valid. The token is used to check if login is successful. If so, this method responds with a 200 OK status, the token in the response body, and an "Authorization" header that sends the token in the response.
     * 
     * If login fails, responds with a 401 Unauthorized status and an error message of "Invalid username or password".
     *
     * @param ctx the Javalin context containing the chef login credentials in the request body
     */
    public void login(Context ctx) {
        Chef loginDetails = ctx.bodyAsClass(Chef.class);
        String token = authService.login(loginDetails);

        if (token != null) {
            ctx.status(200)
                .header("Authorization", "Bearer " + token)
                .result(token);
        } else {
            ctx.status(401).result("Invalid username or password");
        }
    }

    /**
     * TODO: Logs out the currently authenticated chef by invalidating their token. Responds with a 200 OK status and a result of "Logout successful".
     *
     * @param ctx the Javalin context, containing the Authorization token in the request header
     */
    public void logout(Context ctx) {
        String authHeader = ctx.header("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (authService.getChefFromSessionToken(token) != null) {
                authService.logout(token);
                ctx.status(200).result("Logged out successfully");
            } else {
                ctx.status(401).result("Unauthorized");
            }
        } else {
            ctx.status(401).result("Unauthorized");
        }
    }

    /**
     * Configures the routes for authentication operations.
     * 
     * Sets up routes for registration, login, and logout.
     *
     * @param app the Javalin application to which routes are added
     */
    public void configureRoutes(Javalin app) {
        app.post("/register", this::register);
        app.post("/login", this::login);
        app.post("/logout", this::logout);
    }

}
