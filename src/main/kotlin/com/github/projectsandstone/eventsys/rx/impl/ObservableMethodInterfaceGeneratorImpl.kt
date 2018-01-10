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
package com.github.projectsandstone.eventsys.rx.impl

import com.github.jonathanxd.codeapi.CodeSource
import com.github.jonathanxd.codeapi.base.Access
import com.github.jonathanxd.codeapi.base.MethodDeclaration
import com.github.jonathanxd.codeapi.common.VariableRef
import com.github.jonathanxd.codeapi.factory.*
import com.github.jonathanxd.codeapi.util.asGeneric
import com.github.jonathanxd.codeapi.util.conversion.toVariableAccess
import com.github.jonathanxd.codeapi.util.conversion.typeSpec
import com.github.jonathanxd.codeproxy.CodeProxy
import com.github.jonathanxd.codeproxy.InvokeSuper
import com.github.jonathanxd.codeproxy.gen.Custom
import com.github.jonathanxd.codeproxy.gen.CustomHandlerGenerator
import com.github.jonathanxd.codeproxy.gen.DirectInvocationCustom
import com.github.jonathanxd.codeproxy.gen.GenEnv
import com.github.jonathanxd.codeproxy.internals.Util
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.projectsandstone.eventsys.event.Event
import com.github.projectsandstone.eventsys.event.annotation.TypeParam
import com.github.projectsandstone.eventsys.rx.Events
import com.github.projectsandstone.eventsys.rx.ObservableMethodInterfaceGenerator
import com.github.projectsandstone.eventsys.rx.util.createTypeInfoWithBuilder
import io.reactivex.Observable
import java.lang.reflect.Method

/**
 * Default implementation of [ObservableMethodInterfaceGenerator].
 *
 * This class generated method implementations that invoke [Events.observable] on [events].
 *
 * This implementation uses *CodeProxy* [DirectInvocationCustom] to generate invocations, this
 * means that it generates direct invocation to methods (no reflection, no method handle).
 */
class ObservableMethodInterfaceGeneratorImpl(override val events: Events) : ObservableMethodInterfaceGenerator {

    override fun <T> create(itf: Class<T>): T =
            CodeProxy.newProxyInstance<T>(arrayOf(), arrayOf(), {
                it.addInterface(itf)
                        .classLoader(itf.classLoader)
                        .addCustomGenerator(InvokeSuper::class.java)
                        .addCustom(ToEventsCustom(this.events))
                        .invocationHandler { _, _, _, _ ->
                            InvokeSuper.INSTANCE
                        }
            })


    private class ToEventsCustom(val events: Events) : DirectInvocationCustom {

        override fun getAdditionalProperties(): List<Custom.Property> =
                listOf(Custom.Property(VariableRef(Events::class.java, "events"), null))

        override fun getValueForConstructorProperties(): List<Any> =
                listOf(this.events)

        override fun generateSpecCache(m: Method): Boolean = false

        override fun getCustomHandlerGenerators(): List<CustomHandlerGenerator> =
                listOf(EventsInvoke)


        object EventsInvoke : CustomHandlerGenerator {
            private val evts = VariableRef(Events::class.java, "events")
            override fun handle(target: Method, methodDeclaration: MethodDeclaration, env: GenEnv): CodeSource {
                if (target.declaringClass == Object::class.java) {
                    env.isInvokeHandler = false
                    env.isMayProceed = false

                    return CodeSource.fromPart(returnValue(target.returnType,
                            invokeSpecial(target.declaringClass,
                                    Access.SUPER,
                                    target.name,
                                    target.typeSpec,
                                    methodDeclaration.parameters.map { it.toVariableAccess() }
                            )
                    ))
                }

                val typeInfo = TypeUtil.toTypeInfo(target.genericReturnType)
                if (typeInfo.typeParameters.size == 1
                        && Event::class.java.isAssignableFrom(typeInfo.getTypeParameter(0).typeClass)) {

                    env.isInvokeHandler = false
                    env.isMayProceed = false

                    val codeApiTypeInfo =
                            if (target.parameterCount == 1 && target.parameters[0].isAnnotationPresent(TypeParam::class.java))
                                accessVariable(target.parameters[0].type, target.parameters[0].name)
                            else
                                target.genericReturnType.asGeneric.bounds[0].type.createTypeInfoWithBuilder()

                    return CodeSource.fromPart(
                            returnValue(Observable::class.java, accessThisField(Events::class.java, Util.getAdditionalPropertyFieldName(evts))
                                    .invokeInterface(Events::class.java,
                                            "observable",
                                            typeSpec(Observable::class.java, TypeInfo::class.java),
                                            listOf(codeApiTypeInfo))))
                }

                return CodeSource.empty()
            }
        }
    }
}