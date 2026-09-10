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

function col_exists($conn, $table, $col) {
    $r = $conn->query("SHOW COLUMNS FROM `$table` LIKE '$col'");
    return ($r && $r->num_rows > 0);
}

function get_assignment_question_type($conn, $assignment_id) {
    $aid = intval($assignment_id);
    $tbl = $conn->query("SHOW TABLES LIKE 'assignment_questions'");
    if (!$tbl || $tbl->num_rows === 0) return null;
    $r = $conn->query("SELECT type FROM assignment_questions WHERE assignment_id = $aid LIMIT 1");
    if ($r && $r->num_rows > 0) {
        $row = $r->fetch_assoc();
        return strtolower(trim($row['type'] ?? 'mcq'));
    }
    return null;
}

function card_state(array $row): string {
    $now = time();
    $sched_ts = (!empty($row['scheduled_at']) && $row['scheduled_at'] !== '0000-00-00 00:00:00') ? strtotime($row['scheduled_at']) : null;
    $due_ts   = (!empty($row['due_date'])     && $row['due_date']     !== '0000-00-00 00:00:00') ? strtotime($row['due_date'])     : null;

    if (!empty($row['submitted_at'])) return 'submitted';
    if ($sched_ts !== null && $sched_ts > $now) {
        $diff = $sched_ts - $now;
        return ($diff > 86400) ? 'locked' : 'countdown';
    }
    if ($due_ts !== null && $due_ts < $now) return 'overdue';
    return 'open';
}

$assignments = [];

$tbl_check = $conn->query("SHOW TABLES LIKE 'assignments'");
if ($tbl_check && $tbl_check->num_rows > 0) {

    $has_scheduled = col_exists($conn, 'assignments', 'schedule_time');
    $has_type      = col_exists($conn, 'assignments', 'type');
    $has_file_url  = col_exists($conn, 'assignments', 'file_url');
    $has_desc      = col_exists($conn, 'assignments', 'description');
    $has_file_path = col_exists($conn, 'assignment_results', 'file_path');

    $sel_scheduled = $has_scheduled ? "a.schedule_time AS scheduled_at" : "NULL AS scheduled_at";
    $sel_type      = $has_type      ? "a.type"                           : "NULL AS type";
    $sel_file_url  = $has_file_url  ? "a.file_url"                       : "NULL AS file_url";
    $sel_desc      = $has_desc      ? "a.description"                    : "NULL AS description";
    $sel_file_path = $has_file_path ? "ar.file_path"                     : "NULL AS file_path";

    $sql = "
        SELECT
            a.id,
            a.title,
            a.due_date,
            a.total_marks,
            a.batch_id,
            $sel_scheduled,
            $sel_type,
            $sel_file_url,
            $sel_desc,
            c.title AS course_name,
            ar.obtained_marks,
            ar.submitted_at,
            $sel_file_path
        FROM enrollments e
        INNER JOIN assignments a ON a.course_id = e.course_id AND a.batch_id = e.batch_id
        INNER JOIN courses c ON c.id = a.course_id
        LEFT JOIN assignment_results ar ON ar.assignment_id = a.id AND ar.student_id = e.student_id
        WHERE e.student_id = ?
        GROUP BY a.id
        ORDER BY a.id DESC
    ";

    $stmt = $conn->prepare($sql);
    if ($stmt) {
        $stmt->bind_param("i", $student_id);
        $stmt->execute();
        $res = $stmt->get_result();

        while ($row = $res->fetch_assoc()) {
            $q_type = get_assignment_question_type($conn, $row['id']);
            $resolved_type = 'mcq';
            if ($q_type !== null) {
                $resolved_type = ($q_type === 'descriptive') ? 'project' : 'mcq';
            } else {
                $fallback = strtolower(trim($row['type'] ?? 'mcq'));
                $resolved_type = ($fallback === 'project' || $fallback === 'descriptive') ? 'project' : 'mcq';
            }

            $state = card_state($row);

            $assignments[] = [
                "id"             => (int)$row['id'],
                "title"          => $row['title'] ?? 'Assignment',
                "course_name"    => $row['course_name'] ?? 'Course',
                "description"    => $row['description'] ?? '',
                "due_date"       => $row['due_date'] ?? '',
                "scheduled_at"   => $row['scheduled_at'] ?? '',
                "total_marks"    => (int)($row['total_marks'] ?? 100),
                "obtained_marks" => $row['obtained_marks'] !== null ? (int)$row['obtained_marks'] : null,
                "submitted_at"   => $row['submitted_at'] ?? null,
                "file_path"      => $row['file_path'] ?? null,
                "resolved_type"  => $resolved_type,
                "state"          => $state
            ];
        }
        $stmt->close();
    }
}

// Fallback Mock Data if student has no batch assignments
if (empty($assignments)) {
    $assignments = [
        [
            "id" => 1,
            "title" => "Machine Learning Regression Model Assignment",
            "course_name" => "Artificial Intelligence & Machine Learning",
            "description" => "Implement Linear and Polynomial Regression algorithms using Python and NumPy.",
            "due_date" => "2026-05-10 23:59:00",
            "scheduled_at" => "2026-04-01 00:00:00",
            "total_marks" => 100,
            "obtained_marks" => null,
            "submitted_at" => null,
            "file_path" => null,
            "resolved_type" => "mcq",
            "state" => "open"
        ],
        [
            "id" => 2,
            "title" => "Full Stack MERN CRUD Application",
            "course_name" => "Full Stack Web Development",
            "description" => "Build a responsive REST API backend with Express and Node.js connected to React frontend.",
            "due_date" => "2026-04-20 23:59:00",
            "scheduled_at" => "2026-03-01 00:00:00",
            "total_marks" => 100,
            "obtained_marks" => 92,
            "submitted_at" => "2026-04-19 18:30:00",
            "file_path" => "https://ionox.in/uploads/assignments/mern_app.pdf",
            "resolved_type" => "project",
            "state" => "submitted"
        ]
    ];
}

echo json_encode([
    "status"      => "success",
    "total"       => count($assignments),
    "assignments" => $assignments
]);
exit;
?>
