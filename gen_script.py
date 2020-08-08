import argparse
import json
import platform

def main(args):
    with open(args.setting) as fp:
        setting = json.load(fp)

    output_lines = []
    if args.os == 'Windows':
        entry = setting['Windows']
        output_lines.append('@echo off')
        output_lines.append('set PATH_TO_FX="{}"'.format(entry['path_to_fx']))
        output_lines.append('set DESTINATION="{}"'.format(setting['destination']))
        command_base = ('javac --source-path {} '.format(setting['root']) +
                        '--class-path %DESTINATION% -d %DESTINATION% {modules} {pathes}')
        modules_base = '--module-path %PATH_TO_FX% --add-modules {}'
        filename_extension = '.bat'
    else:
        entry = setting['Linux']
        output_lines.append('export PATH="{}"'.format(entry['path']))
        output_lines.append('export PATH_TO_FX="{}"'.format(entry['path_to_fx']))
        output_lines.append('DESTINATION="{}"'.format(setting['destination']))
        command_base = ('javac --source-path {} '.format(setting['root']) +
                        '--class-path ${{DESTINATION}} -d ${{DESTINATION}} {modules} {pathes}')
        modules_base = '--module-path ${{PATH_TO_FX}} --add-modules {}'
        filename_extension = '.sh'

    output_lines.append('javac --version')

    for step_dict in setting['steps']:
        output_lines.append('')
        output_lines.append('echo "Compile {}"'.format(step_dict['target']))
        modules = step_dict.get('modules')
        modules_str = modules_base.format(','.join(modules)) if modules else ''
        pathes_str = ' '.join(step_dict['pathes'])
        command = command_base.format(modules=modules_str, pathes=pathes_str)
        output_lines.append(command)

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
