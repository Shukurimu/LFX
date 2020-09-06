import argparse
import json
import platform

def main(args):
    with open(args.setting) as fp:
        setting = json.load(fp)

    if args.os == 'Windows':
        entry = setting['Windows']
        output_lines = ['@echo off',
                        'set PATH_TO_FX="{}"'.format(entry['path_to_fx']),
                        'set DESTINATION="{}"'.format(setting['destination']),
        ]
        destination_var = '%DESTINATION%'
        path_to_fx_var = '%PATH_TO_FX%'
        filename_extension = '.bat'
    else:  # including Linux & Darwin
        entry = setting['Linux']
        output_lines = ['export PATH="{}"'.format(entry['path']),
                        'export PATH_TO_FX="{}"'.format(entry['path_to_fx']),
                        'DESTINATION="{}"'.format(setting['destination']),
        ]
        destination_var = '${DESTINATION}'
        path_to_fx_var = '${PATH_TO_FX}'
        filename_extension = '.sh'

    output_lines.append('javac --version')

    for step_dict in setting['steps']:
        command_args = ['javac',
                        '-sourcepath', setting['root'],
                        '-classpath', destination_var,
                        '-d', destination_var,
        ]
        modules = step_dict.get('modules')
        if modules:
            command_args += ['--module-path', path_to_fx_var, '--add-modules', ','.join(modules)]
        output_lines += ['',
                         'echo ' + step_dict['target'],
                         ' '.join(command_args + step_dict['pathes']),
        ]

    with open(args.output + filename_extension, 'w') as fp:
        for line in output_lines:
            print(line, file=fp)


def _parse_args():
    parser = argparse.ArgumentParser(description='Generate compile script for different os.')
    parser.add_argument('--os', type=str, default=platform.system(), choices=['Linux', 'Windows'])
    parser.add_argument('--setting', type=str, default='compile_setting.json',
                        help='path of script setting file in json format')
    parser.add_argument('--output', type=str, default='compile_script',
                        help='filename of generated script')
    return parser.parse_args()


if __name__ == '__main__':
    main(_parse_args())
