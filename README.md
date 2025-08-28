# Xplore - A Full-Stack Social Media Platform

This project is a comprehensive social media application I built to apply the concepts and skills learned during Cognizant's Digital Nurture 4.0 Deep Skilling Program for Java Full Stack Engineering. It serves as a practical demonstration of my ability to design, develop, and deploy a robust, scalable, and feature-rich web application using a modern Java technology stack.

## ✨ Features

-   **User Authentication & Security:** Secure user registration with OTP email verification and login functionality using JWT (JSON Web Tokens) for stateless authentication.

-   **User Profiles:** Allows users to create and manage their profiles, including updating their full name, bio, and profile picture.

-   **Social Graph:** Users can follow and unfollow others to build their social network and view follower/following lists.

-   **Content Creation:**
    -   **Posts:** Users can create posts with text content and upload multiple images. They can also update and delete their own posts.
    -   **Comments:** Users can add comments to posts, creating threaded discussions.

-   **User Engagement:**
    -   **Likes:** Users can like and unlike posts.
    -   **Sharing:** A share count is tracked for each post.

-   **Real-time Notifications:** Users receive notifications for key events such as new followers, post likes, and comments on their posts, with a real-time unread notification count.

-   **Content Discovery:**
    -   **Personalized Feed:** A main feed that displays posts from users they follow.
    -   **User Suggestions:** A "who to follow" feature to help users discover new connections.

## 🛠️ Technologies Used

This project is built with a modern Java stack, incorporating best practices and industry-standard tools as covered in the Digital Nurture 4.0 program.

### Backend
-   **Framework:** Spring Boot 3
-   **Security:** Spring Security (JWT-based authentication)
-   **Data Persistence:** Spring Data JPA with Hibernate as the ORM
-   **API:** Spring Web for creating RESTful services
-   **Database:** Configured to support MySQL, PostgreSQL, and SQL Server (with H2 for testing)
-   **Build & Dependency Management:** Maven
-   **Image Storage:** Cloudinary for cloud-based image hosting and management
-   **Email Service:** Spring Mail for sending OTP emails (integrated with Brevo/Sendinblue)
-   **Utilities:** Lombok to reduce boilerplate code, SLF4J for logging
-   **Containerization:** Docker

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites
-   Java 21
-   Apache Maven
-   A relational database (e.g., MySQL, PostgreSQL)
-   A Cloudinary account for image storage
-   An SMTP server for sending emails

### Installation

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/sudipsarkar1193/springboot-social-media.git](https://github.com/sudipsarkar1193/springboot-social-media.git)
    ```

2.  **Configure Environment Variables**

    Create a `.env` file in the root directory and add the necessary configuration:

    ```ini
    # Database Configuration (example for MySQL)
    DATA_SRC_URL=jdbc:mysql://localhost:3306/xplore_db
    DB_USERNAME=your_db_user
    DB_PASSWORD=your_db_password

    # JWT Secret Key
    JWT_SECRET=your-very-strong-and-long-jwt-secret-key

    # Cloudinary Credentials
    CLOUDINARY_CLOUD_NAME=your_cloud_name
    CLOUDINARY_API_KEY=your_api_key
    CLOUDINARY_API_SECRET=your_api_secret

    # SMTP Credentials (for email service)
    SMTP_USERNAME=your_smtp_username
    SMTP_PASSWORD=your_smtp_password
    ```

3.  **Run the application**
    ```bash
    mvn spring-boot:run
    ```
    The API will be available at `http://localhost:8081`.

## 🎓 Showcasing My Learning from Digital Nurture 4.0

This project is a direct result of the intensive training from the DN 4.0 program. Here’s how I've applied the key modules:

-   **Design Patterns and Principles (Module 1):** I've applied SOLID principles throughout the application. The separation of concerns between controllers, services, and repositories is a clear example of the Single Responsibility Principle.

-   **Spring Core and Maven (Module 5):** The entire project is built on the Spring Framework and managed with Maven. I've utilized core concepts like Dependency Injection (DI) and Inversion of Control (IoC) to build a loosely coupled and maintainable application.

-   **Spring Data JPA & Hibernate (Module 6):** The persistence layer is built using Spring Data JPA repositories, which significantly simplifies database interactions. Hibernate is used as the JPA provider for object-relational mapping.

-   **Spring REST using Spring Boot 3 (Module 7):** I built a complete RESTful API using `@RestController`. The controllers (PostController, UserController, etc.) handle HTTP requests with mappings like `@GetMapping` and `@PostMapping` to expose the application's features. I also created a GlobalExceptionHandler to manage API error responses and return appropriate HTTP status codes.

-   **Spring Security:** I secured the API using Spring Security. This includes a SecurityConfig class that defines which endpoints are public (/api/auth/**) and which are protected. I implemented stateless authentication using JSON Web Tokens (JWT), with JwtUtils for token generation/validation and a JwtAuthFilter to process tokens for each request.

-   **Logging Framework (Module 4):** I integrated `SLF4J` for logging throughout the backend, using the `@Slf4j` annotation in my service and controller classes to record events and errors. I also configured logging levels in the application.properties file.

-   **Version Control (Module 11):** The project is version-controlled with Git, and I've used standard practices for commits and branching.

-   **DevOps & Containerization (Modules 12 & 14):** The inclusion of a Dockerfile demonstrates my ability to containerize the application, a fundamental step in modern DevOps practices for creating consistent and portable deployment environments.

-   **Cloud Fundamentals (Module 13):** I demonstrated my understanding of cloud integration by using `Cloudinary` for image storage. Additionally, I configured the application to use `Microsoft Azure SQL DB` for data persistence, showcasing the practical use of a cloud-based Platform as a Service (PaaS) as covered in the curriculum.


## ⚙️ Key Implementation Details

Here's a closer look at some of the core components I built for this application :

### Global Exception Handling

To ensure the API provides consistent and clean error responses, I implemented a centralized exception handler using the `@ControllerAdvice` annotation. This intercepts exceptions from any controller and formats them into a standardized JSON response.

-   **Validation Errors (`@Valid`):** When input data in a request body fails validation (e.g., a blank password), the handler catches the `MethodArgumentNotValidException` and returns a `400 Bad Request` response with details about which fields are invalid.
-   **Duplicate Data Conflicts:** If a user tries to register with an email that already exists, the `AuthService` throws an `IllegalStateException`. The handler catches this and returns a `409 Conflict` status, preventing duplicate accounts.
-   **Authentication Failures:** For incorrect login attempts, Spring Security throws a `BadCredentialsException`. My handler catches this and returns a `401 Unauthorized` response with a generic "Invalid credentials" message to protect against user enumeration attacks.

### JWT-Based Authentication Flow

The application uses a stateless, token-based authentication system with JSON Web Tokens (JWT), which is a modern standard for securing REST APIs. The flow is managed by Spring Security.

**1. Login & Token Generation**
   - A user sends their credentials to the `/api/auth/login` endpoint.
   - The `AuthenticationManager` uses my custom `UserDetailsServiceImpl` to fetch the user from the database and verify the password.
   - If successful, the `JwtUtils` class generates a signed JWT containing the user's username and an expiration date.
   - This JWT is returned to the client, who must include it in the `Authorization` header for all future requests to protected endpoints.

**2. API Request Authentication**
   - For any request to a protected endpoint (e.g., creating a post), the client sends the JWT in the `Authorization: Bearer <token>` header.
   - My custom `JwtAuthFilter` intercepts the request before it reaches the controller.
   - The filter uses `JwtUtils` to validate the token's signature and expiration date.
   - If the token is valid, the filter sets the user's authentication details in Spring Security's `SecurityContext`. This authenticates the user for the duration of the request.
   - If the token is invalid or missing, access is denied, and my custom `AuthEntryPoint` returns a `401 Unauthorized` JSON response.

---

## 🗄️ Database Schema and Entity Relationships

The application's functionality is built upon a relational database schema defined using JPA (Java Persistence API) entities. The core of the application revolves around the `User`, who creates `Post` entities and interacts with them through `Like`, `Follow`, and `Notification` entities.


### User and UserProfile (One-to-One)

-   The **`User`** entity is the central model, containing essential authentication details like username, email, and password.
-   Each `User` has a **`UserProfile`** which stores additional, non-essential information like bio, full name, and profile picture URL.
-   This is a bi-directional **`@OneToOne`** relationship. The `UserProfile` entity owns the relationship by using `@MapsId`, which means its primary key is also a foreign key to the `User`'s primary key. This design separates authentication concerns from profile data.

### User and Post (One-to-Many)

-   A single **`User`** (the author) can create multiple **`Post`** entities.
-   Each `Post` belongs to exactly one `User`. This is a standard **`@OneToMany`** relationship from `User` to `Post` and a **`@ManyToOne`** relationship from `Post` back to `User`.


### Post Self-Relationship (for Comments)

-   The **`Post`** entity has a self-referencing relationship to model comments. A `Post` can be a parent to many other `Post` entities (which are comments).
-   A comment `Post` has a **`@ManyToOne`** link to its `parentPost`. This allows for a nested, threaded comment structure.
-   Cascade settings (`CascadeType.ALL`, `orphanRemoval = true`) ensure that when a post is deleted, all its comments are automatically deleted as well.


### Users and Posts through Likes (Many-to-Many)

-   A **`User`** can like many **`Post`** entities, and a `Post` can be liked by many `User` entities. This many-to-many relationship is implemented through a join table/entity called **`Like`**.
-   The `Like` entity links a `User` and a `Post` together via two **`@ManyToOne`** relationships. A unique constraint on the user and post IDs ensures a user can only like a post once.


### Users and Followers (Many-to-Many)

-   Similar to likes, the "follow" feature is a many-to-many relationship between **`User`** entities. A user can follow many other users and can also be followed by many users.
-   This is implemented with a join entity called **`Follow`**, which contains a `follower` (`User`) and a `followee` (`User`).


### Notifications and Users (Many-to-One)

-   The **`Notification`** entity is used to inform users about events.
-   Each notification has a **`@ManyToOne`** relationship with the `recipient` (User who receives it) and the `sender` (User who triggered it).
-   It also stores the `NotificationType` (e.g., `NEW_FOLLOW`, `POST_LIKE`) and the `UUID` of the related entity (e.g., the post that was liked), making notifications flexible and informative.