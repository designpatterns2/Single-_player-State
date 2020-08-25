To run from the command line use:

(Windows)
java -jar -Xmx256m -Djava.library.path="lib\natives" jME_2.0.jar


To create a new project one simple needs to included jME_2.0.jar in their IDE, 
clean and build, then copy the (included) lib folder under the created project 
dist/lib folder (or where the project jar file was created).  So the actual 
command to run any project (outside of the IDE) would be:

java -jar -Xmx256m -Djava.library.path="lib/lib/natives" jarFile.jar

# Custom instructions
-Add the natives libraries to project dependencies.
-Use JDK 1.8 or earlier.
