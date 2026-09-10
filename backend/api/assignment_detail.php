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

$student_id    = isset($_GET['student_id']) ? intval($_GET['student_id']) : (isset($_POST['student_id']) ? intval($_POST['student_id']) : 0);
$assignment_id = isset($_GET['assignment_id']) ? intval($_GET['assignment_id']) : (isset($_POST['assignment_id']) ? intval($_POST['assignment_id']) : (isset($_GET['id']) ? intval($_GET['id']) : 0));

if ($student_id <= 0 || $assignment_id <= 0) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "student_id and assignment_id required"]);
    exit;
}

// Fetch assignment details + result
$stmt = $conn->prepare("
    SELECT a.*, c.title AS course_name,
           r.obtained_marks, r.total_marks AS result_total,
           r.submitted_at, r.answers, r.file_path,
           r.per_question_marks
    FROM assignments a
    JOIN courses c ON c.id = a.course_id
    LEFT JOIN assignment_results r ON r.assignment_id = a.id AND r.student_id = ?
    WHERE a.id = ?
    LIMIT 1
");

$assignment = null;
if ($stmt) {
    $stmt->bind_param("ii", $student_id, $assignment_id);
    $stmt->execute();
    $assignment = $stmt->get_result()->fetch_assoc();
    $stmt->close();
}

if (!$assignment) {
    http_response_code(44);
    echo json_encode(["status" => "error", "message" => "Assignment not found"]);
    exit;
}

$submitted       = !empty($assignment['submitted_at']);
$student_answers = json_decode($assignment['answers'] ?? '[]', true) ?: [];
$pq_marks        = json_decode($assignment['per_question_marks'] ?? '{}', true) ?: [];
$submitted_file  = $assignment['file_path'] ?? null;

// Detect type
$resolved_type = 'mcq';
$type_check = $conn->prepare("SELECT type FROM assignment_questions WHERE assignment_id = ? LIMIT 1");
if ($type_check) {
    $type_check->bind_param("i", $assignment_id);
    $type_check->execute();
    $type_row = $type_check->get_result()->fetch_assoc();
    $type_check->close();
    if ($type_row) {
        $resolved_type = strtolower(trim($type_row['type'])) === 'descriptive' ? 'descriptive' : 'mcq';
    }
}

// Fetch questions
$questions_raw = [];
$q_stmt = $conn->prepare("SELECT * FROM assignment_questions WHERE assignment_id = ? ORDER BY id ASC");
if ($q_stmt) {
    $q_stmt->bind_param("i", $assignment_id);
    $q_stmt->execute();
    $q_res = $q_stmt->get_result();
    while ($r = $q_res->fetch_assoc()) {
        $questions_raw[] = $r;
    }
    $q_stmt->close();
}

// Calculate Percentage
$total_marks_val = !empty($assignment['result_total']) ? (int)$assignment['result_total'] : (int)$assignment['total_marks'];
$obtained_marks_val = $assignment['obtained_marks'] !== null ? (int)$assignment['obtained_marks'] : null;
$percentage = ($submitted && $total_marks_val > 0 && $obtained_marks_val !== null) ? round(($obtained_marks_val / $total_marks_val) * 100) : null;

// MCQ breakdown
$correct_count = 0;
$wrong_count = 0;
$skipped_count = 0;

$questions_output = [];

foreach ($questions_raw as $q) {
    $qid     = (int)$q['id'];
    $type    = strtolower(trim($q['type'] ?? 'mcq'));
    $options = json_decode($q['options'] ?? '[]', true) ?: [];
    $correct = trim((string)($q['correct_option'] ?? ''));
    $ans     = $submitted ? ($student_answers[$qid] ?? null) : null;

    $awarded = array_key_exists($qid, $pq_marks) ? (float)$pq_marks[$qid] : null;

    $q_state = 'none';
    if ($submitted) {
        if ($type === 'mcq') {
            if ($ans === null || $ans === '') {
                $q_state = 'skipped';
                $skipped_count++;
            } elseif (strcasecmp($ans, $correct) === 0) {
                $q_state = 'correct';
                $correct_count++;
            } else {
                $q_state = 'wrong';
                $wrong_count++;
            }
        } else {
            // Descriptive
            if ($awarded !== null) {
                $q_state = ($awarded > 0) ? 'correct' : 'wrong';
            } elseif ($ans === null || $ans === '') {
                $q_state = 'skipped';
                $skipped_count++;
            } else {
                $q_state = 'pending';
            }
        }
    }

    $questions_output[] = [
        "id"             => $qid,
        "question"       => $q['question'] ?? '',
        "type"           => $type,
        "marks"          => (int)($q['marks'] ?? 10),
        "options"        => $options,
        "correct_option" => $correct,
        "student_answer" => $ans,
        "awarded_marks"  => $awarded,
        "state"          => $q_state
    ];
}

echo json_encode([
    "status"         => "success",
    "assignment_id"  => (int)$assignment['id'],
    "title"          => $assignment['title'] ?? '',
    "course_name"    => $assignment['course_name'] ?? '',
    "total_marks"    => $total_marks_val,
    "obtained_marks" => $obtained_marks_val,
    "percentage"     => $percentage,
    "submitted_at"   => $assignment['submitted_at'] ?? null,
    "file_path"      => $submitted_file,
    "resolved_type"  => $resolved_type,
    "submitted"      => $submitted,
    "correct_count"  => $correct_count,
    "wrong_count"    => $wrong_count,
    "skipped_count"  => $skipped_count,
    "questions"      => $questions_output
]);
exit;
?>
