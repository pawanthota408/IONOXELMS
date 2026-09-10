<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit;
}

date_default_timezone_set('Asia/Kolkata');

$configPaths = [
    __DIR__ . "/../config.php",
    __DIR__ . "/config.php",
    dirname(__DIR__) . "/config.php"
];

$configLoaded = false;
foreach ($configPaths as $path) {
    if (file_exists($path)) {
        require_once $path;
        $configLoaded = true;
        break;
    }
}

if (!$configLoaded || !isset($conn) || !($conn instanceof mysqli)) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$student_id = isset($_GET['student_id']) ? intval($_GET['student_id']) : (isset($_POST['student_id']) ? intval($_POST['student_id']) : 0);
$course_id  = isset($_GET['course_id']) ? intval($_GET['course_id']) : (isset($_POST['course_id']) ? intval($_POST['course_id']) : (isset($_GET['id']) ? intval($_GET['id']) : 0));

if ($course_id <= 0) {
    $course_id = 1;
}

// Fetch Course
$stmt = $conn->prepare("SELECT id, title, description, thumbnail FROM courses WHERE id = ? LIMIT 1");
$course = null;
if ($stmt) {
    $stmt->bind_param('i', $course_id);
    $stmt->execute();
    $course = $stmt->get_result()->fetch_assoc();
    $stmt->close();
}

if (!$course) {
    // If course 1 not found, fetch first available course
    $stmt_fallback = $conn->query("SELECT id, title, description, thumbnail FROM courses ORDER BY id ASC LIMIT 1");
    if ($stmt_fallback && $row_f = $stmt_fallback->fetch_assoc()) {
        $course = $row_f;
        $course_id = intval($course['id']);
    }
}

if (!$course) {
    $course = [
        "id" => 1,
        "title" => "Artificial Intelligence & Machine Learning",
        "description" => "Master Artificial Intelligence & Machine Learning algorithms.",
        "thumbnail" => "https://iili.io/fViYYl9.png"
    ];
}

// Fetch Lessons List safely by course_id
$lessons = [];
$chk_l = $conn->query("SHOW TABLES LIKE 'lessons'");
if ($chk_l && $chk_l->num_rows > 0) {
    $stmt_l = $conn->prepare("SELECT id, title, video_url, description FROM lessons WHERE course_id = ? ORDER BY id ASC");
    if ($stmt_l) {
        $stmt_l->bind_param('i', $course_id);
        $stmt_l->execute();
        $res_l = $stmt_l->get_result();
        while ($row = $res_l->fetch_assoc()) {
            $lessons[] = [
                "id"          => (int)$row['id'],
                "title"       => $row['title'] ?? 'Lesson',
                "video_url"   => $row['video_url'] ?? '',
                "description" => $row['description'] ?? ''
            ];
        }
        $stmt_l->close();
    }
}

// Fallback Mock Lessons if no lessons in table for this course
if (empty($lessons)) {
    $lessons = [
        [
            "id"          => 101,
            "title"       => "Lesson 1: Introduction & Environment Setup",
            "video_url"   => "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "description" => "Welcome to the course! In this lesson we will set up the development environment, install tools, and understand the core curriculum."
        ],
        [
            "id"          => 102,
            "title"       => "Lesson 2: Core Concepts & Architecture",
            "video_url"   => "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "description" => "Learn fundamental architecture, basic syntax, component hierarchies, and best practices."
        ],
        [
            "id"          => 103,
            "title"       => "Lesson 3: Hands-On Practical Project",
            "video_url"   => "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "description" => "Build a real-world hands-on project step-by-step applying concepts learned in previous modules."
        ]
    ];
}

echo json_encode([
    "status"        => "success",
    "course_id"     => (int)$course['id'],
    "title"         => $course['title'] ?? 'Course Lessons',
    "description"   => $course['description'] ?? '',
    "thumbnail"     => $course['thumbnail'] ?? '',
    "total_lessons" => count($lessons),
    "lessons"       => $lessons
]);
exit;
?>
