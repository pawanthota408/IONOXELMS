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
    "is_expired"     => $is_expired,
    "correct_count"  => $correct_count,
    "wrong_count"    => $wrong_count,
    "skipped_count"  => $skipped_count,
    "questions"      => $questions_output
]);
exit;
?>
