import PageLayout from '../components/PageLayout'

export default function PrivacyPage() {
  return (
    <PageLayout>
      <div className="max-w-3xl mx-auto px-6 py-10 space-y-8">
        <h1 className="text-2xl font-bold text-accent-primary">Ochrana osobných údajov</h1>
        <p className="text-sm text-text-secondary">Posledná aktualizácia: 22. 6. 2026</p>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">1. Prevádzkovateľ</h2>
          <p className="text-text-primary leading-relaxed">
            Prevádzkovateľ platformy Predicto:
          </p>
          <p className="text-text-primary leading-relaxed mt-2">
            Erik Šlepecký<br />
            predictoSK@proton.me<br />
            Slovensko, Bardejov, 08501
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">2. Aké údaje spracúvame</h2>
          <p className="text-text-primary leading-relaxed mb-2">Pri registrácii môžeme spracúvať:</p>
          <ul className="list-disc list-inside space-y-1 text-text-primary ml-2">
            <li>používateľské meno,</li>
            <li>e-mailovú adresu,</li>
            <li>IP adresu,</li>
            <li>údaje o aktivite na platforme.</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">3. Účel spracovania</h2>
          <p className="text-text-primary leading-relaxed mb-2">Osobné údaje spracúvame za účelom:</p>
          <ul className="list-disc list-inside space-y-1 text-text-primary ml-2">
            <li>vytvorenia používateľského účtu,</li>
            <li>zabezpečenia fungovania platformy,</li>
            <li>prevencie podvodov,</li>
            <li>komunikácie s používateľmi,</li>
            <li>vyhodnocovania súťaží a odmien.</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">4. Doba uchovávania</h2>
          <p className="text-text-primary leading-relaxed">
            Údaje uchovávame po dobu trvania používateľského účtu alebo podľa zákonných povinností.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">5. Práva používateľa</h2>
          <p className="text-text-primary leading-relaxed mb-2">Používateľ má právo:</p>
          <ul className="list-disc list-inside space-y-1 text-text-primary ml-2">
            <li>na prístup k údajom,</li>
            <li>na opravu údajov,</li>
            <li>na vymazanie údajov,</li>
            <li>na obmedzenie spracovania,</li>
            <li>na prenosnosť údajov,</li>
            <li>podať sťažnosť dozornému orgánu.</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">6. Cookies</h2>
          <p className="text-text-primary leading-relaxed">
            Platforma môže používať cookies na zabezpečenie funkčnosti, analýzu návštevnosti a zlepšovanie používateľského zážitku.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">7. Kontakt</h2>
          <p className="text-text-primary leading-relaxed">
            V prípade otázok týkajúcich sa ochrany osobných údajov nás kontaktujte na: predictoSK@proton.me
          </p>
        </section>
      </div>
    </PageLayout>
  )
}
