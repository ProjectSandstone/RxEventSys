/*
 *      RxEventSys - Java ReactiveX EventSys implementation
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 ProjectSandstone <https://github.com/ProjectSandstone/RxEventSys>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.projectsandstone.eventsys.rx.util

import com.github.jonathanxd.codeapi.CodeInstruction
import com.github.jonathanxd.codeapi.Types
import com.github.jonathanxd.codeapi.factory.*
import com.github.jonathanxd.codeapi.literal.Literals
import com.github.jonathanxd.codeapi.type.CodeType
import com.github.jonathanxd.codeapi.util.asGeneric
import com.github.jonathanxd.codeapi.util.toArray
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.iutils.type.TypeInfoBuilder
import com.github.jonathanxd.iutils.type.TypeInfoUtil
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.jonathanxd.jwiutils.kt.rightOrFail
import java.lang.reflect.Type

private fun Type.resolved(): Type =
        if (this is CodeType) this.bindedDefaultResolver.resolve().rightOrFail as Type else this

fun Type.createTypeInfoWithString(): CodeInstruction =
        TypeUtil.toTypeInfo(this.resolved()).toFullString().let {
            cast(Object::class.java, TypeInfo::class.java,
                    TypeInfoUtil::class.java.invokeStatic(
                            "fromFullString",
                            typeSpec(List::class.java, String::class.java),
                            listOf(Literals.STRING(it))
                    ).invokeInterface(
                            List::class.java,
                            "get",
                            typeSpec(Object::class.java, Types.INT),
                            listOf(Literals.INT(0))
                    )
            )
        }

fun TypeInfo<*>.createTypeInfoWithString(): CodeInstruction =
        this.toFullString().let {
            cast(Object::class.java, TypeInfo::class.java,
                    TypeInfoUtil::class.java.invokeStatic(
                            "fromFullString",
                            typeSpec(List::class.java, String::class.java),
                            listOf(Literals.STRING(it))
                    ).invokeInterface(
                            List::class.java,
                            "get",
                            typeSpec(Object::class.java, Types.INT),
                            listOf(Literals.INT(0))
                    )
            )
        }

fun Type.createTypeInfoWithBuilder(): CodeInstruction =
        if (this.asGeneric.bounds.isEmpty()) {
            TypeInfo::class.java.invokeStatic(
                    "of",
                    typeSpec(TypeInfo::class.java, Class::class.java),
                    listOf(Literals.TYPE(this))
            )
        } else {
            TypeInfo::class.java.invokeStatic(
                    "builderOf",
                    typeSpec(TypeInfoBuilder::class.java, Class::class.java),
                    listOf(Literals.TYPE(this))
            ).let {
                var last = it

                this.asGeneric.bounds
                        .map { it.type }
                        .forEach {
                            last = last.invokeVirtual(TypeInfoBuilder::class.java,
                                    "of",
                                    typeSpec(TypeInfoBuilder::class.java, TypeInfo::class.java.toArray(1)),
                                    listOf(createArray(
                                            TypeInfo::class.java.toArray(1),
                                            listOf(Literals.INT(1)),
                                            listOf(it.createTypeInfoWithBuilder())
                                    ))
                            )
                        }

                last.invokeVirtual(TypeInfoBuilder::class.java, "build", typeSpec(TypeInfo::class.java), listOf())
            }
        }