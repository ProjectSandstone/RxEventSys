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
package com.github.projectsandstone.eventsys.rx.ap

import com.github.jonathanxd.codeapi.base.*
import com.github.jonathanxd.codeapi.base.Annotation
import com.github.jonathanxd.codeapi.factory.parameter
import com.github.jonathanxd.codeapi.type.Generic
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.projectsandstone.eventsys.ap.MethodDesc
import com.github.projectsandstone.eventsys.ap.getUniqueName
import com.github.projectsandstone.eventsys.event.annotation.TypeParam
import com.github.projectsandstone.eventsys.gen.event.eventTypeInfoFieldName
import io.reactivex.Observable

object ObservableEventsInterfaceGenerator {

    fun processNamed(name: String, observableEvents: List<ObservableEventElement>): TypeDeclaration {
        return InterfaceDeclaration.Builder.builder()
                .modifiers(CodeModifier.PUBLIC)
                .name(name)
                .methods(createMethods(observableEvents))
                .build()
    }

    private fun createMethods(observableEvents: List<ObservableEventElement>,
                              methods: MutableList<MethodDesc> = mutableListOf()): List<MethodDeclaration> =
            observableEvents.map {
                val desc = MethodDesc(it.methodName, 0).also {
                    it.copy(name = getUniqueName(it, methods))
                }

                val parameters = mutableListOf<CodeParameter>()

                if (it.origin.typeParameters.isNotEmpty()) {
                    val typeInfoGeneric = Generic.type(TypeInfo::class.java).of(it.type)
                    parameters += parameter(type = typeInfoGeneric, name = eventTypeInfoFieldName, annotations = listOf(
                            Annotation.Builder.builder()
                                    .type(TypeParam::class.java)
                                    .visible(true)
                                    .build()
                    ))
                }

                MethodDeclaration.Builder.builder()
                        .modifiers(CodeModifier.PUBLIC)
                        .genericSignature(it.signature)
                        .returnType(Generic.type(Observable::class.java).of(it.type))
                        .parameters(parameters)
                        .name(desc.name)
                        .build()
            }
}