package org.estatio.dom;

import com.google.common.base.Objects;

import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ToStringEvaluator;

public interface WithCodeGetter {

    public String getCode();
    
    public static class ToString {
        private ToString() {}
        public static String of(WithCodeGetter p) {
            return Objects.toStringHelper(p)
                    .add("code", p.getCode())
                    .toString();
        }
        
        public static ObjectContracts evaluatorFor(ObjectContracts objectContracts) {
            objectContracts.with(new ToStringEvaluator() {
                @Override
                public boolean canEvaluate(Object o) {
                    return o instanceof WithCodeGetter;
                }
                
                @Override
                public String evaluate(Object o) {
                    return ((WithCodeGetter)o).getCode();
                }
            });
            return objectContracts;
        }
    }
}