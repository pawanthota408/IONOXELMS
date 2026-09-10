<?php

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit;
}

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

foreach ($configPaths as $configPath) {
    if (file_exists($configPath)) {
        require_once $configPath;
        $configLoaded = true;
        break;
    }
}

if (!$configLoaded) {
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "Database configuration file not found",
        "student" => null
    ]);
    exit;
}

/*
|--------------------------------------------------------------------------
| DATABASE CHECK
|--------------------------------------------------------------------------
*/

if (!isset($conn) || !($conn instanceof mysqli)) {
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "Database connection not available",
        "student" => null
    ]);
    exit;
}

if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "Database connection failed",
        "student" => null
    ]);
    exit;
}

/*
|--------------------------------------------------------------------------
| READ INPUT (JSON + POST + REQUEST)
|--------------------------------------------------------------------------
*/

$rawInput = file_get_contents("php://input");
$jsonData = [];

if (!empty($rawInput)) {
    $decoded = json_decode($rawInput, true);
    if (is_array($decoded)) {
        $jsonData = $decoded;
    }
}

$email = trim(
    $jsonData["email"]
    ?? $_POST["email"]
    ?? $_REQUEST["email"]
    ?? ""
);

$password = trim(
    $jsonData["password"]
    ?? $_POST["password"]
    ?? $_REQUEST["password"]
    ?? ""
);

/*
|--------------------------------------------------------------------------
| BASIC VALIDATION
|--------------------------------------------------------------------------
*/

if ($email === "" || $password === "") {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "Email and password are required",
        "student" => null
    ]);
    exit;
}

/*
|--------------------------------------------------------------------------
| DEMO LOGIN
|--------------------------------------------------------------------------
*/

if (strtolower($email) === "demo@ionox.in" || strtolower($email) === "demo") {
    if ($password === "demo") {
        echo json_encode([
            "status" => "success",
            "message" => "Login successful",
            "student" => [
                "id" => 999,
                "student_id" => "999",
                "name" => "Demo Student",
                "email" => "demo@ionox.in",
                "phone" => null,
                "batch_id" => null,
                "ca_id" => null,
                "created_at" => null,
                "role" => "student"
            ]
        ]);
        exit;
    }
}

/*
|--------------------------------------------------------------------------
| FIND STUDENT (by email OR student_id OR phone)
|--------------------------------------------------------------------------
*/

$stmt = $conn->prepare("
    SELECT
        id,
        student_id,
        name,
        email,
        phone,
        batch_id,
        ca_id,
        password,
        created_at
    FROM students
    WHERE LOWER(email) = LOWER(?)
       OR LOWER(student_id) = LOWER(?)
       OR phone = ?
    LIMIT 1
");

if (!$stmt) {
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "Failed to prepare database query: " . $conn->error,
        "student" => null
    ]);
    exit;
}

$stmt->bind_param("sss", $email, $email, $email);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();
$stmt->close();

if (!$user) {
    http_response_code(401);
    echo json_encode([
        "status" => "error",
        "message" => "Invalid email or password",
        "student" => null
    ]);
    exit;
}

/*
|--------------------------------------------------------------------------
| PASSWORD VERIFY (supports password_verify, plain text, and md5)
|--------------------------------------------------------------------------
*/

$dbPassword = (string)($user["password"] ?? "");
$passwordValid = false;

if ($dbPassword !== "" && password_verify($password, $dbPassword)) {
    $passwordValid = true;
}

if (!$passwordValid && hash_equals($dbPassword, $password)) {
    $passwordValid = true;
}

if (!$passwordValid && $dbPassword !== "" && md5($password) === strtolower($dbPassword)) {
    $passwordValid = true;
}

if (!$passwordValid) {
    http_response_code(401);
    echo json_encode([
        "status" => "error",
        "message" => "Invalid email or password",
        "student" => null
    ]);
    exit;
}

/*
|--------------------------------------------------------------------------
| SUCCESS RESPONSE
|--------------------------------------------------------------------------
*/

echo json_encode([
    "status" => "success",
    "message" => "Login successful",
    "student" => [
        "id" => (int)$user["id"],
        "student_id" => isset($user["student_id"]) ? (string)$user["student_id"] : null,
        "name" => $user["name"] ?? null,
        "email" => $user["email"] ?? null,
        "phone" => $user["phone"] ?? null,
        "batch_id" => isset($user["batch_id"]) ? (string)$user["batch_id"] : null,
        "ca_id" => isset($user["ca_id"]) ? (string)$user["ca_id"] : null,
        "created_at" => $user["created_at"] ?? null,
        "role" => "student"
    ]
]);

exit;
?>
