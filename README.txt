Zalozene na projekte https://github.com/HolmesProcessing

Vela veci ku konfiguracii a fungovaniu je popisane v diplomovej praci v v dp/ priecinku

zadat chmod +x build.sh
chmod +x run.sh

Zbuildovat cez sh build.sh - obsahuje zakomentovany build Holmes-Totem cez sbt
Pri builde cez sbt assembly sa mi vzdy zasekol terminal. Moze sa spustit aj rucne.
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
