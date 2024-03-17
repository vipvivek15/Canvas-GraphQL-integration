package org.example.project1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; //This annotation is used at the class level to indicate that any properties not bound in the JSON to this class should be ignored.
import com.fasterxml.jackson.annotation.JsonProperty; //This annotation is used to specify the name of a property in JSON and bind it to a Java field or method.

import java.util.List; // Interface that can be used to create dynamic arrays

//References used:
//Jackson Annotations GitHub: https://github.com/FasterXML/jackson-annotations
//Baeldung Jackson Tutorial: https://www.baeldung.com/jackson
//Nested Classes Tutorial by Oracle: https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html
//Annotations in Java: https://docs.oracle.com/javase/tutorial/java/annotations/
//RESTful API Design — Best Practices: https://restfulapi.net/rest-api-design-tutorial-with-example/
//Building a RESTful Web Service with Spring: https://spring.io/guides/gs/rest-service/

 //The GetResponses class contains nested static classes for deserialization of JSON responses.
 //It is designed to parse the structure of JSON data returned from an API,
 //such as course and assignment information.
public class GetResponses {

    //Represents the data structure for course data JSON response.
    public static class CourseData {
        // Maps the "data" JSON object to the Data class.
        @JsonProperty("data")
        private Data data;

        public Data getData() {
            return data;
        }


         // Encapsulates the list of courses.
        public static class Data {
            // Maps the "allCourses" JSON array to a List of Course objects.
            @JsonProperty("allCourses")
            private List<Course> allCourses;

            public List<Course> getAllCourses() {
                return allCourses;
            }
        }


         // Represents a single course, ignoring unknown JSON properties to prevent parsing errors.
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Course {
            private String name;
            private Term term;
            private String id;

            public String getName() {
                return name;
            }

            public Term getTerm() {
                return term;
            }

            public String getId() {
                return id;
            }


            // Represents the term (e.g., semester) information of a course.
            public static class Term {
                private String name;

                public String getName() {
                    return name;
                }
            }
        }
    }

     // Root class to encapsulate the top-level data structure for assignment data JSON response.
    public static class Root {
        private Data data;

        public Data getData() {
            return data;
        }

        public static class Data {
            private Course course;

            public Course getCourse() {
                return course;
            }

             // Encapsulates the connection to assignments, containing a list of nodes (assignments).
            public static class Course {
                @JsonProperty("assignmentsConnection")
                private AssignmentsConnection assignmentsConnection;

                public AssignmentsConnection getAssignmentsConnection() {
                    return assignmentsConnection;
                }


                // Represents the collection of assignment nodes.
                public static class AssignmentsConnection {
                    // Maps the "nodes" JSON array to a List of Node objects.
                    @JsonProperty("nodes")
                    private List<Node> nodes;

                    public List<Node> getNodes() {
                        return nodes;
                    }

                    //Represents an individual assignment node with due date and name.
                    public static class Node {
                        @JsonProperty("dueAt")
                        private String dueAt;
                        @JsonProperty("name")
                        private String name;

                        public String getDueAt() {
                            return dueAt;
                        }

                        public String getAssignmentName() {
                            return name;
                        }
                    }
                }
            }
        }
    }
}