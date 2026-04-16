-- liquibase formatted sql
-- changeset campusly:016-seed-universities

-- title: Seed università italiane (atenei statali + principali privati) --

INSERT INTO universities (id, name, short_name, city, country, email_domain, website_url, international) VALUES

-- ── NORD-OVEST ──────────────────────────────────────────────────────────────
(gen_random_uuid(), 'Politecnico di Milano',                         'PoliMi',     'Milano',          'Italy', 'polimi.it',          'https://www.polimi.it',          false),
(gen_random_uuid(), 'Università degli Studi di Milano',              'UniMi',      'Milano',          'Italy', 'unimi.it',           'https://www.unimi.it',           false),
(gen_random_uuid(), 'Università degli Studi di Milano-Bicocca',      'UniMiB',     'Milano',          'Italy', 'unimib.it',          'https://www.unimib.it',          false),
(gen_random_uuid(), 'Università IULM',                               'IULM',       'Milano',          'Italy', 'iulm.it',            'https://www.iulm.it',            false),
(gen_random_uuid(), 'Università Commerciale Luigi Bocconi',          'Bocconi',    'Milano',          'Italy', 'unibocconi.it',      'https://www.unibocconi.it',      false),
(gen_random_uuid(), 'Università Cattolica del Sacro Cuore',          'UniCatt',    'Milano',          'Italy', 'unicatt.it',         'https://www.unicatt.it',         false),
(gen_random_uuid(), 'Politecnico di Torino',                         'PoliTo',     'Torino',          'Italy', 'polito.it',          'https://www.polito.it',          false),
(gen_random_uuid(), 'Università degli Studi di Torino',              'UniTo',      'Torino',          'Italy', 'unito.it',           'https://www.unito.it',           false),
(gen_random_uuid(), 'Università degli Studi del Piemonte Orientale', 'UPO',        'Vercelli',        'Italy', 'uniupo.it',          'https://www.uniupo.it',          false),
(gen_random_uuid(), 'Università degli Studi di Genova',              'UniGe',      'Genova',          'Italy', 'unige.it',           'https://www.unige.it',           false),
(gen_random_uuid(), 'Università della Valle d''Aosta',               'UniVdA',     'Aosta',           'Italy', 'univda.it',          'https://www.univda.it',          false),

-- ── LOMBARDIA (extra Milano) ─────────────────────────────────────────────────
(gen_random_uuid(), 'Università degli Studi di Pavia',               'UniPv',      'Pavia',           'Italy', 'unipv.it',           'https://www.unipv.it',           false),
(gen_random_uuid(), 'Università degli Studi di Brescia',             'UniBS',      'Brescia',         'Italy', 'unibs.it',           'https://www.unibs.it',           false),
(gen_random_uuid(), 'Università degli Studi di Bergamo',             'UniBG',      'Bergamo',         'Italy', 'unibg.it',           'https://www.unibg.it',           false),
(gen_random_uuid(), 'Università degli Studi dell''Insubria',         'Insubria',   'Varese',          'Italy', 'uninsubria.it',      'https://www.uninsubria.it',      false),

-- ── NORD-EST ─────────────────────────────────────────────────────────────────
(gen_random_uuid(), 'Università degli Studi di Padova',              'UniPD',      'Padova',          'Italy', 'unipd.it',           'https://www.unipd.it',           false),
(gen_random_uuid(), 'Università degli Studi di Venezia Ca'' Foscari','Ca'' Foscari','Venezia',        'Italy', 'unive.it',           'https://www.unive.it',           false),
(gen_random_uuid(), 'Università IUAV di Venezia',                    'IUAV',       'Venezia',         'Italy', 'iuav.it',            'https://www.iuav.it',            false),
(gen_random_uuid(), 'Università degli Studi di Verona',              'UniVR',      'Verona',          'Italy', 'univr.it',           'https://www.univr.it',           false),
(gen_random_uuid(), 'Università degli Studi di Trento',              'UniTN',      'Trento',          'Italy', 'unitn.it',           'https://www.unitn.it',           false),
(gen_random_uuid(), 'Libera Università di Bolzano',                  'UniBZ',      'Bolzano',         'Italy', 'unibz.it',           'https://www.unibz.it',           false),
(gen_random_uuid(), 'Università degli Studi di Trieste',             'UniTS',      'Trieste',         'Italy', 'units.it',           'https://www.units.it',           false),
(gen_random_uuid(), 'Università degli Studi di Udine',               'UniUD',      'Udine',           'Italy', 'uniud.it',           'https://www.uniud.it',           false),

-- ── EMILIA-ROMAGNA ───────────────────────────────────────────────────────────
(gen_random_uuid(), 'Alma Mater Studiorum - Università di Bologna',  'UniBo',      'Bologna',         'Italy', 'unibo.it',           'https://www.unibo.it',           false),
(gen_random_uuid(), 'Università degli Studi di Parma',               'UniPR',      'Parma',           'Italy', 'unipr.it',           'https://www.unipr.it',           false),
(gen_random_uuid(), 'Università degli Studi di Modena e Reggio E.',  'UniMore',    'Modena',          'Italy', 'unimore.it',         'https://www.unimore.it',         false),
(gen_random_uuid(), 'Università degli Studi di Ferrara',             'UniFE',      'Ferrara',         'Italy', 'unife.it',           'https://www.unife.it',           false),

-- ── TOSCANA ──────────────────────────────────────────────────────────────────
(gen_random_uuid(), 'Università degli Studi di Firenze',             'UniFI',      'Firenze',         'Italy', 'unifi.it',           'https://www.unifi.it',           false),
(gen_random_uuid(), 'Università degli Studi di Pisa',                'UniPI',      'Pisa',            'Italy', 'unipi.it',           'https://www.unipi.it',           false),
(gen_random_uuid(), 'Scuola Normale Superiore',                      'SNS',        'Pisa',            'Italy', 'sns.it',             'https://www.sns.it',             false),
(gen_random_uuid(), 'Scuola Superiore Sant''Anna',                   'SSSA',       'Pisa',            'Italy', 'santannapisa.it',    'https://www.santannapisa.it',    false),
(gen_random_uuid(), 'Università degli Studi di Siena',               'UniSI',      'Siena',           'Italy', 'unisi.it',           'https://www.unisi.it',           false),

-- ── CENTRO ───────────────────────────────────────────────────────────────────
(gen_random_uuid(), 'Sapienza Università di Roma',                   'Sapienza',   'Roma',            'Italy', 'uniroma1.it',        'https://www.uniroma1.it',        false),
(gen_random_uuid(), 'Università degli Studi di Roma Tor Vergata',    'UniRoma2',   'Roma',            'Italy', 'uniroma2.it',        'https://www.uniroma2.it',        false),
(gen_random_uuid(), 'Università degli Studi Roma Tre',               'Roma Tre',   'Roma',            'Italy', 'uniroma3.it',        'https://www.uniroma3.it',        false),
(gen_random_uuid(), 'LUISS Guido Carli',                             'LUISS',      'Roma',            'Italy', 'luiss.it',           'https://www.luiss.it',           false),
(gen_random_uuid(), 'Università Campus Bio-Medico di Roma',          'UCBM',       'Roma',            'Italy', 'unicampus.it',       'https://www.unicampus.it',       false),
(gen_random_uuid(), 'Università degli Studi della Tuscia',           'UniTus',     'Viterbo',         'Italy', 'unitus.it',          'https://www.unitus.it',          false),
(gen_random_uuid(), 'Università degli Studi di Perugia',             'UniPG',      'Perugia',         'Italy', 'unipg.it',           'https://www.unipg.it',           false),
(gen_random_uuid(), 'Università per Stranieri di Perugia',           'UniStraPG',  'Perugia',         'Italy', 'unistrapg.it',       'https://www.unistrapg.it',       false),
(gen_random_uuid(), 'Università degli Studi di Camerino',            'UniCam',     'Camerino',        'Italy', 'unicam.it',          'https://www.unicam.it',          false),
(gen_random_uuid(), 'Università degli Studi di Macerata',            'UniMC',      'Macerata',        'Italy', 'unimc.it',           'https://www.unimc.it',           false),
(gen_random_uuid(), 'Università Politecnica delle Marche',           'UniPM',      'Ancona',          'Italy', 'univpm.it',          'https://www.univpm.it',          false),
(gen_random_uuid(), 'Università degli Studi di Urbino Carlo Bo',     'UniUrb',     'Urbino',          'Italy', 'uniurb.it',          'https://www.uniurb.it',          false),

-- ── SUD ──────────────────────────────────────────────────────────────────────
(gen_random_uuid(), 'Università degli Studi di Napoli Federico II',  'UniNa',      'Napoli',          'Italy', 'unina.it',           'https://www.unina.it',           false),
(gen_random_uuid(), 'Università degli Studi di Napoli Parthenope',   'Parthenope', 'Napoli',          'Italy', 'uniparthenope.it',   'https://www.uniparthenope.it',   false),
(gen_random_uuid(), 'Università degli Studi della Campania L.V.',    'UniCampania','Caserta',         'Italy', 'unicampania.it',     'https://www.unicampania.it',     false),
(gen_random_uuid(), 'Università degli Studi di Salerno',             'UniSA',      'Fisciano',        'Italy', 'unisa.it',           'https://www.unisa.it',           false),
(gen_random_uuid(), 'Università degli Studi del Sannio',             'UniSannio',  'Benevento',       'Italy', 'unisannio.it',       'https://www.unisannio.it',       false),
(gen_random_uuid(), 'Università degli Studi di Cassino e L.M.',      'UniCas',     'Cassino',         'Italy', 'unicas.it',          'https://www.unicas.it',          false),
(gen_random_uuid(), 'Università degli Studi dell''Aquila',           'UniAQ',      'L''Aquila',       'Italy', 'univaq.it',          'https://www.univaq.it',          false),
(gen_random_uuid(), 'Università degli Studi G. d''Annunzio',         'UniChieti',  'Chieti',          'Italy', 'unich.it',           'https://www.unich.it',           false),
(gen_random_uuid(), 'Università degli Studi di Teramo',              'UniTE',      'Teramo',          'Italy', 'unite.it',           'https://www.unite.it',           false),
(gen_random_uuid(), 'Università degli Studi del Molise',             'UniMol',     'Campobasso',      'Italy', 'unimol.it',          'https://www.unimol.it',          false),
(gen_random_uuid(), 'Università degli Studi di Bari Aldo Moro',      'UniBa',      'Bari',            'Italy', 'uniba.it',           'https://www.uniba.it',           false),
(gen_random_uuid(), 'Politecnico di Bari',                           'PoliBa',     'Bari',            'Italy', 'poliba.it',          'https://www.poliba.it',          false),
(gen_random_uuid(), 'Università degli Studi di Foggia',              'UniFg',      'Foggia',          'Italy', 'unifg.it',           'https://www.unifg.it',           false),
(gen_random_uuid(), 'Università del Salento',                        'UniSalento', 'Lecce',           'Italy', 'unisalento.it',      'https://www.unisalento.it',      false),
(gen_random_uuid(), 'Università degli Studi della Basilicata',       'UniBas',     'Potenza',         'Italy', 'unibas.it',          'https://www.unibas.it',          false),
(gen_random_uuid(), 'Università degli Studi della Calabria',         'UniCal',     'Rende',           'Italy', 'unical.it',          'https://www.unical.it',          false),
(gen_random_uuid(), 'Università degli Studi Mediterranea R.C.',      'UniRC',      'Reggio Calabria', 'Italy', 'unirc.it',           'https://www.unirc.it',           false),
(gen_random_uuid(), 'Università degli Studi Magna Graecia',          'UMG',        'Catanzaro',       'Italy', 'unicz.it',           'https://www.unicz.it',           false),

-- ── SICILIA ───────────────────────────────────────────────────────────────────
(gen_random_uuid(), 'Università degli Studi di Palermo',             'UniPA',      'Palermo',         'Italy', 'unipa.it',           'https://www.unipa.it',           false),
(gen_random_uuid(), 'Università degli Studi di Catania',             'UniCT',      'Catania',         'Italy', 'unict.it',           'https://www.unict.it',           false),
(gen_random_uuid(), 'Università degli Studi di Messina',             'UniME',      'Messina',         'Italy', 'unime.it',           'https://www.unime.it',           false),
(gen_random_uuid(), 'Università degli Studi di Enna Kore',           'UniKore',    'Enna',            'Italy', 'unikore.it',         'https://www.unikore.it',         false),

-- ── SARDEGNA ──────────────────────────────────────────────────────────────────
(gen_random_uuid(), 'Università degli Studi di Cagliari',            'UniCA',      'Cagliari',        'Italy', 'unica.it',           'https://www.unica.it',           false),
(gen_random_uuid(), 'Università degli Studi di Sassari',             'UniSS',      'Sassari',         'Italy', 'uniss.it',           'https://www.uniss.it',           false);
