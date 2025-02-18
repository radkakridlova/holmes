# -*- coding: Latin-1 -*-
# Copyright (c) 2015, The MITRE Corporation. All rights reserved.
# For more information, please refer to the LICENSE.txt file.

import pefile_parser
import pefile_to_maec_converter

from pprint import pprint
from peinfov2 import pe_info_run

__version__ = '1.0.0-beta1'


def generate_package_from_binary_filepath(input_path):
    "Accept a filepath to a PE file and return a MAEC Package object"
    # Instantiate the pefile parser and parse the pefile object
    parser = pefile_parser.PefileParser(input_path)
    pprint(parser.entry_dict)

    # data = pe_info_run("C:\\Users\\h\\Downloads\\tcmd951x32.exe")
    # pprint(data)
    # Instantiate the MAEC translator and perform the translation
    maec_translator = pefile_to_maec_converter.PefileToMAEC(parser, "C:\\Users\h\\PycharmProjects\\staticservice\\pefile_maec.json")
    # package = maec_translator.package
    # # return the MAEC Package object
    # return package


def create_maec(inputfile, outpath, options_dict):
    package = generate_package_from_binary_filepath(inputfile)
    #package.to_xml_file(outpath, custom_header=options_dict)


if __name__ == '__main__':
    # # Setup the argument parser
    # parser = argparse.ArgumentParser(description="Ero Carrera's Pefile module to MITRE MAEC Translator v" + __version__)
    # parser.add_argument("infile", help="the name of the input portable executable (PE) file to capture the pefile output for.")
    # parser.add_argument("outfile", help="the name of the MAEC XML file to which the output will be written.")

    options_dict = {"Created by":"PEFile to MAEC (http://github.com/MAECProject/pefile-to-maec)"}
    create_maec("C:\\tmp\\tcmd951x32.exe", "pefile_maec.xml", options_dict)

    
    # # Test if the input is a directory or file
    # if os.path.isfile(args.infile):
    #     outfilename = args.outfile
    #     # Test if the output is a directory
    #     # If so, concatenate "_maec.xml" to the input filename
    #     # and use this as the output filename
    #     if os.path.isdir(args.outfile):
    #         outfilename = os.path.join(args.outfile, str(os.path.basename(args.infile))[:-4] + "_maec.xml")
    #     # If we're dealing with a single file, just call create_maec()
    #     create_maec(args.infile, outfilename, options_dict)
    # # If a directory was specified, perform the corresponding conversion
    # elif os.path.isdir(args.infile):
    #     # Iterate and try to parse/convert each file in the directory
    #     for filename in os.listdir(args.infile):
    #         # Only handle PE files
    #         if str(filename).lower()[-3:] not in ["exe", "dll", "sys"]:
    #             print(str("Error: {0} does not appear to be an PE file. Skipping.\n").format(filename))
    #             continue
    #         outfilename = str(filename)[:-4] + "pefile_maec.xml"
    #         create_maec(os.path.join(args.infile, filename), os.path.join(args.outfile, outfilename), options_dict)
    # else:
    #     print("Input file " + args.infile + " does not exist")
