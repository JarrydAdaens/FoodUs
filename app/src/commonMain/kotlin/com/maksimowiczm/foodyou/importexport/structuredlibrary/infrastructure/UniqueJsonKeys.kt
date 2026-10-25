package com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure

import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.LibraryErrorCode
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.LibraryImportException
import kotlinx.serialization.json.Json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder

/** Validate raw JSON grammar, DTO token kinds and duplicate decoded keys before decoder coercion. */
@OptIn(ExperimentalSerializationApi::class)
internal class UniqueJsonKeys(private val text:String) {
    private var position=0
    fun validate(descriptor:SerialDescriptor) {
        value(0,descriptor); whitespace()
        if(position!=text.length)invalid()
    }
    private fun value(depth:Int,descriptor:SerialDescriptor) {
        if(depth>StructuredLibraryParser.MAX_DEPTH)throw LibraryImportException(LibraryErrorCode.LIMIT)
        whitespace()
        when(peek()) {
            '{' -> {
                if(descriptor.kind!=StructureKind.CLASS && descriptor.kind!=StructureKind.MAP)invalid()
                position++
                val keys=mutableSetOf<String>()
                whitespace()
                if(take('}'))return
                do {
                    whitespace()
                    if(peek()!='"')invalid()
                    val key=Json.decodeFromString<String>(stringToken())
                    if(!keys.add(key))invalid()
                    val child=if(descriptor.kind==StructureKind.MAP)descriptor.getElementDescriptor(1) else {
                        val index=descriptor.getElementIndex(key)
                        if(index==CompositeDecoder.UNKNOWN_NAME)invalid()
                        descriptor.getElementDescriptor(index)
                    }
                    whitespace(); if(!take(':'))invalid()
                    value(depth+1,child); whitespace()
                    if(take('}'))return
                } while(take(','))
                invalid()
            }
            '[' -> {
                if(descriptor.kind!=StructureKind.LIST)invalid()
                position++; whitespace()
                if(take(']'))return
                do {
                    value(depth+1,descriptor.getElementDescriptor(0)); whitespace()
                    if(take(']'))return
                } while(take(','))
                invalid()
            }
            '"' -> {
                if(descriptor.kind!=PrimitiveKind.STRING && descriptor.kind!=SerialKind.ENUM)invalid()
                stringToken()
            }
            null -> invalid()
            else -> {
                val start=position
                while(peek()!=null && peek() !in listOf(' ', '\r', '\n', '\t', ',', '}', ']'))position++
                if(position==start)invalid()
                val token=text.substring(start,position)
                when(token) {
                    "null" -> if(!descriptor.isNullable)invalid()
                    "true","false" -> if(descriptor.kind!=PrimitiveKind.BOOLEAN)invalid()
                    else -> {
                        if(!NUMBER.matches(token))invalid()
                        when(descriptor.kind) {
                            PrimitiveKind.INT,PrimitiveKind.LONG -> if(!INTEGER.matches(token))invalid()
                            PrimitiveKind.DOUBLE,PrimitiveKind.FLOAT -> {
                                val number=token.toDoubleOrNull()
                                val nonzero=token.substringBefore('e').substringBefore('E').any { it in '1'..'9' }
                                if(number==null || !number.isFinite() || (number==0.0 && nonzero))
                                    throw LibraryImportException(LibraryErrorCode.NUMBER)
                            }
                            else -> invalid()
                        }
                    }
                }
            }
        }
    }
    private fun stringToken():String {
        val start=position
        if(!take('"'))invalid()
        while(position<text.length) {
            val char=text[position++]
            when(char) {
                '"' -> return text.substring(start,position)
                '\\' -> {
                    if(position>=text.length)invalid()
                    when(text[position++]) {
                        '"','\\','/','b','f','n','r','t' -> Unit
                        'u' -> repeat(4) {
                            val hex=text.getOrNull(position++) ?: invalid()
                            if(hex !in '0'..'9' && hex !in 'a'..'f' && hex !in 'A'..'F')invalid()
                        }
                        else -> invalid()
                    }
                }
                else -> if(char.code<0x20)invalid()
            }
        }
        invalid()
    }
    private fun whitespace() { while(peek() in listOf(' ', '\r', '\n', '\t'))position++ }
    private fun peek():Char?=text.getOrNull(position)
    private fun take(char:Char):Boolean=if(peek()==char){position++;true}else false
    private fun invalid():Nothing=throw LibraryImportException(LibraryErrorCode.MALFORMED)
    private companion object {
        val NUMBER=Regex("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")
        val INTEGER=Regex("-?(?:0|[1-9][0-9]*)")
    }
}
