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

from wheel.pep425tags import get_supported
from json import dump
import sys

supported_values = []
for entry in get_supported():
  supported_values.append( { 'pythonTag': entry[0], 'abiTag': entry[1], 'platformTag': entry[2] })

result_file = sys.argv[1]
with open(result_file, 'w') as outfile:
    dump(supported_values, outfile)

