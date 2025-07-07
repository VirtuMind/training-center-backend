package com.marketplace.trainingcenter.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.trainingcenter.dto.course.CourseMeta;
import com.marketplace.trainingcenter.dto.course.CourseRequestUpdate;
import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.dto.course.CourseRequest;
import com.marketplace.trainingcenter.dto.quiz.QuizRequest;
import com.marketplace.trainingcenter.dto.review.ReviewResponse;
import com.marketplace.trainingcenter.exception.BadRequestException;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.*;
import com.marketplace.trainingcenter.model.entity.Module;
import com.marketplace.trainingcenter.repository.*;
import com.marketplace.trainingcenter.service.CourseService;
import com.marketplace.trainingcenter.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CompletedLessonRepository completedLessonRepository;
    private final ReviewRepository reviewRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final FileUploadUtil fileUploadUtil;
    private final EnrollmentRepository enrollmentRepository;


    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id, Long studentId) {
        Course course = getCourseEntityById(id);
        return mapToCourseDetailResponse(course, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses(Long studentId) {
        List<Course> courses = courseRepository.findByDeletedFalse();
        return courses.stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseMeta> getCoursesByTrainerId(Long trainerId) {
        List<Course> courses = courseRepository.findByTrainerIdAndDeletedFalse(trainerId);
        return courses.stream()
                .map(this::mapToCourseMeta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByCategoryId(Long categoryId, Long studentId) {
        List<Course> courses = courseRepository.findByCategoryIdAndDeletedFalse(categoryId);
        return courses.stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseEntityById(id);
        course.setDeleted(true);
        courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long courseId) {
        return reviewRepository.getAverageRatingByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getEnrollmentCount(Long courseId) {
        return courseRepository.getEnrollmentCountByCourseId(courseId);
    }

    
    @Override
    @Transactional(readOnly = true)
    public void validateCourseOwnership(Long courseId, Long trainerId) {
        Course course = getCourseEntityById(courseId);
        
        if (!course.getTrainer().getId().equals(trainerId)) {
            throw new com.marketplace.trainingcenter.exception.UnauthorizedException(
                "You are not authorized to modify this course");
        }
    }

    @Override
    @Transactional
    public CourseResponse createCourseWithModulesAndQuiz(CourseRequest courseRequest, Long trainerId, Map<String, MultipartFile> videoMap) throws IOException {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", trainerId));
        
        Category category = categoryRepository.findById(courseRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", courseRequest.getCategoryId()));

        // Create course entity
        Course course = Course.builder()
                .title(courseRequest.getTitle())
                .description(courseRequest.getDescription())
                .level(courseRequest.getLevel())
                .duration(courseRequest.getDuration())
                .trainer(trainer)
                .category(category)
                .build();
        
        // Handle cover image
        if (courseRequest.getCoverImage() != null && !courseRequest.getCoverImage().isEmpty()) {
            try {
                String coverImagePath = fileUploadUtil.saveFile(courseRequest.getCoverImage());
                course.setCoverImage(coverImagePath);
            } catch (IOException e) {
                throw new BadRequestException("Failed to upload cover image: " + e.getMessage());
            }
        }
        
        // Save the course
        Course savedCourse = courseRepository.save(course);

        List<CourseRequest.ModuleRequest> modules =
                new ObjectMapper().readValue(courseRequest.getModules(), new TypeReference<>() {});

        QuizRequest quizRequest =
                new ObjectMapper().readValue(courseRequest.getQuiz(), new TypeReference<>() {});

        // Create modules and lessons
        List<Module> savedModules = createModulesAndLessons(modules, savedCourse, videoMap);
        
        // Create quiz with questions and answers if available
        if (courseRequest.getQuiz() != null) {
            createQuizQuestionsAndAnswers(quizRequest, savedCourse);
        }
        
        // Build the response
        return mapToCourseResponse(savedCourse, savedModules, null);
    }
    
    @Override
    @Transactional
    public CourseResponse updateCourseWithModulesAndQuiz(Long courseId, CourseRequestUpdate courseRequest, Map<String, MultipartFile> videoMap) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        Category category = categoryRepository.findById(courseRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", courseRequest.getCategoryId()));
        
        // Update course fields
        course.setTitle(courseRequest.getTitle());
        course.setDescription(courseRequest.getDescription());
        course.setLevel(courseRequest.getLevel());
        course.setDuration(courseRequest.getDuration());
        course.setCategory(category);
        
        // Handle cover image if new image is provided
        if (courseRequest.getCoverImage() != null && !courseRequest.getCoverImage().isEmpty()) {
            try {
                String coverImagePath = fileUploadUtil.saveFile(courseRequest.getCoverImage());
                course.setCoverImage(coverImagePath);
            } catch (IOException e) {
                throw new BadRequestException("Failed to upload cover image: " + e.getMessage());
            }
        }
        
        // Save updated course
        Course updatedCourse = courseRepository.save(course);

        // Parse modules from JSON string
        List<CourseRequest.ModuleRequest> moduleRequests =
                new ObjectMapper().readValue(courseRequest.getModules(), new TypeReference<>() {});

        QuizRequest quizRequest =
                new ObjectMapper().readValue(courseRequest.getQuiz(), new TypeReference<>() {});
        
        // Update modules and lessons - this is more complex as we need to handle updates, additions, and removals
        List<Module> updatedModules = updateModulesAndLessons(moduleRequests, updatedCourse, videoMap);
        
        // Update quiz if available
        if (courseRequest.getQuiz() != null) {
            updateQuizQuestionsAndAnswers(quizRequest, updatedCourse);
        } else {
            // Delete all questions if the quiz is not included in the update
            List<Question> existingQuestions = questionRepository.findByCourseId(courseId);
            questionRepository.deleteAll(existingQuestions);
        }
        
        // Build the response
        return mapToCourseResponse(updatedCourse, updatedModules, null);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseDetailsForEdit(Long id) {
        Course course = getCourseEntityById(id);
        return mapToCourseDetailResponseWithQuiz(course);
    }

    /*

        HELPER METHODS

     */
    private List<Module> createModulesAndLessons(List<CourseRequest.ModuleRequest> moduleRequests, Course course, Map<String, MultipartFile> videoMap) throws IOException {
        List<Module> savedModules = new ArrayList<>();

        int moduleIndex = 0;
        for (CourseRequest.ModuleRequest moduleRequest : moduleRequests) {
            // Create module
            Module module = Module.builder()
                    .title(moduleRequest.getTitle())
                    .course(course)
                    .build();

            // Save module
            Module savedModule = moduleRepository.save(module);
            savedModules.add(savedModule);

            // Save video file

            // Create lessons
            int lessonIndex = 0;
            List<CourseRequest.LessonRequest> lessonRequests = moduleRequest.getLessons();
            if (lessonRequests != null && !lessonRequests.isEmpty()) {
                for (CourseRequest.LessonRequest lessonRequest : lessonRequests) {
                    String key = "video_" + moduleIndex + "_" + lessonIndex;
                    MultipartFile vid = videoMap.get(key);      // ← lookup

                    String videoUrl = (vid != null) ? fileUploadUtil.saveFile(vid) : null;
                    // Create lesson
                    Lesson lesson = Lesson.builder()
                            .title(lessonRequest.getTitle())
                            .module(savedModule)
                            .duration(lessonRequest.getDuration())
                            .videoUrl(videoUrl)
                            .build();

                    // Save lesson
                    lessonRepository.save(lesson);
                }
                lessonIndex++;
            }
            moduleIndex++;
        }

        return savedModules;
    }

    private List<Module> updateModulesAndLessons(List<CourseRequest.ModuleRequest> moduleRequests, Course course, Map<String, MultipartFile> videoMap) throws IOException {
        // Get existing modules
        List<Module> existingModules = moduleRepository.getModulesByCourseId(course.getId());

        // Track modules that are kept in the update
        Set<Long> retainedModuleIds = new HashSet<>();
        List<Module> updatedModules = new ArrayList<>();

        // Process each module from the request
        int moduleIndex = 0;
        for (CourseRequest.ModuleRequest moduleRequest : moduleRequests) {
            if (moduleRequest.getId() != null) {
                // This is an existing module - update it
                Module existingModule = existingModules.stream()
                        .filter(m -> m.getId().equals(moduleRequest.getId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleRequest.getId()));

                // Update module fields
                existingModule.setTitle(moduleRequest.getTitle());

                // Save updated module
                Module updatedModule = moduleRepository.save(existingModule);
                updatedModules.add(updatedModule);
                retainedModuleIds.add(updatedModule.getId());

                // Update lessons for this module
                updateLessonsForModule(moduleRequest.getLessons(), updatedModule, videoMap, moduleIndex);

            } else {
                // This is a new module - create it
                Module newModule = Module.builder()
                        .title(moduleRequest.getTitle())
                        .course(course)
                        .build();

                // Save new module
                Module savedModule = moduleRepository.save(newModule);
                updatedModules.add(savedModule);
                retainedModuleIds.add(savedModule.getId());

                // Create lessons for this new module
                List<CourseRequest.LessonRequest> lessonRequests = moduleRequest.getLessons();
                if (lessonRequests != null && !lessonRequests.isEmpty()) {
                    int lessonIndex = 0;
                    for (CourseRequest.LessonRequest lessonRequest : lessonRequests) {
                        String key = "video_" + moduleIndex + "_" + lessonIndex;
                        MultipartFile vid = videoMap.get(key); // Lookup video file
                        String videoUrl = (vid != null) ? fileUploadUtil.saveFile(vid) : null;
                        // Create lesson
                        Lesson lesson = Lesson.builder()
                                .title(lessonRequest.getTitle())
                                .module(savedModule)
                                .duration(lessonRequest.getDuration())
                                .videoUrl(videoUrl)
                                .build();

                        // Save lesson
                        lessonRepository.save(lesson);
                        lessonIndex++;
                    }
                }
            }
            moduleIndex++;
        }

        // Delete modules that are no longer part of the course
        List<Module> modulesToDelete = existingModules.stream()
                .filter(module -> !retainedModuleIds.contains(module.getId()))
                .collect(Collectors.toList());

        moduleRepository.deleteAll(modulesToDelete);

        return updatedModules;
    }

    private void updateLessonsForModule(List<CourseRequest.LessonRequest> lessonRequests, Module module, Map<String, MultipartFile> videoMap, Integer moduleIndex) throws IOException {
        // Get existing lessons
        List<Lesson> existingLessons = lessonRepository.getLessonsByModuleId(module.getId());

        // Track lessons that are kept in the update
        Set<Long> retainedLessonIds = new HashSet<>();

        // Process each lesson from the request
        if (lessonRequests != null) {
            int lessonIndex = 0;
            for (CourseRequest.LessonRequest lessonRequest : lessonRequests) {
                if (lessonRequest.getId() != null) {
                    // This is an existing lesson - update it
                    Lesson existingLesson = existingLessons.stream()
                            .filter(l -> l.getId().equals(lessonRequest.getId()))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonRequest.getId()));

                    // Update lesson fields
                    existingLesson.setTitle(lessonRequest.getTitle());
                    existingLesson.setDuration(lessonRequest.getDuration());

                    // Handle video file upload
                    String key = "video_" + moduleIndex + "_" + lessonIndex;
                    MultipartFile vid = videoMap.get(key); // Lookup video file
                    String videoUrl = (vid != null) ? fileUploadUtil.saveFile(vid) : existingLesson.getVideoUrl();

                    existingLesson.setVideoUrl(videoUrl);

                    // Save updated lesson
                    Lesson updatedLesson = lessonRepository.save(existingLesson);
                    retainedLessonIds.add(updatedLesson.getId());

                } else {
                    // This is a new lesson - create it
                    String key = "video_" + moduleIndex + "_" + lessonIndex;
                    MultipartFile vid = videoMap.get(key); // Lookup video file
                    String videoUrl = (vid != null) ? fileUploadUtil.saveFile(vid) : null;
                    Lesson newLesson = Lesson.builder()
                            .title(lessonRequest.getTitle())
                            .module(module)
                            .duration(lessonRequest.getDuration())
                            .videoUrl(videoUrl)
                            .build();

                    // Save new lesson
                    Lesson savedLesson = lessonRepository.save(newLesson);
                    retainedLessonIds.add(savedLesson.getId());
                }
                lessonIndex++;
            }
        }

        // Delete lessons that are no longer part of the module
        List<Lesson> lessonsToDelete = existingLessons.stream()
                .filter(lesson -> !retainedLessonIds.contains(lesson.getId()))
                .collect(Collectors.toList());

        lessonRepository.deleteAll(lessonsToDelete);
    }

    private void createQuizQuestionsAndAnswers(QuizRequest quizRequest, Course course) {
        if (quizRequest.getQuestions() == null || quizRequest.getQuestions().isEmpty()) {
            return;
        }

        for (QuizRequest.QuestionRequest questionRequest : quizRequest.getQuestions()) {
            // Create question
            Question question = Question.builder()
                    .question(questionRequest.getQuestion())
                    .course(course)
                    .build();

            // Save question
            Question savedQuestion = questionRepository.save(question);

            // Create answers
            if (questionRequest.getAnswers() != null && !questionRequest.getAnswers().isEmpty()) {
                for (QuizRequest.AnswerRequest answerRequest : questionRequest.getAnswers()) {
                    // Create answer
                    Answer answer = Answer.builder()
                            .answer(answerRequest.getAnswer())
                            .correct(answerRequest.isCorrect())
                            .question(savedQuestion)
                            .build();

                    // Save answer
                    answerRepository.save(answer);
                }
            }
        }
    }

    private void updateQuizQuestionsAndAnswers(QuizRequest quizRequest, Course course) {
        // Get existing questions for this course
        List<Question> existingQuestions = questionRepository.findByCourseId(course.getId());

        // Delete all existing questions and answers first
        // This is simpler than tracking updates/deletes/inserts for a quiz
        questionRepository.deleteAll(existingQuestions);

        // Create new questions and answers
        createQuizQuestionsAndAnswers(quizRequest, course);
    }

    /*
        MAPPING METHODS

        These methods convert between entity and DTO objects.
        They can be further optimized or moved to a separate mapper class.
     */

    private CourseResponse mapToCourseResponse(Course course) {
        return mapToCourseResponse(course, null, null);
    }

    private CourseResponse mapToCourseResponse(Course course, List<Module> modules, Long studentId) {
        Double averageRating = reviewRepository.getAverageRatingByCourseId(course.getId());
        Integer enrollmentsCount = courseRepository.getEnrollmentCountByCourseId(course.getId());

        CourseResponse.CourseResponseBuilder builder = CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel().name())
                .duration(course.getDuration())
                .coverImage(course.getCoverImage())
                .categoryId(course.getCategory().getId())
                .categoryName(course.getCategory().getName())
                .trainer(CourseResponse.TrainerInfo.builder()
                        .id(course.getTrainer().getId())
                        .username(course.getTrainer().getUsername())
                        .fullName(course.getTrainer().getFullName())
                        .build())
                .createdAt(course.getCreatedAt())
                .averageRating(averageRating)
                .enrollmentsCount(enrollmentsCount);

        // Add module and lesson details if modules are provided
        if (modules != null && !modules.isEmpty()) {
            int totalLessons = 0;
            List<CourseResponse.ModuleResponse> moduleResponses = new ArrayList<>();

            for (Module module : modules) {
                List<Lesson> lessons = lessonRepository.getLessonsByModuleId(module.getId());
                totalLessons += lessons.size();

                List<CourseResponse.LessonResponse> lessonResponses = lessons.stream()
                        .map(lesson -> {
                            boolean completed = false;
                            if (studentId != null) {
                                completed = completedLessonRepository.findByStudentIdAndLessonId(studentId, lesson.getId()).isPresent();
                            }

                            return CourseResponse.LessonResponse.builder()
                                    .id(lesson.getId())
                                    .title(lesson.getTitle())
                                    .duration(lesson.getDuration())
                                    .videoUrl(lesson.getVideoUrl())
                                    .completed(completed)
                                    .build();
                        })
                        .collect(Collectors.toList());

                moduleResponses.add(CourseResponse.ModuleResponse.builder()
                        .id(module.getId())
                        .title(module.getTitle())
                        .lessons(lessonResponses)
                        .build());
            }

            builder.moduleCount(modules.size())
                    .lessonCount(totalLessons)
                    .modules(moduleResponses);
        }

        return builder.build();
    }

    private CourseMeta mapToCourseMeta(Course course) {
        Double averageRating = reviewRepository.getAverageRatingByCourseId(course.getId());
        Integer enrollmentsCount = courseRepository.getEnrollmentCountByCourseId(course.getId());

        return CourseMeta.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel().name())
                .duration(course.getDuration())
                .coverImage(course.getCoverImage())
                .categoryName(course.getCategory().getName())
                .updatedAt(course.getCreatedAt())
                .averageRating(averageRating)
                .enrollmentsCount(enrollmentsCount)
                .build();
    }

    private CourseResponse mapToCourseDetailResponse(Course course, Long studentId) {
        CourseResponse courseResponse = mapToCourseResponse(course);

        List<CourseResponse.ModuleResponse> moduleResponses = new ArrayList<>();
        List<Module> modules = moduleRepository.getModulesByCourseId(course.getId());
        List<Review> reviews = reviewRepository.findByCourseId(course.getId());
        Boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, course.getId());

        courseResponse.setReviews(reviews.stream()
                .map(review -> ReviewResponse.builder()
                        .id(review.getId())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .studentName(review.getStudent().getFullName())
                        .build())
                .collect(Collectors.toList()));

        courseResponse.setIsEnrolled(isEnrolled);

        for (Module module : modules) {
            List<CourseResponse.LessonResponse> lessonResponses = new ArrayList<>();
            List<Lesson> lessons = lessonRepository.getLessonsByModuleId(module.getId());

            for (Lesson lesson : lessons) {
                boolean completed = false;
                if (studentId != null) {
                    completed = completedLessonRepository.findByStudentIdAndLessonId(studentId, lesson.getId()).isPresent();
                }

                lessonResponses.add(CourseResponse.LessonResponse.builder()
                        .id(lesson.getId())
                        .title(lesson.getTitle())
                        .duration(lesson.getDuration())
                        .videoUrl(lesson.getVideoUrl())
                        .completed(completed)
                        .build());
            }

            moduleResponses.add(CourseResponse.ModuleResponse.builder()
                    .id(module.getId())
                    .title(module.getTitle())
                    .lessons(lessonResponses)
                    .build());
        }

        courseResponse.setModules(moduleResponses);

        // Add quiz information
        List<Question> questions = questionRepository.findByCourseId(course.getId());
        if (!questions.isEmpty()) {
            List<QuizRequest.QuestionRequest> questionRequests = new ArrayList<>();

            for (Question question : questions) {
                List<Answer> answers = answerRepository.findByQuestionId(question.getId());
                List<QuizRequest.AnswerRequest> answerRequests = new ArrayList<>();

                for (Answer answer : answers) {
                    answerRequests.add(QuizRequest.AnswerRequest.builder()
                            .id(answer.getId())
                            .answer(answer.getAnswer())
                            .correct(answer.isCorrect())
                            .build());
                }

                questionRequests.add(QuizRequest.QuestionRequest.builder()
                        .id(question.getId())
                        .question(question.getQuestion())
                        .answers(answerRequests)
                        .build());
            }

            courseResponse.setQuiz(QuizRequest.builder()
                    .questions(questionRequests)
                    .build());
        }

        return courseResponse;
    }

    private CourseResponse mapToCourseDetailResponseWithQuiz(Course course) {
        CourseResponse courseResponse = mapToCourseResponse(course);

        List<CourseResponse.ModuleResponse> moduleResponses = new ArrayList<>();
        List<Module> modules = moduleRepository.getModulesByCourseId(course.getId());

        for (Module module : modules) {
            List<CourseResponse.LessonResponse> lessonResponses = new ArrayList<>();
            List<Lesson> lessons = lessonRepository.getLessonsByModuleId(module.getId());

            for (Lesson lesson : lessons) {
                lessonResponses.add(CourseResponse.LessonResponse.builder()
                        .id(lesson.getId())
                        .title(lesson.getTitle())
                        .duration(lesson.getDuration())
                        .videoUrl(lesson.getVideoUrl())
                        .completed(false) // Not relevant for edit mode
                        .build());
            }

            moduleResponses.add(CourseResponse.ModuleResponse.builder()
                    .id(module.getId())
                    .title(module.getTitle())
                    .lessons(lessonResponses)
                    .build());
        }

        courseResponse.setModules(moduleResponses);

        // Add quiz information
        List<Question> questions = questionRepository.findByCourseId(course.getId());
        if (!questions.isEmpty()) {
            List<QuizRequest.QuestionRequest> questionRequests = new ArrayList<>();

            for (Question question : questions) {
                List<Answer> answers = answerRepository.findByQuestionId(question.getId());
                List<QuizRequest.AnswerRequest> answerRequests = new ArrayList<>();

                for (Answer answer : answers) {
                    answerRequests.add(QuizRequest.AnswerRequest.builder()
                            .id(answer.getId())
                            .answer(answer.getAnswer())
                            .correct(answer.isCorrect())
                            .build());
                }

                questionRequests.add(QuizRequest.QuestionRequest.builder()
                        .id(question.getId())
                        .question(question.getQuestion())
                        .answers(answerRequests)
                        .build());
            }

            courseResponse.setQuiz(QuizRequest.builder()
                    .questions(questionRequests)
                    .build());
        }

        return courseResponse;
    }
}
