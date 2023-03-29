# pefilemaec service for Holmes-Totem

## Description

This service extracts meta information contained in PE binary files. Currently the work is based on the implemention of [pefile](https://github.com/erocarrera/pefile).
And converts them to MAEC 5.0 format. Results are then sent to MaecToOwl webservice if it is configured to be added to ontology.  

you can see the final analysis results [here](#output).

## Usage

Build and start the docker container using the included Dockerfile. Since this container needs to have access to the sample file, you need to run this container with:

`-v /tmp:/tmp:ro`

This allows the container to access /tmp on the local file system in read-only mode.

## Output

```json
{
    "id": "package--5209a025-c112-4eff-8163-74ee40b49be6",
    "type": "package",
    "schema_version": "5.0",
    "maec_objects": [
        {
            "id": "malware-instance--152f4a00-e976-4d26-b6cf-ef7279b7fe60",
            "type": "malware-instance",
            "hashes": {
                "MD5": "c971e699678832afaeefe152f5fa121d",
                "SHA-1": "448b886efe8fd453f5a7d04d84025129269c4857",
                "SHA-256": "e80a6c363ac9c62c5849dc4301190c2b2275e2d745baf6390c7826081f2b77bf",
                "SHA-512": "3df571438873d7e6e4de4651d96afd4c34e181f1a07ba2bb573e9645bb07401c54110e0691a3a4dcac051900ea314de22c5c99810a6fc63a81b556e963b6fda7"
            },
            "instance_object_refs": [
                "0"
            ],
            "static_features": {
                "type": "file",
                "mime_type": "vnd.microsoft.portable-executable",
                "extensions": {
                    "windows-pebinary-ext": {
                        "pe_type": "exe",
                        "machine_hex": "0x14c",
                        "number_of_sections": 4,
                        "time_date_stamp": "0x5E71CAF5 [Wed Mar 18 07:17:09 2020 UTC]",
                        "pointer_to_symbol_table_hex": "0x0",
                        "number_of_symbols": 0,
                        "size_of_optional_header": 224,
                        "characteristics_hex": "0x10f",
                        "optional_header": {
                            "magic_hex": "0x10b",
                            "major_linker_version": 6,
                            "minor_linker_version": 0,
                            "size_of_code": 98304,
                            "size_of_initialized_data": 61440,
                            "size_of_unintialized_data": 0,
                            "address_of_entry_point": 79117,
                            "base_of_code": 4096,
                            "base_of_data": 102400,
                            "image_base": 4194304,
                            "section_alignment": 4096,
                            "file_alignment": 4096,
                            "major_os_system_version": 4,
                            "minor_os_system_version": 0,
                            "major_image_version": 0,
                            "minor_image_version": 0,
                            "major_subsystem_version": 4,
                            "minor_subsystem_version": 0,
                            "win32_version_value_hex": "0x0",
                            "size_of_image": 163840,
                            "size_of_headers": 4096,
                            "checksum_hex": "0x4750de",
                            "subsystem_hex": "0x2",
                            "dll_characteristics_hex": "0x0",
                            "size_of_stack_reserve": 1048576,
                            "size_of_stack_commit": 4096,
                            "size_of_heap_reserve": 1048576,
                            "size_of_heap_commit": 4096,
                            "loader_flags_hex": "0x0",
                            "number_of_rva_and_sizes": 16
                        },
                        "sections": [
                            {
                                "name": ".text",
                                "size": 98304,
                                "entropy": 6.581942182936413,
                                "hashes": [
                                    "d0784e279389774d2ba7e3857011958b",
                                    "cc24dd21fa24cf0d70203b41fa100ed05d7914db",
                                    "3fb26df8b191adb9258cc8812c12c5cf36fef3d6e015986a8e0c71a4581978ee",
                                    "f483e1d103d8a743427615caa2ff3448ab1becaf72a78021891b4e3fea8c27036c34cd5954a97828d7c92e2452cb911470224bb3ef0a1df462921fc1dafee626"
                                ]
                            },
                            {
                                "name": ".rdata",
                                "size": 8192,
                                "entropy": 5.210410933709092,
                                "hashes": [
                                    "3ddfb0cb52a3e27ccde4c6455755368d",
                                    "dd52726cfb5722546f28b6df9745caa7ea824b37",
                                    "373757b455f6a9dd48a476c23379c6c8871b67eaf42bc143b4e5754f40cee99f",
                                    "288cb4c0a8d7bdc41e8be6a9fddf08dda4fd07a16450e4941b4605055b76fb2892e0a09a6107426167bb0c36862531493c2b64b8df6b3ed9d3f8cb9b094a5eb3"
                                ]
                            },
                            {
                                "name": ".data",
                                "size": 20480,
                                "entropy": 2.9337948714395856,
                                "hashes": [
                                    "0d9d53989ba3b423f8fb83ca9df6879d",
                                    "e6bb4105c6b73f92a0c376ab43b01b06c7ddbd23",
                                    "64a77069897d0fb58b83f397df10f8b44ebe761a5073c40caa593c3b59fda51e",
                                    "815098f61db0088a799d17f5153436008229f809c91982158e017b63fb71ed647950c2bafbc945399bad9daed6470b78637fa4368b677bbc2637e0d66cf1ed48"
                                ]
                            },
                            {
                                "name": ".rsrc",
                                "size": 12288,
                                "entropy": 3.8133287237913662,
                                "hashes": [
                                    "1be0cd1cb88f0fa10a8974ce1fb50874",
                                    "0840ce67385c582a6a1ece3abf2ef60d2868c408",
                                    "c901e44889cf4f5994a429f8dd934c6b4052d44b27cb8879da9f0c60db1ee08b",
                                    "b0f4b2d5ae9b0eb9316d476e505a9a8c5f63ca261beb2d02d1299f2e18acb15bc8e119f88df1dac4b50f4b50e3d51307a2b65ffc1a2610a29aa623729eb654c0"
                                ]
                            }
                        ]
                    }
                }
            },
            "analysis_metadata": [
                {
                    "is_automated": true,
                    "analysis_type": "static",
                    "tool_refs": [
                        "1"
                    ],
                    "description": "Automated static analysis by pefile Python package created by Ero Carrera."
                }
            ]
        }
    ],
    "observable_objects": {
        "0": {
            "type": "file",
            "name": "tcmd951x32.exe",
            "size": 4664352,
            "mime_type": "FileObjectType"
        },
        "1": {
            "type": "software",
            "name": "pefile"
        }
    }
}

```
