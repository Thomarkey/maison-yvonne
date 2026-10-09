package net.persgroep.maisonyvonne.application.domain

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

/**
 * Reflection-based equals/toString so domain objects compare by value without
 * every class hand-writing it. Works because domain properties are `val`.
 */
abstract class AbstractBase {
    override fun equals(other: Any?): Boolean = EqualsBuilder.reflectionEquals(this, other)

    override fun toString(): String =
        ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE, false)
}
