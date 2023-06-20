#!/bin/sh

echo "Please enter the password for csirt:"
stty -echo
read pass
stty echo
echo

echo $pass | sudo -S -u csirt /home/csirt/.rbenv/versions/2.7.8/bin/fakes3 -r /mnt/fakes3_root -p 4567 --license 3285516909 &
/opt/apache-cassandra-3.11.14/bin/cassandra &
/opt/apache-tomee-plume-8.0.14/bin/startup.sh &

docker container stop pefilemaec
docker container remove pefilemaec
docker container run -v /tmp:/tmp:ro --publish 7820:8080 --detach --name pefilemaec pefilemaec:1.0 &
docker container stop cuckoo
docker container remove cuckoo
docker container run -v /tmp:/tmp:ro --publish 7200:8080 --detach --name cuckoo cuckoo:1.0

gnome-terminal --title "Cuckoo Api" -- bash -c "echo $pass | sudo -S -u cuckoo bash -c '. ~/cuckoo/bin/activate && vmcloak-vboxnet0 && cuckoo api --host 192.168.100.103 --port 1337'"
gnome-terminal --title "Cuckoo Web" -- bash -c "echo $pass | sudo -S -u cuckoo bash -c '. ~/cuckoo/bin/activate && cuckoo web --host 127.0.0.1 --port 8080'"
gnome-terminal --title "Cuckoo Debug" -- bash -c "echo $pass | sudo -S -u cuckoo bash -c '. ~/cuckoo/bin/activate && cuckoo --debug'"
sleep 5

gnome-terminal --title "Holmes Gateway" -- /bin/sh -c "cd /usr/local/go/src/holmes/ && ./Holmes-Gateway/Holmes-Gateway --config Holmes-Gateway/config/gateway.conf"
gnome-terminal --title "Holmes Storage" -- /bin/sh -c "cd /usr/local/go/src/holmes/ && ./Holmes-Storage/Holmes-Storage --config Holmes-Storage/config/storage.conf"
gnome-terminal --title "Holmes Frontend" -- /bin/sh -c "cd /usr/local/go/src/holmes/Holmes-Frontend/ && ./Holmes-Frontend --config config/interrogation_config.json"
gnome-terminal --title "Holmes Totem" -- /bin/sh -c "cd /usr/local/go/src/holmes/Holmes-Totem/ && java -jar target/scala-2.11/totem-assembly-0.5.0.jar"
gnome-terminal --title "Holmes Totem Dynamic" -- /bin/sh -c "cd /usr/local/go/src/holmes/ && ./Holmes-Totem-Dynamic/Holmes-Totem-Dynamic --config Holmes-Totem-Dynamic/config/totem-dynamic.conf"
