<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit;
}

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

$student_id = isset($_GET['student_id']) ? intval($_GET['student_id']) : (isset($_POST['student_id']) ? intval($_POST['student_id']) : 0);

// Student verification
$student_name = "Student";
$student_code = "";
if ($student_id > 0) {
    $s_stmt = $conn->prepare("SELECT name, student_id FROM students WHERE id = ? LIMIT 1");
    if ($s_stmt) {
        $s_stmt->bind_param("i", $student_id);
        $s_stmt->execute();
        $s_res = $s_stmt->get_result()->fetch_assoc();
        if ($s_res) {
            $student_name = $s_res['name'] ?? 'Student';
            $student_code = $s_res['student_id'] ?? '';
        }
        $s_stmt->close();
    }
}

// Fetch enrolled courses
$courses_arr = [];
if ($student_id > 0) {
    $c_stmt = $conn->prepare("
        SELECT c.id, c.title, c.description, c.thumbnail
        FROM courses c
        JOIN enrollments e ON c.id = e.course_id
        WHERE e.student_id = ?
        ORDER BY c.title ASC
    ");
    if ($c_stmt) {
        $c_stmt->bind_param("i", $student_id);
        $c_stmt->execute();
        $c_res = $c_stmt->get_result();
        while ($row = $c_res->fetch_assoc()) {
            $courses_arr[] = [
                "id"          => (string)$row['id'],
                "title"       => $row['title'] ?? '',
                "description" => $row['description'] ?? '',
                "thumbnail"   => $row['thumbnail'] ?? ''
            ];
        }
        $c_stmt->close();
    }
}

// Fallback: If no enrollments, fetch all available courses so student can browse
if (empty($courses_arr)) {
    $all_res = $conn->query("SELECT id, title, description, thumbnail FROM courses ORDER BY title ASC LIMIT 10");
    if ($all_res) {
        while ($row = $all_res->fetch_assoc()) {
            $courses_arr[] = [
                "id"          => (string)$row['id'],
                "title"       => $row['title'] ?? '',
                "description" => $row['description'] ?? '',
                "thumbnail"   => $row['thumbnail'] ?? ''
            ];
        }
    }
}

// Pending assignments count
$assignments_pending = 0;
$assign_table = $conn->query("SHOW TABLES LIKE 'assignments'");
if ($assign_table && $assign_table->num_rows > 0) {
    $a_stmt = $conn->prepare("
        SELECT COUNT(*) FROM assignments a
        JOIN enrollments e ON a.course_id = e.course_id
        WHERE e.student_id = ? AND a.due_date > NOW()
    ");
    if ($a_stmt) {
        $a_stmt->bind_param("i", $student_id);
        $a_stmt->execute();
        $assignments_pending = (int)$a_stmt->get_result()->fetch_row()[0];
        $a_stmt->close();
    }
}

echo json_encode([
    "status"              => "success",
    "student_name"        => $student_name,
    "student_id"          => $student_code,
    "total_courses"       => count($courses_arr),
    "assignments_pending" => $assignments_pending,
    "courses"             => $courses_arr
]);
exit;
?>
