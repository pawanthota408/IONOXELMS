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

$rawInput = file_get_contents("php://input");
$input = json_decode($rawInput, true) ?: [];

$student_id = isset($_GET['student_id']) ? intval($_GET['student_id']) : (isset($_POST['student_id']) ? intval($_POST['student_id']) : intval($input['student_id'] ?? 0));

if ($student_id <= 0) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Student ID required"]);
    exit;
}

// Handle Profile Update POST
if ($_SERVER['REQUEST_METHOD'] === 'POST' && (isset($_POST['update_profile']) || isset($input['update_profile']))) {
    $name    = trim($_POST['name'] ?? $input['name'] ?? '');
    $email   = trim($_POST['email'] ?? $input['email'] ?? '');
    $phone   = trim($_POST['phone'] ?? $input['phone'] ?? '');
    $address = trim($_POST['address'] ?? $input['address'] ?? '');

    if (!empty($name) && !empty($email)) {
        $stmt = $conn->prepare("UPDATE students SET name=?, email=?, phone=?, address=? WHERE id=?");
        if ($stmt) {
            $stmt->bind_param("ssssi", $name, $email, $phone, $address, $student_id);
            $stmt->execute();
            $stmt->close();
        }
    }
}

// Fetch Student Profile
$stmt = $conn->prepare("
    SELECT s.id, s.student_id, s.name, s.email, s.phone, s.address, s.created_at, b.batch_name
    FROM students s
    LEFT JOIN batches b ON s.batch_id = b.id
    WHERE s.id = ?
    LIMIT 1
");

$student = null;
if ($stmt) {
    $stmt->bind_param("i", $student_id);
    $stmt->execute();
    $student = $stmt->get_result()->fetch_assoc();
    $stmt->close();
}

if (!$student) {
    http_response_code(404);
    echo json_encode(["status" => "error", "message" => "Student not found"]);
    exit;
}

echo json_encode([
    "status"      => "success",
    "id"          => (int)$student['id'],
    "student_id"  => $student['student_id'] ?? '',
    "name"        => $student['name'] ?? 'Student',
    "email"       => $student['email'] ?? '',
    "phone"       => $student['phone'] ?? '',
    "address"     => $student['address'] ?? '',
    "batch_name"  => $student['batch_name'] ?? 'General Batch',
    "created_at"  => !empty($student['created_at']) ? date('d M Y', strtotime($student['created_at'])) : 'Joined recently'
]);
exit;
?>
