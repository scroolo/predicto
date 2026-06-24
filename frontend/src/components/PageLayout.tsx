import { ReactNode } from 'react'
import TopNav from './TopNav'
import Footer from './Footer'

export default function PageLayout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen bg-bg flex flex-col">
      <TopNav />
      <main className="pt-16 flex-1">
        {children}
      </main>
      <Footer />
    </div>
  )
}
