<?php

// course_lesson.php — JSON API for Android
// Returns lessons for a course, optionally filtered by the student's batch.

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit;
}

date_default_timezone_set("Asia/Kolkata");

// ── Load config.php ─────────────────────────────────────────────
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

// ── Read params ─────────────────────────────────────────────────
$student_id = isset($_GET["student_id"]) ? intval($_GET["student_id"]) : (isset($_POST["student_id"]) ? intval($_POST["student_id"]) : 0);
$course_id  = isset($_GET["course_id"])  ? intval($_GET["course_id"])  : (isset($_POST["course_id"])  ? intval($_POST["course_id"])  : (isset($_GET["id"]) ? intval($_GET["id"]) : 0));

if ($course_id <= 0) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "course_id is required"]);
    exit;
}

// ── Fetch course ────────────────────────────────────────────────
$course = null;
$stmt = $conn->prepare("SELECT id, title, description, thumbnail FROM courses WHERE id = ? LIMIT 1");
if ($stmt) {
    $stmt->bind_param("i", $course_id);
    $stmt->execute();
    $course = $stmt->get_result()->fetch_assoc();
    $stmt->close();
}

if (!$course) {
    http_response_code(404);
    echo json_encode([
        "status"    => "error",
        "message"   => "Course not found",
        "course_id" => $course_id
    ]);
    exit;
}

// ── Resolve student's batch (optional) ──────────────────────────
$batch_id = 0;

if ($student_id > 0) {
    $bs = $conn->prepare("SELECT batch_id FROM students WHERE id = ? LIMIT 1");
    if ($bs) {
        $bs->bind_param("i", $student_id);
        $bs->execute();
        $bRow = $bs->get_result()->fetch_assoc();
        $batch_id = (int)($bRow['batch_id'] ?? 0);
        $bs->close();
    }
}

// Allow explicit override for testing: ?batch_id=5
if (isset($_GET['batch_id'])) {
    $batch_id = intval($_GET['batch_id']);
}

// ── Fetch lessons for THIS course ───────────────────────────────
$lessons = [];

$baseSql = "SELECT id, course_id, title, description, video_url, lesson_order, batch_id
            FROM lessons
            WHERE course_id = ?";

$orderSql = " ORDER BY
                CASE WHEN lesson_order IS NULL OR lesson_order = 0 THEN 999999
                     ELSE lesson_order
                END ASC,
                id ASC";

if ($batch_id > 0) {
    $sql = $baseSql . " AND (batch_id = ? OR batch_id IS NULL)" . $orderSql;
    $stmtLessons = $conn->prepare($sql);
    if ($stmtLessons) {
        $stmtLessons->bind_param("ii", $course_id, $batch_id);
    }
} else {
    // No batch info → return all lessons for the course
    $sql = $baseSql . $orderSql;
    $stmtLessons = $conn->prepare($sql);
    if ($stmtLessons) {
        $stmtLessons->bind_param("i", $course_id);
    }
}

if (!$stmtLessons) {
    http_response_code(500);
    echo json_encode([
        "status"  => "error",
        "message" => "Failed to prepare lessons query: " . $conn->error
    ]);
    exit;
}

$stmtLessons->execute();
$resL = $stmtLessons->get_result();

while ($row = $resL->fetch_assoc()) {
    $lessons[] = [
        "id"           => (int)$row["id"],
        "course_id"    => (int)$row["course_id"],
        "title"        => $row["title"]       ?? "Lesson",
        "description"  => $row["description"] ?? "",
        "video_url"    => $row["video_url"]   ?? "",
        "lesson_order" => isset($row["lesson_order"]) ? (int)$row["lesson_order"] : 0,
        "batch_id"     => isset($row["batch_id"]) && $row["batch_id"] !== null
                            ? (int)$row["batch_id"]
                            : null,
    ];
}
$stmtLessons->close();

// ── Respond ─────────────────────────────────────────────────────
echo json_encode([
    "status"        => "success",
    "course_id"     => (int)$course["id"],
    "title"         => $course["title"]       ?? "Course Lessons",
    "description"   => $course["description"] ?? "",
    "thumbnail"     => $course["thumbnail"]   ?? "",
    "batch_id"      => $batch_id > 0 ? $batch_id : null,
    "total_lessons" => count($lessons),
    "lessons"       => $lessons
]);
exit;
?>
