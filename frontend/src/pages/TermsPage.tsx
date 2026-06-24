import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import PageLayout from '../components/PageLayout'

export default function TermsPage() {
  const { t } = useTranslation()
  return (
    <PageLayout>
      <div className="max-w-3xl mx-auto px-6 py-10 space-y-8">
        <h1 className="text-2xl font-bold text-accent-primary">Podmienky používania portálu Predicto</h1>
        <p className="text-sm text-text-secondary">Posledná aktualizácia: 22. 6. 2026</p>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">1. Úvod</h2>
          <p className="text-text-primary leading-relaxed">
            Vitajte na platforme Predicto. Používaním tejto webovej stránky súhlasíte s týmito podmienkami používania. Predicto je komunitná platforma určená na predikovanie výsledkov esports a športových udalostí pomocou virtuálnych bodov.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">2. Charakter platformy</h2>
          <p className="text-text-primary leading-relaxed mb-3">
            Predicto nie je stávková kancelária ani hazardná hra. Všetky body, odmeny a funkcie platformy slúžia výlučne na komunitné a zábavné účely.
          </p>
          <p className="text-text-primary leading-relaxed mb-2">Virtuálne body:</p>
          <ul className="list-disc list-inside space-y-1 text-text-primary ml-2">
            <li>nemajú finančnú hodnotu,</li>
            <li>nie je možné ich zameniť za peniaze,</li>
            <li>nie je možné ich previesť na inú osobu.</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">3. Používateľský účet</h2>
          <p className="text-text-primary leading-relaxed">
            Používateľ je povinný poskytovať pravdivé údaje. Jeden používateľ môže vlastniť iba jeden účet. Prevádzkovateľ si vyhradzuje právo zablokovať účty vytvorené za účelom zneužívania systému.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">4. Odmeny</h2>
          <p className="text-text-primary leading-relaxed">
            Prevádzkovateľ môže organizovať súťaže a poskytovať odmeny podľa vlastného uváženia. Na získanie odmeny nevzniká právny nárok. Prevádzkovateľ si vyhradzuje právo zmeniť alebo zrušiť súťaž bez predchádzajúceho upozornenia.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">5. Zakázané správanie</h2>
          <p className="text-text-primary leading-relaxed mb-2">Používateľ nesmie:</p>
          <ul className="list-disc list-inside space-y-1 text-text-primary ml-2">
            <li>používať botov alebo automatizované nástroje,</li>
            <li>vytvárať viacero účtov,</li>
            <li>manipulovať systém bodovania,</li>
            <li>zneužívať chyby platformy.</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">6. Zodpovednosť</h2>
          <p className="text-text-primary leading-relaxed">
            Predicto neposkytuje finančné ani investičné poradenstvo. Prevádzkovateľ nezodpovedá za škody vzniknuté používaním platformy.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-accent-primary mb-3">7. Zmena podmienok</h2>
          <p className="text-text-primary leading-relaxed">
            Prevádzkovateľ si vyhradzuje právo tieto podmienky kedykoľvek upraviť. Používaním platformy po zverejnení zmien používateľ vyjadruje súhlas s novými podmienkami.
          </p>
        </section>
      </div>
    </PageLayout>
  )
}
