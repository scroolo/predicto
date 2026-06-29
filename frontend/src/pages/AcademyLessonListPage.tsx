import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

interface Lesson {
  id: string;
  title: string;
  summary: string;
  estimatedMinutes: number;
  sortOrder: number;
}

interface Progress {
  completedLessons: number;
  certificates: { course: { id: string } }[];
}

export default function AcademyLessonListPage() {
  const { sport, courseId } = useParams();
  const navigate = useNavigate();
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [courseTitle, setCourseTitle] = useState("");
  const [completedIds, setCompletedIds] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!courseId) return;
    Promise.all([
      fetch(`/api/academy/courses`).then(r => r.json()),
      fetch(`/api/academy/courses/${courseId}/lessons`).then(r => r.json())
    ]).then(async ([courses, lessons]) => {
      const course = courses.find((c: any) => c.id === courseId);
      if (course) setCourseTitle(course.title);
      setLessons(lessons);

      try {
        const res = await fetch("/api/academy/progress/lessons", { credentials: "include" });
        if (res.ok) {
          const ids: string[] = await res.json();
          setCompletedIds(ids);
        }
      } catch (e) {}

      setLoading(false);
    });
  }, [courseId]);

  if (loading) return <div style={{ color: "white", padding: "2rem", textAlign: "center" }}>Načítavam...</div>;

  return (
    <div style={{ minHeight: "100vh", background: "#0a0a14", color: "white", padding: "2rem", maxWidth: 800, margin: "0 auto" }}>
      <button onClick={() => navigate(`/academy/${sport}`)}
        style={{ background: "none", border: "none", color: "#94a3b8", cursor: "pointer", marginBottom: "1.5rem", fontSize: "0.9rem" }}>
        ← Späť
      </button>

      <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "1.5rem", color: "#94a3b8", fontSize: "0.85rem" }}>
        <span onClick={() => navigate("/academy")} style={{ cursor: "pointer", color: "#7c3aed" }}>Academy</span>
        <span>›</span>
        <span onClick={() => navigate(`/academy/${sport}`)} style={{ cursor: "pointer", color: "#7c3aed" }}>{sport?.toUpperCase()}</span>
        <span>›</span>
        <span>{courseTitle}</span>
      </div>

      <h1 style={{ fontSize: "1.8rem", fontWeight: 800, marginBottom: "2rem" }}>{courseTitle}</h1>

      <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
        {lessons.map((lesson, index) => {
          const isDone = completedIds.includes(lesson.id);
          return (
            <div key={lesson.id}
              onClick={() => navigate(`/academy/lesson/${lesson.id}`)}
              style={{
                background: isDone ? "#1e2e1e" : "#1e1e2e",
                borderRadius: 10, padding: "1.25rem 1.5rem",
                border: `1px solid ${isDone ? "#22c55e" : "#334155"}`,
                cursor: "pointer", display: "flex", alignItems: "center", gap: "1rem",
                filter: "grayscale(0)",
                opacity: isDone ? 1 : 0.85
              }}
              onMouseEnter={e => { e.currentTarget.style.opacity = "1"; e.currentTarget.style.borderColor = isDone ? "#22c55e" : "#7c3aed"; }}
              onMouseLeave={e => { e.currentTarget.style.opacity = isDone ? "1" : "0.85"; e.currentTarget.style.borderColor = isDone ? "#22c55e" : "#334155"; }}>
              <div style={{
                width: 36, height: 36, borderRadius: "50%",
                background: isDone ? "#14532d" : "#1a1a2e",
                border: `2px solid ${isDone ? "#22c55e" : "#334155"}`,
                display: "flex", alignItems: "center", justifyContent: "center",
                fontWeight: 700, flexShrink: 0, color: isDone ? "#4ade80" : "#94a3b8"
              }}>
                {isDone ? "✓" : index + 1}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 600, marginBottom: "0.25rem", color: isDone ? "#4ade80" : "white" }}>{lesson.title}</div>
                <div style={{ fontSize: "0.8rem", color: "#94a3b8" }}>{lesson.summary}</div>
              </div>
              <div style={{ fontSize: "0.8rem", color: "#64748b", flexShrink: 0 }}>{lesson.estimatedMinutes} min</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
