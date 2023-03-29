FROM python:3.8-slim

# add tornado
RUN pip3 install tornado
RUN pip3 install requests

# create folder
RUN mkdir -p /service
WORKDIR /service

# add dependencies for pefilemaec
RUN pip3 install --upgrade pefile

# add the files to the container
ADD . /service

# add the configuration file (possibly from a storage uri)
ARG conf=service.conf
ADD $conf /service/service.conf

CMD ["python3", "/service/main.py"]
