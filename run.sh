#!/bin/sh

#fakes3 -r /mnt/fakes3_root -p 4567 --license 3285516909

gnome-terminal -- /bin/sh -c "cd /home/h/go/src/holmes-dp/ && ./Holmes-Gateway/Holmes-Gateway --config Holmes-Gateway/config/gateway.conf"

gnome-terminal -- /bin/sh -c "cd /home/h/go/src/holmes-dp/ && ./Holmes-Storage/Holmes-Storage --config Holmes-Storage/config/storage.conf"

gnome-terminal -- /bin/sh -c "cd /home/h/go/src/holmes-dp/Holmes-Frontend/ && ./Holmes-Frontend --config config/interrogation_config.json"

#gnome-terminal -- /bin/sh -c "cd /home/h/go/src/holmes-dp/Holmes-Totem/ && java -jar target/scala/totem-assembly-0.5.0.jar"

gnome-terminal -- /bin/sh -c "cd /home/h/go/src/holmes-dp/Holmes-Totem/ && java -jar target/scala-2.11/totem-assembly-0.5.0.jar"

gnome-terminal -- /bin/sh -c "cd /home/h/go/src/holmes-dp/ && ./Holmes-Totem-Dynamic/Holmes-Totem-Dynamic --config Holmes-Totem-Dynamic/config/totem-dynamic.conf"





