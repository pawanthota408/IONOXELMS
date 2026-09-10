<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

error_reporting(E_ALL);
ini_set('display_errors', 0);

register_shutdown_function(function () {
    $error = error_get_last();
    if ($error && in_array($error['type'], [E_ERROR, E_PARSE, E_CORE_ERROR, E_COMPILE_ERROR])) {
        http_response_code(500);
        echo json_encode([
            "status"  => "error",
            "message" => "Fatal Error: " . $error['message']
        ]);
    }
});

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

try {

    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        http_response_code(405);
        echo json_encode([
            "status"  => "error",
            "message" => "Method Not Allowed. Use POST."
        ]);
        exit();
    }

    // Fix path to config.php (include parent directory slash)
    if (file_exists(__DIR__ . "/../config.php")) {
        require_once __DIR__ . "/../config.php";
    } elseif (file_exists("../config.php")) {
        require_once "../config.php";
    } elseif (file_exists("config.php")) {
        require_once "config.php";
    }

    if (!isset($conn) || !$conn) {
        http_response_code(500);
        echo json_encode([
            "status"  => "error",
            "message" => "Database connection failed"
        ]);
        exit();
    }

    // Get input (supports JSON, POST, and REQUEST)
    $raw   = file_get_contents("php://input");
    $input = json_decode($raw, true);

    if (!is_array($input)) {
        $input = [];
    }

    $email = '';
    if (!empty($_POST['email'])) {
        $email = trim($_POST['email']);
    } elseif (!empty($input['email'])) {
        $email = trim($input['email']);
    } elseif (!empty($_REQUEST['email'])) {
        $email = trim($_REQUEST['email']);
    }

    $password = '';
    if (!empty($_POST['password'])) {
        $password = trim($_POST['password']);
    } elseif (!empty($input['password'])) {
        $password = trim($input['password']);
    } elseif (!empty($_REQUEST['password'])) {
        $password = trim($_REQUEST['password']);
    }

    // ==================== DEMO ACCOUNT ====================
    if (($email === 'demo@ionox.in' || $email === 'demo') && $password === 'demo') {
        echo json_encode([
            "status"  => "success",
            "message" => "Login successful",
            "student" => [
                "id"         => 999,
                "student_id" => "999",
                "name"       => "Demo Student",
                "email"      => "demo@ionox.in",
                "phone"      => null,
                "batch_id"   => null,
                "ca_id"      => null,
                "role"       => "student"
            ]
        ]);
        exit();
    }

    // ==================== VALIDATION ====================
    if ($email === '' || $password === '') {
        http_response_code(400);
        echo json_encode([
            "status"  => "error",
            "message" => "Email and password are required"
        ]);
        exit();
    }

    // ==================== DATABASE QUERY ====================
    // Supports email OR student_id
    $stmt = $conn->prepare("SELECT id, student_id, name, email, phone, batch_id, ca_id, password
                            FROM students
                            WHERE LOWER(email) = LOWER(?) OR LOWER(student_id) = LOWER(?)
                            LIMIT 1");

    if (!$stmt) {
        http_response_code(500);
        echo json_encode([
            "status"  => "error",
            "message" => "Prepare failed: " . $conn->error
        ]);
        exit();
    }

    $stmt->bind_param("ss", $email, $email);
    $stmt->execute();
    $result  = $stmt->get_result();
    $student = $result->fetch_assoc();
    $stmt->close();

    if (!$student) {
        http_response_code(401);
        echo json_encode([
            "status"  => "error",
            "message" => "Invalid email or password"
        ]);
        exit();
    }

    // ==================== PASSWORD VERIFY ====================
    $db_password = $student['password'];
    $is_password_valid = password_verify($password, $db_password)
                         || ($password === $db_password)
                         || (md5($password) === $db_password);

    if ($is_password_valid) {

        echo json_encode([
            "status"  => "success",
            "message" => "Login successful",
            "student" => [
                "id"         => (int) $student['id'],
                "student_id" => $student['student_id'] ?? (string) $student['id'],
                "name"       => $student['name'],
                "email"      => $student['email'],
                "phone"      => $student['phone'] ?? null,
                "batch_id"   => $student['batch_id'] ?? null,
                "ca_id"      => $student['ca_id'] ?? null,
                "role"       => "student"
            ]
        ]);

    } else {
        http_response_code(401);
        echo json_encode([
            "status"  => "error",
            "message" => "Invalid email or password"
        ]);
    }

} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode([
        "status"  => "error",
        "message" => "Exception: " . $e->getMessage()
    ]);
}
?>
