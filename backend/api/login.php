<?php
// ===================================================
// IONOXE TECH SOLUTIONS - LMS REST API ENDPOINT
// Path: api/login.php  (or /lms/api/login)
// ===================================================

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

define('ACCESS', true);
session_start();

// Include database config
if (file_exists("../config.php")) {
    require_once "../config.php";
} elseif (file_exists("config.php")) {
    require_once "config.php";
}

// ---------- Robust Input Parsing ----------
$raw_input = file_get_contents("php://input");
$input_data = json_decode($raw_input, true);

if (!is_array($input_data)) {
    $input_data = [];
}

$email       = trim($input_data['email']       ?? $_POST['email']       ?? '');
$password    = trim($input_data['password']    ?? $_POST['password']    ?? '');
$reset_email = trim($input_data['reset_email'] ?? $_POST['reset_email'] ?? '');

// ---------- 1. Password Reset Request ----------
if (!empty($reset_email)) {
    if (filter_var($reset_email, FILTER_VALIDATE_EMAIL)) {
        // TODO: Generate token + send email here
        http_response_code(200);
        echo json_encode([
            "status"  => "success",
            "message" => "If this email exists, a reset link has been sent."
        ]);
    } else {
        http_response_code(400);
        echo json_encode([
            "status"  => "error",
            "message" => "Please enter a valid email address."
        ]);
    }
    exit();
}

// ---------- 2. Login Validation ----------
if (empty($email) || empty($password)) {
    http_response_code(400);
    echo json_encode([
        "status"  => "error",
        "message" => "Email and password are required."
    ]);
    exit();
}

$login_success = false;
$student_data  = null;

// ---------- Database Login ----------
if (isset($conn) && $conn) {
    $stmt = $conn->prepare("
        SELECT
            id,
            student_id,
            name,
            email,
            password,
            phone,
            batch_id,
            ca_id
        FROM students
        WHERE email = ?
        LIMIT 1
    ");

    if ($stmt) {
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $result  = $stmt->get_result();
        $student = $result->fetch_assoc();
        $stmt->close();

        if ($student && password_verify($password, $student['password'])) {
            $login_success = true;

            $student_data = [
                "id"          => (int)$student['id'],
                "student_id"  => $student['student_id'],
                "name"        => $student['name'],
                "email"       => $student['email'],
                "phone"       => $student['phone'] ?? null,
                "batch_id"    => $student['batch_id'] ?? null,
                "ca_id"       => $student['ca_id'] ?? null,
                "role"        => "student"
            ];
        }
    }
}

// ---------- Demo Account (for testing) ----------
if (!$login_success && $email === 'demo@ionox.in' && $password === 'demo') {
    $login_success = true;
    $student_data = [
        "id"         => 999,
        "student_id" => "DEMO999",
        "name"       => "Demo Student",
        "email"      => "demo@ionox.in",
        "phone"      => null,
        "batch_id"   => null,
        "ca_id"      => null,
        "role"       => "student"
    ];
}

// ---------- Final Response ----------
if ($login_success && $student_data !== null) {
    // Optional: Create session
    $_SESSION['student_id'] = $student_data['id'];
    $_SESSION['student']    = $student_data;

    http_response_code(200);
    echo json_encode([
        "status"  => "success",
        "message" => "Login successful",
        "student" => $student_data
    ]);
} else {
    http_response_code(401);
    echo json_encode([
        "status"  => "error",
        "message" => "Invalid email or password."
    ]);
}
?>
