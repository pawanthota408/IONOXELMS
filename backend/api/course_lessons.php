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

/*
|--------------------------------------------------------------------------
| CONFIG
|--------------------------------------------------------------------------
*/

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

if (
    !$configLoaded ||
    !isset($conn) ||
    !($conn instanceof mysqli)
) {
    http_response_code(500);

    echo json_encode([
        "status" => "error",
        "message" => "Database connection failed"
    ]);

    exit;
}

/*
|--------------------------------------------------------------------------
| INPUT
|--------------------------------------------------------------------------
*/

$student_id = isset($_GET["student_id"])
    ? intval($_GET["student_id"])
    : (
        isset($_POST["student_id"])
            ? intval($_POST["student_id"])
            : 0
    );

$course_id = isset($_GET["course_id"])
    ? intval($_GET["course_id"])
    : (
        isset($_POST["course_id"])
            ? intval($_POST["course_id"])
            : (
                isset($_GET["id"])
                    ? intval($_GET["id"])
                    : 0
            )
    );

/*
|--------------------------------------------------------------------------
| COURSE ID REQUIRED
|--------------------------------------------------------------------------
*/

if ($course_id <= 0) {

    http_response_code(400);

    echo json_encode([
        "status" => "error",
        "message" => "Valid course_id is required"
    ]);

    exit;
}

/*
|--------------------------------------------------------------------------
| FETCH COURSE
|--------------------------------------------------------------------------
*/

$stmt = $conn->prepare("
    SELECT
        id,
        title,
        description,
        thumbnail
    FROM courses
    WHERE id = ?
    LIMIT 1
");

if (!$stmt) {

    http_response_code(500);

    echo json_encode([
        "status" => "error",
        "message" => "Failed to prepare course query",
        "error" => $conn->error
    ]);

    exit;
}

$stmt->bind_param("i", $course_id);

if (!$stmt->execute()) {

    http_response_code(500);

    echo json_encode([
        "status" => "error",
        "message" => "Failed to fetch course",
        "error" => $stmt->error
    ]);

    exit;
}

$result = $stmt->get_result();

$course = $result->fetch_assoc();

$stmt->close();

/*
|--------------------------------------------------------------------------
| COURSE NOT FOUND
|--------------------------------------------------------------------------
*/

if (!$course) {

    http_response_code(404);

    echo json_encode([
        "status" => "error",
        "message" => "Course not found",
        "course_id" => $course_id
    ]);

    exit;
}

/*
|--------------------------------------------------------------------------
| FETCH LESSONS
|--------------------------------------------------------------------------
*/

$lessons = [];

$stmtLessons = $conn->prepare("
    SELECT
        id,
        course_id,
        title,
        description,
        video_url,
        lesson_order,
        batch_id
    FROM lessons
    WHERE course_id = ?
    ORDER BY
        CASE
            WHEN lesson_order IS NULL THEN 999999
            ELSE lesson_order
        END ASC,
        id ASC
");

if (!$stmtLessons) {

    http_response_code(500);

    echo json_encode([
        "status" => "error",
        "message" => "Failed to prepare lessons query",
        "error" => $conn->error
    ]);

    exit;
}

$stmtLessons->bind_param(
    "i",
    $course_id
);

if (!$stmtLessons->execute()) {

    http_response_code(500);

    echo json_encode([
        "status" => "error",
        "message" => "Failed to fetch lessons",
        "error" => $stmtLessons->error
    ]);

    exit;
}

$resultLessons = $stmtLessons->get_result();

while ($row = $resultLessons->fetch_assoc()) {

    $lessons[] = [
        "id" => (int)$row["id"],

        "course_id" => (int)$row["course_id"],

        "title" =>
            $row["title"] ?? "Lesson",

        "description" =>
            $row["description"] ?? "",

        "video_url" =>
            $row["video_url"] ?? "",

        "lesson_order" =>
            isset($row["lesson_order"])
                ? (int)$row["lesson_order"]
                : 0,

        "batch_id" =>
            isset($row["batch_id"])
                ? (int)$row["batch_id"]
                : null
    ];
}

$stmtLessons->close();

/*
|--------------------------------------------------------------------------
| SUCCESS RESPONSE
|--------------------------------------------------------------------------
*/

echo json_encode([
    "status" => "success",

    "course_id" =>
        (int)$course["id"],

    "title" =>
        $course["title"] ?? "",

    "description" =>
        $course["description"] ?? "",

    "thumbnail" =>
        $course["thumbnail"] ?? "",

    "total_lessons" =>
        count($lessons),

    "lessons" =>
        $lessons
]);

exit;
?>
