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

$student_id = isset($_GET['student_id']) ? intval($_GET['student_id']) : (isset($_POST['student_id']) ? intval($_POST['student_id']) : 0);

if ($student_id <= 0) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Student ID required"]);
    exit;
}

// Fetch Attendance Records
$records = [];
$stmt = $conn->prepare("
    SELECT a.id, a.date, a.status, c.title AS course_name
    FROM attendance a
    JOIN courses c ON a.course_id = c.id
    WHERE a.student_id = ?
    ORDER BY a.date DESC
");
if ($stmt) {
    $stmt->bind_param("i", $student_id);
    $stmt->execute();
    $res = $stmt->get_result();
    while ($row = $res->fetch_assoc()) {
        $ts = strtotime($row['date']);
        $records[] = [
            "id"          => (int)$row['id'],
            "course_name" => $row['course_name'] ?? 'Course',
            "date"        => date('d M Y', $ts),
            "day"         => date('l', $ts),
            "status"      => strtolower(trim($row['status'] ?? 'present'))
        ];
    }
    $stmt->close();
}

// Total Classes
$t_stmt = $conn->prepare("
    SELECT COUNT(ts.id) AS total
    FROM training_schedules ts
    JOIN enrollments e ON ts.course_id = e.course_id
    WHERE e.student_id = ?
");
$total_classes = 0;
if ($t_stmt) {
    $t_stmt->bind_param("i", $student_id);
    $t_stmt->execute();
    $total_classes = (int)($t_stmt->get_result()->fetch_assoc()['total'] ?? 0);
    $t_stmt->close();
}

if ($total_classes < count($records)) {
    $total_classes = count($records);
}

// Attended Classes
$p_stmt = $conn->prepare("SELECT COUNT(*) AS present FROM attendance WHERE student_id=? AND status='present'");
$attended_classes = 0;
if ($p_stmt) {
    $p_stmt->bind_param("i", $student_id);
    $p_stmt->execute();
    $attended_classes = (int)($p_stmt->get_result()->fetch_assoc()['present'] ?? 0);
    $p_stmt->close();
}

$absent_classes = max(0, $total_classes - $attended_classes);
$attendance_percentage = $total_classes > 0 ? round(($attended_classes / $total_classes) * 100) : 100;
$is_compliant = $attendance_percentage >= 75;

echo json_encode([
    "status"                => "success",
    "total_classes"         => $total_classes,
    "attended_classes"      => $attended_classes,
    "absent_classes"        => $absent_classes,
    "attendance_percentage" => $attendance_percentage,
    "is_compliant"          => $is_compliant,
    "records"               => $records
]);
exit;
?>
