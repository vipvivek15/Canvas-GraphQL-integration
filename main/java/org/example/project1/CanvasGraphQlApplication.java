package org.example.project1;

import com.fasterxml.jackson.core.JsonProcessingException; // Used for checking JSON exceptions
import com.fasterxml.jackson.databind.ObjectMapper; // Used for JSON parsing.
import org.springframework.boot.autoconfigure.SpringBootApplication; // Spring Boot's annotation to mark this class as the application's entry point.

import picocli.CommandLine; // Picocli is a framework for building command-line applications in Java.
import picocli.CommandLine.Command; // Annotation to mark a class as a command with picocli.
import picocli.CommandLine.Option; // Annotation to mark a field as a command option.
import picocli.CommandLine.Parameters; // Annotation to mark a field as command parameters.

import java.time.ZoneId; // Used for handling time zones.
import java.time.ZonedDateTime; // Used for handling dates and times with time zone information.
import java.time.format.DateTimeFormatter; // Used for formatting and parsing date-time objects.

import java.time.format.DateTimeParseException; // Used for handling date time parse exceptions
import java.util.ArrayList; // Used for creating dynamic arrays.
import java.util.List; // Interface that can be used to create dynamic arrays

/*Using Java Logging API */
import java.util.logging.Logger; // The Logger class is a part of the Java Logging API and is used to log messages for a specific system or application component.
import java.util.logging.Level; // The Level class defines a set of standard logging levels that can be used to control logging output.

//References used:
//GraphQL: https://graphql.org/
//Canvas LMS API: https://canvas.instructure.com/doc/api/
//SpringBoot: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using.spring-boot.structuring-your-code
//ObjectMapper: https://github.com/FasterXML/jackson
//Picocli: https://picocli.info/
//Logger: https://www.vogella.com/tutorials/Logging/article.html
//Logging levels: https://www.papertrail.com/solution/tips/logging-in-java-best-practices-and-tips/#:~:text=Logging%20in%20Java%20is%20facilitated,defined%20by%20the%20Java%20framework.
//Exceptions: https://docs.oracle.com/javase/8/docs/api/java/lang/Exception.html

// Logging levels available: OFF, FINE, FINER, FINEST, CONFIG, INFO, WARNING, SEVERE, ALL

@SpringBootApplication // Marks this class as a Spring Boot application.
@Command(name = "canvasgraphql", description = "Canvas GraphQL application", mixinStandardHelpOptions = true, subcommands = {
        CanvasGraphQlApplication.ListCoursesCommand.class, CanvasGraphQlApplication.ListAssignmentsCommand.class
})

//File handles all exceptions
// Declares this class as a command-line application with subcommands to list courses and assignments.
public class CanvasGraphQlApplication {
    //main logger
    private static final Logger logger = Logger.getLogger(CanvasGraphQlApplication.class.getName());
    //main endpoint for sending graphQL query
    private static final String endpoint = "https://sjsu.instructure.com/api/graphql";

    // Option to specify the Canvas API token required for authentication.
    @Option(names = {"-t", "--token"}, description = "Canvas API Token", required = true)
    private static String token;

    public static void main(String[] args) {
        // The main method that serves as the entry point of the application.
        new CommandLine(new CanvasGraphQlApplication()).execute(args);
    }
    // Subcommand to list courses.
    @Command(name = "list-courses", description = "Lists courses")
    // set class as protected to be only accessed within the current package
    protected static class ListCoursesCommand implements Runnable {
        // Implements Runnable to be executed as a command.
        @Option(names = "--active", description = "List only active courses")
        // Option to filter and list only active courses.
        private static boolean isCourseActive;
        @Option(names = "--no-active", description = "List non active courses")
        // Option to filter and list only non-active courses.
        private static boolean isCourseNonactive;

        // Variable to keep track of activeCourses
        private static boolean activeterm;
        
        // Variable to keep track of default term
        private static boolean defaultterm;

        // Variable to keep track of termName
        private static String termName;

        @Override
        // The core logic for listing courses based on the specified filters.
        public void run() {
            // ObjectMapper instance for parsing JSON responses.
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // Preparing the GraphQL query to fetch courses.
                String query = new GraphQlQueries().GetCoursesquery();
                // Sending the GraphQL query and receiving the response.
                String response = new GraphQlConnect(token, endpoint).sendCourseQuery(query);
                // Parsing the JSON response into Java objects.
                var data = objectMapper.readValue(response, GetResponses.CourseData.class);
                // Ensuring data is not null
                if(data != null) {
                    // Extracting the list of courses from the parsed data.
                    var allCourses = data.getData().getAllCourses();
                    // Ensuring allCourses is not null
                    if(allCourses != null) {
                        // Looping through each course to filter and print based on the active status.
                        allCourses.forEach(course -> {
                            // get term name and see if its null
                            if (course != null && course.getTerm() != null && course.getTerm().getName() != null) {
                                // Checking if the course term is "Spring 2024" to determine if it's active
                                activeterm = course.getTerm().getName().equals("Spring 2024");
                                // Checking if the course term is "Default"
                                defaultterm = course.getTerm().getName().equals("Default Term");
                                // Get the current term name
                                termName = course.getTerm().getName();
                                //check whether we need to print or not
                                boolean shouldPrintData = !defaultterm && (isCourseNonactive ? !activeterm : activeterm);
                                if (shouldPrintData && (course.getName() != null)) {
                                    // Filtering courses based on the active/non-active flags and printing them.
                                    System.out.println(course.getName());
                                }
                            }
                        });
                    }
                }
                // Handling exceptions that might occur during the operation.
                // Handle JSONProcessingException
            } catch (JsonProcessingException  e) {
                logger.log(Level.SEVERE, "A JSONProcessingException error occurred", e.getMessage()); // Log the error that occurred alongside the type of exception, this is severe
            }   // Handle general exception
              catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error occurred when getting courses", e.getMessage()); // Log the error that occurred alongside the type of exception, this is severe
            }

        }
    }

    // Subcommand to list assignments for a specific course.
    @Command(name = "list-assignments", description = "Lists assignments for a given course")
    // Implementation similar to ListCoursesCommand, but for listing assignments.
    // This includes parsing command-line parameters and options, querying GraphQL,
    // and filtering assignments based on their active status.
    // set class as protected to be only accessed within the current package
    protected static class ListAssignmentsCommand implements Runnable {
        // Implements Runnable to be executed as a command.

        private static String id; // Holds the ID of a course once it's determined.
        // A list to store courses that match the user input.
        private static final List<GetResponses.CourseData.Course> matchingCourses = new ArrayList<>();

        // Marks the first CLI parameter as the course name. Picocli uses this to parse command-line inputs.
        @Parameters(index = "0", description = "Course name")
        private static String courseNameEntered;

        // Defines a command-line option to filter for only active assignments.
        @Option(names = {"--active"}, description = "List only active assignments")
        private static boolean isAssignmentActive;

        // Defines a command-line option to filter for non-active assignments.
        @Option(names = {"--no-active"}, description = "List non active assignments")
        private static boolean isAssignmentNonactive;

        @Override
        // This method contains the logic executed by the command.
        public void run() {
            // ObjectMapper is used for parsing JSON data.
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // Prepares the GraphQL query to fetch courses.
                String Coursesquery = new GraphQlQueries().GetCoursesquery();
                // Executes the GraphQL query using a custom client and stores the response.
                String init_response = new GraphQlConnect(token, endpoint).sendCourseQuery(Coursesquery);
                // Parses the JSON response into Java objects.
                var data = objectMapper.readValue(init_response, GetResponses.CourseData.class);
                //Ensuring data is not null
                if(data != null) {
                    // Extracts the courses from the response data.
                    var allCourses = data.getData().getAllCourses();
                    //Ensuring all courses is not null
                    if (allCourses != null) {
                        // Filtering logic to find courses that match the user's input.
                        allCourses.forEach(course -> {
                            if (course != null && course.getName() != null && course.getName().toLowerCase().contains(courseNameEntered.toLowerCase())) {
                                matchingCourses.add(course);
                            }
                        });
                        // Handle multiple matches for course names.
                        if (matchingCourses.size() > 1) {
                            System.out.println("Matches are not unique");
                            matchingCourses.forEach(course -> System.out.println(course.getName()));
                            return;
                        }
                        // Handle no matches for course names.
                        if (matchingCourses.isEmpty()) {
                            System.out.println("Course could not be found with the course substring entered.");
                            return;
                        }
                        // Get the id from matchingCourses List
                        id = matchingCourses.get(0).getId();
                        // Handle no course ID found
                        if (id == null) {
                            System.out.println("No course ID found.");
                            return;
                        }
                    }
                }
                // Process the matching course to list its assignments.
                String Assignmentsquery = new GraphQlQueries().GetAssignmentsquery(id);
                String final_response = new GraphQlConnect(token, endpoint).sendAssignmentQuery(Assignmentsquery);

                // Date-time handling to filter assignments based on their due date. Consider UTC format
                ZonedDateTime nowUTC = ZonedDateTime.now(ZoneId.of("UTC"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

                //Root to get objectMapper head
                var root = objectMapper.readValue(final_response, GetResponses.Root.class);
                //Ensure root is not null
                if(root != null) {
                    //Nodes to get from root
                    if (root.getData() != null && root.getData().getCourse() != null && root.getData().getCourse().getAssignmentsConnection() != null && root.getData().getCourse().getAssignmentsConnection().getNodes() != null) {
                        //Ensure nodes is not null
                        var nodes = root.getData().getCourse().getAssignmentsConnection().getNodes();
                        // Filtering and printing assignments based on their active status.
                        nodes.stream().
                                filter(node -> {
                                    // check if node is null or node due date is null
                                    if (node == null || node.getDueAt() == null || node.getAssignmentName() == null) {
                                        return false;
                                    }
                                    // try catch for date-time parse exceptions
                                    ZonedDateTime dueDateUTC;
                                    try {
                                        // get the due date in UTC format and check appropriate format
                                        dueDateUTC = ZonedDateTime.parse(node.getDueAt(), formatter.withZone(ZoneId.of("UTC")));
                                    } catch (DateTimeParseException e) {
                                        logger.log(Level.WARNING, "Failed to parse date: " + node.getDueAt(), e.getMessage()); // warning level set as warning for error
                                        return false;
                                    }
                                    // if Assignment is Non-Active, due date is before UTC, else due date is after UTC
                                    return isAssignmentNonactive ? dueDateUTC.isBefore(nowUTC) : dueDateUTC.isEqual(nowUTC) || dueDateUTC.isAfter(nowUTC);
                                })
                                .forEach(node ->
                                {
                                    // print the assignment name
                                    System.out.println(node.getAssignmentName() + " due at " + node.getDueAt());
                                });
                    }
                }
            // Handling exceptions that might occur during the operation.
            // Handle JSONProcessingException
            } catch (JsonProcessingException  e) {
                logger.log(Level.SEVERE, "A JSONProcessingException error occurred", e.getMessage()); // Log the error that occurred alongside the type of exception, this is severe
            }   // Handle general exception
            catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error occurred when geting assignments", e.getMessage()); // Log the error that occurred alongside the type of exception, this is severe
            }
        }
    }
}
