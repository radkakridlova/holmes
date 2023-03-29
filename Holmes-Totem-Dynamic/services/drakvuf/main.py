# imports for tornado
from typing import Optional, Awaitable

import tornado
from tornado import web, httpserver, ioloop

# imports for logging
import traceback
import os
from os import path

import json
from drakvuf import Drakvuf


# Reading configuration file
def service_config(filename):
    configPath = filename
    try:
        config = json.loads(open(configPath).read())
        return config
    except FileNotFoundError:
        raise tornado.web.HTTPError(500)


# Get service meta information and configuration
Config = service_config("./service.conf")
Drakvuf = Drakvuf(Config["DrakvufURL"])


Metadata = {
    "Name": "Drakvuf",
    "Version": "0.7",
    "Description": "./README.md",
}


class DrakvufStatus(tornado.web.RequestHandler):
    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        pass

    def get(self):
        resp = dict()
        resp["Degraded"] = False
        resp["Error"] = ""
        resp["FreeSlots"] = 0

        try:
            resp = Drakvuf.get_status()
            if int(resp["Status"]) != 200:
                raise Exception("Error: Drakvuf service returned status " + str(resp["Status"]))
        except Exception as e:
            print(str(e))
            self.set_status(500, str(e))
            resp["Error"] = str(e)

        self.write(resp)


class DrakvufFeed(tornado.web.RequestHandler):
    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        pass

    def get(self):
        resp = dict()
        resp["Error"] = ""
        resp["TaskID"] = ""

        try:
            filename = self.get_argument("obj", strip=False)
            fullPath = os.path.join('/tmp/', filename)
            task_id = Drakvuf.new_task(fullPath, filename)
            resp["TaskID"] = task_id

        except tornado.web.MissingArgumentError:
            raise tornado.web.HTTPError(400)
        except Exception as e:
            print(str(e))
            Drakvuf.freeSlots = 1
            self.set_status(500, str(e))
            resp["Error"] = str(e)

        self.write(resp)


class DrakvufCheck(tornado.web.RequestHandler):
    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        pass

    def get(self):
        resp = dict()
        resp["Error"] = ""
        resp["Done"] = False

        try:
            task_id = self.get_argument("taskid", strip=False)
            status = Drakvuf.task_status(str(task_id))
            if status == "done":
                resp["Done"] = True

        except tornado.web.MissingArgumentError:
            raise tornado.web.HTTPError(400)
        except Exception as e:
            print(str(e))
            Drakvuf.freeSlots = 1
            self.set_status(500, str(e))
            resp["Error"] = str(e)

        self.write(resp)


class DrakvufResults(tornado.web.RequestHandler):
    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        pass

    def get(self):
        resp = dict()
        resp["Error"] = ""
        resp["Results"] = ""

        try:
            task_id = self.get_argument("taskid", strip=False)
            if Config["MAEC"]:
                resp["Results"] = Drakvuf.task_report_maec(task_id)
            else:
                resp["Results"] = Drakvuf.task_report(task_id)

        except tornado.web.MissingArgumentError:
            raise tornado.web.HTTPError(400)
        except Exception as e:
            print(str(e))
            Drakvuf.freeSlots = 1
            self.set_status(500, str(e))
            resp["Error"] = str(e)

        self.write(resp)


class Info(tornado.web.RequestHandler):
    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        pass

    # Emits a string which describes the purpose of the analytics
    def get(self):
        info = """
            <p>{name:s} - {version:s}</p>
            <hr>
            <p>{description:s}</p>
            <hr>
        """.format(
            name=str(Metadata["Name"]).replace("\n", "<br>"),
            version=str(Metadata["Version"]).replace("\n", "<br>"),
            description=str(Metadata["Description"]).replace("\n", "<br>"),
        )
        self.write(info)


class DrakvufServiceApp(tornado.web.Application):
    def __init__(self):

        for key in ["Description"]:
            fpath = Metadata[key]
            if os.path.isfile(fpath):
                with open(fpath) as file:
                    Metadata[key] = file.read()

        handlers = [
            (r'/status/', DrakvufStatus),
            (r'/feed/', DrakvufFeed),
            (r'/check/', DrakvufCheck),
            (r'/results/', DrakvufResults),
        ]

        settings = dict(
            template_path=path.join(path.dirname(__file__), 'templates'),
            static_path=path.join(path.dirname(__file__), 'static'),
        )
        tornado.web.Application.__init__(self, handlers, **settings)
        self.engine = None


def main():
    server = tornado.httpserver.HTTPServer(DrakvufServiceApp())
    server.listen(Config["HTTPBinding"])
    try:
        tornado.ioloop.IOLoop.current().start()
    except KeyboardInterrupt:
        tornado.ioloop.IOLoop.current().stop()


if __name__ == '__main__':
    main()
