# BuggySoft Verification Service 

This is a user verification service for the BuggySoft Job Tracker. 

## Building and Running a Local Instance
In order to build and use our service you must install the following:

1. Maven 3.9.5: https://maven.apache.org/download.cgi Download and follow the installation instructions. Be sure to set the bin as described in Maven's README as a new path variable by editing the system variables if you are on Windows, or by following the instructions for MacOS.
2. JDK 17: This project used JDK 17 for development: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
3. IntelliJ IDE: We recommend using IntelliJ but you are free to use any other IDE that you are comfortable with: https://www.jetbrains.com/idea/download/?section=windows
4. Clone the project from GitHub and open it in IntelliJ (or whatever IDE).
5. In order to run the project, either use the IntelliJ built in run function, or go to verification/target and execute *java -jar verification-0.0.1-SNAPSHOT.jar*.

## Endpoints
This section describes the endpoints that our service provides, as well as their inputs and outputs.

#### GET /, /index, /home

* Redirects to the homepage.


---

#### POST /sendCode

* Sends a verification code to the specified email address and stores the code/email pair in a database.
*
* @param email -- A `String` representing the email.
*
* @return A `ResponseEntity` object containing either a success message or a failure and an error code.

---

#### POST /verify

* Verifies if a verification code is correct.
*
* @param email -- A `String` representing the email which received the code.
*
* @param code A `String` representing the code.
*
* @return A `ResponseEntity` object containing either a success message or a failure and an error code.

## Local Variables

Certain local variables need to be configured before the service can execute properly. 

1. SPRING_MAIL_USERNAME -- the email address from which you would like to send the verification codes.
2. SPRING_MAIL_PASSWORD -- the *app* password for the email address (not the regular password).
3. DB_URL -- the url of the cloud database used to store the codes.
4. DB_USERNAME -- the username of the user account used to access the cloud database.
5. DB_PASSWORD -- the password of the user account used to access the cloud database.