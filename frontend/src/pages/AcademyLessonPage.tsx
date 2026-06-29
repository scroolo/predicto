import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

interface Lesson {
  id: string;
  title: string;
  summary: string;
  content: string;
  estimatedMinutes: number;
  xpReward: number;
  xpQuizBonus: number;
  course: { id: string; title: string; category: string };
}

interface QuizQuestion {
  id: string;
  question: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  correctOption: string;
  sortOrder: number;
}

const sportKey: Record<string, string> = { CS2: "cs2", LOL: "lol", F1: "f1" };

export default function AcademyLessonPage() {
  const { lessonId } = useParams();
  const navigate = useNavigate();
  const [lesson, setLesson] = useState<Lesson | null>(null);
  const [quiz, setQuiz] = useState<QuizQuestion[]>([]);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [submitted, setSubmitted] = useState(false);
  const [score, setScore] = useState(0);
  const [xpEarned, setXpEarned] = useState(0);
  const [loading, setLoading] = useState(true);
  const [completing, setCompleting] = useState(false);

  useEffect(() => {
    if (!lessonId) return;
    Promise.all([
      fetch(`/api/academy/lessons/${lessonId}`).then(r => r.json()),
      fetch(`/api/academy/lessons/${lessonId}/quiz`).then(r => r.json())
    ]).then(([lessonData, quizData]) => {
      setLesson(lessonData);
      setQuiz(quizData);
      setLoading(false);
    });
  }, [lessonId]);

  const handleAnswer = (questionId: string, option: string) => {
    if (submitted) return;
    setAnswers(prev => ({ ...prev, [questionId]: option }));
  };

  const handleSubmit = async () => {
    if (!lesson) return;
    const correct = quiz.filter(q => answers[q.id] === q.correctOption).length;
    setScore(correct);
    setSubmitted(true);
    setCompleting(true);
    try {
      const res = await fetch(`/api/academy/lessons/${lessonId}/complete`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ quizScore: correct })
      });
      if (res.ok) {
        const data = await res.json();
        setXpEarned(data.xpEarned);
      }
    } catch (e) { console.error(e); }
    setCompleting(false);
  };

  const options = ["A", "B", "C", "D"] as const;
  const optionKeys: Record<string, keyof QuizQuestion> = { A: "optionA", B: "optionB", C: "optionC", D: "optionD" };

  if (loading) return <div style={{ color: "white", padding: "2rem", textAlign: "center" }}>Načítavam lekciu...</div>;
  if (!lesson) return <div style={{ color: "white", padding: "2rem", textAlign: "center" }}>Lekcia nenájdená.</div>;

  const sport = sportKey[lesson.course.category] || "cs2";

  return (
    <div style={{ minHeight: "100vh", background: "#0a0a14", color: "white", padding: "2rem", maxWidth: 800, margin: "0 auto" }}>
      <button onClick={() => navigate(`/academy/${sport}/course/${lesson.course.id}`)}
        style={{ background: "none", border: "none", color: "#94a3b8", cursor: "pointer", marginBottom: "1.5rem", fontSize: "0.9rem" }}>
        ← Späť na kurz
      </button>

      <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "1.5rem", color: "#94a3b8", fontSize: "0.85rem" }}>
        <span onClick={() => navigate("/academy")} style={{ cursor: "pointer", color: "#7c3aed" }}>Academy</span>
        <span>›</span>
        <span onClick={() => navigate(`/academy/${sport}`)} style={{ cursor: "pointer", color: "#7c3aed" }}>{lesson?.course?.category}</span>
        <span>›</span>
        <span onClick={() => navigate(`/academy/${sport}/course/${lesson?.course?.id}`)} style={{ cursor: "pointer", color: "#7c3aed" }}>{lesson?.course?.title}</span>
        <span>›</span>
        <span>{lesson?.title}</span>
      </div>

      <h1 style={{ fontSize: "1.8rem", fontWeight: 800, marginBottom: "0.5rem" }}>{lesson.title}</h1>
      <p style={{ color: "#94a3b8", marginBottom: "2rem" }}>{lesson.summary}</p>

      <div style={{ background: "#1e1e2e", borderRadius: 12, padding: "2rem", marginBottom: "2rem", lineHeight: 1.8, whiteSpace: "pre-wrap" }}>
        {lesson.content}
      </div>

      {quiz.length > 0 && (
        <div>
          <h2 style={{ fontSize: "1.3rem", fontWeight: 700, marginBottom: "1.5rem" }}>📝 Kvíz</h2>

          {submitted && (
            <div style={{ background: score >= 3 ? "#14532d" : "#7f1d1d", borderRadius: 10, padding: "1rem 1.5rem", marginBottom: "1.5rem", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div>
                <div style={{ fontWeight: 700, fontSize: "1.1rem" }}>{score >= 3 ? "🎉 Výborne!" : "📚 Skús znova!"}</div>
                <div style={{ color: "#cbd5e1" }}>{score}/{quiz.length} správnych odpovedí</div>
              </div>
              {xpEarned > 0 && <div style={{ fontWeight: 700, color: "#a78bfa", fontSize: "1.2rem" }}>+{xpEarned} XP</div>}
            </div>
          )}

          {quiz.map((q, idx) => (
            <div key={q.id} style={{ marginBottom: "1.5rem" }}>
              <div style={{ fontWeight: 600, marginBottom: "0.75rem" }}>{idx + 1}. {q.question}</div>
              <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                {options.map(opt => {
                  const isSelected = answers[q.id] === opt;
                  const isCorrect = submitted && opt === q.correctOption;
                  const isWrong = submitted && isSelected && opt !== q.correctOption;
                  return (
                    <button key={opt} onClick={() => handleAnswer(q.id, opt)}
                      style={{
                        padding: "0.75rem 1rem", borderRadius: 8, border: "1px solid",
                        borderColor: isCorrect ? "#22c55e" : isWrong ? "#ef4444" : isSelected ? "#7c3aed" : "#334155",
                        background: isCorrect ? "#14532d" : isWrong ? "#7f1d1d" : isSelected ? "#2d1b69" : "#1e1e2e",
                        color: "white", cursor: submitted ? "default" : "pointer", textAlign: "left",
                        display: "flex", justifyContent: "space-between", alignItems: "center"
                      }}>
                      <span><strong>{opt}.</strong> {q[optionKeys[opt]] as string}</span>
                      {isCorrect && <span style={{ color: "#4ade80", fontSize: "0.85rem" }}>✓ Správne</span>}
                      {isWrong && <span style={{ color: "#f87171", fontSize: "0.85rem" }}>✗ Nesprávne</span>}
                    </button>
                  );
                })}
              </div>
            </div>
          ))}

          {!submitted && (
            <button onClick={handleSubmit}
              disabled={Object.keys(answers).length < quiz.length || completing}
              style={{
                width: "100%", padding: "0.875rem", borderRadius: 10, border: "none",
                background: Object.keys(answers).length < quiz.length ? "#334155" : "#7c3aed",
                color: "white", fontWeight: 700, fontSize: "1rem",
                cursor: Object.keys(answers).length < quiz.length ? "not-allowed" : "pointer"
              }}>
              {completing ? "Odosielam..." : `Odoslať kvíz (${Object.keys(answers).length}/${quiz.length})`}
            </button>
          )}

          {submitted && (
            <button onClick={() => navigate(`/academy/${sport}/course/${lesson.course.id}`)}
              style={{ width: "100%", padding: "0.875rem", borderRadius: 10, border: "none", background: "#7c3aed", color: "white", fontWeight: 700, fontSize: "1rem", cursor: "pointer", marginTop: "1rem" }}>
              ← Späť na kurz
            </button>
          )}
        </div>
      )}
    </div>
  );
}
