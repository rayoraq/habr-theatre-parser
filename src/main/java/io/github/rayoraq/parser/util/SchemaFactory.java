package io.github.rayoraq.parser.util;

public final class SchemaFactory {
    private SchemaFactory() {}

    public static TheatreMappingSchema createEkvusSchema() {
        return new TheatreMappingSchema(
                "p.h1",
                "span.age_limit_s, span.age_limit",
                "a[href*=/media/show/big/] img, img[src*=/media/show/]",
                "a[href*=ticketManager.session]",
                ""
        );
    }

    public static TheatreMappingSchema createTheatreSchema() {
        return new TheatreMappingSchema(
                "h3 a",
                ".value_limit",
                ".td2 img",
                ".td1 .date_afisha",
                ".td3 .td2 .td1 div"
        );
    }
}