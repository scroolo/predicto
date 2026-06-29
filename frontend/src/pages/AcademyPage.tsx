import { useState } from "react";
import { useNavigate } from "react-router-dom";

const sports = [
  {
    key: "CS2",
    name: "Counter-Strike 2",
    emoji: "🔫",
    description: "Od základných pravidiel až po taktické princípy profesionálneho CS2.",
    color: "#f59e0b"
  },
  {
    key: "LOL",
    name: "League of Legends",
    emoji: "🎮",
    description: "Kompletné základy aj pokročilé témy o najväčšej MOBA hre na svete.",
    color: "#22c55e"
  },
  {
    key: "F1",
    name: "Formula 1",
    emoji: "🏎️",
    description: "Pravidlá, stratégie a fungovanie sveta Formuly 1.",
    color: "#ef4444"
  }
];

export default function AcademyPage() {
  const navigate = useNavigate();

  return (
    <div style={{ minHeight: "100vh", background: "#0a0a14", color: "white", padding: "2rem" }}>
      <div style={{ textAlign: "center", marginBottom: "3rem", maxWidth: 700, margin: "0 auto 3rem" }}>
        <h1 style={{ fontSize: "2.5rem", fontWeight: 800, marginBottom: "0.75rem" }}>
          🎓 Predicto Academy
        </h1>
        <p style={{ color: "#94a3b8", fontSize: "1.1rem" }}>
          Staň sa informovanejším fanúšikom. Krátke, praktické lekcie o esporte a motorsporte.
        </p>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: "2rem", maxWidth: 1000, margin: "0 auto" }}>
        {sports.map(sport => (
          <div key={sport.key}
            onClick={() => navigate(`/academy/${sport.key.toLowerCase()}`)}
            style={{
              background: "#1e1e2e",
              borderRadius: 16,
              padding: "2.5rem 2rem",
              border: `2px solid #334155`,
              cursor: "pointer",
              textAlign: "center",
              transition: "border-color 0.2s, transform 0.2s",
            }}
            onMouseEnter={e => {
              e.currentTarget.style.borderColor = sport.color;
              e.currentTarget.style.transform = "translateY(-4px)";
            }}
            onMouseLeave={e => {
              e.currentTarget.style.borderColor = "#334155";
              e.currentTarget.style.transform = "translateY(0)";
            }}>
            <div style={{ fontSize: "4rem", marginBottom: "1rem" }}>{sport.emoji}</div>
            <h2 style={{ fontSize: "1.3rem", fontWeight: 700, marginBottom: "0.75rem", color: sport.color }}>{sport.name}</h2>
            <p style={{ color: "#94a3b8", fontSize: "0.9rem", lineHeight: 1.6 }}>{sport.description}</p>
            <div style={{ marginTop: "1.5rem", padding: "0.5rem 1.5rem", borderRadius: 20, background: sport.color + "22", color: sport.color, fontWeight: 600, display: "inline-block", fontSize: "0.85rem" }}>
              Začať →
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
