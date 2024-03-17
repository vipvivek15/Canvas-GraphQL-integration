package org.example.project1;

//Reference used:
//https://graphql.org/learn/queries/

//class for graphQL queries
public class GraphQlQueries {

    // Query to fetch courses with information including name, id and term name
    public String GetCoursesquery() {
        return "query MyQuery {\n" +
                "  allCourses {\n" +
                "    name\n" +
                "    id\n" +
                "    term {\n" +
                "      name\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
    }

    // Query to fetch assignments with information about assignment name and due data
    // Use string format to pass in arguments
    public String GetAssignmentsquery(final String courseId) {
        return String.format("{\"query\":\"query myquery { course(id: \\\"%s\\\") { assignmentsConnection { nodes { dueAt name } } } }\"}", courseId);

    }
}
