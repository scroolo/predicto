import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useToastContext } from '../contexts/ToastContext'
import api from '../lib/api'

export default function SettingsPage() {
  const { t } = useTranslation()
  const { toast } = useToastContext()
  const navigate = useNavigate()

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const [email, setEmail] = useState('')

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault()
    if (newPassword !== confirmPassword) {
      toast.error(t('settings.passwordMismatch'))
      return
    }
    try {
      await api.put('/api/users/me/password', { currentPassword, newPassword })
      toast.success(t('settings.passwordChanged'))
      setCurrentPassword(''); setNewPassword(''); setConfirmPassword('')
    } catch (err: any) {
      toast.error(err.response?.data?.message || t('settings.passwordError'))
    }
  }

  const handleEmailChange = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await api.put('/api/users/me/email', { email })
      toast.success(t('settings.emailChanged'))
      setEmail('')
    } catch (err: any) {
      toast.error(err.response?.data?.message || t('common.error'))
    }
  }

  return (
    <div className="min-h-screen bg-[#0a0e1a] pt-24 pb-12 px-4">
      <div className="max-w-xl mx-auto space-y-8">
        <button
          onClick={() => navigate(-1)}
          style={{
            background: 'none', border: 'none', color: '#aaa',
            cursor: 'pointer', fontSize: '14px', marginBottom: '16px',
            display: 'flex', alignItems: 'center', gap: '6px', padding: '0',
          }}
        >
          {t('common.back')}
        </button>
        <h1 className="text-2xl font-bold text-white">{t('settings.title')}</h1>

        <form onSubmit={handlePasswordChange} className="bg-[#111827] border border-[#1e2d45] rounded-lg p-6 space-y-4">
          <h2 className="text-lg font-semibold text-white">{t('settings.changePassword')}</h2>
          <input type="password" placeholder={t('settings.currentPassword')} value={currentPassword}
            onChange={e => setCurrentPassword(e.target.value)}
            className="w-full bg-[#0a0e1a] border border-[#1e2d45] rounded px-3 py-2 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-accent-primary" />
          <input type="password" placeholder={t('settings.newPassword')} value={newPassword}
            onChange={e => setNewPassword(e.target.value)}
            className="w-full bg-[#0a0e1a] border border-[#1e2d45] rounded px-3 py-2 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-accent-primary" />
          <input type="password" placeholder={t('settings.confirmPassword')} value={confirmPassword}
            onChange={e => setConfirmPassword(e.target.value)}
            className="w-full bg-[#0a0e1a] border border-[#1e2d45] rounded px-3 py-2 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-accent-primary" />
          <button type="submit" className="btn-primary text-sm !px-4 !py-2">{t('settings.save')}</button>
        </form>

        <form onSubmit={handleEmailChange} className="bg-[#111827] border border-[#1e2d45] rounded-lg p-6 space-y-4">
          <h2 className="text-lg font-semibold text-white">{t('settings.changeEmail')}</h2>
          <input type="email" placeholder={t('settings.newEmail')} value={email}
            onChange={e => setEmail(e.target.value)}
            className="w-full bg-[#0a0e1a] border border-[#1e2d45] rounded px-3 py-2 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-accent-primary" />
          <button type="submit" className="btn-primary text-sm !px-4 !py-2">{t('settings.save')}</button>
        </form>

        <div className="bg-red-900/10 border border-red-500/30 rounded-lg p-6 space-y-4">
          <h2 className="text-lg font-semibold text-red-400">{t('settings.dangerZone')}</h2>
          <p className="text-sm text-gray-400">{t('settings.deleteWarning')}</p>
          <button disabled className="btn-primary text-sm !px-4 !py-2 opacity-50 cursor-not-allowed">{t('settings.deleteAccount')}</button>
        </div>
      </div>
    </div>
  )
}
