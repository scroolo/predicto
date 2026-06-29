INSERT INTO achievements (id, name, description, category, rarity, icon, points, hidden) VALUES
('academy_first_lesson', 'Prvý krok', 'Dokončil si svoju prvú lekciu v Predicto Academy', 'ACADEMY', 'COMMON', '🎓', 50, false),
('academy_first_course', 'Absolvent', 'Dokončil si celý kurz a získal certifikát', 'ACADEMY', 'RARE', '📜', 200, false),
('academy_10_lessons', 'Usilovný študent', 'Dokončil si 10 lekcií v Predicto Academy', 'ACADEMY', 'UNCOMMON', '📚', 150, false),
('academy_lol_complete', 'LoL Expert', 'Dokončil si všetky LoL kurzy', 'ACADEMY', 'EPIC', '🎮', 300, false),
('academy_cs2_complete', 'CS2 Expert', 'Dokončil si všetky CS2 kurzy', 'ACADEMY', 'EPIC', '🔫', 300, false),
('academy_f1_complete', 'F1 Expert', 'Dokončil si všetky F1 kurzy', 'ACADEMY', 'EPIC', '🏎️', 300, false),
('academy_all_complete', 'Predicto Scholar', 'Dokončil si všetky kurzy vo všetkých kategóriách', 'ACADEMY', 'LEGENDARY', '🏆', 1000, false)
ON CONFLICT (id) DO NOTHING;
