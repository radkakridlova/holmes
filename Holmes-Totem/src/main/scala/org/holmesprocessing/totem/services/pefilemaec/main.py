from typing import Optional, Awaitable

import os
from os import path
# import traceback

import tornado
from tornado import web, httpserver, ioloop
from tornado.log import enable_pretty_logging

import time
import json
import requests

import pefile_parser
import pefile_to_maec_converter

# reading configuration file

Metadata = {
    "Name": "pefilemaec",
    "Version": "1.0",
    "Description": "./README.md",
}


def service_config(filename):
    config_path = filename
    try:
        config = json.loads(open(config_path).read())
        return config
    except FileNotFoundError:
        raise tornado.web.HTTPError(500)


Config = service_config("./service.conf")


def send_to_ontology(msg, filename):
    if Config["settings"]["maectoowlwebserviceurl"]:
        json_data = json.dumps(msg)
        payload = {"filename": filename,
                   "data": json_data,
                   "md5": "",
                   "sha1": "",
                   "sha256": "",
                   "service_name": "",
                   "tags": [""],
                   "comment": "",
                   "started_date_time": "",
                   "finished_date_time": ""}
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        r = requests.post(Config["settings"]["maectoowlwebserviceurl"], data=json.dumps(payload), headers=headers)
        r.raise_for_status()
    else:
        print("Error: Bad configuration, maectoowlwebserviceurl is missing.")


class PefilemaecProcess(tornado.web.RequestHandler):
    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        pass

    def get(self):
        try:
            filename = self.get_argument("obj", strip=False)
            full_path = os.path.join('/tmp', filename)
            start_time = time.time()

            parser = pefile_parser.PefileParser(full_path)
            # Instantiate the MAEC translator and perform the translation
            maec_translator = pefile_to_maec_converter.PefileToMAEC(parser)
            # print(maec_translator.get_output())
            data = maec_translator.get_output()

            # Posli data do ontologie
            send_to_ontology(data, filename)

            print("Writing data")
            self.write(data)
            print("--- Done analysing Total time taken %s ms --- \n" % ((time.time() - start_time) * 1000))
        except tornado.web.MissingArgumentError:
            raise tornado.web.HTTPError(400)
        except TypeError as e:
            print(str(e))
            print("Type error")
            raise tornado.web.HTTPError(500)
        except Exception as e:
            # TODO toto sa berie v Holmes Totem ako Sucess lebo sa nepozera obsah ale HTTP kod
            # self.write({"error": traceback.format_exc(2, e)})
            print(str(e))
            print("Error")
            raise tornado.web.HTTPError(500)


class Info(tornado.web.RequestHandler):
    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        pass

    # Emits a string which describes the purpose of the analytics
    def get(self):
        info = """
            <p>{name:s} - {version:s}</p>
            <hr>
            <p>{description:s}</p>
        """.format(
            name=str(Metadata["Name"]).replace("\n", "<br>"),
            version=str(Metadata["Version"]).replace("\n", "<br>"),
            description=str(Metadata["Description"]).replace("\n", "<br>")
        )
        self.write(info)


class PefilemaecApp(tornado.web.Application):
    def __init__(self):
        for key in ["Description"]:
            fpath = Metadata[key]
            if os.path.isfile(fpath):
                with open(fpath) as file:
                    Metadata[key] = file.read()

        handlers = [
            (r'/', Info),
            (r'/analyze/', PefilemaecProcess),
        ]
        settings = dict(
            template_path=path.join(path.dirname(__file__), 'templates'),
            static_path=path.join(path.dirname(__file__), 'static'),
        )
        tornado.web.Application.__init__(self, handlers, **settings)
        self.engine = None


def main():
    enable_pretty_logging()
    server = tornado.httpserver.HTTPServer(PefilemaecApp())
    server.listen(Config["settings"]["port"])
    try:
        tornado.ioloop.IOLoop.current().start()
    except KeyboardInterrupt:
        tornado.ioloop.IOLoop.current().stop()


if __name__ == '__main__':
    main()
