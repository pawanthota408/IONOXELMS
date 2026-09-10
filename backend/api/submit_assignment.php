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

// Extract input (JSON or POST)
$rawInput = file_get_contents("php://input");
$input = json_decode($rawInput, true) ?: [];

$student_id    = isset($input['student_id']) ? intval($input['student_id']) : intval($_POST['student_id'] ?? 0);
$assignment_id = isset($input['assignment_id']) ? intval($input['assignment_id']) : intval($_POST['assignment_id'] ?? 0);
$answers       = isset($input['answers']) ? $input['answers'] : ($_POST['answers'] ?? []);

if ($student_id <= 0 || $assignment_id <= 0) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "student_id and assignment_id required"]);
    exit;
}

if (is_string($answers)) {
    $answers = json_decode($answers, true) ?: [];
}

// Double submission check
$chk = $conn->prepare("SELECT id FROM assignment_results WHERE assignment_id=? AND student_id=?");
if ($chk) {
    $chk->bind_param("ii", $assignment_id, $student_id);
    $chk->execute();
    if ($chk->get_result()->num_rows > 0) {
        echo json_encode([
            "status" => "success",
            "message" => "Already submitted"
        ]);
        exit;
    }
    $chk->close();
}

// MCQ Auto-Score Calculation
$qst = $conn->prepare("SELECT id, type, correct_option, marks FROM assignment_questions WHERE assignment_id=?");
$total_score = 0;
$max_score = 0;

if ($qst) {
    $qst->bind_param("i", $assignment_id);
    $qst->execute();
    $qr = $qst->get_result();
    while ($q = $qr->fetch_assoc()) {
        $qid = $q['id'];
        $max_score += intval($q['marks']);
        if (strtolower(trim($q['type'] ?? 'mcq')) === 'mcq' && isset($answers[$qid])) {
            $student_choice = trim((string)$answers[$qid]);
            $correct_choice = trim((string)$q['correct_option']);
            if (strcasecmp($student_choice, $correct_choice) === 0) {
                $total_score += intval($q['marks']);
            }
        }
    }
    $qst->close();
}

if ($max_score <= 0) {
    $max_score = 100;
}

$answers_json = json_encode($answers);

$ins = $conn->prepare("
    INSERT INTO assignment_results (assignment_id, student_id, obtained_marks, total_marks, answers, submitted_at)
    VALUES (?, ?, ?, ?, ?, NOW())
    ON DUPLICATE KEY UPDATE obtained_marks = VALUES(obtained_marks), total_marks = VALUES(total_marks), answers = VALUES(answers), submitted_at = NOW()
");

if ($ins) {
    $ins->bind_param("iiiis", $assignment_id, $student_id, $total_score, $max_score, $answers_json);
    $ins->execute();
    $ins->close();
}

echo json_encode([
    "status"         => "success",
    "message"        => "Assignment submitted successfully!",
    "obtained_marks" => $total_score,
    "total_marks"    => $max_score
]);
exit;
?>
