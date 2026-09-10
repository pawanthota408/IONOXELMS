<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit;
}

date_default_timezone_set("Asia/Kolkata");

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

$student_id = isset($_GET["student_id"]) ? intval($_GET["student_id"]) : (isset($_POST["student_id"]) ? intval($_POST["student_id"]) : 0);
$course_id  = isset($_GET["course_id"]) ? intval($_GET["course_id"]) : (isset($_POST["course_id"]) ? intval($_POST["course_id"]) : (isset($_GET["id"]) ? intval($_GET["id"]) : 0));

// Fetch Course
$course = null;
if ($course_id > 0) {
    $stmt = $conn->prepare("SELECT id, title, description, thumbnail FROM courses WHERE id = ? LIMIT 1");
    if ($stmt) {
        $stmt->bind_param("i", $course_id);
        $stmt->execute();
        $course = $stmt->get_result()->fetch_assoc();
        $stmt->close();
    }
}

if (!$course) {
    $c_res = $conn->query("SELECT id, title, description, thumbnail FROM courses ORDER BY id ASC LIMIT 1");
    if ($c_res && $c_row = $c_res->fetch_assoc()) {
        $course = $c_row;
        $course_id = intval($course['id']);
    }
}

if (!$course) {
    $course = [
        "id" => 1,
        "title" => "Artificial Intelligence & Machine Learning",
        "description" => "Master Artificial Intelligence and Machine Learning algorithms.",
        "thumbnail" => "https://iili.io/fViYYl9.png"
    ];
    $course_id = 1;
}

// Fetch Lessons from lessons table
$lessons = [];
$chk_l = $conn->query("SHOW TABLES LIKE 'lessons'");
if ($chk_l && $chk_l->num_rows > 0) {
    // Try fetching lessons for specific course_id
    $stmtLessons = $conn->prepare("SELECT id, course_id, title, description, video_url, lesson_order FROM lessons WHERE course_id = ? ORDER BY CASE WHEN lesson_order IS NULL THEN 999999 ELSE lesson_order END ASC, id ASC");
    if ($stmtLessons) {
        $stmtLessons->bind_param("i", $course_id);
        $stmtLessons->execute();
        $resL = $stmtLessons->get_result();
        while ($row = $resL->fetch_assoc()) {
            $lessons[] = [
                "id"           => (int)$row["id"],
                "course_id"    => (int)$row["course_id"],
                "title"        => $row["title"] ?? "Lesson",
                "description"  => $row["description"] ?? "",
                "video_url"    => $row["video_url"] ?? "",
                "lesson_order" => isset($row["lesson_order"]) ? (int)$row["lesson_order"] : 0
            ];
        }
        $stmtLessons->close();
    }

    // If no lessons found for this specific course_id, fetch ALL available lessons in database
    if (empty($lessons)) {
        $all_l = $conn->query("SELECT id, course_id, title, description, video_url, lesson_order FROM lessons ORDER BY id ASC LIMIT 20");
        if ($all_l) {
            while ($row = $all_l->fetch_assoc()) {
                $lessons[] = [
                    "id"           => (int)$row["id"],
                    "course_id"    => (int)$row["course_id"],
                    "title"        => $row["title"] ?? "Lesson",
                    "description"  => $row["description"] ?? "",
                    "video_url"    => $row["video_url"] ?? "",
                    "lesson_order" => isset($row["lesson_order"]) ? (int)$row["lesson_order"] : 0
                ];
            }
        }
    }
}

echo json_encode([
    "status"        => "success",
    "course_id"     => (int)$course["id"],
    "title"         => $course["title"] ?? "Course Lessons",
    "description"   => $course["description"] ?? "",
    "thumbnail"     => $course["thumbnail"] ?? "",
    "total_lessons" => count($lessons),
    "lessons"       => $lessons
]);
exit;
?>
