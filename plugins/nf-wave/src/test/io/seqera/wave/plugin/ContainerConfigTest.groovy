/*
 * Copyright 2020-2022, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.wave.plugin

import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ContainerConfigTest extends Specification {

    @Unroll
    def 'should merge env' () {
        given:
        def config = new ContainerConfig()

        expect:
        config.mergeEnv(ENV1, ENV2) == EXPECTED

        where:
        ENV1                | ENV2              | EXPECTED
        null                | null              | null
        []                  | null              | []
        ['foo=1']           | []                | ['foo=1']
        ['foo=1']           | ['bar=2']         | ['foo=1','bar=2']
        ['foo=1']           | ['foo=2']         | ['foo=2']         // <-- env2 overrides env1
        ['foo=1','baz=3']   | ['foo=2']         | ['foo=2','baz=3'] // <-- overrides 'foo' in env1 and keep as first entry
        ['foo=1']           | ['baz=3','foo=2'] | ['foo=2','baz=3'] // <-- overrides 'foo' in env1 and keep as first entry
    }

    def 'should override entry' () {
        expect:
        new ContainerConfig(entrypoint: LEFT) + new ContainerConfig(entrypoint: RIGHT)
                == new ContainerConfig(entrypoint: EXPECTED)

        where:
        LEFT            | RIGHT         | EXPECTED
        null            | null          | null
        ['entry1.sh']   | null          | ['entry1.sh']
        null            | ['entry2.sh'] | ['entry2.sh']
        ['entry1.sh']   | ['entry2.sh'] | ['entry2.sh']
    }

    def 'should override cmd' () {
        expect:
        new ContainerConfig(cmd: LEFT) + new ContainerConfig(cmd: RIGHT)
                == new ContainerConfig(cmd: EXPECTED)
        where:
        LEFT            | RIGHT         | EXPECTED
        null            | null          | null
        ['cmd1.sh']     | null          | ['cmd1.sh']
        null            | ['cmd2.sh']   | ['cmd2.sh']
        ['cmd1.sh']     | ['cmd2.sh']   | ['cmd2.sh']

    }

    def 'should override workdir' () {
        expect:
        new ContainerConfig(workingDir: LEFT) + new ContainerConfig(workingDir: RIGHT)
                == new ContainerConfig(workingDir: EXPECTED)

        where:
        LEFT            | RIGHT         | EXPECTED
        null            | null          | null
        '/foo'          | null          | '/foo'
        null            | '/bar'        | '/bar'
        '/foo'          | '/bar'        | '/bar'

    }

    def 'should merge env config' () {
        expect:
        new ContainerConfig(env:LEFT) + new ContainerConfig(env: RIGHT) == new ContainerConfig(env: EXPECTED)

        where:
        LEFT                    | RIGHT         | EXPECTED
        null                    | null          | null
        ['alpha=1']             | null          | ['alpha=1']
        null                    |  ['beta=2']   |  ['beta=2']
        ['alpha=1','delta=x']   |   ['beta=2','delta=z']  | ['alpha=1','delta=z','beta=2']
    }

    @Unroll
    def 'should merge layers' () {

        expect:
        new ContainerConfig(layers:LEFT) + new ContainerConfig(layers: RIGHT) == new ContainerConfig(layers: EXPECTED)

        where:
        LEFT                                        | RIGHT     | EXPECTED
        null                                        | null      | null
        []                                          | []        | []
        [new ContainerLayer(location: 'http://x')]  | null      | [new ContainerLayer(location: 'http://x')]
        and:
        [new ContainerLayer(location: 'http://x')]  | [new ContainerLayer(location: 'http://y'),new ContainerLayer(location: 'http://y')]   | [new ContainerLayer(location: 'http://x'), new ContainerLayer(location: 'http://y'),new ContainerLayer(location: 'http://y')]
    }
}
