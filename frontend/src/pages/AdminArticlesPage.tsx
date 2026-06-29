import { useEffect, useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'

interface ArticleItem {
  id: string
  slug: string
  title: string
  summary: string
  content: string
  coverImageUrl: string | null
  category: string
  game: string
  language: string
  featured: boolean
  status: string
  publishedAt: string | null
}

interface Toast {
  type: 'success' | 'error'
  text: string
}

export default function AdminArticlesPage() {
  const { t } = useTranslation()
  const [articles, setArticles] = useState<ArticleItem[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState<string | null>(null)
  const [toast, setToast] = useState<Toast | null>(null)

  const [title, setTitle] = useState('')
  const [summary, setSummary] = useState('')
  const [content, setContent] = useState('')
  const [coverImageUrl, setCoverImageUrl] = useState('')
  const [category, setCategory] = useState('NEWS')
  const [game, setGame] = useState('LOL')
  const [language, setLanguage] = useState('sk')
  const [featured, setFeatured] = useState(false)

  const showToast = (t: Toast) => {
    setToast(t)
    setTimeout(() => setToast(null), 4000)
  }

  const fetchArticles = useCallback(async () => {
    try {
      const res = await api.get('/api/admin/articles')
      setArticles(res.data?.content ?? [])
    } catch {
      setArticles([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchArticles()
  }, [fetchArticles])

  const resetForm = () => {
    setTitle('')
    setSummary('')
    setContent('')
    setCoverImageUrl('')
    setCategory('NEWS')
    setGame('LOL')
    setLanguage('sk')
    setFeatured(false)
    setEditingId(null)
  }

  const openEditForm = (a: ArticleItem) => {
    setTitle(a.title)
    setSummary(a.summary)
    setContent(a.content)
    setCoverImageUrl(a.coverImageUrl ?? '')
    setCategory(a.category)
    setGame(a.game)
    setLanguage(a.language || 'sk')
    setFeatured(a.featured)
    setEditingId(a.id)
    setShowForm(true)
    setToast(null)
  }

  const openCreateForm = () => {
    resetForm()
    setShowForm(true)
    setToast(null)
  }

  const handleSave = async (publishAfter: boolean) => {
    setToast(null)
    if (!title.trim() || !summary.trim() || !content.trim()) {
      showToast({ type: 'error', text: t('admin.povinne_pole') })
      return
    }
    setSaving(true)
    try {
      const body = { title: title.trim(), summary: summary.trim(), content, coverImageUrl: coverImageUrl || null, category, game, language, featured }
      if (editingId) {
        console.log('Saving article, editingId:', editingId)
        console.log('PUT URL:', `/api/admin/articles/${editingId}`)
        await api.put(`/api/admin/articles/${editingId}`, body)
        if (publishAfter) {
          await api.patch(`/api/admin/articles/${editingId}/publish`)
        }
        showToast({ type: 'success', text: publishAfter ? t('admin.aktualizovany_zverejnene') : t('admin.aktualizovany') })
      } else {
        const res = await api.post('/api/admin/articles', body)
        if (publishAfter) {
          await api.patch(`/api/admin/articles/${res.data.id}/publish`)
          showToast({ type: 'success', text: t('admin.vytvoreny_zverejnene') })
        } else {
          showToast({ type: 'success', text: t('admin.ulozeny_koncept') })
        }
      }
      resetForm()
      setShowForm(false)
      setLoading(true)
      await fetchArticles()
    } catch (err: any) {
      showToast({ type: 'error', text: err.response?.data?.message || err.response?.data || t('admin.chyba_ulozenie') })
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id: string) => {
    if (!window.confirm(t('admin.potvrdit_odstranenie'))) return
    setDeleting(id)
    try {
      await api.delete(`/api/admin/articles/${id}`)
      showToast({ type: 'success', text: t('admin.clanok_vymazany') })
      setArticles(prev => prev.filter(a => a.id !== id))
    } catch {
      showToast({ type: 'error', text: t('admin.chyba_vymazanie') })
    } finally {
      setDeleting(null)
    }
  }

  const handlePublish = async (id: string) => {
    try {
      await api.patch(`/api/admin/articles/${id}/publish`)
      showToast({ type: 'success', text: t('admin.clanok_zverejneny') })
      setArticles(prev => prev.map(a => a.id === id ? { ...a, status: 'PUBLISHED', publishedAt: new Date().toISOString() } : a))
    } catch {
      showToast({ type: 'error', text: t('admin.chyba_zverejnenie') })
    }
  }

  const handleUnpublish = async (id: string) => {
    try {
      await api.patch(`/api/admin/articles/${id}/unpublish`)
      showToast({ type: 'success', text: t('admin.clanok_stiahnuty') })
      setArticles(prev => prev.map(a => a.id === id ? { ...a, status: 'DRAFT' } : a))
    } catch {
      showToast({ type: 'error', text: t('admin.chyba_stiahnutie') })
    }
  }

  const handleFeature = async (id: string) => {
    try {
      const res = await api.patch(`/api/admin/articles/${id}/feature`)
      showToast({ type: 'success', text: t('admin.feature_prepnute') })
      setArticles(prev => prev.map(a => a.id === id ? { ...a, featured: res.data.featured } : a))
    } catch {
      showToast({ type: 'error', text: t('admin.chyba_feature') })
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-bold">{t('admin.clanky')}</h1>
        <button onClick={() => { if (showForm) { resetForm(); setShowForm(false) } else { openCreateForm() } }} className="btn-primary text-sm">
          {showForm ? t('admin.zrusit') : t('admin.novy_clanok')}
        </button>
      </div>

      {toast && (
        <div className={`mb-4 px-4 py-3 rounded-lg text-sm ${toast.type === 'success' ? 'bg-green-900/30 text-green-400 border border-green-800/50' : 'bg-red-900/30 text-red-400 border border-red-800/50'}`}>
          {toast.text}
        </div>
      )}

      {showForm && (
        <div className="card p-5 mb-5 space-y-4">
          <div>
            <label className="block text-xs text-text-secondary mb-1">{t('admin.nadpis')}</label>
            <input className="input-field" value={title} onChange={(e) => setTitle(e.target.value)} placeholder={t('admin.nadpis')} />
          </div>
          <div>
            <label className="block text-xs text-text-secondary mb-1">{t('admin.summary')} ({summary.length}/500)</label>
            <textarea className="input-field resize-none" rows={3} maxLength={500} value={summary} onChange={(e) => setSummary(e.target.value)} placeholder={t('admin.summary')} />
          </div>
          <div>
            <label className="block text-xs text-text-secondary mb-1">{t('admin.obsah')}</label>
            <textarea className="input-field resize-none" rows={8} value={content} onChange={(e) => setContent(e.target.value)} placeholder={t('admin.obsah')} />
          </div>
          <div>
            <label className="block text-xs text-text-secondary mb-1">{t('admin.cover_url')}</label>
            <input className="input-field" value={coverImageUrl} onChange={(e) => setCoverImageUrl(e.target.value)} placeholder="https://..." />
          </div>
          <div className="grid grid-cols-4 gap-4">
            <div>
              <label className="block text-xs text-text-secondary mb-1">{t('admin.kategoria')}</label>
              <select className="select-field" value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="NEWS">{t('admin.news')}</option>
                <option value="ANALYSIS">{t('admin.analysis')}</option>
                <option value="TIPS">{t('admin.tips')}</option>
              </select>
            </div>
            <div>
              <label className="block text-xs text-text-secondary mb-1">{t('admin.hra')}</label>
              <select className="select-field" value={game} onChange={(e) => setGame(e.target.value)}>
                <option value="LOL">{t('admin.lol')}</option>
                <option value="CS2">{t('admin.cs2')}</option>
                <option value="F1">Formula 1</option>
                <option value="ALL">{t('admin.all')}</option>
              </select>
            </div>
            <div>
              <label className="block text-xs text-text-secondary mb-1">{t('admin.articleLanguage')}</label>
              <select className="select-field" value={language} onChange={(e) => setLanguage(e.target.value)}>
                <option value="sk">{t('admin.slovak')}</option>
                <option value="en">{t('admin.english')}</option>
              </select>
            </div>
            <div className="flex items-end pb-2">
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" checked={featured} onChange={(e) => setFeatured(e.target.checked)} className="w-4 h-4 rounded border-border bg-surface-elevated text-accent-primary focus:ring-accent-primary" />
                <span className="text-sm text-text-primary">{t('admin.featured')}</span>
              </label>
            </div>
          </div>
          <div className="flex gap-3">
            <button onClick={() => handleSave(false)} disabled={saving} className="btn-secondary text-sm">
              {saving ? t('admin.ukladam') : t('admin.ulozit_koncept')}
            </button>
            <button onClick={() => handleSave(true)} disabled={saving} className="btn-primary text-sm">
              {saving ? t('admin.zverejnujem') : t('admin.zverejnit')}
            </button>
          </div>
        </div>
      )}

      {loading ? (
        <p className="text-text-secondary">{t('common.nacitavam')}</p>
      ) : (
        <div className="bg-surface border border-border rounded-xl overflow-hidden">
          <table className="w-full text-sm">
            <thead>
                  <tr className="border-b border-border text-text-secondary text-xs uppercase">
                    <th className="table-header w-8">Lang</th>
                    <th className="table-header">{t('admin.nadpis')}</th>
                    <th className="table-header hidden md:table-cell">{t('admin.status')}</th>
                <th className="table-header hidden md:table-cell">{t('admin.featured')}</th>
                <th className="table-header">{t('admin.zverejnene_datum')}</th>
                <th className="table-header text-right">{t('admin.akcie')}</th>
              </tr>
            </thead>
            <tbody>
              {(articles || []).map((a) => (
                <tr key={a.id} className="border-b border-border/30">
                  <td className="table-cell text-center text-lg">{a.language === 'sk' ? '🇸🇰' : '🇬🇧'}</td>
                  <td className="table-cell">
                    <div className="font-medium">{a.title}</div>
                    <div className="text-xs text-text-secondary">{a.slug}</div>
                  </td>
                  <td className="table-cell hidden md:table-cell">
                    <span className={`badge text-[10px] ${a.status === 'PUBLISHED' ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400'}`}>
                      {a.status === 'PUBLISHED' ? t('admin.zverejnene') : t('admin.koncept')}
                    </span>
                  </td>
                  <td className="table-cell hidden md:table-cell">
                    {a.featured ? <span className="text-amber-400">★</span> : <span className="text-text-secondary">☆</span>}
                  </td>
                  <td className="table-cell text-text-secondary whitespace-nowrap">
                    {a.publishedAt ? new Date(a.publishedAt).toLocaleDateString() : '—'}
                  </td>
                  <td className="table-cell text-right">
                    <div className="flex items-center justify-end gap-1">
                      <button onClick={() => openEditForm(a)} className="btn-ghost text-xs px-2 py-1 hover:bg-surface-elevated rounded" title={t('admin.upravit_tooltip')}>✏️</button>
                      {a.status === 'DRAFT' ? (
                        <button onClick={() => handlePublish(a.id)} className="btn-ghost text-xs px-2 py-1 hover:bg-surface-elevated rounded" title={t('admin.zverejnit_tooltip')}>📰</button>
                      ) : (
                        <button onClick={() => handleUnpublish(a.id)} className="btn-ghost text-xs px-2 py-1 hover:bg-surface-elevated rounded" title={t('admin.stiahnut_tooltip')}>📥</button>
                      )}
                      <button onClick={() => handleFeature(a.id)} className={`btn-ghost text-xs px-2 py-1 hover:bg-surface-elevated rounded ${a.featured ? 'text-amber-400' : ''}`} title={t('admin.feature_tooltip')}>⭐</button>
                      <button onClick={() => handleDelete(a.id)} disabled={deleting === a.id} className="btn-ghost text-xs px-2 py-1 hover:bg-surface-elevated rounded text-red-400" title={t('admin.odstranit_tooltip')}>🗑️</button>
                    </div>
                  </td>
                </tr>
              ))}
              {articles.length === 0 && (
                <tr>
                  <td colSpan={6} className="table-cell text-center text-text-secondary py-8">
                    {t('admin.ziadne_clanky')}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
