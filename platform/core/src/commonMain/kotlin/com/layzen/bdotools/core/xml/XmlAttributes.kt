package com.layzen.bdotools.core.xml

private val ATTRIBUTE = Regex("""(\w+)="([^"]*)"""")

/** Атрибуты XML-тега [tag] в виде «имя → значение». */
fun attributesOf(tag: String): Map<String, String> =
    ATTRIBUTE.findAll(tag).associate { it.groupValues[1] to it.groupValues[2] }
