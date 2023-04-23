# holmes-dp

Zalozene na projekte [HolmesProcessing](https://github.com/HolmesProcessing)

Vela veci ku konfiguracii a fungovaniu je popisane v diplomovej praci v v dp/ priecinku

zadat:
```
chmod +x build.sh
chmod +x run.sh
```

Zbuildovat cez sh build.sh - obsahuje zakomentovany build Holmes-Totem cez sbt
Pri builde cez sbt assembly sa mi vzdy zasekol terminal. Moze sa spustit aj rucne.

Pre build MaecToOwlWeb sa da pouzit maven.
Skompilovat a zbalit do jar cez:

```
mvn clean compile package
```

Zmenit licencny kluc pre fakes3 v run.sh
Spustit fakes3 v inom terminali pred spustenim run.sh
Ak sa nainstaloval cez rbenv, tak z adresara ~/.rbenv/versions/2.7.7/bin/fakes3
Treba pustat cez sudo ak sa mountuje root adresar do /mnt/fakes3_root
Musia bezat aj Apache Cassandra a RabbitMQ
Prvy krat treba spustit Holmes-Storage s parametrami --setup a --objSetup pre nastavenie databaz (vytvori tabulky atd)

Pred prvym spustenim je dobre si rucne spustit kazdy komponent zvlast a zistit ci sa spusti bez chyb
Priklad spustenia je v run.sh

Zakomentovat komponenty v run.sh
Spustit run.sh

Konfiguracie -
Uplne example konfiguraky, ktore urcite nebudu fungovat su v priecinku example_configs
cesty su rovnake ako v moduloch

Konfiguracne subory, ktore su v moduloch by mali fungovat out of box pre localhost.

Apache Fuseki v neskorsich verziach (asi od verzie 4.0) zrusili /upload endpoint a nie je mozne ich pouzit. Verzia 3.14 funguje. Ak sa pouzila novsia verzia, tak treba vymazat konfiguraciu v /home/etc/fuseki.

# Postup pre instalaciu maecreport modulu pre cuckoo

Prevzate od [The MAEC Project](https://github.com/MAECProject/cuckoo/tree/maec5.0-cuckoo2.0/cuckoo/reporting)
Postup je tiez v komentaroch na zaciatku maecreport.py.

Modul sa pouziva na to aby cuckoo vedelo generovat maec reporty.

v cuckoo/common/config.py pridat maecreport do reporting

```
"reporting": {
            ...
            "mattermost": {
                "enabled": Boolean(False),
                "username": String("cuckoo"),
                "url": String(),
                "myurl": String(),
                "show_virustotal": Boolean(False),
                "show_signatures": Boolean(False),
                "show_urls": Boolean(False),
                "hash_filename": Boolean(False),
                "hash_url": Boolean(False),
            },
            "maecreport": {
                "enabled": Boolean(False)
            }
        },
```

v praci v kapitole 3.2 je chyba, ze sa upravuje adresar processing a processing.conf. Spravne je reporting a reporting.conf. 
V postupe na konci je to spravne.

Upravit v  ~/.cuckoo/conf/reporting.conf a pridat riadky

```
[maecreport]
enabled = yes
```

Potom skopirovat subory maecreport.py a maec_api_call_mappings.json z "holmes/maec module for cuckoo" 
do priecinku vo virtual enviromente ~/cuckoo/lib/python2.7/site-packages/cuckoo/reporting/
Z prace tiez nie je jasne co treba kopirovat, tak pisem aspon sem.

Vystup musi mat nazov report.MAEC-5.0.json

# Testovaci klient na posielanie suborov

V priecinku testWS je go program na posielanie suborov do MaecToOwlWeb webservisu. Volanie je rovnake ako je pouzite v Holmes-Totem-Dynamic.

Program prebehne cez adresar testWS/testfiles a posle vsetky subory, ktore su v adresari do webservisu cez POST request. Pre detailne vystupy treba kontrolovat vystup na serveri, ktory obsahuje webservis. Tento klient ukaze iba jednoduchu spravu.

Tiez su prilozene testovacie maec subory. Neobsahuju ale vsetky edge case a je mozne, ze sa vyskytnu neocakavane chyby pri konverzii suborov do OWL.

Treba zbuildovat cez

```
go build .
```

a spustit cez main