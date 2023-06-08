#!/bin/sh

# pustit fakes3 z konkretnej ruby verzie cez rbenv
#/home/csirt/.rbenv/versions/2.7.8/bin/fakes3 -r /mnt/fakes3_root -p 4567 --license 3285516909 &

# pustit cassandru
#/opt/apache-cassandra-3.11.14/bin/cassandra &

#tomee
#/opt/apache-tomee-plume-8.0.14/bin/startup.sh &

#gnome-terminal --title "Holmes Gateway" -- /bin/sh -c "cd /usr/local/go/src/holmes/ && ./Holmes-Gateway/Holmes-Gateway --config Holmes-Gateway/config/gateway.conf"

gnome-terminal --title "Holmes Storage" -- /bin/sh -c "cd /usr/local/go/src/holmes/ && ./Holmes-Storage/Holmes-Storage --config Holmes-Storage/config/storage.conf"

gnome-terminal --title "Holmes Frontend" -- /bin/sh -c "cd /usr/local/go/src/holmes/Holmes-Frontend/ && ./Holmes-Frontend --config config/interrogation_config.json"

#gnome-terminal --title "Holmes Totem" -- /bin/sh -c "cd /usr/local/go/src/holmes/Holmes-Totem/ && java -jar target/scala-2.11/totem-assembly-0.5.0.jar"

#gnome-terminal --title "Holmes Totem Dynamic" -- /bin/sh -c "cd /usr/local/go/src/holmes/ && ./Holmes-Totem-Dynamic/Holmes-Totem-Dynamic --config Holmes-Totem-Dynamic/config/totem-dynamic.conf"
