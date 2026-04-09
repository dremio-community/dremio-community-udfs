package org.sheinbergon.dremio.udf.gis.h3;

public class H3ListOutputDerivation implements com.dremio.exec.expr.fn.OutputDerivation {
    @Override
    public com.dremio.common.expression.CompleteType getOutputType(com.dremio.common.expression.CompleteType baseReturn, java.util.List<com.dremio.common.expression.LogicalExpression> args) {
        return new com.dremio.common.expression.CompleteType(
                org.apache.arrow.vector.types.pojo.ArrowType.List.INSTANCE,
                com.dremio.common.expression.CompleteType.BIGINT.toField(org.apache.arrow.vector.complex.ListVector.DATA_VECTOR_NAME));
    }
}
