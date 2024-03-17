# GraphQL-Canvas-Integration
 This program allows users to fetch active and non-active courses and assignments from Canvas. This is done through the graphqlendpoint. 
 This should get the active courses for the current semester. 

To compile for courses:

java -jar your_jar_file.jar list-courses --active

java -jar your_jar_file.jar list-courses --no-active

To compile for assignments:

java -jar your_jar_file.jar list-assignments --active

java -jar your_jar_file.jar list-assignments --no-active
