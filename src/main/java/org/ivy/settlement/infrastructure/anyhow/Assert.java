package org.ivy.settlement.infrastructure.anyhow;


import org.ivy.settlement.infrastructure.string.StringUtils;

/**
 * description:
 * @author carrot
 */
public class Assert {

    public static void ensure(boolean condition, String err, Object...params) {
        if (!condition) {
            throw new RuntimeException(StringUtils.format(err, params));
        }
    }
}
