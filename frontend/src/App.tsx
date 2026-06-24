import { Routes, Route, Navigate } from 'react-router-dom'
import PageTransition from './components/PageTransition'
import ProtectedRoute from './components/ProtectedRoute'
import AdminLayout from './components/AdminLayout'
import SplashScreen from './components/SplashScreen'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DiscordCallbackPage from './pages/DiscordCallbackPage'
import AuthCallbackPage from './pages/AuthCallbackPage'
import HomePage from './pages/HomePage'
import DashboardPage from './pages/DashboardPage'
import MatchDetailPage from './pages/MatchDetailPage'
import MyPredictionsPage from './pages/MyPredictionsPage'
import LeaderboardPage from './pages/LeaderboardPage'
import ProfilePage from './pages/ProfilePage'
import SettingsPage from './pages/SettingsPage'
import ArticleListPage from './pages/ArticleListPage'
import ArticleDetailPage from './pages/ArticleDetailPage'
import TermsPage from './pages/TermsPage'
import PrivacyPage from './pages/PrivacyPage'
import AdminMatchesPage from './pages/AdminMatchesPage'
import AdminSeasonsPage from './pages/AdminSeasonsPage'
import AdminArticlesPage from './pages/AdminArticlesPage'
import AdminSyncPage from './pages/AdminSyncPage'
import AdminUsersPage from './pages/AdminUsersPage'
import AdminLeaderboardPage from './pages/AdminLeaderboardPage'
import AdminPredictionsPage from './pages/AdminPredictionsPage'
import F1Page from './pages/F1Page'
import F1MeetingPage from './pages/F1MeetingPage'
import F1SessionPage from './pages/F1SessionPage'
import F1LeaderboardPage from './pages/F1LeaderboardPage'
import AdminF1Page from './pages/AdminF1Page'
import AdminRoute from './components/AdminRoute'
import { useAuth } from './context/AuthContext'

export default function App() {
  const { showSplash, setShowSplash } = useAuth()

  return (
    <>
      {showSplash && <SplashScreen onFinish={() => setShowSplash(false)} />}
      <PageTransition>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/auth/callback" element={<AuthCallbackPage />} />
        <Route path="/auth/discord/callback" element={<DiscordCallbackPage />} />
        <Route path="/matches" element={<DashboardPage />} />
        <Route path="/matches/:id" element={<MatchDetailPage />} />
        <Route path="/articles" element={<ArticleListPage />} />
        <Route path="/articles/:slug" element={<ArticleDetailPage />} />
        <Route path="/terms" element={<TermsPage />} />
        <Route path="/privacy" element={<PrivacyPage />} />
        <Route path="/predictions" element={
          <ProtectedRoute><MyPredictionsPage /></ProtectedRoute>
        } />
        <Route path="/my-bets" element={<Navigate to="/predictions" replace />} />
        <Route path="/mybets" element={<Navigate to="/predictions" replace />} />
        <Route path="/dashboard" element={<Navigate to="/matches" replace />} />
        <Route path="/leaderboard" element={<LeaderboardPage />} />
        <Route path="/profile" element={
          <ProtectedRoute><ProfilePage /></ProtectedRoute>
        } />
        <Route path="/settings" element={
          <ProtectedRoute><SettingsPage /></ProtectedRoute>
        } />
        <Route path="/admin/matches" element={
          <AdminRoute requiredRole="ADMIN"><AdminLayout><AdminMatchesPage /></AdminLayout></AdminRoute>
        } />
        <Route path="/admin/seasons" element={
          <AdminRoute requiredRole="ADMIN"><AdminLayout><AdminSeasonsPage /></AdminLayout></AdminRoute>
        } />
        <Route path="/admin/articles" element={
          <AdminRoute><AdminLayout><AdminArticlesPage /></AdminLayout></AdminRoute>
        } />
        <Route path="/admin/sync" element={
          <AdminRoute requiredRole="ADMIN"><AdminLayout><AdminSyncPage /></AdminLayout></AdminRoute>
        } />
        <Route path="/admin/users" element={
          <AdminRoute requiredRole="ADMIN"><AdminLayout><AdminUsersPage /></AdminLayout></AdminRoute>
        } />
        <Route path="/admin/leaderboard" element={
          <AdminRoute requiredRole="ADMIN"><AdminLayout><AdminLeaderboardPage /></AdminLayout></AdminRoute>
        } />
        <Route path="/admin/predictions" element={
          <AdminRoute requiredRole="ADMIN"><AdminLayout><AdminPredictionsPage /></AdminLayout></AdminRoute>
        } />
        <Route path="/admin/f1" element={
          <AdminRoute requiredRole="ADMIN"><AdminLayout><AdminF1Page /></AdminLayout></AdminRoute>
        } />
        <Route path="/f1" element={<F1Page />} />
        <Route path="/f1/meetings/:id" element={<F1MeetingPage />} />
        <Route path="/f1/sessions/:id" element={<F1SessionPage />} />
        <Route path="/f1/leaderboard" element={<F1LeaderboardPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      </PageTransition>
    </>
  )
}
