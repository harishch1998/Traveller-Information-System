# Traveller-Information-System
A distributed Systems project
					FRAMEWORKS USED

•	Spring Boot (server-side)
•	HTML (client-side) and JavaScript
•	Database used (PostgreSQL)

					HOW TO DEPLOY?

-	Open a terminal and cd into the project folder -> “traveller”
-	Type the command -> “docker-compose up” -> all containers and images would be created in this step, this should spin-up the server on localhost:8080 successfully)
-	Open the index.html file stored in the static folder “src/main/resources/static/index.html” in a web browser.
	
					CLIENT-SERVER INTERACTION

-	Two-way communication is achieved between using GET and POST HTTP requests.
-	The add a user button on the client-side is hooked with a POST API endpoint served by the Spring Boot application. Upon sending a POST request, a User Object is created with the given name and topic and stored in the PostgreSQL database.
-	The list of users and subscriber tables are automatically displayed on the client-side. These are fetched using a GET API endpoint served by the Spring Boot application. The endpoint pulls data from the PostgreSQL database.
-	The subscribe endpoint modifies user subscription with any new topic choice.
-	The unsubscribe endpoint clears a topic subscription for the user.

					ABOUT THE APPLICATION

-	This application is a representation of a pub-sub system. Loose-coupling is achieved in the application by using interfaces in the services and repositories which keep the concrete classes independent of each-other.
-	The application has been dockerized by using four docker containers; three for the Spring     Boot broker nodes and one for the PostgreSQL database. They have been linked using docker-compose and docker networking.
-	The three topics that subscribers can choose from are -> currency, vaccinations, and advise
-	The three publishers involved in the system are -> India, Egypt, and Singapore which poll data from an external API -> travelbriefing.org

					Rendezvous implementation 

-	3 Broker nodes have been dockerized which handle 1 topic each. Broker node communication has been achieved using the rendezvous method where each broker has been mapped to a unique port and respective topic requests are served by the broker nodes assigned to those topics.
-	The implementation logic for the same can be found in the sendNotifications method in the file UserDataController.java




				API’S IMPLEMENTED

-	publish(e) -> This will poll the topic data from the publisher and persist the data which we want to send to our subscriber as a message. A notification is added in the notification table immediately which will be later used (in the notify API) to send the freshly polled data to all the subscribers associated with this topic.
-	notify(topic) -> will check the notifications table for pending entries and poll data from our persistent Postgres database and by carrying out the filtering technique mentioned in Ch.6, notify all the subscribers of the topic with the updated message.
-	register (user, topic) -> Register users by passing a name as input and choose one topic for the user (Reloading the webpage should display the list of users registered).
-	subscribe (user, topic) – this endpoint adds an entry in the subscriber table which stores subscription data of all users.
-	unsubscribe (user) – this endpoint unsubscribes users from the topic they’re associated with.
-	advertise(t) – displays all the topics which could be at the user’s disposal to select from in the future.
-	deadvertise – clears the list of topics advertised

