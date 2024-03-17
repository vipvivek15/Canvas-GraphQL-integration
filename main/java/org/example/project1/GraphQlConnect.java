package org.example.project1;

import java.io.IOException; // Imports the IOException class, which is thrown when an I/O operation fails or is interrupted.

import java.net.URI; // Imports the URI class, which represents a Uniform Resource Identifier, a string of characters used to identify a name or a resource on the Internet.
import java.net.http.HttpClient; // Imports the HttpClient class, which provides a means to send HTTP requests and receive HTTP responses. HttpClient is part of the HTTP Client API introduced in Java 11.
import java.net.http.HttpRequest; // Imports the HttpRequest class, which represents an HTTP request. It is used to build requests that can be sent over the network to a server.
import java.net.http.HttpResponse; // Imports the HttpResponse interface, which represents an HTTP response received from an HTTP request. HttpResponse includes methods to access the status code, headers, and body of the response.
import java.net.http.HttpResponse.BodyHandlers; // Imports the BodyHandlers class, which is a utility class providing implementations of HttpResponse.BodyHandler. BodyHandlers are used to process and convert the body of an HTTP response.

/*Using Java Logging API */
import java.util.logging.Logger; // The Logger class is a part of the Java Logging API and is used to log messages for a specific system or application component.
import java.util.logging.Level; // The Level class defines a set of standard logging levels that can be used to control logging output.

// References used:
// Java HTTP Client: https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html
// Handling HTTP Requests and Responses in Java: https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpRequest.html
// Java URI class: https://docs.oracle.com/javase/7/docs/api/java/net/URI.html
// Logger: https://www.vogella.com/tutorials/Logging/article.html
// Logging levels: https://www.papertrail.com/solution/tips/logging-in-java-best-practices-and-tips/#:~:text=Logging%20in%20Java%20is%20facilitated,defined%20by%20the%20Java%20framework.
// Exceptions: https://docs.oracle.com/javase/8/docs/api/java/lang/Exception.html
// Status codes: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status#information_responses

// Logging levels available: OFF, FINE, FINER, FINEST, CONFIG, INFO, WARNING, SEVERE, ALL

// File handles all exceptions and status code errors
// GraphQlConnect class to interact with GraphQL API.
public class GraphQlConnect {

    //main logger
    private static final Logger logger = Logger.getLogger(GraphQlConnect.class.getName());
    //Token for API authentication
    private final String token;

    //Endpoint for sending graphQL query
    private final String endpoint;
    //Used for performing Http Requests
    private final HttpClient httpClient;

    // GraphQlConnect class constructor to initialize GraphQlConnect with an API token.
    public GraphQlConnect(final String token, final String endpoint) {
        // Handle case where token is null or empty
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("API token cannot be null or empty");
        }
        // Handle case where endpoint is null or empty
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Endpoint URL cannot be null or empty");
        }
        this.token = token;
        this.httpClient = HttpClient.newHttpClient();
        this.endpoint = endpoint;
    }
    // Sends a GraphQL query for courses to graphQL endpoint and returns the response as a String.
    public String sendCourseQuery(String graphqlQuery) {
        //Assing a try and catch in case the course request fails
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + this.token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"query\":\"" + graphqlQuery + "\"}"))
                    .build();
            return responseChecker(request);
        }
        //Handle illegal arguments being passed in
        catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Invalid URI or argument: " + endpoint, e.getMessage()); //log as severe
        }
        // Handle general exception
        catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error occurred during sendCourseQuery", e.getMessage()); //log as severe
        }
        return null;
    }

    // Sends a GraphQL query for assignments to graphQL endpoint and returns the response as a String.
    public String sendAssignmentQuery(String graphqlQuery) {
        //Adding a try and catch in case the assignment request fails
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + this.token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(graphqlQuery))
                    .build();
            return responseChecker(request);
        }
        //Handle illegal arguments being passed in
        catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Invalid URI or argument: " + endpoint, e.getMessage()); //log as severe
        }
        // Handle general exception
        catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error occurred during sendAssignmentQuery", e.getMessage()); //log as severe
        }
        return null;
    }

    // Helper method to send the HTTP request and return the response body.
    public String responseChecker(HttpRequest request) {
        // Adding a final try and catch in case the response fails
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            int statusCode = response.statusCode();
            //Handle all response status Code errors
            switch(statusCode/100) {
                case 1: // Informational response
                {
                    logger.log(Level.INFO, "Informational response with status code and response: " + statusCode + response.body()); // Logging informational responses
                    return null;
                }
                case 2: // Success
                {
                    return response.body();
                }
                case 3: // Redirection error
                {
                    logger.log(Level.INFO, "Redirection error with status code: " + statusCode); // Logging redirection info
                    return null;
                }
                case 4: // Client errors
                {
                    logger.log(Level.WARNING, "Client error with status code: " + statusCode);  // Logging or handling specific client error codes
                    return null;
                }
                case 5: // Server errors
                {
                    logger.log(Level.SEVERE, "Server error with status code: " + statusCode);  // Logging or handling specific server error codes
                    return null;
                }
                default: // Default error not handled above
                {
                    logger.log(Level.SEVERE, "Unexpected response status code: " + statusCode); // Logging unexpected status code
                    return null;
                }
            }
        }
        //For I/O errors during the HTTP request.
        catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error during HTTP communication", e.getMessage()); //log as severe
        }
        //When thread executing the request is interrupted
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, "HTTP request was interrupted", e.getMessage()); //log as severe
            Thread.currentThread().interrupt(); // Proper handling by re-interrupting the thread
        }
        //Handle general exception
        catch(Exception e) {
            logger.log(Level.SEVERE, "Unexpected error occurred during send response", e.getMessage()); //log as severe
        }
        return null;
    }
}