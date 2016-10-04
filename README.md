thymeleaf-jersey
================

An extension for Thymeleaf forwarding object-to-string conversions to the
appropriate JAX-RS ParamConverter. A possible use for this is to ensure
round-trip safety when Thymeleaf writes a parameter (e.g. form field value)
that JAX-RS will try to read back.

As the necessary functionality is not standardised, a generic JAX-RS solution
is not possible; this module works on Jersey only. Also, this only works with
the Standard Dialect - Spring users would probably be content with using the
existing Spring converter integration, and have no need for this module.

Remember: the Conversion Service only drives double-brace expressions
(e.g. `${{user.lastLogin}}`). Single-brace ones (`${user.lastLogin}`) are
not affected by this library, and will keep using `toString()` just like
before.

Usage
-----

1. Include the library in your project. For example with Maven:

    ```xml
        <dependency>
            <groupId>hu.inbuss</groupId>
            <artifactId>thymeleaf-jersey</artifactId>
            <version>0.0.9</version>
        </dependency>
    ```

    or with Gradle:

    ```gradle
    dependencies {
        compile 'hu.inbuss:thymeleaf-jersey:0.0.9'
    }
    ```

2. Inject a reference to the `hu.inbuss.thymeleaf.jaxrs.ParamConverterBridge`
    singleton bean into the class that creates the Thymeleaf `TemplateEngine`.

3. Either follow the [Thymeleaf documentation on custom Conversion Services][1], i.e.

    ```java
    import hu.inbuss.thymeleaf.jaxrs.ParamConverterBridge;
    import javax.inject.Inject;
    import org.thymeleaf.TemplateEngine;
    import org.thymeleaf.standard.StandardDialect;

    public class ExampleApplication {
        @Inject private ParamConverterBridge pcb;
        private final TemplateEngine engine;

        public ExampleApplication() {
            engine = new TemplateEngine();
            final StandardDialect sd = new StandardDialect();
            sd.setConversionService(pcb);
            engine.setDialect(sd);
            /* do other setup */
        }
    }
    ```

    or just ask the converter bean to install itself into the Standard Dialect:

    ```java
    import hu.inbuss.thymeleaf.jaxrs.ParamConverterBridge;
    import javax.inject.Inject;
    import org.thymeleaf.TemplateEngine;

    public class ExampleApplication {
        @Inject private ParamConverterBridge pcb;
        private final TemplateEngine engine;

        public ExampleApplication() {
            engine = new TemplateEngine();
            pcb.installInto(engine);
            /* do other setup */
        }
    }
    ```

[1]: http://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#conversion-services