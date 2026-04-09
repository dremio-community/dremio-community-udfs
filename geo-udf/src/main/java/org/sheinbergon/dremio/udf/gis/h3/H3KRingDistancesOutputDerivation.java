package org.sheinbergon.dremio.udf.gis.h3;

import com.dremio.common.expression.CompleteType;
import com.dremio.common.expression.LogicalExpression;
import org.apache.arrow.vector.complex.ListVector;
import org.apache.arrow.vector.types.pojo.ArrowType;

import java.util.List;

public class H3KRingDistancesOutputDerivation implements com.dremio.exec.expr.fn.OutputDerivation {
    @Override
    public CompleteType getOutputType(CompleteType baseReturn, List<LogicalExpression> args) {
        return new CompleteType(
                ArrowType.List.INSTANCE,
                new CompleteType(
                        ArrowType.Struct.INSTANCE,
                        CompleteType.BIGINT.toField("index"),
                        CompleteType.INT.toField("distance")
                ).toField(ListVector.DATA_VECTOR_NAME)
        );
    }
}
