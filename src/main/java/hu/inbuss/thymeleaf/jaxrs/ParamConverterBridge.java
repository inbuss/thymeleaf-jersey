package hu.inbuss.thymeleaf.jaxrs;

import java.lang.annotation.Annotation;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.internal.inject.ParamConverterFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.AbstractStandardConversionService;

/**
 * A Thymeleaf conversion service implementation using registered JAX-RS ParamConverters in addition to the default
 * conversion methods.
 *
 * The JAX-RS standard does not expose the set of registered ParamConverters, so only implementation-specific solutions
 * are possible. This class works with Jersey. It is implemented as a singleton bean, because it needs a reference to
 * another (singleton) bean injected.
 * @author PÁLFALVI Tamás &lt;tamas.palfalvi@inbuss.hu&gt;
 */
@Singleton @Provider
public class ParamConverterBridge extends AbstractStandardConversionService {
    private static final Annotation[] NO_ANNOTATIONS = {};
    private final ParamConverterFactory pcf;

    @Inject public ParamConverterBridge(final ParamConverterFactory pcf) {
        this.pcf = pcf;
    }

    @Override protected String convertToString(IExpressionContext context, Object object) {
        return convertToStringX(context, object, object.getClass());
    }

    private <T> String convertToStringX(final IExpressionContext context, final Object object, final Class<T> sourceClass) {
        final ParamConverter<T> pc = pcf.getConverter(sourceClass, sourceClass, NO_ANNOTATIONS);
        if (pc == null)
            return super.convertToString(context, object);

        @SuppressWarnings("unchecked") final T tobj = (T) object;
        return pc.toString(tobj);
    }

    @Override protected <T> T convertOther(final IExpressionContext context, final Object object, final Class<T> targetClass) {
        if (object instanceof String) {
            final ParamConverter<T> pc = pcf.getConverter(targetClass, targetClass, NO_ANNOTATIONS);
            if (pc != null)
                return pc.fromString((String) object);
        }
        return super.convertOther(context, object, targetClass);
    }

    /**
     * Installs the service instance into a Thymeleaf {@link TemplateEngine} instance. Locates the Standard Dialect
     * object and sets its conversion service. This can be used in situations where the dialect may be modified in
     * some way, either by changing properties or by subclassing and extending the default implementation. It cannot
     * coexist with other custom conversion service implementations - the previous value is completely overwritten.
     *
     * @param engine the engine into which this service instance is to be installed
     */
    public void installInto(final TemplateEngine engine) {
        for (final IDialect d : engine.getDialects())
            if (d instanceof StandardDialect)
                ((StandardDialect)d).setConversionService(this);
    }
}
