import { useEffect, useState, useCallback } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'
import PageLayout from '../components/PageLayout'

interface ArticleItem {
  id: string
  slug: string
  title: string
  summary: string
  coverImageUrl: string | null
  category: string
  game: string
  authorDisplayName: string
  publishedAt: string
}

const categoryNames: Record<string, string> = {
  NEWS: 'News',
  ANALYSIS: 'Analysis',
  TIPS: 'Predictions',
}

const categoryLabels: Record<string, string> = {
  NEWS: 'Správy',
  ANALYSIS: 'Analýzy',
  TIPS: 'Predikcie',
}

const categoryColors: Record<string, string> = {
  NEWS: 'bg-blue-500/20 text-blue-400',
  ANALYSIS: 'bg-purple-500/20 text-purple-400',
  TIPS: 'bg-green-500/20 text-green-400',
}

const gameNames: Record<string, string> = {
  LOL: 'LoL',
  CS2: 'CS2',
  ALL: 'All',
}

export default function ArticleListPage() {
  const { t, i18n } = useTranslation()
  const [searchParams, setSearchParams] = useSearchParams()
  const activeCategory = searchParams.get('category') || null

  const [articles, setArticles] = useState<ArticleItem[]>([])
  const [loading, setLoading] = useState(true)

  const validCategory = activeCategory && ['NEWS', 'ANALYSIS', 'TIPS'].includes(activeCategory) ? activeCategory : null

  const fetchArticles = useCallback(async () => {
    setLoading(true)
    try {
      const params: Record<string, string> = { language: i18n.language }
      if (validCategory) params.category = validCategory
      const res = await api.get('/api/articles', { params })
      setArticles(res.data?.content ?? [])
    } catch {
      setArticles([])
    } finally {
      setLoading(false)
    }
  }, [validCategory, i18n.language])

  useEffect(() => {
    fetchArticles()
  }, [fetchArticles])

  const newsArticles = articles.filter(a => a.category === 'NEWS')
  const analysisArticles = articles.filter(a => a.category === 'ANALYSIS')
  const tipsArticles = articles.filter(a => a.category === 'TIPS')

  const sections = validCategory
    ? [{ key: validCategory, label: categoryLabels[validCategory] || validCategory, items: articles }]
    : [
        { key: 'NEWS', label: t('articles.spravy'), items: newsArticles },
        { key: 'ANALYSIS', label: t('articles.analyzy'), items: analysisArticles },
        { key: 'TIPS', label: t('articles.tipy'), items: tipsArticles },
      ]

  const renderCard = (article: ArticleItem) => (
    <Link
      key={article.id}
      to={`/articles/${article.slug}`}
      className="bg-surface border border-border rounded-xl p-4 flex gap-4 hover:border-accent-primary cursor-pointer transition-colors"
    >
      {article.coverImageUrl ? (
        <img src={article.coverImageUrl} alt={article.title} className="w-[120px] h-[80px] object-cover rounded-lg shrink-0" />
) : (
  <div style={{ width: 120, height: 80, borderRadius: 8, flexShrink: 0, overflow: "hidden" }}>
    {article.game === "CS2" && (
      <svg viewBox="0 0 120 80" xmlns="http://www.w3.org/2000/svg" style={{ width: "100%", height: "100%" }}>
        <defs><linearGradient id="cs2grad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stopColor="#1a1a2e"/><stop offset="100%" stopColor="#16213e"/></linearGradient></defs>
        <rect width="120" height="80" fill="url(#cs2grad)"/>
        <circle cx="60" cy="35" r="18" fill="none" stroke="#f59e0b" strokeWidth="2"/>
        <circle cx="60" cy="35" r="4" fill="#f59e0b"/>
        <line x1="60" y1="12" x2="60" y2="17" stroke="#f59e0b" strokeWidth="2"/>
        <line x1="60" y1="53" x2="60" y2="58" stroke="#f59e0b" strokeWidth="2"/>
        <line x1="37" y1="35" x2="42" y2="35" stroke="#f59e0b" strokeWidth="2"/>
        <line x1="78" y1="35" x2="83" y2="35" stroke="#f59e0b" strokeWidth="2"/>
        <text x="60" y="72" textAnchor="middle" fill="#f59e0b" fontSize="8" fontWeight="bold" fontFamily="Arial">CS2</text>
      </svg>
    )}
    {article.game === "LOL" && (
      <svg viewBox="0 0 120 80" xmlns="http://www.w3.org/2000/svg" style={{ width: "100%", height: "100%" }}>
        <defs><linearGradient id="lolgrad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stopColor="#0a0a1a"/><stop offset="100%" stopColor="#1a1a0a"/></linearGradient></defs>
        <rect width="120" height="80" fill="url(#lolgrad)"/>
        <polygon points="60,8 75,28 95,32 80,50 83,72 60,62 37,72 40,50 25,32 45,28" fill="none" stroke="#c89b3c" strokeWidth="2"/>
        <polygon points="60,20 70,33 84,36 74,47 76,62 60,55 44,62 46,47 36,36 50,33" fill="#c89b3c" opacity="0.3"/>
        <text x="60" y="76" textAnchor="middle" fill="#c89b3c" fontSize="7" fontWeight="bold" fontFamily="Arial">LEAGUE OF LEGENDS</text>
      </svg>
    )}
    {article.game === "F1" && (
      <svg viewBox="0 0 120 80" xmlns="http://www.w3.org/2000/svg" style={{ width: "100%", height: "100%" }}>
        <defs><linearGradient id="f1grad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stopColor="#1a0000"/><stop offset="100%" stopColor="#2a0a0a"/></linearGradient></defs>
        <rect width="120" height="80" fill="url(#f1grad)"/>
        <ellipse cx="60" cy="38" rx="35" ry="12" fill="none" stroke="#ef4444" strokeWidth="1.5"/>
        <ellipse cx="60" cy="38" rx="20" ry="12" fill="none" stroke="#ef4444" strokeWidth="1.5"/>
        <line x1="25" y1="38" x2="95" y2="38" stroke="#ef4444" strokeWidth="1.5"/>
        <circle cx="60" cy="38" r="4" fill="#ef4444"/>
        <text x="60" y="62" textAnchor="middle" fill="#ef4444" fontSize="10" fontWeight="bold" fontFamily="Arial">FORMULA 1</text>
      </svg>
    )}
    {(!article.game || (article.game !== "LOL" && article.game !== "CS2" && article.game !== "F1")) && (
      <div style={{ width: "100%", height: "100%", background: "linear-gradient(135deg, #1e2d45, #111827)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 11, fontWeight: 600, color: "#94a3b8" }}>
        {categoryNames[article.category] || article.category}
      </div>
    )}
  </div>
)}
      <div className="flex flex-col justify-between min-w-0 flex-1">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className={`badge text-[10px] ${categoryColors[article.category] || 'bg-gray-500/20 text-gray-400'}`}>
              {categoryNames[article.category] || article.category}
            </span>
            <span className="badge text-[10px] bg-accent-glow text-accent-primary">
              {gameNames[article.game] || article.game}
            </span>
          </div>
          <h2 className="text-base font-semibold text-text-primary leading-snug">{article.title}</h2>
          <p className="text-sm text-text-secondary leading-snug" style={{ overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>{article.summary}</p>
        </div>
        <div className="flex items-center gap-2 text-xs text-text-secondary mt-2">
          <span>{article.authorDisplayName}</span>
          <span>·</span>
          <span>{new Date(article.publishedAt).toLocaleDateString('sk-SK') + ' o ' + new Date(article.publishedAt).toLocaleTimeString('sk-SK', { hour: '2-digit', minute: '2-digit' })}</span>
        </div>
      </div>
    </Link>
  )

  return (
    <PageLayout>
      <div className="max-w-6xl mx-auto px-4 py-5 space-y-8">
        {/* Filter tabs */}
        <div className="flex items-center gap-1 bg-surface border border-border rounded-xl p-1 overflow-x-auto">
          {([{ key: null, label: t('articles.vsetko') }, { key: 'NEWS', label: t('articles.spravy') }, { key: 'ANALYSIS', label: t('articles.analyzy') }, { key: 'TIPS', label: t('articles.tipy') }] as const).map(tab => {
            const active = tab.key === activeCategory || (tab.key === null && activeCategory === null)
            return (
              <button
                key={tab.key ?? 'all'}
                onClick={() => {
                  if (tab.key) {
                    setSearchParams({ category: tab.key })
                  } else {
                    setSearchParams({})
                  }
                }}
                className={`px-4 py-2 text-sm font-medium rounded-lg whitespace-nowrap transition ${
                  active ? 'bg-accent-primary text-white' : 'text-text-secondary hover:text-text-primary'
                }`}
              >
                {tab.label}
              </button>
            )
          })}
        </div>

        {loading ? (
          <p className="text-text-secondary">{t('articles.nacitavam')}</p>
        ) : articles.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-text-secondary">
            <span className="text-3xl mb-3">📰</span>
            <p className="text-sm">{t('articles.ziadne_clanky')}</p>
          </div>
        ) : (
          sections.map(section => section.items.length > 0 && (
            <div key={section.key}>
              {!validCategory && (
                <h2 className="text-lg font-bold text-text-primary mb-3 pl-3 border-l-4 border-accent-primary">{section.label}</h2>
              )}
              <div className="grid grid-cols-1 gap-4">
                {section.items.map(renderCard)}
              </div>
            </div>
          ))
        )}
      </div>
    </PageLayout>
  )
}
