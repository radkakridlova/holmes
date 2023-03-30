#!/bin/sh

# pustit fakes3 z konkretnej ruby verzie cez rbenv
#sudo ~/.rbenv/versions/2.7.7/bin/fakes3 -r /mnt/fakes3_root -p 4567 --license 3285516909

# pustit cassandru
# cd ~/apache-cassandra-3.11.14/
# ./bin/cassandra

gnome-terminal -- /bin/sh -c "cd ~/go/src/holmes-dp/ && ./Holmes-Gateway/Holmes-Gateway --config Holmes-Gateway/config/gateway.conf"

gnome-terminal -- /bin/sh -c "cd ~/go/src/holmes-dp/ && ./Holmes-Storage/Holmes-Storage --config Holmes-Storage/config/storage.conf"

gnome-terminal -- /bin/sh -c "cd ~/go/src/holmes-dp/Holmes-Frontend/ && ./Holmes-Frontend --config config/interrogation_config.json"

#gnome-terminal -- /bin/sh -c "cd ~/go/src/holmes-dp/Holmes-Totem/ && java -jar target/scala/totem-assembly-0.5.0.jar"

gnome-terminal -- /bin/sh -c "cd ~/go/src/holmes-dp/Holmes-Totem/ && java -jar target/scala-2.11/totem-assembly-0.5.0.jar"

gnome-terminal -- /bin/sh -c "cd ~/go/src/holmes-dp/ && ./Holmes-Totem-Dynamic/Holmes-Totem-Dynamic --config Holmes-Totem-Dynamic/config/totem-dynamic.conf"





