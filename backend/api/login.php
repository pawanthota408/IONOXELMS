<?php
// ===================================================
// IONOXE TECH SOLUTIONS - LMS REST API ENDPOINT
// Path: api/login.php
// Place this file inside your server's api/ directory.
// ===================================================

// CORS & Headers
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

// Include your main database configuration
if (file_exists("../config.php")) {
    require_once "../config.php";
} elseif (file_exists("config.php")) {
    require_once "config.php";
}

// Parse input payload (JSON or Form URL-Encoded)
$input_data = json_decode(file_get_contents("php://input"), true);

$email = "";
$password = "";
$reset_email = "";

if (isset($input_data['email'])) {
    $email = trim($input_data['email']);
} elseif (isset($_POST['email'])) {
    $email = trim($_POST['email']);
}

if (isset($input_data['password'])) {
    $password = trim($input_data['password']);
} elseif (isset($_POST['password'])) {
    $password = trim($_POST['password']);
}

if (isset($input_data['reset_email'])) {
    $reset_email = trim($input_data['reset_email']);
} elseif (isset($_POST['reset_email'])) {
    $reset_email = trim($_POST['reset_email']);
}

// ---------------------------------------------------
// 1. PASSWORD RESET REQUEST
// ---------------------------------------------------
if (!empty($reset_email)) {
    if (filter_var($reset_email, FILTER_VALIDATE_EMAIL)) {
        http_response_code(200);
        echo json_encode([
            "status" => "success",
            "message" => "If this email exists, a reset link has been sent."
        ]);
    } else {
        http_response_code(400);
        echo json_encode([
            "status" => "error",
            "message" => "Please enter a valid email address."
        ]);
    }
    exit();
}

// ---------------------------------------------------
// 2. STUDENT LOGIN AUTHENTICATION
// ---------------------------------------------------
if (empty($email) || empty($password)) {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "Email and password are required."
    ]);
    exit();
}

$login_success = false;
$student_data = null;

// Database Check against 'students' table
if (isset($conn) && $conn) {
    $stmt = $conn->prepare("SELECT id, name, email, password FROM students WHERE email=? LIMIT 1");
    if ($stmt) {
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $result = $stmt->get_result();
        $student = $result->fetch_assoc();
        $stmt->close();

        if ($student && password_verify($password, $student['password'])) {
            $login_success = true;
            $student_data = [
                "id" => (int)$student['id'],
                "name" => $student['name'],
                "email" => $student['email'],
                "role" => "student"
            ];
        }
    }
}

// Demo Account Check (for testing)
if (!$login_success && $email === 'demo@ionox.in' && $password === 'demo') {
    $login_success = true;
    $student_data = [
        "id" => 999,
        "name" => "Demo Student",
        "email" => "demo@ionox.in",
        "role" => "student"
    ];
}

if ($login_success && $student_data !== null) {
    http_response_code(200);
    echo json_encode([
        "status" => "success",
        "message" => "Login successful",
        "student" => $student_data
    ]);
} else {
    http_response_code(401);
    echo json_encode([
        "status" => "error",
        "message" => "Invalid email or password."
    ]);
}
?>
