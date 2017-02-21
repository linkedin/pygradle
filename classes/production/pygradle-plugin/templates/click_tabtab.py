#!/usr/bin/env python
#
# Copyright 2016 LinkedIn Corp.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


from __future__ import absolute_import
from __future__ import print_function

import logging
import os
import pkg_resources
import re
import sys
import textwrap

try:
    import click
except ImportError:
    print("This completion script only supports Click based entrypoints.")
    sys.exit(1)


def is_group(obj):
    """ Examine the object to determine if it is wrapping a click.Group

    :param obj: the object in question
    :returns: if the object is a click.Group in some fashion
    """
    # Using self of the bound method so this is safe across weirdly decorated groups.
    return hasattr(obj, 'command') and isinstance(obj.command.__self__, click.Group)


def is_command(obj):
    """ Examine the object to determine if it is wrapping a click.Command

    :param obj: the object in question
    :returns: if the object is a click.Command in some fashion
    """
    # Using self of the bound method so this is safe across weirdly decorated commands.
    return hasattr(obj, 'parse_args') and isinstance(obj.parse_args.__self__, click.Command)


def escape_description(desc):
    """ Escape a string for use as an argument description.

    :param str desc: the description
    :returns: esacped description
    :rtype: str
    """
    desc = desc.replace('\n', ' ')
    desc = desc.replace('\'', '')

    # Special meaning in zsh optspec, so substitute inoffensive braces.
    desc = desc.replace('[', '{')
    desc = desc.replace(']', '}')
    desc = re.sub(' +', ' ', desc)

    # Descriptions are way too long.  Kill everything after the first sentence.
    desc = re.sub('\.\s.*$', '', desc)

    return desc


def zsh_generate_optionspecs(click_command):
    """ Given a command, dump the appropriate optionspecs.

    See the man page for zshcompsys, as these are arguments destined for
    _arguments.

    :param Click.Command click_command: the command
    :returns: completion code
    :rtype: str
    """
    results = []
    for param in click_command.params:
        if isinstance(param, click.Option):
            for opt in param.opts:
                desc = param.make_metavar()
                if hasattr(param, "help") and param.help:
                    desc = param.help
                    # TODO: nargs, listing all options before all arguments, etc.
                desc = escape_description(desc)
                spec = ""
                if param.nargs:
                    spec = ':{name}:'.format(name=opt.lstrip('-'), desc=desc)
                results.append('"{multiple}{opt}[{desc}]{spec}"'.format(
                    opt=opt,
                    desc=desc,
                    spec=spec,
                    multiple='*' if param.multiple else '')
                )
    for param in click_command.params:
        if isinstance(param, click.Argument):
            for opt in param.opts:
                desc = param.make_metavar()
                # TODO: nargs
                results.append(
                    '": : _message \'{opt} = {desc}\'"'.format(opt=opt, desc=desc)
                )

    return ' '.join(results)


def zsh_gen_subcommand_arguments(click_group):
    """ Given a command group, generate the completions for the *arguments* to the group.

    See the man page for zshcompsys, as these are arguments destined for
    _arguments.

    :param Click.Group click_group: the command group
    :returns: completion code
    :rtype: str
    """
    results = []
    for name, command in list(click_group.commands.items()):
        desc = name
        if hasattr(command, 'help') and command.help:
            desc = command.help
        desc = escape_description(desc)
        results.append('{name}\\:\'{desc}\''.format(name=name, desc=desc))

    return '": :(({0}))"'.format(' '.join(results))


def zsh_generate_group_completions(click_group):
    """ Given a command group, generate the completions for the group.

    See the man page for zshcompsys, which has the doc for _arguments.

    :param Click.Group click_group: the command group
    :returns: completion code
    :rtype: str
    """
    result = []
    result.append("#  generate_group_completions for %s" % click_group.name)
    result.append(
        '_arguments -S -C "*:: :->{name}_subcommand" {subcommands} {optionspecs}\n'
        'if [[ $state == {name}_subcommand ]]\n'
        'then\n'.format(
            name=click_group.name,
            subcommands=zsh_gen_subcommand_arguments(click_group),
            optionspecs=zsh_generate_optionspecs(click_group)
        )
    )
    result.append('case ${line[1]} in')
    for name, command in list(click_group.commands.items()):
        result.append('{0} )'.format(name))
        result.append(zsh_generate_completions(command))
        result.append(';;')
    result.append('* )')
    result.append('_message "No known completions"')
    result.append(';;')
    result.append('esac')
    result.append('fi')

    return '\n'.join(result)


def zsh_generate_command_completions(click_command):
    """ Given a command, generate the completions for the command.

    See the man page for zshcompsys, which has the doc for _arguments.

    :param Click.Command click_command: the command
    :returns: completion code
    :rtype: str
    """
    return '_arguments -S {optionspecs}'.format(
        optionspecs=zsh_generate_optionspecs(click_command)
    )


def zsh_generate_completions(click_obj):
    """ Given a command or group, generate the completions for the command.

    See the man page for zshcompsys, which has the doc for _arguments.

    :param click_obj: the command (or group)
    :returns: completion code
    :rtype: str
    """
    if is_group(click_obj):
        return zsh_generate_group_completions(click_obj)
    elif is_command(click_obj):
        return zsh_generate_command_completions(click_obj)


def zsh_generate_top_completions(name, click_obj):
    """ Given a click based entry point, write the ZSH completion to a file.

    :param str name: the name of the entrypoint
    :param Click.Command click_obj: the command (or group)
    """
    script = textwrap.dedent('''\
    #compdef {name}
    typeset -A opt_args
    local context state line ret curcontext="$curcontext"
    {body}
    compdef _{name} {name}
    ''')
    if not os.path.exists('zsh'):
        os.makedirs('zsh')
    with open('zsh/_{name}'.format(name=name), 'w') as out:
        out.write(script.format(name=name, body=zsh_generate_completions(click_obj)))


def bash_generate_top_completions(name, click_obj):
    """ Given a click based entry point, write the Bash completion to a file.

    :param str name: the name of the entrypoint
    :param Click.Command click_obj: the command (or group)
    """
    # cur and prev are currently unused, but could be useful if we wanted to complete options
    # line and words are used to get me an array of non-option words, so my logic can mirror zsh's
    # then, i consume the subcommands by popping them off the front of line and switching on the new one.
    script = textwrap.dedent('''\
    _{name}() {{
    local cur prev cur_word prev_word
    local -a line
    local -a words

    cur=${{COMP_WORDS[COMP_CWORD]}}
    prev=${{COMP_WORDS[COMP_CWORD-1]}}
    words=("${{COMP_WORDS[@]:1:COMP_CWORD+1}}")
    line=("${{words[@]//^-*/}}")
    {body}
    }}
    complete -F _{name} {name}
    ''')
    if not os.path.exists('bash'):
        os.makedirs('bash')
    with open('bash/{name}.bash'.format(name=name), 'w') as out:
        out.write(script.format(name=name, body=bash_generate_completions(click_obj)))


def bash_generate_completions(click_obj):
    """ Given a command or group, generate the completions for the command.

    :param Click.Command click_obj: the command (or group)
    :returns: completion code
    :rtype: str
    """
    # TODO complete option args?
    result = []
    if is_group(click_obj):
        result.append('case "${line[0]}" in')
        words = []
        for name, command in list(click_obj.commands.items()):
            result.append(' {name} )'.format(name=name))
            result.append('line=("${line[@]:1}")')  # shift line so we don't need to track depth
            result.append(bash_generate_completions(command))
            result.append(';;')
            words.append(name)
        for param in click_obj.params:
            if hasattr(param, 'opts'):
                words.extend(param.opts)
        result.append('*)')
        # we could also switch on $cur and $prev as well to  complete option arguments
        result.append(
            'COMPREPLY=($(compgen -W "{words}" -- ${{cur}} ))'.format(words=' '.join(words))
        )
        result.append(';;')
        result.append('esac')
    elif is_command(click_obj):
        words = []
        for param in click_obj.params:
            if hasattr(param, 'opts'):
                words.extend(param.opts)
        # we could also switch on $cur and $prev as well to  complete option arguments
        result.append(
            'COMPREPLY=($(compgen -W "{words}" -- ${{cur}} ))'.format(words=' '.join(words))
        )

    return '\n'.join(result)


def fish_generate_completions(click_obj, cmdname, topname):
    """ Given a command or group, generate the completions for the command.

    :param Click.Command click_obj: the command (or group)
    :param str cmdname: the command/subcommand
    :param str topname: the entrypoint name
    :returns: completion code
    :rtype: str
    """
    # TODO complete option args?
    result = []
    if cmdname != topname:
        condition = '__fish_seen_subcommand_from {cmdname}'.format(cmdname=cmdname)
    else:
        condition = '__fish_use_subcommand'

    prefix = '''complete -c {topname} -n '{condition}' '''.format(
        topname=topname,
        condition=condition
    )

    if is_group(click_obj):
        for param in click_obj.params:
            if hasattr(param, 'opts'):
                result.append(prefix + fish_generate_options(param))
        for name, command in list(click_obj.commands.items()):
            desc = name
            if hasattr(command, 'help') and command.help:
                desc = command.help
            desc = escape_description(desc)
            result.append(prefix + ''' -xa '{name}' -d '{desc}' '''.format(name=name, desc=desc))
            result.append(fish_generate_completions(command, name, topname))
    elif is_command(click_obj):
        for param in click_obj.params:
            if hasattr(param, 'opts'):
                result.append(prefix + fish_generate_options(param))

    return '\n'.join(result)


def fish_generate_options(param):
    """ Generates the arguments to fish's 'complete' builtin for the given parameter.

    :param Click.Parameter param: the parameter
    :returns: completion code
    :rtype: str
    """
    results = []
    if isinstance(param, click.Option):
        longOpt = ''
        shortOpt = ''
        for opt in param.opts:
            if opt.startswith('--'):
                longOpt = opt[2:]
            else:
                shortOpt = opt[1:]

        desc = param.make_metavar()
        if hasattr(param, "help") and param.help:
            desc = param.help
            # TODO nargs, listing all options before all arguments, etc.
            desc = escape_description(desc)
        if shortOpt:
            results.append('-s ' + shortOpt)
        if longOpt:
            results.append('-l ' + longOpt)
        if desc:
            results.append('-d "{desc}"'.format(desc=desc))
    elif isinstance(param, click.Argument):
        for opt in param.opts:
            desc = param.make_metavar()
            results.append('-d ' + desc)

    return ' '.join(results)


def fish_generate_top_completions(name, click_obj):
    """ Given a click based entry point, write the Fish completion to a file.

    :param str name: the name of the entrypoint
    :param Click.Command click_obj: the command (or group)
    """
    script = textwrap.dedent('''\
    {body}
    ''')
    if not os.path.exists('fish'):
        os.makedirs('fish')
    with open('fish/{name}.fish'.format(name=name), 'w') as out:
        out.write(script.format(name=name, body=fish_generate_completions(click_obj, name, name)))


def generate_completions(name, click_obj):
    """ Given a click based entry point, write the completions to disk.

    :param Str name: the name of the entrypoint
    :param Click.Command click_obj: the command (or group)
    """
    zsh_generate_top_completions(name, click_obj)
    fish_generate_top_completions(name, click_obj)
    bash_generate_top_completions(name, click_obj)


def main():
    ran = False
    console_scripts = list(
        next(iter(pkg_resources.working_set)).get_entry_map(group='console_scripts').items()
    )
    for name, entrypoint in console_scripts:
        try:
            ep = entrypoint.load()
            if is_command(ep):
                print("Generating completions for {name}...".format(name=name))
                generate_completions(name, ep)
                ran = True
            else:
                print("{name} is not Click based, skipping.".format(name=name))
        except ImportError as e:
            print(
                "ERROR: Cannot generate completions for {name}; see stacktrace below.".format(name=name)
            )
            print("#" * 80)
            logging.exception(e)
            print("#" * 80)
            continue
    if not ran:
        print("Did not find a Click command to generate completions for.")
        sys.exit(1)


if __name__ == "__main__":
    main()
