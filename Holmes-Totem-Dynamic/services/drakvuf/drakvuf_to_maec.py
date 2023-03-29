import json
import os
import sys
import hashlib
from pprint import pprint

from maecreport2 import MaecReport2


class Hashes:
    def __init__(self):
        self.md5 = None
        self.sha1 = None
        self.sha256 = None
        self.sha512 = None

    def calculate_hashes(self, target_path):
        # Prevzate z https://stackoverflow.com/questions/22058048/hashing-a-file-in-python
        # BUF_SIZE is totally arbitrary, change for your app!
        BUF_SIZE = 65536  # lets read stuff in 64kb chunks!

        md5 = hashlib.md5()
        sha1 = hashlib.sha1()
        sha256 = hashlib.sha256()
        sha512 = hashlib.sha512()

        with open(target_path, 'rb') as f:
            while True:
                data = f.read(BUF_SIZE)
                if not data:
                    break
                md5.update(data)
                sha1.update(data)
                sha256.update(data)
                sha512.update(data)
        #
        # print("MD5: {0}".format(md5.hexdigest()))
        # print("SHA1: {0}".format(sha1.hexdigest()))
        # print("SHA256: {0}".format(sha256.hexdigest()))
        # print("SHA512: {0}".format(sha512.hexdigest()))

        self.md5 = md5.hexdigest()
        self.sha1 = sha1.hexdigest()
        self.sha256 = sha256.hexdigest()
        self.sha512 = sha512.hexdigest()


class DrakvufParser:
    def __init__(self):
        self.plugins = ["procmon", "regmon", "memdump", "inject", "delaymon", "cpuidmon", "exmon", "filetracer",
                        "filedelete", "drakmon"]
        self.bad_chars = ['\\??\\', '\\?', '￿??\\']
        self.filtered_processes = ["system", "svchost.exe", "conhost.exe", "services.exe", "searchindexer.exe",
                                   "searchfilterhost.exe", "searchprotocolhost.exe", "audiodg.exe", "mobsync.exe",
                                   "wmiprvse.exe", "mscorsvw.exe"]#, "explorer.exe"]
        self.apis_in_process = dict()
        self.processes_by_pid = dict()
        self.apis_in_process_by_pid = dict()
        self.out_dict = dict()

    def add_process(self, json_line):
        """
        Metoda vytvori proces zo zadaneho riadku ak este neexistuje ak existuje nevytvara novy.
        Proces sa identifikuje podla PID.
        :param json_line: riadok z drakvuf log suboru v json formate nacitany cey json.loads
        :return: novy alebo existujuci proces
        """
        if json_line["PID"] not in self.processes_by_pid:
            process = dict()
            if "ProcessName" in json_line:
                process_name = json_line["ProcessName"].replace("\\Device\\HarddiskVolume2", "C:")
                process["process_path"] = process_name
            process["calls"] = []
            if "PID" in json_line:
                process["pid"] = json_line["PID"]
            if "PPID" in json_line:
                process["ppid"] = json_line["PPID"]
            if "CmdLine" in json_line:
                process["command_line"] = json_line["CmdLine"]
            if "ProcessName" in json_line:
                process["process_name"] = str(json_line["ProcessName"]).split("\\")[-1].lower()
            process["modules"] = []
            process["time"] = 0
            if "TID" in json_line:
                process["tid"] = json_line["TID"]
            if "TimeStamp" in json_line:
                process["first_seen"] = float(json_line["TimeStamp"])
            process["type"] = "process"

            self.out_dict["processes"].append(process)  # toto sa vypisuje
            self.processes_by_pid[json_line["PID"]] = process
            self.apis_in_process_by_pid[json_line["PID"]] = []
        else:
            process = self.processes_by_pid[json_line["PID"]]

        return process

    def process_logs(self, file_path):
        """
        Spracuje vsetky logy a vysledky prida vysledneho dictionary.
        :param file_path: cesta k log suborom
        """
        self.out_dict["processes"] = []

        try:
            self.process_regmon(os.path.join(file_path, 'regmon.log'))
        except Exception as e:
            print(e)
        try:
            self.process_filetracer(os.path.join(file_path, 'filetracer.log'))
        except Exception as e:
            print(e)
        try:
            self.process_filedelete(os.path.join(file_path, 'filedelete.log'))
        except Exception as e:
            print(e)
        try:
            self.process_procmon(os.path.join(file_path, 'procmon.log'))
        except Exception as e:
            print(e)
        # self.process_drakmon("C:\\Users\\h\\Desktop\\drakvuf outputs\\drakvuf-output2\\output\\drakmon.log")

    #{'ValueName', 'UserName', 'PID', 'TID', 'Plugin', 'TimeStamp', 'Method', 'PPID', 'Value', 'Key', 'UserId',
     #'ProcessName'}
    def process_regmon(self, file_path):
        with open(file_path, 'r', encoding="utf-8") as f:
            print("Processing regmon")
            for line in f:
                err = False
                for filtered_process in self.filtered_processes:
                    if "ProcessName" not in line or filtered_process in line.lower():
                        err = True
                        break
                if err:
                    continue

                line = json.loads(line.rstrip())

                process = self.add_process(line)

                # API volanie je duplicitne - nedava ziadne nove udaje - ak je metoda a kluc rovnaky
                if "Method" in line and "Key" in line and (line["Method"], line["Key"]) not in self.apis_in_process_by_pid[line["PID"]]:
                    registry = dict()
                    registry["category"] = "registry"
                    registry["api"] = line["Method"]
                    registry["time"] = float(line["TimeStamp"])

                    registry["arguments"] = dict()
                    key = line["Key"].lower()
                    if "\\registry\\machine" in key:
                        key = key.replace("\\registry\\machine", "HKEY_LOCAL_MACHINE")
                    elif "\\registry\\user" in key:
                        key = key.replace("\\registry\\user", "HKEY_CURRENT_USER")
                    registry["arguments"]["regkey"] = key

                    if "ValueName" in line:
                        registry["arguments"]["value_name"] = line["ValueName"]
                    # Strasne dlhy text bytov...
                    #if "Value" in line:
                    #    registry["arguments"]["data"] = line["Value"]

                    process["calls"].append(registry)
                    self.apis_in_process_by_pid[line["PID"]].append((line["Method"], line["Key"]))

    # {"Control": "SE_DACL_PRESENT",
    # "Dacl": [{
    # "Type": "ACCESS_ALLOWED_ACE_TYPE",
    # "AccessMask": "GENERIC_ALL", "SID": "S-1-5-32-544"},
    # {"Type": "ACCESS_ALLOWED_ACE_TYPE",
    # "AccessMask": "GENERIC_ALL",
    # "SID": "S-1-5-21-566405880-83951504-3990687655-1000"}

    # {'Plugin', 'UserName', 'CreateOptions', 'PPID', 'ProcessName', 'Method', 'UserId', 'FileName', 'PID',
    # 'SecurityDescriptor', 'FileAttributes', 'Handle', 'TimeStamp', 'ObjectAttributes', 'TID', 'DesiredAccess',
    # 'ShareAccess', 'CreateDisposition'}
    def process_filetracer(self, filetracer_file_path):
        with open(filetracer_file_path, 'r', encoding="utf-8") as f:
            print("Processing filetracer")
            for line in f:
                err = False
                for filtered_process in self.filtered_processes:
                    if "ProcessName" not in line or filtered_process in line.lower():
                        err = True
                        break
                if err:
                    continue

                line = json.loads(line.rstrip())

                process = self.add_process(line)

                if "FileName" in line and "Method" in line:
                    filename = line["FileName"]
                    for c in self.bad_chars:
                        filename = filename.replace(c, "")

                    if (line["Method"], filename) not in self.apis_in_process_by_pid[line["PID"]]:
                        file = dict()
                        file["category"] = "file"
                        file["api"] = line["Method"]
                        file["time"] = float(line["TimeStamp"])

                        file["arguments"] = dict()
                        if "Handle" in line:
                            file["arguments"]["file_handle"] = line["Handle"]
                        if "CreateOptions" in line:
                            file["arguments"]["create_options"] = line["CreateOptions"]

                        # file["arguments"]["filepath_r"] = line["FileName"] Nepouziva sa pri preklade
                        file["arguments"]["filepath"] = filename.lower()

                        file["flags"] = dict()
                        if "ShareAccess" in line:
                            file["flags"]["share_access"] = line["ShareAccess"]
                        if "FileAttributes" in line:
                            file["flags"]["file_attributes"] = line["FileAttributes"]
                        if "DesiredAccess" in line:
                            file["flags"]["desired_access"] = line["DesiredAccess"]
                        if "CreateDisposition" in line:
                            file["flags"]["create_disposition"] = line["CreateDisposition"]
                        if "SecurityDescriptor" in line:
                            file["flags"]["security_descriptor"] = line["SecurityDescriptor"]
                            # print(line["SecurityDescriptor"])
                        if "ObjectAtributes" in line:
                            file["flags"]["object_attributes"] = line["ObjectAtributes"]

                        process["calls"].append(file)

                        self.apis_in_process_by_pid[line["PID"]].append((line["Method"], filename))

    # {'TimeStamp', 'TID', 'FileName', 'UserId', 'Size',
    # 'PPID', 'UserName', 'Plugin', 'FlagsExpanded', 'PID',
    # 'ProcessName', 'Flags', 'Method'}
    def process_filedelete(self, filedelete_file_path):
        with open(filedelete_file_path, 'r', encoding="utf-8") as f:
            print("Processing filedelete")
            for line in f:
                err = False
                for filtered_process in self.filtered_processes:
                    if "ProcessName" not in line or filtered_process in line.lower():
                        err = True
                        break
                if err:
                    continue

                line = json.loads(line.rstrip())

                process = self.add_process(line)

                if (line["Method"], line["FileName"], line["Flags"]) not in self.apis_in_process_by_pid[line["PID"]]:
                    deleted_file = dict()
                    deleted_file["category"] = "system"
                    deleted_file["api"] = line["Method"]
                    deleted_file["time"] = float(line["TimeStamp"])

                    deleted_file["arguments"] = dict()
                    deleted_file["arguments"]["file_name"] = line["FileName"].lower()

                    deleted_file["flags"] = dict()
                    if "Flags" in line:
                        deleted_file["flags"]["flags"] = line["Flags"]
                    if "FlagsExpanded" in line:
                        deleted_file["flags"]["flags_expanded"] = line["FlagsExpanded"]

                    process["calls"].append(deleted_file)

                    self.apis_in_process_by_pid[line["PID"]].append((line["Method"], line["FileName"], line["Flags"]))

    #{'UserId', 'NewPid', 'DesiredAccess', 'NewProtectWin32', 'DllPath', 'ImagePathName', 'Method', 'CmdLine', 'ExitPid',
    # 'TID', 'TimeStamp', 'Status', 'PPID', 'ObjectAttributes', 'ClientName', 'UserName', 'ProcessName', 'NewState',
    # 'ClientID', 'Plugin', 'CurDir', 'ProcessHandle', 'ExitStatus', 'PID'}
    def process_procmon(self, procmon_file_path):
        with open(procmon_file_path, 'r', encoding="utf-8") as f:
            print("Processing procmon")
            for line in f:
                err = False
                for filtered_process in self.filtered_processes:
                    if "ProcessName" not in line or filtered_process in line.lower():
                        err = True
                        break
                if err:
                    continue

                line = json.loads(line.rstrip())
                if "Method" in line and line["Method"] is None:
                    continue

                process = self.add_process(line)
                if line["Method"] not in self.apis_in_process_by_pid[line["PID"]]:
                    process_call = dict()
                    process_call["category"] = "process"
                    process_call["api"] = line["Method"]
                    process_call["time"] = float(line["TimeStamp"])

                    process_call["arguments"] = dict()
                    process_call["arguments"]["process_identifier"] = line["PID"]

                    # process_call["flags"] = dict()
                    # if "Flags" in line:
                    #     process_call["flags"]["flags"] = line["Flags"]
                    # if "FlagsExpanded" in line:
                    #     process_call["flags"]["flags_expanded"] = line["FlagsExpanded"]
                    if "CmdLine" in line:
                        command_line = line["CmdLine"].replace("\\\\", "\\").replace("\\\"", "").replace('\\??\\', "")
                        process["command_line"] = command_line

                    process["calls"].append(process_call)

                    self.apis_in_process_by_pid[line["PID"]].append(line["Method"])


    # syscall {'UserName', 'PPID', 'VCPU', 'TimeStamp', 'TID', 'Method',
    # 'CR3', 'UserId', 'Module', 'Args', 'ProcessName', 'PID', 'Type'}
    # sysret {'UserName', 'PPID', 'VCPU', 'TimeStamp', 'TID', 'Method',
    # 'CR3', 'UserId', 'Module', 'Args', 'ProcessName', 'PID', 'Type'}
    # https://www.evilsocket.net/2014/02/11/on-windows-syscall-mechanism-and-syscall-numbers-extraction-methods/
    # Toto trva strasne dlho
    def process_syscall(self, file_path):
        with open(file_path + "\\syscall.log", 'r', encoding="utf-8") as f:
            print("Processing syscall")
            for line in f:
                line = json.loads(line.rstrip())
                # try:
                #     fixed_line = json.loads(line.rstrip())
                # except Exception as e:
                #     # Toto su fixy kvole nevalidnemu JSON vystupu z Drakvuf
                #     fixed_line = line.replace("SessionID", "1")
                #     fixed_line = fixed_line.replace("\"PID\" :", ",\"PID\" :")
                #     fixed_line = fixed_line.replace("[\"Args\":", "")
                #     fixed_line = fixed_line.replace("[\"Ret\": ", "[{\"Ret\": ")
                #     fixed_line = fixed_line.replace("\"]", "\"}]")
                #
                #     try:
                #         fixed_line = json.loads(fixed_line.rstrip())
                #     except Exception as e:
                #         # Ak to aj po upravach neprejde, vypis riadok a pokracuj
                #         print(fixed_line)
                #         continue

                if "Type" in line and line["Type"] == "syscall":
                    process = self.add_process(line)

                    # API volanie je duplicitne - nedava ziadne nove udaje - ak je metoda a kluc rovnaky
                    if (line["Method"], line["Module"], line["Args"], line["Type"]) not in \
                            self.apis_in_process_by_pid[line["PID"]]:
                        syscall = dict()
                        syscall["category"] = "system"
                        syscall["api"] = line["Method"]
                        syscall["time"] = line["TimeStamp"]
                        syscall["type"] = line["Type"]
                        syscall["module"] = line["Module"]
                        syscall["arguments"] = line["Args"]

                        # # ValueName
                        process["calls"].append(syscall)

                        self.apis_in_process_by_pid[line["PID"]].append(
                            (line["Method"], line["Module"],
                             line["Args"], line["Type"]))

    @staticmethod
    def write_target(target_path, filename):
        hashes = Hashes()
        hashes.calculate_hashes(target_path)
        size = os.path.getsize(target_path)

        target_dict = dict()
        target_dict["category"] = "file"
        target_dict["file"] = dict()
        target_dict["file"]["yara"] = []
        target_dict["file"]["sha1"] = str(hashes.sha1)
        target_dict["file"]["name"] = filename
        target_dict["file"]["type"] = "file"
        target_dict["file"]["sha256"] = str(hashes.sha256)
        target_dict["file"]["urls"] = []
        target_dict["file"]["size"] = size
        target_dict["file"]["sha512"] = str(hashes.sha512)
        target_dict["file"]["md5"] = str(hashes.md5)

        return target_dict

    def drakvuf_parse(self, filename, file_path):
        output_dict = dict()
        output_dict["info"] = {"version": "0.7"}
        output_dict["target"] = self.write_target(os.path.join("/tmp/", filename), filename)
        self.process_logs(file_path)
        output_dict["behavior"] = self.out_dict

        #with open("d/drakvuf-out.json", "w", encoding="utf-8") as fOut:
        #    json.dump(output_dict, fOut, indent=4)
            # json.dump(processes_by_pid, fOut, indent=4)

        # with open("reports/report.json", "r", encoding="utf-8") as f:
        #     data = json.load(f)
        maec_report = MaecReport2(output_dict)
        #maec_report.output()
        return maec_report.get_json_output()


if __name__ == '__main__':
    #target_path = "C:\\Users\\h\\Desktop\\drakvuf outputs\\drakvuf-output\\output"
    #target_path = "C:\\Users\\h\\Desktop\\drakvuf outputs\\drakvuf-output2\\output"
    #target_path = "C:\\Users\\h\\Desktop\\drakvuf outputs\\drakvuf-output3\\output"
#//    target_path = "C:\\Users\\h\\Desktop\\drakvuf outputs\\drakvuf-output5"
    # exe_path = "C:\\Users\\h\\Desktop\\drakvuf outputs\\drakvuf-output2\\malwar.exe"

    drakvuf = DrakvufParser()
    #log_path = os.path.join('/tmp/', 'abcd' + '/')
    # Parsuje a vytvori maec report, vrati nazov vystupneho suboru
    maec_report = drakvuf.drakvuf_parse('malwar.exe', target_path)
    # pprint(maec_report)
