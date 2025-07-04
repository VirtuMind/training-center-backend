# Spring Boot API Development Instructions for GitHub Copilot Agent

## Project Overview

**Application Type:** Training Center Management System API
**Architecture:** Layered architecture (Controller → Service → Repository → Entity) with the use of DTOs and necessary config files
**Database:** MySQL
**Security:** Spring Security with JWT authentication
**General description:** This API will manage users, courses, modules, lessons, enrollments, quizzes, and results for a training center. It will support role-based access control and provide endpoints for both public and authenticated users.

## Core Requirements

### Authentication & Authorization

- **Roles:** ADMIN, TRAINER, STUDENT (enum-based)
- **JWT Token:** Access tokens (15 min) + Refresh tokens (7 days)
- **Password:** BCrypt encoding, minimum 8 characters
- **Session Management:** Stateless authentication
- **CORS:** Configure for frontend integration

### Database Schema

```sql
-- Core entities with relationships
Users (id, username, email, password, role(enum/str), fullname, createdAt, deleted)
Courses (id, title, description, level(enum))(beginner, intermediate, advanced)), duration(str), coverimg, categoryId, trainerId, createdAt, deleted)
Modules (id, title, courseId, orderIndex)
Lessons (id, title, moduleId, duration, video, orderIndex)
Enrollments (id, studentId, courseId, enrollmentDate, status)
Reviews (id, courseId, studentId, rating, comment, createdAt)
CompletedModules(id, studentId, lessonId, createdAt)
Questions (id, courseId, question)
Answers(id, questionId, answer, correct)
Results (id, studentId, courseId, score, completedAt)
Categories (id, name, description)
```

### Entity Layer Development

```
Generate JPA entities for a training center management system with these requirements:
- Users entity with role-based enum (ADMIN, TRAINER, STUDENT)
- Courses entity with trainer relationship, category linkage, and soft delete capability and cover image
- Modules entity with course relationship and ordering
- Lessons entity with module relationship and ordering and video content
- Reviews entity for course feedback with rating and comment
- Properly handle file uploads for course cover images and lesson videos, accept them as Multipart files, save them to a designated directory, and store the file paths in the database, use UUID for unique identifiers
- Enrollments entity with student-course relationship and progress tracking
- CompletedModules entity for tracking completed modules by students
- Questions entity for course quizzes with multiple-choice answers
- Answers entity for question options with correct answer flag
- Results entity for storing student scores and completion status
- Use Lombok for boilerplate code reduction
- Category entity for course categorization
- Include proper JPA annotations, validation constraints, and bidirectional relationships
- Add audit fields (createdAt, updatedAt) using @CreationTimestamp and @UpdateTimestamp
- Implement equals() and hashCode() methods properly
```

**Expected Output:**

- Complete entity classes with proper annotations
- Enum classes for roles and level types
- Audit fields configuration
- Proper relationship mappings

### Phase 3: Repository Layer Development

```
Create Spring Data JPA repositories for all entities with custom query methods (these methods should be tailored to the specific needs of the application):
- UserRepository: findAll
- CourseRepository: findByTrainerId, findByCategoryId, findAll,
- ModuleRepository: findByCourseIdOrderByOrderIndex, countByCourseId
- EnrollmentRepository: findByStudentIdAndCourseId, findByStudentId, findByCourseId, countByStudentIdAndCourseId
- QuizRepository: findQuestionsByCourseId, findAnswersByQuestionId
- ResultRepository: findByStudentIdAndCourseId, findByCourseId
- CompletedModuleRepository: findByStudentIdAndCourseId, findByCourseId, deleteByStudentIdAndLessonId
- CategoryRepository: findByName, findAll
- QuestionRepository: findByCourseId, findById
- AnswerRepository: findByQuestionId, findById
- ReviewRepository: findByCourseId, findByStudentId
- ReviewRepository: findByCourseId, findByStudentId
- Include native queries for complex analytics and reporting
- Add custom repository implementations for complex business logic
- Use @Query annotations with proper JPQL and native SQL
- Add any other necessary methods for data retrieval and manipulation
```

**Expected Output:**

- Repository interfaces extending JpaRepository
- Custom query methods with proper annotations
- Complex queries for analytics and reporting
- Custom repository implementations where needed

### Phase 4: Service Layer Development

```
Implement comprehensive service layer with business logic:
- UserService: user registration, authentication, role management, profile updates
- CourseService: CRUD operations, trainer-specific courses, enrollment management, progress calculation
- ModuleService: CRUD with ordering, content management, completion tracking
- EnrollmentService: enrollment process, progress tracking, completion logic
- AssessmentService: quiz management, question handling, time tracking
- ResultService: score calculation, answer validation, progress updates
- ReviewService: course feedback management, rating calculations
- StatisticsService: for student: courses enrolled, hours completed, average scores, average progress;
   for trainer: total courses created, total students enrolled, average ratings, average completion rates;
   for admin: total users, total courses, total students
- AuthenticationService: JWT token generation, refresh token logic, logout functionality
- Include proper error handling with custom exceptions
- Implement validation logic and business rules
- Add transaction management with @Transactional
- Use DTOs for data transfer and validation
- Implement pagination and sorting for list operations
```

**Expected Output:**

- Service classes with comprehensive business logic
- Custom exception classes
- DTO classes for request/response
- Proper transaction management
- Validation and error handling

### Phase 5: Controller Layer Development

**Prompt:**

```
Create REST controllers with comprehensive API endpoints:
- AuthController: /api/auth/* endpoints for login, register, refresh token, logout
- UserController: /api/users/* for user management (admin only)
- CourseController: /api/courses/* for course CRUD and public course listing
- ModuleController: /api/modules/* for module management within courses
- EnrollmentController: /api/enrollments/* for student enrollment and progress
- AssessmentController: /api/assessments/* for quiz management and taking
- ResultController: /api/results/* for score viewing and analytics
- CategoryController: /api/categories/* for course categorization
- Include proper HTTP status codes and response handling
- Implement role-based access control with @PreAuthorize
- Add comprehensive input validation and error responses
- Use ResponseEntity for proper HTTP responses
- Add any other necessary endpoints for business requirements
```

**Expected Output:**

- REST controllers with proper endpoints
- Role-based security annotations
- Comprehensive error handling
- Proper HTTP response handling

### Phase 6: Security Configuration

**Prompt:**

```
Implement Spring Security configuration with JWT authentication:
- JWT authentication filter for token validation
- JWT utility class for token generation, validation, and extraction
- Security configuration with role-based access control
- Password encoder configuration with BCrypt
- CORS configuration for frontend integration
- Security rules: /api/auth/** permitAll, /api/courses GET permitAll, all other endpoints authenticated
- Role-based access: ADMIN can access all endpoints, TRAINER can manage own courses, STUDENT can view and enroll
- JWT token expiration handling and refresh token mechanism
- Security headers configuration
- Exception handling for authentication and authorization failures
```

**Expected Output:**

- Security configuration class
- JWT utility class
- Authentication filter
- Security exception handlers
- CORS configuration

### Phase 7: Exception Handling & Validation

**Prompt:**

```
Implement comprehensive error handling and validation:
- Global exception handler with @ControllerAdvice
- Custom exception classes: UserNotFoundException, CourseNotFoundException, EnrollmentException, etc.
- Validation groups for different scenarios (creation, update)
- Custom validators for business rules
- Error response DTOs with proper error codes and messages
- Logging configuration for error tracking
- HTTP status code mapping for different exception types
- Validation error handling with field-specific messages
```

**Expected Output:**

- Global exception handler
- Custom exception classes
- Validation annotations and groups
- Error response DTOs
- Proper logging configuration

### API Response Format

```json
{
  "success": true,
  "data": {},
  "message": "Operation successful",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### Error Response Format

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": []
  },
  "timestamp": "2024-01-01T10:00:00Z"
}
```
