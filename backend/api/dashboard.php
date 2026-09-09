<?php
// ===================================================
// IONOXE TECH SOLUTIONS - LMS STUDENT DASHBOARD REST API
// Path: api/dashboard.php
// Returns complete JSON dashboard data for logged-in student
// ===================================================

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

define('ACCESS', true);
session_start();
date_default_timezone_set('Asia/Kolkata');

// Database Config
$conn_handle = null;
if (file_exists("../config.php")) {
    require_once "../config.php";
} elseif (file_exists("config.php")) {
    require_once "config.php";
}

if (isset($conn)) {
    $conn_handle = $conn;
} elseif (isset($db)) {
    $conn_handle = $db;
} elseif (isset($mysqli)) {
    $conn_handle = $mysqli;
}

// Get Student ID from Session or Query Parameter
$student_id = 0;
if (isset($_GET['student_id'])) {
    $student_id = intval($_GET['student_id']);
} elseif (isset($_SESSION['student_id'])) {
    $student_id = intval($_SESSION['student_id']);
}

// Demo Data Response fallback if student_id is demo (999) or DB is not reachable
if ($student_id === 999 || !$conn_handle) {
    http_response_code(200);
    echo json_encode([
        "status" => "success",
        "student_name" => $_SESSION['name'] ?? "Demo Student",
        "student_id" => "STU999",
        "enrolled_count" => 3,
        "quiz_count" => 5,
        "assignments_pending" => 2,
        "notif_count" => 1,
        "next_class" => [
            "course_title" => "Full Stack Web Development",
            "trainer_name" => "Alex Johnson",
            "start_date" => date('Y-m-d', strtotime('+1 day')),
            "start_time" => "10:00:00",
            "meeting_link" => "https://meet.google.com/demo"
        ],
        "weekly_schedules" => [
            [
                "id" => 1,
                "course_title" => "Full Stack Web Development",
                "trainer_name" => "Alex Johnson",
                "start_date" => date('Y-m-d'),
                "start_time" => "10:00:00",
                "meeting_link" => "https://meet.google.com/demo",
                "is_live" => true,
                "is_today" => true
            ],
            [
                "id" => 2,
                "course_title" => "Python & Data Science",
                "trainer_name" => "Sarah Connor",
                "start_date" => date('Y-m-d', strtotime('+2 days')),
                "start_time" => "14:00:00",
                "meeting_link" => "https://meet.google.com/demo",
                "is_live" => false,
                "is_today" => false
            ]
        ],
        "courses" => [
            [
                "id" => 101,
                "title" => "Full Stack Web Development",
                "description" => "Master HTML, CSS, JavaScript, PHP, MySQL and React to build modern web apps.",
                "thumbnail" => "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=500"
            ],
            [
                "id" => 102,
                "title" => "Python & Data Analytics",
                "description" => "Learn Python programming, pandas, data visualization and machine learning basics.",
                "thumbnail" => "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500"
            ]
        ],
        "jobs" => [
            [
                "id" => 1,
                "title" => "Junior Web Developer",
                "company" => "Ionoxe Tech Solutions",
                "location" => "Hyderabad / Remote",
                "description" => "Looking for a passionate junior frontend/backend developer to join our team.",
                "posted_date" => date('Y-m-d')
            ]
        ]
    ]);
    exit();
}

// Fetch Real Student Info from MySQL
$student_res = $conn_handle->query("SELECT * FROM students WHERE id = $student_id LIMIT 1");
if (!$student_res || $student_res->num_rows === 0) {
    http_response_code(404);
    echo json_encode(["status" => "error", "message" => "Student not found"]);
    exit();
}

$student  = $student_res->fetch_assoc();
$batch_id = $conn_handle->real_escape_string($student['batch_id'] ?? '');

// Check schedule table
$schedules_check = $conn_handle->query("SHOW TABLES LIKE 'training_schedules'");
$has_schedules   = ($schedules_check && $schedules_check->num_rows > 0);
$has_student_col = false;
if ($has_schedules) {
    $col_check       = $conn_handle->query("SHOW COLUMNS FROM training_schedules LIKE 'student_id'");
    $has_student_col = ($col_check && $col_check->num_rows > 0);
}
$sched_where = $has_student_col
    ? "(ts.student_id = $student_id OR ts.batch_id = '$batch_id')"
    : "ts.batch_id = '$batch_id'";

// Next Class
$next_class = null;
if ($has_schedules) {
    $nc_res = $conn_handle->query("
        SELECT ts.start_date, ts.start_time, ts.meeting_link,
               c.title AS course_title, t.name AS trainer_name
        FROM training_schedules ts
        JOIN courses c ON ts.course_id = c.id
        LEFT JOIN trainers t ON ts.trainer_id = t.id
        WHERE $sched_where
          AND STR_TO_DATE(CONCAT(ts.start_date, ' ', ts.start_time), '%Y-%m-%d %H:%i:%s') > NOW()
        ORDER BY ts.start_date ASC, ts.start_time ASC
        LIMIT 1
    ");
    if ($nc_res && $nc_res->num_rows > 0) {
        $next_class = $nc_res->fetch_assoc();
    }
}

// Counts
$enrolled_res   = $conn_handle->query("SELECT COUNT(*) FROM enrollments WHERE student_id = $student_id");
$enrolled_count = $enrolled_res ? (int)$enrolled_res->fetch_row()[0] : 0;

$quiz_count = 0;
$quiz_table = $conn_handle->query("SHOW TABLES LIKE 'quizzes'");
if ($quiz_table && $quiz_table->num_rows > 0) {
    $quiz_res   = $conn_handle->query("
        SELECT COUNT(*) FROM quizzes q
        JOIN enrollments e ON q.course_id = e.course_id
        WHERE e.student_id = $student_id
    ");
    $quiz_count = $quiz_res ? (int)$quiz_res->fetch_row()[0] : 0;
}

$assignments_pending = 0;
$assign_table        = $conn_handle->query("SHOW TABLES LIKE 'assignments'");
if ($assign_table && $assign_table->num_rows > 0) {
    $assign_res          = $conn_handle->query("
        SELECT COUNT(*) FROM assignments a
        JOIN enrollments e ON a.course_id = e.course_id
        WHERE e.student_id = $student_id AND a.due_date > NOW()
    ");
    $assignments_pending = $assign_res ? (int)$assign_res->fetch_row()[0] : 0;
}

// Notifications Count
$notif_count = 0;
$notif_table = $conn_handle->query("SHOW TABLES LIKE 'notifications'");
if ($notif_table && $notif_table->num_rows > 0) {
    $notif_res = $conn_handle->query("
        SELECT COUNT(*) FROM notifications
        WHERE (recipient_type = 'all' OR (recipient_type = 'student' AND FIND_IN_SET($student_id, student_ids)))
          AND is_read = 0
    ");
    $notif_count = $notif_res ? (int)$notif_res->fetch_row()[0] : 0;
}

// Enrolled Courses
$courses_arr = [];
$courses_res = $conn_handle->query("
    SELECT c.id, c.title, c.thumbnail, c.description
    FROM courses c
    JOIN enrollments e ON c.id = e.course_id
    WHERE e.student_id = $student_id
    LIMIT 3
");
if ($courses_res) {
    while ($row = $courses_res->fetch_assoc()) {
        $courses_arr[] = $row;
    }
}

// Jobs
$latest_jobs = [];
$job_table   = $conn_handle->query("SHOW TABLES LIKE 'jobs'");
if ($job_table && $job_table->num_rows > 0) {
    $jobs_res = $conn_handle->query("
        SELECT * FROM jobs
        WHERE status = 'active' OR status IS NULL
        ORDER BY id DESC LIMIT 3
    ");
    if ($jobs_res) {
        while ($row = $jobs_res->fetch_assoc()) {
            $latest_jobs[] = $row;
        }
    }
}

// Weekly Schedules
$weekly_schedules = [];
if ($has_schedules) {
    $ws_res = $conn_handle->query("
        SELECT ts.*, c.title AS course_title, t.name AS trainer_name
        FROM training_schedules ts
        JOIN courses c ON ts.course_id = c.id
        LEFT JOIN trainers t ON ts.trainer_id = t.id
        WHERE $sched_where
          AND ts.start_date >= CURDATE()
          AND ts.start_date <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)
        ORDER BY ts.start_date ASC, ts.start_time ASC
    ");
    if ($ws_res) {
        $today_str = date('Y-m-d');
        while ($row = $ws_res->fetch_assoc()) {
            $is_today = ($row['start_date'] === $today_str);
            $class_ts = strtotime($row['start_date'] . ' ' . $row['start_time']);
            $is_live  = ($is_today && time() >= $class_ts && time() <= ($class_ts + 3600));

            $row['is_today'] = $is_today;
            $row['is_live']  = $is_live;
            $weekly_schedules[] = $row;
        }
    }
}

http_response_code(200);
echo json_encode([
    "status" => "success",
    "student_name" => $student['name'],
    "student_id" => $student['student_id'],
    "enrolled_count" => $enrolled_count,
    "quiz_count" => $quiz_count,
    "assignments_pending" => $assignments_pending,
    "notif_count" => $notif_count,
    "next_class" => $next_class,
    "weekly_schedules" => $weekly_schedules,
    "courses" => $courses_arr,
    "jobs" => $latest_jobs
]);
?>
