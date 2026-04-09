/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sheinbergon.dremio.udf.gis.h3;

import javax.inject.Inject;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;

/**
 *
 *  @name			H3_Wrap
 *  @args			([bigint] {h3Value})
 *  @returnType		bigint[]
 *  @description	Takes a single H3 value and wraps it in a list
 *  @example		H3_Wrap(H3_FromText('847b59dffffffff'))[0] -> 596645165859340287
 *
 *  @author			Brian Holman <bholman@dezota.com>
 *
 */

@FunctionTemplate(name = "h3_wrap", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL, derivation = org.sheinbergon.dremio.udf.gis.h3.H3ListOutputDerivation.class)
public class H3Wrap implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.BigIntHolder h3Value;

    @Output
    org.apache.arrow.vector.complex.writer.BaseWriter.ComplexWriter out;

    @Inject
    org.apache.arrow.memory.ArrowBuf buffer;

    public void setup() {
    }

    public void eval() {
        org.apache.arrow.vector.complex.writer.BaseWriter.ListWriter listWriter = out.rootAsList();
        listWriter.startList();
        listWriter.bigInt().writeBigInt(h3Value.value);
        listWriter.endList();
    }
}
