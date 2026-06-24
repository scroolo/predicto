import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

const InstagramIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#6b7a99" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="2" width="20" height="20" rx="5" ry="5"/><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"/></svg>
)

const DiscordIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="#6b7a99"><path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.054C1.483 9.11.862 13.74.87 18.31c.002.023.013.045.03.058a19.901 19.901 0 0 0 5.985 3.022.076.076 0 0 0 .084-.028c.46-.628.87-1.294 1.226-1.995a.074.074 0 0 0-.04-.1 13.105 13.105 0 0 1-1.872-.892.075.075 0 0 1-.018-.125c.126-.094.252-.192.372-.293a.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.1.246.2.372.293a.074.074 0 0 1-.018.125c-.598.35-1.22.645-1.872.892a.074.074 0 0 0-.04.1c.357.7.767 1.366 1.226 1.994a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.022.077.077 0 0 0 .03-.058c.01-5.112-.886-9.675-3.734-13.886a.067.067 0 0 0-.032-.053zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/></svg>
)

export default function Footer() {
  const { t } = useTranslation()
  return (
    <footer className="bg-[#0a0e1a] border-t border-[#1e2d45] mt-12">
      <div className="max-w-7xl mx-auto px-4 lg:px-8 py-10">
        {/* Three columns */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Left column — logo + tagline */}
          <div>
            <img src="/logo.png" alt="Predicto" style={{ height: 120 }} />
            <p className="text-xs text-accent-primary tracking-[0.3em] font-semibold mt-2 mb-3">{t('footer.tagline')}</p>
            <p className="text-sm text-text-secondary leading-relaxed">{t('footer.popis')}</p>
            <p style={{ fontSize: '11px', color: '#6b7a99', marginTop: '12px', lineHeight: '1.5' }}>{t('footer.disclaimer')}</p>
          </div>

          {/* Center column — quick links */}
          <div>
            <h3 className="text-sm font-semibold text-text-primary mb-3">{t('footer.rychle_odkazy')}</h3>
            <div className="space-y-2">
              <Link to="/" className="block text-sm text-text-secondary hover:text-text-primary transition">{t('footer.domov')}</Link>
              <Link to="/articles" className="block text-sm text-text-secondary hover:text-text-primary transition">{t('footer.clanky')}</Link>
              <Link to="/matches" className="block text-sm text-text-secondary hover:text-text-primary transition">{t('footer.zapasy')}</Link>
              <Link to="/leaderboard" className="block text-sm text-text-secondary hover:text-text-primary transition">{t('footer.leaderboard')}</Link>
              <Link to="/predictions" className="block text-sm text-text-secondary hover:text-text-primary transition">{t('footer.predikcie')}</Link>
            </div>
          </div>

          {/* Right column — social & info */}
          <div>
            <h3 className="text-sm font-semibold text-text-primary mb-3">{t('footer.sledujte_nas')}</h3>
            <div className="space-y-3">
              <a href="https://instagram.com/predicto" target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 text-sm text-text-secondary hover:text-text-primary transition">
                <InstagramIcon /> {t('footer.instagram')}
              </a>
              <a href="https://discord.gg/dy4RBbSpS8" target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 text-sm text-text-secondary hover:text-text-primary transition">
                <DiscordIcon /> {t('footer.discord')}
              </a>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom bar */}
      <div className="border-t border-[#1e2d45] px-4 lg:px-8">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between py-4 text-xs text-text-secondary">
          <span>{t('footer.copyright')}</span>
          <Link to="/terms" className="hover:text-text-primary transition">{t('footer.podmienky')}</Link>
          <Link to="/privacy" className="hover:text-text-primary transition">{t('footer.ochrana')}</Link>
          <span>{t('footer.prava')}</span>
        </div>
      </div>
    </footer>
  )
}
