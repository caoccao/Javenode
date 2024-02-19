'''
  Copyright (c) 2021-2024 caoccao.com Sam Cao
  All rights reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
'''

import importlib
import logging
import pathlib
import re
import sys

if importlib.util.find_spec('coloredlogs'):
  import coloredlogs
  coloredlogs.install(level=logging.DEBUG, fmt='%(asctime)-15s %(name)s %(levelname)s: %(message)s')

class ChangeVersion(object):

  def __init__(self, version) -> None:
    self._root_path = (pathlib.Path(__file__) / '../../../').resolve().absolute()
    self._version = version

  def update_javenode(self):
    self._update(
      'README.rst', '\n',
      re.compile(r'^        <version>(?P<version>\d+\.\d+\.\d+)</version>$'),
      re.compile(r'javenode[\-\w]*:(?P<version>\d+\.\d+\.\d+)["\']{1}'))
    self._update(
      'build.gradle.kts', '\n',
      re.compile(r'JAVENODE = "(?P<version>\d+\.\d+\.\d+)"$'))
    self._update(
      'docs/conf.py', '\n',
      re.compile(r'release\s*=\s*\'(?P<version>\d+\.\d+\.\d+)\'$'))

  def update_javet(self):
    self._update(
      'build.gradle.kts', '\n',
      re.compile(r'JAVET = "(?P<version>\d+\.\d+\.\d+)"$'))

  def _update(self, relative_file_path: str, line_separator: str, *patterns: list):
    file_path = (self._root_path / relative_file_path).resolve().absolute()
    logging.info('Updating %s.', str(file_path))
    lines, line_number = [], 1
    original_buffer = file_path.read_bytes()
    for line in original_buffer.decode('utf-8').split(line_separator):
      for pattern in patterns:
        match_object = pattern.search(line)
        if match_object is not None:
          version = self._version
          if ',' in match_object.group('version'):
            version = version.replace('.', ',')
          logging.info(
            '  %d: %s -> %s',
            line_number,
            match_object.group('version'),
            version)
          line = '{prefix}{version}{suffix}'.format(
            prefix=line[:match_object.start('version')],
            version=version,
            suffix=line[match_object.end('version'):])
          break
      lines.append(line)
      line_number += 1
    new_buffer = line_separator.join(lines).encode('utf-8')
    if original_buffer == new_buffer:
      logging.warning('  Skipped.')
    else:
      file_path.write_bytes(new_buffer)
      logging.info('  Updated.')

def main():
  change_javenode_version = ChangeVersion('0.5.0')
  change_javenode_version.update_javenode()
  change_javet_version = ChangeVersion('3.0.4')
  change_javet_version.update_javet()
  return 0

if __name__ == '__main__':
  sys.exit(int(main() or 0))
