import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

interface Course {
  id: string;
  title: string;
  description: string;
  category: string;
  level: string;
  xpReward: number;
}

interface Lesson {
  id: string;
  title: string;
  summary: string;
  estimatedMinutes: number;
  sortOrder: number;
}

interface Progress {
  completedLessons: number;
  totalXp: number;
  certificates: { course: { id: string } }[];
}

const levelLabel: Record<string, string> = {
  BEGINNER: "Začiatočník",
  ADVANCED: "Pokročilý",
  EXPERT: "Expert"
};

const levelOrder = ["BEGINNER", "ADVANCED", "EXPERT"];

const sportName: Record<string, string> = {
  cs2: "Counter-Strike 2",
  lol: "League of Legends",
  f1: "Formula 1"
};

const sportEmoji: Record<string, string> = {
  cs2: "🔫",
  lol: "🎮",
  f1: "🏎️"
};

const sportColor: Record<string, string> = {
  cs2: "#f59e0b",
  lol: "#22c55e",
  f1: "#ef4444"
};

export default function AcademyCoursePage() {
  const { sport } = useParams<{ sport: string }>();
  const navigate = useNavigate();
  const [courses, setCourses] = useState<Course[]>([]);
  const [lessonCounts, setLessonCounts] = useState<Record<string, number>>({});
  const [completedCounts, setCompletedCounts] = useState<Record<string, number>>({});
  const [certificates, setCertificates] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    if (!sport) return;
    const category = sport.toUpperCase();

    fetch("/api/academy/courses")
      .then(r => r.json())
      .then(async (all: Course[]) => {
        const filtered = all.filter(c => c.category === category);
        setCourses(filtered);

        const counts: Record<string, number> = {};
        await Promise.all(filtered.map(async (c) => {
          const lessons: Lesson[] = await fetch(`/api/academy/courses/${c.id}/lessons`).then(r => r.json());
          counts[c.id] = lessons.length;
        }));
        setLessonCounts(counts);

        // Try to get user progress
        try {
          const res = await fetch("/api/academy/progress", { credentials: "include" });
          if (res.ok) {
            setIsLoggedIn(true);
            const progress: Progress = await res.json();
            setCertificates(progress.certificates.map(cert => cert.course.id));
          }
        } catch (e) {}

        setLoading(false);
      });
  }, [sport]);

  if (loading) return <div style={{ color: "white", padding: "2rem", textAlign: "center" }}>Načítavam...</div>;

  const color = sportColor[sport || "cs2"];
  const sorted = [...courses].sort((a, b) => levelOrder.indexOf(a.level) - levelOrder.indexOf(b.level));

  return (
    <div style={{ minHeight: "100vh", background: "#0a0a14", color: "white", padding: "2rem", maxWidth: 900, margin: "0 auto" }}>
      <button onClick={() => navigate("/academy")}
        style={{ background: "none", border: "none", color: "#94a3b8", cursor: "pointer", marginBottom: "1.5rem", fontSize: "0.9rem" }}>
        ← Späť na Academy
      </button>

      <div style={{ textAlign: "center", marginBottom: "3rem" }}>
        <div style={{ fontSize: "4rem", marginBottom: "0.5rem" }}>{sportEmoji[sport || "cs2"]}</div>
        <h1 style={{ fontSize: "2rem", fontWeight: 800, color }}>{sportName[sport || "cs2"]}</h1>
        <p style={{ color: "#94a3b8" }}>Vyber úroveň a začni sa učiť</p>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
        {sorted.map(course => {
          const completed = certificates.includes(course.id);
          const lessonCount = lessonCounts[course.id] || 0;

          return (
            <div key={course.id}
              onClick={() => navigate(`/academy/${sport}/course/${course.id}`)}
              style={{
                background: completed ? "#1e2e1e" : "#1e1e2e",
                borderRadius: 14,
                padding: "1.75rem 2rem",
                border: `2px solid ${completed ? "#22c55e" : "#334155"}`,
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: "1.5rem",
                filter: (!isLoggedIn || !completed) ? "none" : "none",
                opacity: 1,
                transition: "border-color 0.2s"
              }}
              onMouseEnter={e => e.currentTarget.style.borderColor = completed ? "#22c55e" : color}
              onMouseLeave={e => e.currentTarget.style.borderColor = completed ? "#22c55e" : "#334155"}>

              <div style={{
                width: 60, height: 60, borderRadius: "50%",
                background: completed ? "#14532d" : "#0f0f1a",
                border: `2px solid ${completed ? "#22c55e" : "#334155"}`,
                display: "flex", alignItems: "center", justifyContent: "center",
                fontSize: "1.5rem", flexShrink: 0
              }}>
                {completed ? "✅" : levelOrder.indexOf(course.level) === 0 ? "🟢" : levelOrder.indexOf(course.level) === 1 ? "🟡" : "🔴"}
              </div>

              <div style={{ flex: 1 }}>
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "0.25rem" }}>
                  <h3 style={{ fontSize: "1.1rem", fontWeight: 700, color: completed ? "#4ade80" : "white" }}>{course.title}</h3>
                  {completed && <span style={{ fontSize: "0.7rem", padding: "2px 8px", borderRadius: 10, background: "#14532d", color: "#4ade80" }}>Dokončené ✓</span>}
                </div>
                <p style={{ color: "#94a3b8", fontSize: "0.85rem", marginBottom: "0.5rem" }}>{course.description}</p>
                <div style={{ display: "flex", gap: "1rem", fontSize: "0.8rem" }}>
                  <span style={{ color: color }}>{levelLabel[course.level]}</span>
                  <span style={{ color: "#64748b" }}>{lessonCount} lekcií</span>
                  <span style={{ color: "#a78bfa" }}>+{course.xpReward} XP</span>
                </div>
              </div>

              <div style={{ color: "#64748b", fontSize: "1.5rem", flexShrink: 0 }}>→</div>
            </div>
          );
        })}

        {sorted.length === 0 && (
          <div style={{ textAlign: "center", color: "#94a3b8", padding: "3rem" }}>
            Kurzy sa pripravujú. Čoskoro!
          </div>
        )}
      </div>
    </div>
  );
}
