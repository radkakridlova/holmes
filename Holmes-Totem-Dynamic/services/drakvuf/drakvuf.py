import requests
import os
import shutil

from pathlib import Path
from drakvuf_to_maec import DrakvufParser


class Drakvuf:
    def __init__(self, url):
        self.URL = url
        # toto by mal riesit sandbox, nie servis ale v sandboxe to zatial nie je implementovane.
        self.freeSlots = 1
        self.tasks = dict()

    def get_status(self):
        resp = dict()
        r = requests.get(self.URL + "/list")
        r.raise_for_status()

        resp["Degraded"] = False
        resp["Error"] = ""
        resp["FreeSlots"] = self.freeSlots
        resp["Status"] = r.status_code
        return resp

    def new_task(self, filepath, file_name):
        # read file as bytes
        with open(filepath, "rb") as data:
            files = {'file': (file_name, data)}
            r = requests.post(self.URL + "/upload", files=files, allow_redirects=False)
            response = r.json()
            task_id = response["task_uid"]

            # Vytvori sa novy priecinok na ukladanie
            filename = file_name
            if "." in filename:
                filename = filename.split(".")[0]
            logPath = os.path.join(filename+"_logs")
            Path(logPath).mkdir(exist_ok=True)

        self.tasks[task_id] = file_name

        return task_id

    def task_status(self, task_id):
        r = requests.get(self.URL + "/status/" + str(task_id))
        r.raise_for_status()
        return r.json()["status"]

    def task_report(self, task_id):
        # TODO
        raise Exception()

    def task_report_maec(self, task_id):
        # TaskID je zapisane ak sa pouzil upload
        if task_id in self.tasks:
            filename = self.tasks[task_id]
            if "." in filename:
                filename = filename.split(".")[0]
        else:
            raise Exception("bad taskid")

        output_logs = ["procmon", "regmon", "filetracer", "filedelete"]
        log_path = os.path.join(filename + '_logs/')
        # Ziskaju sa styri vystupy a ulozia sa do lokalneho adresara
        for output_log in output_logs:
            try:
                r = requests.get(self.URL + "/logs/" + str(task_id) + "/" + output_log)
                r.raise_for_status()
                # Stiahne vsetky log subory po jednom do lokalneho adresara
                full_path = os.path.join(filename+"_logs/", output_log + '.log')
                with open(full_path, "w") as f:
                    f.write(r.text)
            except Exception as e:
                print(str(e))

        # Parsuje a vytvori maec report, vrati nazov vystupneho suboru
        p = DrakvufParser()
        maec_report = p.drakvuf_parse(self.tasks[task_id], log_path)

        shutil.rmtree(filename+"_logs/")
        self.tasks.pop(task_id, None)

        return maec_report
