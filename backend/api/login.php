<?php
// ===================================================
// IONOXE TECH SOLUTIONS - LMS REST API ENDPOINT
// Path: api/login.php  (or /lms/api/login)
// Fully synced with website & handles all password hash types
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

// Database config
$conn_handle = null;
if (file_exists("../config.php")) {
    require_once "../config.php";
} elseif (file_exists("config.php")) {
    require_once "config.php";
}

// Detect database handle name ($conn, $db, $mysqli)
if (isset($conn)) {
    $conn_handle = $conn;
} elseif (isset($db)) {
    $conn_handle = $db;
} elseif (isset($mysqli)) {
    $conn_handle = $mysqli;
}

// PHPMailer (same as website)
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

if (file_exists(__DIR__ . '/../phpmailer/src/Exception.php')) {
    require_once __DIR__ . '/../phpmailer/src/Exception.php';
    require_once __DIR__ . '/../phpmailer/src/PHPMailer.php';
    require_once __DIR__ . '/../phpmailer/src/SMTP.php';
}

// ---------- Robust Input Parsing (JSON + Form) ----------
$raw_input  = file_get_contents("php://input");
$input_data = json_decode($raw_input, true);

if (!is_array($input_data)) {
    $input_data = [];
}

$email       = trim($input_data['email']       ?? $_POST['email']       ?? '');
$password    = trim($input_data['password']    ?? $_POST['password']    ?? '');
$reset_email = trim($input_data['reset_email'] ?? $_POST['reset_email'] ?? '');

// ===================================================
// 1. PASSWORD RESET REQUEST
// ===================================================
if (!empty($reset_email)) {

    if (!filter_var($reset_email, FILTER_VALIDATE_EMAIL)) {
        http_response_code(400);
        echo json_encode([
            "status"  => "error",
            "message" => "Please enter a valid email address."
        ]);
        exit();
    }

    $message = "If this email exists, a reset link has been sent.";

    if ($conn_handle) {
        $stmt = $conn_handle->prepare("SELECT id, name FROM students WHERE LOWER(email) = LOWER(?) LIMIT 1");
        if ($stmt) {
            $stmt->bind_param("s", $reset_email);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            if ($user && class_exists('PHPMailer\PHPMailer\PHPMailer')) {
                // Generate token
                $token       = bin2hex(random_bytes(16));
                $hashedToken = password_hash($token, PASSWORD_DEFAULT);
                $expires     = date("Y-m-d H:i:s", strtotime('+1 hour'));

                $stmt = $conn_handle->prepare("UPDATE students SET password_reset_token = ?, password_reset_expires = ? WHERE id = ?");
                $stmt->bind_param("ssi", $hashedToken, $expires, $user['id']);
                $stmt->execute();
                $stmt->close();

                // Reset link
                $reset_link = "https://ionox.in/lms/reset_password.php?token=" . $token;

                // Send email
                $mail = new PHPMailer(true);
                try {
                    $mail->isSMTP();
                    $mail->Host       = 'smtp.gmail.com';
                    $mail->SMTPAuth   = true;
                    $mail->Username   = 'ionoxetech@gmail.com';
                    $mail->Password   = 'hsewiqwlatefgcpc';
                    $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
                    $mail->Port       = 587;

                    $mail->setFrom('ionoxetech@gmail.com', 'IONOXE LMS');
                    $mail->addAddress($reset_email, $user['name']);
                    $mail->isHTML(true);
                    $mail->Subject = "Password Reset — Ionoxe LMS";
                    $mail->Body    = "
                        Hello {$user['name']},<br><br>
                        Click the link below to reset your password (valid for 1 hour):<br><br>
                        <a href='$reset_link'>$reset_link</a><br><br>
                        Regards,<br>
                        <b>IONOXE TECH SOLUTIONS</b>
                    ";

                    $mail->send();
                } catch (Exception $e) {
                    error_log("API Email Error: " . $mail->ErrorInfo);
                }
            }
        }
    }

    http_response_code(200);
    echo json_encode([
        "status"  => "success",
        "message" => $message
    ]);
    exit();
}

// ===================================================
// 2. STUDENT LOGIN
// ===================================================
if (empty($email) || empty($password)) {
    http_response_code(400);
    echo json_encode([
        "status"  => "error",
        "message" => "Email/Student ID and password are required."
    ]);
    exit();
}

$login_success = false;
$student_data  = null;

// Database authentication (matches email OR student_id OR phone)
if ($conn_handle) {
    $stmt = $conn_handle->prepare("
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
        WHERE LOWER(email) = LOWER(?) OR LOWER(student_id) = LOWER(?) OR phone = ?
        LIMIT 1
    ");

    if ($stmt) {
        $stmt->bind_param("sss", $email, $email, $email);
        $stmt->execute();
        $result  = $stmt->get_result();
        $student = $result->fetch_assoc();
        $stmt->close();

        if ($student) {
            $db_password = $student['password'];

            // Robust Password Check: supports password_verify(), plain text, MD5, and SHA1
            $is_password_valid = password_verify($password, $db_password)
                                 || ($password === $db_password)
                                 || (md5($password) === $db_password)
                                 || (sha1($password) === $db_password);

            if ($is_password_valid) {
                $login_success = true;

                $student_data = [
                    "id"         => (int)$student['id'],
                    "student_id" => $student['student_id'] ?? null,
                    "name"       => $student['name'],
                    "email"      => $student['email'],
                    "phone"      => $student['phone'] ?? null,
                    "batch_id"   => $student['batch_id'] ?? null,
                    "ca_id"      => $student['ca_id'] ?? null,
                    "role"       => "student"
                ];
            }
        }
    }
}

// Demo account
if (!$login_success && ($email === 'demo@ionox.in' || $email === 'demo') && $password === 'demo') {
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

// Final Response
if ($login_success && $student_data !== null) {

    // Create session
    $_SESSION['student_id'] = $student_data['id'];
    $_SESSION['name']       = $student_data['name'];
    $_SESSION['role']       = 'student';
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
        "message" => "Invalid email/student ID or password."
    ]);
}
?>
