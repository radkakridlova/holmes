# -*- coding: Latin-1 -*-
# Copyright (c) 2015, The MITRE Corporation. All rights reserved.
# For more information, please refer to the LICENSE.txt file.

import os
import hashlib
import pefile

from copy import deepcopy
from mappings.image_dos_header import IMAGE_DOS_HEADER_MAPPINGS
from mappings.image_file_header import IMAGE_FILE_HEADER_MAPPINGS
from mappings.image_optional_header import IMAGE_OPTIONAL_HEADER32_MAPPINGS
from mappings.image_sections import IMAGE_SECTION_HEADER_MAPPINGS


class PefileParser(object):
    def __init__(self, infile):
        self.infile = infile
        self.root_entry = None
        self.entry_dict = {}
        self.process_entry()

    # Build a nested dictionary from a list
    # Set it to a value
    def build_nested_dictionary(self, child_list, value):
        nested_dict = {}
        if len(child_list) == 1:
            if isinstance(value, str) and '\\x00' in value:
                value = value.rstrip('\\x00')
            nested_dict[child_list[0]] = value
            return nested_dict

        for list_item in child_list:
            next_index = child_list.index(list_item) + 1
            nested_dict[list_item] = self.build_nested_dictionary(child_list[next_index:], value)
            break

        return nested_dict

    # Function for merging multiple dictionaries
    def dict_merge(self, target, *args):
        # Merge multiple dicts
        if len(args) > 1:
            for obj in args:
                self.dict_merge(target, obj)
            return target

        # Recursively merge dicts and set non-dict values
        obj = args[0]
        if not isinstance(obj, dict):
            return obj
        for k, v in obj.items():
            if k in target and isinstance(target[k], dict):
                self.dict_merge(target[k], v)
            else:
                target[k] = deepcopy(v)
        return target

    def set_dictionary_value(self, dictionary, key, value):
        if '/' in key:
            split_names = key.split('/')
            if split_names[0] not in dictionary:
                dictionary[split_names[0]] = self.build_nested_dictionary(split_names[1:], value)
            else:
                self.dict_merge(dictionary[split_names[0]], self.build_nested_dictionary(split_names[1:], value))
        else:
            if isinstance(value, str) and '\\x00' in value:
                value = value.rstrip('\\x00')

            dictionary[key] = value

    def perform_mapping(self, struct_dict, element_mapping_dict):
        output_dict = {}
        for key, value in struct_dict.items():
            if key in element_mapping_dict:
                if isinstance(value, str):
                    self.set_dictionary_value(output_dict, element_mapping_dict[key], value)
                elif isinstance(value, dict):
                    for k, v in value.items():
                        if k == 'Value':
                            self.set_dictionary_value(output_dict, element_mapping_dict[key], value[k])
                elif isinstance(value, list):
                    for entry in value:
                        for k, v in entry.items():
                            if k == 'Value':
                                self.set_dictionary_value(output_dict, element_mapping_dict[key], entry[k])

        return output_dict

    def perform_mappings(self, element_list, element_mapping_dict):
        output_dict = {}
        for element in element_list:
            print(element)

    def handle_input_file(self):
        try:
            self.root_entry = pefile.PE(self.infile, fast_load=True)
        except pefile.PEFormatError:
            return None

    def handle_file_object(self):
        file_dictionary = {'type': 'FileObjectType', 'file_name': os.path.basename(self.infile),
                           'file_path': os.path.abspath(self.infile), 'size_in_bytes': os.path.getsize(self.infile)}

        return file_dictionary

    def parse_headers(self):
        headers_dictionary = {'dos_header': self.perform_mapping(
            self.root_entry.DOS_HEADER.dump_dict(), IMAGE_DOS_HEADER_MAPPINGS), 'file_header': self.perform_mapping(
            self.root_entry.FILE_HEADER.dump_dict(), IMAGE_FILE_HEADER_MAPPINGS),
            'optional_header': self.perform_mapping(
                self.root_entry.OPTIONAL_HEADER.dump_dict(), IMAGE_OPTIONAL_HEADER32_MAPPINGS)}

        return headers_dictionary

    @staticmethod
    def get_hash_list(item):
        hash_list = []
        hash_methods = [
            item.get_hash_md5(),
            item.get_hash_sha1(),
            item.get_hash_sha256(),
            item.get_hash_sha512()]
        for hash_method in hash_methods:
            hash_list.append(hash_method)

        return hash_list

    @staticmethod
    def get_entropy(item):
        entropy_dict = {'value': item.get_entropy()}
        return entropy_dict

    def parse_sections(self):
        sections_list = []
        for section in self.root_entry.sections:
            section_dict = self.perform_mapping(section.dump_dict(), IMAGE_SECTION_HEADER_MAPPINGS)
            section_dict['data_hashes'] = self.get_hash_list(section)
            section_dict['entropy'] = self.get_entropy(section)
            sections_list.append(section_dict)

        return sections_list

    def load_data_directories(self):
        self.root_entry.parse_data_directories(directories=[
            pefile.DIRECTORY_ENTRY['IMAGE_DIRECTORY_ENTRY_IMPORT'],
            pefile.DIRECTORY_ENTRY['IMAGE_DIRECTORY_ENTRY_EXPORT'],
            pefile.DIRECTORY_ENTRY['IMAGE_DIRECTORY_ENTRY_RESOURCE'],
            pefile.DIRECTORY_ENTRY['IMAGE_DIRECTORY_ENTRY_DEBUG'],
            pefile.DIRECTORY_ENTRY['IMAGE_DIRECTORY_ENTRY_TLS'],
            pefile.DIRECTORY_ENTRY['IMAGE_DIRECTORY_ENTRY_DELAY_IMPORT'],
            pefile.DIRECTORY_ENTRY['IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT']])

    def parse_import_directory(self):
        imports_list = []

        for entry in self.root_entry.DIRECTORY_ENTRY_IMPORT:
            library_dictionary = {}
            api_list = []
            library_dictionary['file_name'] = entry.dll.decode('UTF-8')
            library_dictionary['imported_functions'] = api_list
            for imp in entry.imports:
                if imp.name is not None:
                    api_list.append({'function_name': imp.name.decode('UTF-8')})
            imports_list.append(library_dictionary)

        return imports_list

    def parse_export_directory(self):
        exports_list = []

        for entry in self.root_entry.DIRECTORY_ENTRY_EXPORT.symbols:
            library_dictionary = {}
            api_list = []
            library_dictionary['file_name'] = entry.name.decode('UTF-8')
            exports_list.append(library_dictionary)

        return exports_list

    def handle_resources(self):
        resource_list = []

        for entry in self.root_entry.DIRECTORY_ENTRY_RESOURCE.entries:
            entry_dict = {}
            if hasattr(entry, 'name') and entry.name:
                entry_dict['name'] = str(entry.name)
                if hasattr(entry, 'directory'):
                    if hasattr(entry.directory, 'entries'):
                        for child_entry in entry.directory.entries:
                            if hasattr(child_entry, 'directory'):
                                if hasattr(child_entry.directory, 'entries'):
                                    for grandchild in child_entry.directory.entries:
                                        if hasattr(grandchild, 'data'):
                                            entry_dict['language'] = pefile.LANG[grandchild.data.lang]
                                            entry_dict['sub_language'] = pefile.SUBLANG[grandchild.data.sublang][0]
                                            entry_dict['size'] = grandchild.data.struct.Size
            elif hasattr(entry, 'id') and entry.id:
                entry_dict['type'] = pefile.RESOURCE_TYPE[entry.id]
                if hasattr(entry, 'directory'):
                    if hasattr(entry.directory, 'entries'):
                        for child_entry in entry.directory.entries:
                            if hasattr(child_entry, 'name') and child_entry.name:
                                entry_dict['name'] = str(child_entry.name)
                            if hasattr(child_entry, 'directory'):
                                if hasattr(child_entry.directory, 'entries'):
                                    for grandchild in child_entry.directory.entries:
                                        if hasattr(grandchild, 'data'):
                                            entry_dict['language'] = pefile.LANG[grandchild.data.lang]
                                            if grandchild.data.sublang in pefile.SUBLANG:
                                                entry_dict['sub_language'] = pefile.SUBLANG[grandchild.data.sublang][0]
                                            entry_dict['size'] = grandchild.data.struct.Size

            resource_list.append(entry_dict)

        return resource_list

    def handle_pe_object(self):
        pe_dictionary = {'xsi:type': 'WindowsExecutableFileObjectType', 'headers': self.parse_headers(),
                         'sections': self.parse_sections()}

        self.load_data_directories()
        if hasattr(self.root_entry, 'DIRECTORY_ENTRY_IMPORT'):
            pe_dictionary['imports'] = self.parse_import_directory()

        if hasattr(self.root_entry, 'DIRECTORY_ENTRY_EXPORT'):
            pe_dictionary['exports'] = self.parse_export_directory()

        if hasattr(self.root_entry, 'DIRECTORY_ENTRY_RESOURCE'):
            pe_dictionary['resources'] = self.handle_resources()

        return pe_dictionary

    def calculate_hashes(self):
        hashes = dict()

        # Prevzate z https://stackoverflow.com/questions/22058048/hashing-a-file-in-python
        # BUF_SIZE is totally arbitrary, change for your app!
        BUF_SIZE = 65536  # lets read stuff in 64kb chunks!

        md5 = hashlib.md5()
        sha1 = hashlib.sha1()
        sha256 = hashlib.sha256()
        sha512 = hashlib.sha512()

        with open(self.infile, 'rb') as f:
            while True:
                data = f.read(BUF_SIZE)
                if not data:
                    break
                md5.update(data)
                sha1.update(data)
                sha256.update(data)
                sha512.update(data)

        hashes["MD5"] = md5.hexdigest()
        hashes["SHA-1"] = sha1.hexdigest()
        hashes["SHA-256"] = sha256.hexdigest()
        hashes["SHA-512"] = sha512.hexdigest()
        return hashes

    def process_entry(self):
        self.handle_input_file()

        if self.root_entry:
            self.entry_dict['file'] = self.handle_file_object()
            self.entry_dict['pe'] = self.handle_pe_object()
            self.entry_dict['hashes'] = self.calculate_hashes()
        else:
            raise TypeError('Error: Not a valid PE file.')
