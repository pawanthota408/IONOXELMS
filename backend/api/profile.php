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

/* CONFIG */
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

    echo json_encode([
        "status" => "error",
        "message" => "Database connection failed"
    ]);

    exit;
}

/* STUDENT ID */

$student_id = isset($_GET["student_id"])
    ? intval($_GET["student_id"])
    : 0;

if ($student_id <= 0) {
    http_response_code(400);

    echo json_encode([
        "status" => "error",
        "message" => "Valid student_id is required"
    ]);

    exit;
}

/* FETCH STUDENT */

$stmt = $conn->prepare("
    SELECT
        id,
        student_id,
        name,
        email,
        phone,
        created_at,
        batch_id,
        ca_id
    FROM students
    WHERE id = ?
    LIMIT 1
");

if (!$stmt) {
    http_response_code(500);

    echo json_encode([
        "status" => "error",
        "message" => "Failed to prepare query",
        "error" => $conn->error
    ]);

    exit;
}

$stmt->bind_param("i", $student_id);

if (!$stmt->execute()) {
    http_response_code(500);

    echo json_encode([
        "status" => "error",
        "message" => "Failed to execute query",
        "error" => $stmt->error
    ]);

    exit;
}

$result = $stmt->get_result();
$student = $result->fetch_assoc();

$stmt->close();

/* STUDENT NOT FOUND */

if (!$student) {
    http_response_code(404);

    echo json_encode([
        "status" => "error",
        "message" => "Student not found"
    ]);

    exit;
}

/* BATCH */

$batch_name = "General Batch";

if (!empty($student["batch_id"])) {

    $batch_id = intval($student["batch_id"]);

    $batchStmt = $conn->prepare("
        SELECT batch_name
        FROM batches
        WHERE id = ?
        LIMIT 1
    ");

    if ($batchStmt) {

        $batchStmt->bind_param("i", $batch_id);

        if ($batchStmt->execute()) {

            $batchResult = $batchStmt->get_result();
            $batch = $batchResult->fetch_assoc();

            if ($batch && !empty($batch["batch_name"])) {
                $batch_name = $batch["batch_name"];
            }
        }

        $batchStmt->close();
    }
}

/* CREATED DATE */

$created_at = "Joined recently";

if (!empty($student["created_at"])) {

    $timestamp = strtotime($student["created_at"]);

    if ($timestamp !== false) {
        $created_at = date("d M Y", $timestamp);
    }
}

/* SUCCESS */

echo json_encode([
    "status" => "success",
    "id" => (int)$student["id"],
    "student_id" => $student["student_id"] ?? null,
    "name" => $student["name"] ?? null,
    "email" => $student["email"] ?? null,
    "phone" => $student["phone"] ?? null,
    "batch_id" => isset($student["batch_id"])
        ? (int)$student["batch_id"]
        : null,
    "ca_id" => isset($student["ca_id"])
        ? (int)$student["ca_id"]
        : null,
    "batch_name" => $batch_name,
    "created_at" => $created_at
]);

exit;
?>