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

This section describes the endpoints provided by the Verification service, along with their inputs and outputs.

---

### GET `/`, `/index`, `/home`
- **Description:** Provides a welcome message for the Verification API.
- **Returns:** A JSON object with a welcome message and relevant links for the service.

---

### POST `/generateCodeAndSave`
- **Description:** Generates a verification code and saves it to the database.
- **Request Parameters:**
  - `email` (String): The email address to associate with the generated code.
- **Returns:** A `ResponseEntity` containing a success or error message.

---

### POST `/verify`
- **Description:** Verifies the user's code sent to their email.
- **Request Parameters:**
  - `email` (String): The email address associated with the verification code.
  - `code` (String): The verification code to validate.
- **Returns:** A `ResponseEntity` containing a success or error message.

---

### GET `/auth/callback`
- **Description:** Handles OAuth callback to retrieve user information from Google.
- **Request Parameters:**
  - `code` (String): The authorization code provided by Google.
- **Returns:** A `ResponseEntity` containing user information or an error message.

---

### POST `/register`
- **Description:** Triggers the registration process for a user by sending a verification email.
- **Request Parameters:**
  - `email` (String): The email address of the user to register.
- **Returns:** A `ResponseEntity` containing a callback URL for checking the registration status.

---

### GET `/register/status`
- **Description:** Retrieves the status of a registration request.
- **Request Parameters:**
  - `executionId` (String): The unique identifier for the registration process.
- **Returns:** A `ResponseEntity` containing the registration status or an error message.

---

### GET `/exchangeGoogleToken`
- **Description:** Exchanges a Google token for a JWT token after validation.
- **Request Body:**
  - `email` (String): The email address of the user.
  - `token` (String): The Google token to validate.
- **Returns:** A `ResponseEntity` containing a JWT token or an error message.


## Local Variables

Certain local variables need to be configured before the service can execute properly. 

1. SPRING_MAIL_USERNAME -- the email address from which you would like to send the verification codes.
2. SPRING_MAIL_PASSWORD -- the *app* password for the email address (not the regular password).
3. DB_URL -- the url of the cloud database used to store the codes.
4. DB_USERNAME -- the username of the user account used to access the cloud database.
5. DB_PASSWORD -- the password of the user account used to access the cloud database.
