import uuid
import json
from collections import OrderedDict
import re

# Dictionary key sort order, for sorted output
sort_order = ["id", "type", "name", "value", "hashes", "path", "size",
              "parent_directory_ref", "mime_type", "key", "values", "data",
              "data_type", "src_ref", "src_port", "dst_ref", "dst_port",
              "protocols", "pid", "created", "parent_ref", "command_line",
              "service_name", "display_name", "service_type", "binary_ref",
              "timestamp", "input_object_refs", "output_object_refs",
              "signature_type", "instance_object_refs", "labels",
              "capabilities", "dynamic_features", "static_features",
              "analysis_metadata", "description", "severity", "strings",
              "process_tree", "is_automated", "analysis_type", "tool_refs",
              "vm_ref", "summary", "process_ref", "ordinal_position",
              "initiated_action_refs", "schema_version", "maec_objects",
              "observable_objects", "relationships", "extensions",
              "triggered_signatures"]


def convert_to_unicode(inp):
    """Convert any strings in a dict to UTF-8 Unicode"""
    if isinstance(inp, OrderedDict):
        od = OrderedDict()
        for key, value in inp.items():
            od[convert_to_unicode(key)] = convert_to_unicode(value)
        return od
    elif isinstance(inp, dict):
        return {convert_to_unicode(key): convert_to_unicode(value)
                for key, value in inp.items()}
    elif isinstance(inp, list):
        return [convert_to_unicode(element) for element in inp]
    # elif isinstance(input, str) or isinstance(input, int) \
    #         or isinstance(input, float):
    #     return unicode(input).decode('utf-8')
    else:
        return inp


def sort_dict(d):
    """Sort and return an observed dict for an input dictionary"""
    od = OrderedDict(
        sorted(d.items(),
               key=lambda k_v: sort_order.index(k_v[0])))
    return od


class PefileToMAEC(object):
    def __init__(self, pefile_parser):
        self.pefile_parser = pefile_parser
        self.package = {"type": "package",
                        "id": None,
                        "schema_version": "5.0",
                        "maec_objects": [],
                        "observable_objects": {}}
        self.primary_instance = None
        self.currentObjectIndex = 0
        self.objectMap = {}
        self.generate_maec()

    def create_malware_instance(self, file_data, file_hashes):
        """Create a base Malware Instance"""
        malwareInstance = {"type": "malware-instance", "id": "malware-instance--" + str(uuid.uuid4())}
        file_obj_id, file_obj = self.create_file_obj(file_data)

        # Add the hashes for the Malware Instance Object Attributes
        file_obj["hashes"] = file_hashes

        malwareInstance['instance_object_refs'] = [file_obj_id]
        self.package['observable_objects'][file_obj_id] = sort_dict(file_obj)
        return malwareInstance

    def create_obj_id(self):
        """Create and return a Cyber Observable Object ID"""
        id_ = str(self.currentObjectIndex)
        self.currentObjectIndex += 1
        return id_

    def create_file_obj(self, file_dict):
        obj_id = self.create_obj_id()

        file_obj = {
            "type": "file",
            "name": file_dict['file_name'],
            "size": file_dict['size_in_bytes']
        }
        if 'type' in file_dict and file_dict['type']:
            file_obj['mime_type'] = file_dict['type']

        # If file path given, have to create another STIX object
        # for a directory, then reference it

        # Dropped files use the "file_path" field for the actual directory of
        # dropped file
        if 'filepath' in file_dict and file_dict['file_path']:
            self.create_directory_from_file_path(file_obj, file_dict['file_path'])

        # Target file uses the "path" field for recording directory
        elif "path" in file_dict and file_dict['path']:
            self.create_directory_from_file_path(file_obj, file_dict['path'])

        return obj_id, file_obj

    def create_directory_from_file_path(self, file_obj, path):
        """Create and add a Directory to a File"""

        file_name = re.split(r'[\\/]', path)[-1]
        # Make sure we have a file name and not just a directory
        if file_name or ('name' in file_obj and file_obj['name'] != path):
            dir_path = path.rstrip(file_name)
            dir_obj = self.create_directory_obj(dir_path)
            # Add the file name to the File Object if it does not already exist
            if 'name' not in file_obj or file_obj['name'] == 'null' or '\\' in file_obj['name'] or '/' in file_obj['name']:
                file_obj['name'] = file_name
            if dir_obj["path"]:
                dir_obj_id = self.deduplicate_obj(dir_obj)
                # Insert parent directory reference in file obj
                file_obj['parent_directory_ref'] = dir_obj_id
        # We actually have a directory and not a file
        else:
            dir_obj = self.create_directory_obj(path)
            file_obj['type'] = 'directory'
            file_obj['path'] = dir_obj['path']
            file_obj.pop('name', None)

    @staticmethod
    def create_directory_obj(dir_path):
        """Create and return a Directory Object from an input path"""
        if "\\" in dir_path:
            dir_path = dir_path.rstrip("\\")
        elif "/" in dir_path:
            dir_path = dir_path.rstrip("/")
        dir_obj = {
            'type': 'directory',
            'path': dir_path
        }
        return dir_obj

    def deduplicate_obj(self, obj):
        """Deduplicate a Cyber Observable Object by checking to see if it already exists in self.objectMap"""
        obj_hash = json.dumps(obj, sort_keys=True)
        if obj_hash not in self.objectMap:
            obj_id = self.create_obj_id()
            self.package['observable_objects'][obj_id] = sort_dict(obj)
            self.objectMap[obj_hash] = obj_id
            return obj_id
        elif obj_hash in self.objectMap:
            return self.objectMap[obj_hash]

    # def populate(self, entry_dict, static_bundle, malware_subject=None):
    #     if 'file' in entry_dict and len(entry_dict['file'].keys()) > 1:
    #         file_dict = self.create_malware_instance(entry_dict['file'])
    #         if malware_subject:
    #             malware_subject.malware_instance_object_attributes = file_dict
    #             # Add the hashes for the Malware Instance Object Attributes
    #             data = open(self.pefile_parser.infile, 'rb').read()
    #             if data:
    #                 md5_hash = hashlib.md5(data).hexdigest()
    #                 sha1_hash = hashlib.sha1(data).hexdigest()
    #                 malware_subject.malware_instance_object_attributes.properties.add_hash(md5_hash)
    #                 malware_subject.malware_instance_object_attributes.properties.add_hash(sha1_hash)
    #         else:
    #             static_bundle.add_object(Object.from_dict(file_dict))
    #     if 'pe' in entry_dict and len(entry_dict['pe'].keys()) > 1:
    #         pe_dict = self.create_malware_instance(entry_dict['pe'])
    #         static_bundle.add_object(Object.from_dict(pe_dict))
    #
    # def generate_analysis(self, static_bundle):
    #     analysis = {"type": 'triage', "method": 'static'}
    #     analysis["tool"] = {'id': "tool--" + str(uuid.uuid4()), 'vendor': 'Ero Carrera', 'name': 'pefile'}
    #     findings_bundle_reference = []
    #     if self.bundle_has_content(static_bundle):
    #         findings_bundle_reference.append(BundleReference.from_dict({'bundle_idref': static_bundle.id_}))
    #     analysis.findings_bundle_reference = findings_bundle_reference
    #     return analysis

    @staticmethod
    def bundle_has_content(bundle):
        if bundle.actions and len(bundle.actions) > 0:
            return True
        if bundle.objects and len(bundle.objects) > 0:
            return True
        if bundle.behaviors and len(bundle.behaviors) > 0:
            return True
        return False

    # def setup_primary_malware_instance(self):
    #     """Instantiate the primary (target) Malware Instance"""
    #     malwareInstance = dict()
    #     if "target" in self.results and self.results['target']['category'] == 'file':
    #         malwareInstance = self.create_malware_instance(self.results['target']['file'])
    #
    #         # Add dynamic features
    #         malwareInstance['dynamic_features'] = {}
    #
    #         # Grab static strings
    #         if "strings" in self.results and self.results['strings']:
    #             malwareInstance["static_features"] = {"strings": self.results['strings']}
    #
    #     elif "target" in self.results and self.results['target']['category'] == 'url':
    #         malwareInstance = {"type": "malware-instance", 'id': "malware-instance--" + str(uuid.uuid4()),
    #                            'instance_object_refs': [{
    #                                "type": "url",
    #                                "value": self.results['target']['url']
    #                            }]}
    #         # Add malwareInstance to package
    #         self.package['maec_objects'].append(sort_dict(malwareInstance))

    def populate_optional_header(self, pe_info):
        optional_header = dict()

        optional = pe_info["pe"]["headers"]["optional_header"]

        if "magic" in optional:
            optional_header["magic_hex"] = hex(optional["magic"])
        if "major_linker_version" in optional:
            optional_header["major_linker_version"] = optional["major_linker_version"]
        if "minor_linker_version" in optional:
            optional_header["minor_linker_version"] = optional["minor_linker_version"]
        if "size_of_code" in optional:
            optional_header["size_of_code"] = optional["size_of_code"]
        if "size_of_initialized_data" in optional:
            optional_header["size_of_initialized_data"] = optional["size_of_initialized_data"]
        if "size_of_unintialized_data" in optional:
            optional_header["size_of_unintialized_data"] = optional["size_of_unintialized_data"]
        if "address_of_entry_point" in optional:
            optional_header["address_of_entry_point"] = optional["address_of_entry_point"]
        if "base_of_code" in optional:
            optional_header["base_of_code"] = optional["base_of_code"]
        if "base_of_data" in optional:
            optional_header["base_of_data"] = optional["base_of_data"]
        if "image_base" in optional:
            optional_header["image_base"] = optional["image_base"]
        if "section_alignment" in optional:
            optional_header["section_alignment"] = optional["section_alignment"]
        if "file_alignment" in optional:
            optional_header["file_alignment"] = optional["file_alignment"]
        if "major_os_version" in optional:
            optional_header["major_os_system_version"] = optional["major_os_version"]
        if "minor_os_version" in optional:
            optional_header["minor_os_system_version"] = optional["minor_os_version"]
        if "major_image_version" in optional:
            optional_header["major_image_version"] = optional["major_image_version"]
        if "minor_image_version" in optional:
            optional_header["minor_image_version"] = optional["minor_image_version"]
        if "major_subsystem_version" in optional:
            optional_header["major_subsystem_version"] = optional["major_subsystem_version"]
        if "minor_subsystem_version" in optional:
            optional_header["minor_subsystem_version"] = optional["minor_subsystem_version"]
        if "win32_version_value" in optional:
            optional_header["win32_version_value_hex"] = hex(optional["win32_version_value"])
        if "size_of_image" in optional:
            optional_header["size_of_image"] = optional["size_of_image"]
        if "size_of_headers" in optional:
            optional_header["size_of_headers"] = optional["size_of_headers"]
        if "checksum" in optional:
            optional_header["checksum_hex"] = hex(optional["checksum"])
        if "subsystem" in optional:
            optional_header["subsystem_hex"] = hex(optional["subsystem"])
        if "dll_characteristics" in optional:
            optional_header["dll_characteristics_hex"] = hex(optional["dll_characteristics"])
        if "size_of_stack_reserve" in optional:
            optional_header["size_of_stack_reserve"] = optional["size_of_stack_reserve"]
        if "size_of_stack_commit" in optional:
            optional_header["size_of_stack_commit"] = optional["size_of_stack_commit"]
        if "size_of_heap_reserve" in optional:
            optional_header["size_of_heap_reserve"] = optional["size_of_heap_reserve"]
        if "size_of_heap_commit" in optional:
            optional_header["size_of_heap_commit"] = optional["size_of_heap_commit"]
        if "loader_flags" in optional:
            optional_header["loader_flags_hex"] = hex(optional["loader_flags"])
        if "number_of_rva_and_sizes" in optional:
            optional_header["number_of_rva_and_sizes"] = optional["number_of_rva_and_sizes"]

        return optional_header

    def populate_sections(self, pe_info):
        sections = []

        pe_sections = pe_info["pe"]["sections"]
        for s in pe_sections:
            section = {"name": s["section_header"]["name"],
                       "size": s["section_header"]["size_of_raw_data"],
                       "entropy": s["entropy"]["value"],
                       "hashes": s["data_hashes"]}
            sections.append(section)
        return sections

    def populate_pe_extensions(self, pe_info):
        pe_dict = dict()

        file_header = pe_info["pe"]["headers"]["file_header"]

        if "." in pe_info["file"]["file_name"]:
            pe_dict["pe_type"] = pe_info["file"]["file_name"].split(".")[1]
        else:
            pe_dict["pe_type"] = "unknown"

        if "machine" in file_header:
            pe_dict["machine_hex"] = hex(file_header["machine"])
        if "number_of_sections" in file_header:
            pe_dict["number_of_sections"] = file_header["number_of_sections"]
        if "time_date_stamp" in file_header:
            pe_dict["time_date_stamp"] = file_header["time_date_stamp"]
        if "pointer_to_symbol_table" in file_header:
            pe_dict["pointer_to_symbol_table_hex"] = hex(file_header["pointer_to_symbol_table"])
        if "number_of_symbols" in file_header:
            pe_dict["number_of_symbols"] = file_header["number_of_symbols"]
        if "size_of_optional_header" in file_header:
            pe_dict["size_of_optional_header"] = file_header["size_of_optional_header"]
        if "characteristics" in file_header:
            pe_dict["characteristics_hex"] = hex(file_header["characteristics"])

        pe_dict["optional_header"] = self.populate_optional_header(pe_info)
        pe_dict["sections"] = self.populate_sections(pe_info)
        return pe_dict

    def file_headers(self, pefile_info):
        file_header_id = self.create_obj_id()
        file_header_obj = dict()
        file_header_obj["type"] = "file"
        file_header_obj["hashes"] = pefile_info["hashes"]
        file_header_obj["mime_type"] = "vnd.microsoft.portable-executable"

        file_header_obj["extensions"] = dict()
        file_header_obj["extensions"]["windows-pebinary-ext"] = self.populate_pe_extensions(pefile_info)

        return file_header_id, file_header_obj

    def generate_maec(self):
        entry_dict = self.pefile_parser.entry_dict
        self.package["id"] = "package--" + str(uuid.uuid4())

        if "file" in entry_dict:
            self.primary_instance = self.create_malware_instance(entry_dict["file"], entry_dict["hashes"])

            # Add cuckoo information
            tool_id = self.create_obj_id()
            tool_dict = OrderedDict()
            tool_dict['type'] = 'software'
            tool_dict['name'] = 'pefile'
            self.package['observable_objects'][tool_id] = tool_dict

            # Add the analysis metadata
            analysis_dict = OrderedDict()
            analysis_dict['is_automated'] = True
            analysis_dict['analysis_type'] = 'static'
            analysis_dict['tool_refs'] = [tool_id]
            analysis_dict['description'] = "Automated static analysis by pefile Python package created by Ero Carrera."
            self.primary_instance['analysis_metadata'] = [analysis_dict]

            fileheader_id, header_dict = self.file_headers(entry_dict)

            self.primary_instance["static_features"] = {"file_headers": [fileheader_id]}
            self.package['observable_objects'][fileheader_id] = header_dict

        # self.populate(entry_dict, static_bundle)
        # malware_subject.add_analysis(self.generate_analysis(static_bundle))
        # if self.bundle_has_content(static_bundle):
        #     malware_subject.add_findings_bundle(static_bundle)
        #
        # self.package["observable_objects"].append(malware_subject)

    def get_output(self):
        # Add the primary Malware Instance to the package
        self.package['maec_objects'].append(sort_dict(self.primary_instance))
        # Convert any strings in the dictionary to Unicode
        unicode_package = convert_to_unicode(self.package)
        # Return report dictionary
        return unicode_package
