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
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "course_id required"]);
    exit;
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
    http_response_code(404);
    echo json_encode(["status" => "error", "message" => "Course not found"]);
    exit;
}

// Fetch Student Batch
$batch_id = 0;
if ($student_id > 0) {
    $s_res = $conn->query("SELECT batch_id FROM students WHERE id = $student_id LIMIT 1");
    if ($s_res && $s_row = $s_res->fetch_assoc()) {
        $batch_id = intval($s_row['batch_id'] ?? 0);
    }
}

// Fetch Live / Upcoming Class
$live_class = null;
if ($batch_id > 0) {
    $stmt_s = $conn->prepare("SELECT * FROM training_schedules WHERE batch_id = ? AND course_id = ? ORDER BY start_date ASC, start_time ASC");
    if ($stmt_s) {
        $stmt_s->bind_param('ii', $batch_id, $course_id);
        $stmt_s->execute();
        $all_scheds = $stmt_s->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmt_s->close();
        $now_ts = time();
        foreach ($all_scheds as $sched) {
            $start_ts = strtotime($sched['start_date'] . ' ' . $sched['start_time']);
            $end_ts   = strtotime($sched['start_date'] . ' ' . $sched['end_time']);
            if ($now_ts >= $start_ts && $now_ts <= $end_ts) {
                $live_class = [
                    "status"       => "live",
                    "meeting_link" => $sched['meeting_link'] ?? '',
                    "start_time"   => date('h:i A', $start_ts),
                    "end_time"     => date('h:i A', $end_ts)
                ];
                break;
            }
            if ($start_ts > $now_ts) {
                $live_class = [
                    "status"       => "upcoming",
                    "meeting_link" => $sched['meeting_link'] ?? '',
                    "start_time"   => date('h:i A', $start_ts),
                    "end_time"     => date('h:i A', $end_ts)
                ];
                break;
            }
        }
    }
}

// Fetch Lessons List
$lessons = [];
$chk_l = $conn->query("SHOW TABLES LIKE 'lessons'");
if ($chk_l && $chk_l->num_rows > 0) {
    if ($batch_id > 0) {
        $stmt_l = $conn->prepare("SELECT id, title, video_url, description FROM lessons WHERE course_id = ? AND (batch_id = ? OR batch_id IS NULL OR batch_id = 0) ORDER BY id ASC");
        $stmt_l->bind_param('ii', $course_id, $batch_id);
    } else {
        $stmt_l = $conn->prepare("SELECT id, title, video_url, description FROM lessons WHERE course_id = ? ORDER BY id ASC");
        $stmt_l->bind_param('i', $course_id);
    }

    if ($stmt_l) {
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

// Fallback Mock Lessons if no lessons in table
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
    "next_class"    => $live_class,
    "total_lessons" => count($lessons),
    "lessons"       => $lessons
]);
exit;
?>
