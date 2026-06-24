import { useEffect, useState, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'
import PageLayout from '../components/PageLayout'

interface ArticleData {
  id: string
  slug: string
  title: string
  summary: string
  content: string
  coverImageUrl: string | null
  authorDisplayName: string
  publishedAt: string
}

export default function ArticleDetailPage() {
  const { t } = useTranslation()
  const { slug } = useParams<{ slug: string }>()
  const [article, setArticle] = useState<ArticleData | null>(null)
  const [loading, setLoading] = useState(true)
  const [imgError, setImgError] = useState(false)
  const imgRef = useRef<HTMLImageElement>(null)

  useEffect(() => {
    if (!slug) return
    ;(async () => {
      try {
        const res = await api.get(`/api/articles/${slug}`)
        setArticle(res.data)
      } catch {
        setArticle(null)
      } finally {
        setLoading(false)
      }
    })()
  }, [slug])

  useEffect(() => {
    if (!article) return
    document.title = `${article.title} | Predicto`
    const metaDesc = document.querySelector('meta[name="description"]')
    const originalDesc = metaDesc?.getAttribute('content')
    if (metaDesc) metaDesc.setAttribute('content', article.summary || article.title)
    return () => {
      document.title = 'Predicto'
      if (metaDesc && originalDesc) metaDesc.setAttribute('content', originalDesc)
    }
  }, [article])

  if (loading) return <PageLayout><div className="max-w-6xl mx-auto px-4 py-5"><p className="text-text-secondary">{t('articles.nacitavam_detail')}</p></div></PageLayout>
  if (!article) return <PageLayout><div className="max-w-6xl mx-auto px-4 py-5"><p className="text-text-secondary">{t('articles.clanok_nenajdeny')}</p></div></PageLayout>

  return (
    <PageLayout>
      <div className="max-w-6xl mx-auto px-4 py-5">
        <div className="card p-6 space-y-4">
        {article.coverImageUrl && !imgError && (
          <img ref={imgRef} src={article.coverImageUrl} alt={article.title} onError={() => setImgError(true)} style={{ width: '100%', maxHeight: '400px', objectFit: 'cover', borderRadius: '8px', marginBottom: '24px' }} />
        )}
        <h1 className="text-2xl font-bold text-text-primary">{article.title}</h1>
        <p className="text-xs text-text-secondary">{new Date(article.publishedAt).toLocaleDateString('sk-SK') + ' o ' + new Date(article.publishedAt).toLocaleTimeString('sk-SK', { hour: '2-digit', minute: '2-digit' })}</p>
        <p className="text-xs text-text-secondary">{t('articles.autor', { name: article.authorDisplayName })}</p>
        {article.summary && <p className="text-lg italic text-text-secondary" style={{ marginBottom: '32px', paddingBottom: '24px', borderBottom: '1px solid #1e2d45' }}>{article.summary}</p>}
        <div className="text-text-primary leading-relaxed whitespace-pre-wrap">{article.content}</div>
      </div>
      </div>
    </PageLayout>
  )
}
