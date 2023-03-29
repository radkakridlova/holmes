Na build image: sudo docker image build --no-cache -t pefilemaec:1.0 .

sudo docker container run -v /tmp:/tmp:ro --publish 7820:8080 --detach --name pefilemaec pefilemaec:1.0

